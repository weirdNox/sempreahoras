package backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Server {
	final int port = 8080;
	Connection dbConn = null;
	ObjectMapper mapper;

	Server() {
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
	}

	void run() {
		try {
			dbConn = DriverManager.getConnection("jdbc:sqlite:data.db");

	        Statement statement = dbConn.createStatement();
	        statement.addBatch("CREATE TABLE IF NOT EXISTS events (" +
                               "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                               "UserId STRING NOT NULL," +
                               "Title TEXT NOT NULL," +
                               "Description TEXT NOT NULL," +
                               "StartMillis INTEGER NOT NULL," +
                               "DurationMillis INTEGER NOT NULL," +
                               "IsAllDay BOOLEAN NOT NULL," +
                               "RepeatType INTEGER NOT NULL," +
                               "RepeatCount INTEGER NOT NULL," +
                               "EndMillis INTEGER NOT NULL," +
                               "LastEdit INTEGER NOT NULL," +
                               "Color INTEGER NOT NULL," +
                               "Location STRING NOT NULL," +
                               "Deleted BOOLEAN DEFAULT false" +
                               ")");

	        statement.addBatch("CREATE TABLE IF NOT EXISTS tasks (" +
                               "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                               "UserId STRING NOT NULL," +
                               "Title TEXT NOT NULL," +
                               "Description TEXT NOT NULL," +
                               "Color INTEGER NOT NULL," +
                               "LastEdit INTEGER NOT NULL," +
                               "Deleted BOOLEAN DEFAULT false" +
                               ")");

            statement.executeBatch();
		}
        catch (SQLException e) {
			throw new RuntimeException("Could not open database!", e);
		}

		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
		}
        catch (IOException e) {
	        throw new RuntimeException("Cannot open port " + port, e);
		}

		server.createContext("/", (t) -> {
			// NOTE: Catch-all
			sendResponse(t, 400, "Invalid endpoint");
		});

		server.createContext("/get", (t) -> {
            String method = t.getRequestMethod();
            if(method.equals("GET")) {
                Map<String, String> parameters = queryToMap(t.getRequestURI().getQuery());
                if(parameters.containsKey("userId")) {
                    try {
                    	PreparedStatement eventStatement;
                    	PreparedStatement taskStatement;

                        long since = 0;
                        if(parameters.containsKey("since")) {
                            try {
                                since = Long.parseLong(parameters.get("since"));
                            }
                            catch(Exception e) {
                                since = 0;
                            }
                        }

                    	if(since > 0) {
                            eventStatement = dbConn.prepareStatement("SELECT * FROM events WHERE UserId == ? AND LastEdit > ?");
                            eventStatement.setString(1, parameters.get("userId"));
                            eventStatement.setLong(2, since);

                            taskStatement = dbConn.prepareStatement("SELECT * FROM tasks WHERE UserId == ? AND LastEdit > ?");
                            taskStatement.setString(1, parameters.get("userId"));
                            taskStatement.setLong(2, since);
                    	}
                    	else {
                            eventStatement = dbConn.prepareStatement("SELECT * FROM events WHERE UserId == ? AND Deleted == false");
                            eventStatement.setString(1, parameters.get("userId"));

                            taskStatement = dbConn.prepareStatement("SELECT * FROM tasks WHERE UserId == ? AND Deleted == false");
                            taskStatement.setString(1, parameters.get("userId"));
                    	}

                        ResultSet result = eventStatement.executeQuery();
                        ArrayList<Event> events = new ArrayList<>();
                        while(result.next()) {
                        	events.add(new Event(result));
                        }

                        result = taskStatement.executeQuery();
                        ArrayList<Task> tasks = new ArrayList<>();
                        while(result.next()) {
                        	tasks.add(new Task(result));
                        }

                        ObjectNode root = mapper.createObjectNode();
                        JsonNode eventsNode = mapper.valueToTree(events);
                        JsonNode tasksNode = mapper.valueToTree(tasks);
                        root.set("events", eventsNode);
                        root.set("tasks", tasksNode);

                        String response = root.toString();
                        sendResponse(t, 200, response);
                    }
                    catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    sendResponse(t, 403, "Missing userId");
                }
            }
            else {
            	sendResponse(t, 405, "Invalid method");
            }

			t.close();
        });

		server.createContext("/insertEvent", (t) -> {
            String method = t.getRequestMethod();
            if(method.equals("POST")) {
                try {
                    InputStream input = t.getRequestBody();
                    Event event = mapper.readValue(input, Event.class);

                    if(event.userId.isEmpty()) {
                    	sendResponse(t, 401, "Invalid user id");
                    }
                    else if(event.startMillis == 0) {
                    	sendResponse(t, 400, "Invalid start time");
                    }
                    else if(event.durationMillis == 0) {
                    	sendResponse(t, 400, "Invalid duration millis");
                    }
                    else {
                        PreparedStatement statement = event.prepareStatement(dbConn);

                        statement.executeUpdate();
                        if(event.id == 0) {
                        	ResultSet id = statement.getGeneratedKeys();
                        	if(id != null && id.next()) {
                        		event.id = id.getLong(1);
                                sendResponse(t, 200, mapper.writeValueAsString(event));
                        	}
                            else {
                                sendResponse(t, 500, "Could not create event");
                            }
                        }
                        else {
                            if(statement.getUpdateCount() == 1) {
                                sendResponse(t, 200, mapper.writeValueAsString(event));
                            }
                            else {
                                sendResponse(t, 500, "Could not update event");
                            }
                        }
                    }
                }
                catch(JsonParseException|UnrecognizedPropertyException e) {
                	sendResponse(t, 400, "Invalid input JSON");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
            	sendResponse(t, 405, "Invalid method");
            }

			t.close();
        });

		server.createContext("/deleteEvent", (t) -> {
            String method = t.getRequestMethod();
            if(method.equals("POST")) {
                try {
                	Event event = new Event();

                    InputStream input = t.getRequestBody();
                    JsonNode json = mapper.readTree(input);
                    event.id = json.get("id").asLong();
                    event.userId = json.get("userId").asText();

                    if(event.userId.isEmpty()) {
                    	sendResponse(t, 401, "Invalid user id");
                    }
                    else if(event.id == 0) {
                    	sendResponse(t, 400, "Invalid event id");
                    }
                    else {
                        event.deleted = true;
                        PreparedStatement statement = event.prepareStatement(dbConn);

                        statement.executeUpdate();
                        if(statement.getUpdateCount() == 1) {
                            sendResponse(t, 200, "");
                        }
                        else {
                            sendResponse(t, 500, "Could not delete event");
                        }
                    }
                }
                catch(JsonParseException|UnrecognizedPropertyException e) {
                	sendResponse(t, 400, "Invalid input JSON");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
            	sendResponse(t, 405, "Invalid method");
            }

			t.close();
        });

		server.createContext("/insertTask", (t) -> {
            String method = t.getRequestMethod();
            if(method.equals("POST")) {
                try {
                    InputStream input = t.getRequestBody();
                    Task task = mapper.readValue(input, Task.class);

                    if(task.userId.isEmpty()) {
                    	sendResponse(t, 401, "Invalid user id");
                    }
                    else {
                        PreparedStatement statement = task.prepareStatement(dbConn);

                        statement.executeUpdate();
                        if(task.id == 0) {
                        	ResultSet id = statement.getGeneratedKeys();
                        	if(id != null && id.next()) {
                        		task.id = id.getLong(1);
                                sendResponse(t, 200, mapper.writeValueAsString(task));
                        	}
                            else {
                                sendResponse(t, 500, "Could not create task");
                            }
                        }
                        else {
                            if(statement.getUpdateCount() == 1) {
                                sendResponse(t, 200, mapper.writeValueAsString(task));
                            }
                            else {
                                sendResponse(t, 500, "Could not update task");
                            }
                        }
                    }
                }
                catch(JsonParseException|UnrecognizedPropertyException e) {
                	sendResponse(t, 400, "Invalid input JSON");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
            	sendResponse(t, 405, "Invalid method");
            }

			t.close();
        });

		server.createContext("/deleteTask", (t) -> {
            String method = t.getRequestMethod();
            if(method.equals("POST")) {
                try {
                	Task task = new Task();

                    InputStream input = t.getRequestBody();
                    JsonNode json = mapper.readTree(input);
                    task.id = json.get("id").asLong();
                    task.userId = json.get("userId").asText();

                    if(task.userId.isEmpty()) {
                    	sendResponse(t, 401, "Invalid user id");
                    }
                    else if(task.id == 0) {
                    	sendResponse(t, 400, "Invalid task id");
                    }
                    else {
                        task.deleted = true;
                        PreparedStatement statement = task.prepareStatement(dbConn);

                        statement.executeUpdate();
                        if(statement.getUpdateCount() == 1) {
                            sendResponse(t, 200, "");
                        }
                        else {
                            sendResponse(t, 500, "Could not delete task");
                        }
                    }
                }
                catch(JsonParseException|UnrecognizedPropertyException e) {
                	sendResponse(t, 400, "Invalid input JSON");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
            	sendResponse(t, 405, "Invalid method");
            }

			t.close();
        });

 		ExecutorService serverExecutor = Executors.newFixedThreadPool(4);
		server.setExecutor(serverExecutor);
        server.start();
	}

	void sendResponse(HttpExchange t, int code, String response) {
        OutputStream os = t.getResponseBody();
		byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        try {
        	t.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
			t.sendResponseHeaders(code, responseBytes.length);
	        os.write(responseBytes);
	        os.close();
		}
        catch (IOException e) {
        	// NOTE: Ignore!
		}
	}

    public Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if(query != null) {
            for(String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                }
                else{
                    result.put(entry[0], "");
                }
            }
        }

        return result;
    }
}
