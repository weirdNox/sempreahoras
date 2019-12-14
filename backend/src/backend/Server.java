package backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.fasterxml.jackson.databind.ObjectMapper;
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
	        statement.executeUpdate("CREATE TABLE IF NOT EXISTS events (" +
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
	                                "Location STRING NOT NULL" +
	                                ")");
		} catch (SQLException e) {
			throw new RuntimeException("Could not open database!", e);
		}

		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException e) {
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
                    	PreparedStatement statement;
                    	if(parameters.containsKey("since")) {
                            statement = dbConn.prepareStatement("SELECT * FROM events WHERE userId == ? AND lastEdit > ?");
                            statement.setString(1, parameters.get("userId"));
                            statement.setString(2, parameters.get("since"));
                    	}
                    	else {
                            statement = dbConn.prepareStatement("SELECT * FROM events WHERE userId == ?");
                            statement.setString(1, parameters.get("userId"));
                    	}

                        ResultSet result = statement.executeQuery();
                        ArrayList<Event> events = new ArrayList<>();
                        while(result.next()) {
                        	events.add(new Event(result));
                        }

                        String response = mapper.writeValueAsString(events);
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

		server.createContext("/insert", (t) -> {
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
                        event.lastEdit = Calendar.getInstance().getTimeInMillis();
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
                catch(JsonParseException e) {
                	sendResponse(t, 400, "Invalid input JSON");
                	e.printStackTrace();
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
		byte[] responseBytes = response.getBytes();

        try {
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
