package backend;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.InetAddress;

class ServerTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Server server = new Server();
		server.run();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		
	}

	@BeforeEach
	void setUp() throws Exception {

	}

	@AfterEach
	void tearDown() throws Exception {
	}
	
	class ResponseCode { 
	    public int valor; 
	  
	    ResponseCode(int valor) 
	    { 
	        // Using wrapper class 
	        // so as to wrap integer value 
	        // in mutable object 
	        // which can be changed or modified 
	        this.valor = valor; 
	    } 
	} 
	
	void eventoGenerico(Event event) {
		event.id = 0;
		event.isAllDay = false;
		event.color = 10;
		event.deleted = false;
		event.description = "EVENTO GENERICO";
		event.durationMillis = 40000;
		event.endMillis = 80000;
		event.lastEdit = 0;
		event.location = "SITIO";
		event.repeatCount = 3;
		event.repeatType = 2;
		event.startMillis = 40000;
		event.title = "EVENTO GENERICO";
		event.userId = "UNIT_TESTS_SERVIDOR";
		event.notifMinutes = 40;
	}
	
	void taskGenerico(Task event) {
		event.id = 0;
		event.color = 10;
		event.deleted = false;
		event.description = "TASK GENERICO";
		event.lastEdit = 0;
		event.title = "TASK GENERICO";
		event.userId = "UNIT_TESTS_SERVIDOR";
	}
	
	String httpRequest(String context, String method, ObjectNode root, ResponseCode responsecode) {
		String url = "";
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getLocalHost();
			url = "http://" + inetAddress.getHostAddress() + ":8080/";
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		}
		
		StringBuilder response = null;
		
		try {
			HttpURLConnection httpClient = (HttpURLConnection) new URL(url + context).openConnection();
			
			//add request header
	        httpClient.setRequestMethod(method);
	
	        // Send post request
	        if (method.equals("POST")) {
	        	
	        	httpClient.setDoOutput(true);
		        
		        DataOutputStream wr = new DataOutputStream(httpClient.getOutputStream());
		        if (root.has("ERRO")) {
		        	wr.writeBytes(root.get("ERRO").textValue());
			        wr.flush();
		        }
		        else {
		        	wr.writeBytes(root.toString());
			        wr.flush();
		        }
	        }
	        
	        BufferedReader in = null;
	        
	    	responsecode.valor = httpClient.getResponseCode();
	        
	    	if (httpClient.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
	    		in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()));
	    	}
	    	else in = new BufferedReader(new InputStreamReader(httpClient.getErrorStream()));
	        
	        String line;
	        response = new StringBuilder();
	
	        while ((line = in.readLine()) != null) {
	            response.append(line);
	        }
	        
		} catch (MalformedURLException e1) {
			return e1.getMessage();
		} catch (IOException e1) {
			return e1.getMessage();
		}
        
        return response.toString();
	}

	@Test
	void insertEvent_AddingAndEditingEvents_PositiveResults() {
		
		ObjectMapper mapper;
		Connection dbConn = null;
		PreparedStatement statement;
		ResultSet result;
		
        try {
			dbConn = DriverManager.getConnection("jdbc:sqlite:servertestdata.db");
			statement = dbConn.prepareStatement("DELETE FROM tasks");
			statement.executeUpdate();
			statement = dbConn.prepareStatement("DELETE FROM events");
			statement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		
		Event event = new Event();
		Event resp = new Event();
		Event eventdb = new Event();
	
		eventoGenerico(event);
		
		ObjectNode root = mapper.valueToTree(event);
		
        String context = "insertEvent";
        String response;
        ResponseCode responsecode = new ResponseCode(0);
        
		try {
			response = httpRequest(context, "POST", root, responsecode);
	        
	        resp = mapper.readValue(response, Event.class);
	        
	        event.id = resp.id;
	        event.lastEdit = resp.lastEdit;
	        
	        statement = dbConn.prepareStatement("SELECT * FROM events WHERE Id = ?");
	        statement.setLong(1, event.id);
	        result = statement.executeQuery();
	        while(result.next()) {
            	eventdb = new Event(result);
            }
	        
	        assertEquals(mapper.writeValueAsString(event), mapper.writeValueAsString(eventdb));
	        assertEquals(mapper.writeValueAsString(event), mapper.writeValueAsString(resp));
	        assertEquals(responsecode.valor, 200);
	        
	        event.title = "TESTE UPDATE EVENTO";
	        event.description = "UPDATE EVENTO";
	        
	        root = mapper.valueToTree(event);
	        
	        response = httpRequest(context, "POST", root, responsecode);
        	
        	resp = mapper.readValue(response.toString(), Event.class);
        	
        	event.lastEdit = resp.lastEdit;
        	
        	statement = dbConn.prepareStatement("SELECT * FROM events WHERE Id = ?");
	        statement.setLong(1, event.id);
	        result = statement.executeQuery();
	        while(result.next()) {
            	eventdb = new Event(result);
            }
	        
	        assertEquals(mapper.writeValueAsString(event), mapper.writeValueAsString(eventdb));
        	assertEquals(mapper.writeValueAsString(event), mapper.writeValueAsString(resp));
        	assertEquals(responsecode.valor, 200);
	        
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	void insertEvent_AddingAndEditingEvents_NegativeResults(){
		
		ObjectMapper mapper;
		
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        
		Event event = new Event();
		eventoGenerico(event);
		
        String context = "insertEvent";
        String response;
        ResponseCode responsecode = new ResponseCode(0);
        
		event.userId = "";
		
		ObjectNode root = mapper.valueToTree(event);
        
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Invalid user id");
		assertEquals(responsecode.valor, 401);
		
		event.durationMillis = 0;
		event.userId = "UNIT_TESTS_NEG_SERVIDOR";
		
		root = mapper.valueToTree(event);
		
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Invalid duration millis");
		assertEquals(responsecode.valor, 400);
		
		event.startMillis = 0;
		event.durationMillis = 40000;
		
		root = mapper.valueToTree(event);
		
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Invalid start time");
		assertEquals(responsecode.valor, 400);
		
		root = mapper.createObjectNode();
		root.put("ERRO", "ISTO NAO E UMA MENSAGEM FORMATO JSON");
		
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Invalid input JSON");
		assertEquals(responsecode.valor, 400);
		
		root = mapper.createObjectNode();
		root.put("TESTE", "TESTE HTTP REQUEST COM METHOD GET");
		
		response = httpRequest(context, "GET", root, responsecode);
		
		assertEquals(response, "Invalid method");
		assertEquals(responsecode.valor, 405);
		
		event.id = 5;
		event.startMillis = 40000;
		
		root = mapper.valueToTree(event);
		
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Could not update event");
		assertEquals(responsecode.valor, 500);
		
	}

	@Test
	void deleteEvent_DeletingEvents_PositiveResults() {

		ObjectMapper mapper;
		Connection dbConn = null;
		PreparedStatement statement;
		ResultSet result;
		
        try {
			dbConn = DriverManager.getConnection("jdbc:sqlite:servertestdata.db");
			statement = dbConn.prepareStatement("DELETE FROM tasks");
			statement.executeUpdate();
			statement = dbConn.prepareStatement("DELETE FROM events");
			statement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		
		Event event = new Event();
		Event eventdb = new Event();
		eventoGenerico(event);
		
		String context = "deleteEvent";
        String response;
        ResponseCode responsecode = new ResponseCode(0);
        
        try {
			statement = event.prepareStatement(dbConn);
			statement.executeUpdate();
			result = statement.getGeneratedKeys();
        	if(result != null && result.next()) event.id = result.getLong(1);
        	
        	ObjectNode root = mapper.valueToTree(event);
        	
        	response = httpRequest(context, "POST", root, responsecode);
        	
        	assertEquals("", response);
        	assertEquals(200, responsecode.valor);
        	
        	statement = dbConn.prepareStatement("SELECT * FROM events WHERE Id = ?");
	        statement.setLong(1, event.id);
	        result = statement.executeQuery();
	        while(result != null && result.next()) {
	        	eventdb = new Event(result);
	        }
	        
	        assertEquals(true, eventdb.deleted);
        	
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void deleteEvent_DeletingEvents_NegativeResults(){
		ObjectMapper mapper;
		
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        
		Event event = new Event();
		eventoGenerico(event);
		
        String context = "deleteEvent";
        String response;
        ResponseCode responsecode = new ResponseCode(0);
        
		event.userId = "";
		
		ObjectNode root = mapper.valueToTree(event);
        
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Invalid user id");
		assertEquals(responsecode.valor, 401);
		
		event.id = 0;
		event.userId = "UNIT_TESTS_NEG_SERVIDOR";
		
		root = mapper.valueToTree(event);
		
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Invalid event id");
		assertEquals(responsecode.valor, 400);
		
		event.id = 5;
		
		root = mapper.valueToTree(event);
		
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Could not delete event");
		assertEquals(responsecode.valor, 500);
		
		root = mapper.createObjectNode();
		root.put("ERRO", "ISTO NAO E UMA MENSAGEM FORMATO JSON");
		
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Invalid input JSON");
		assertEquals(responsecode.valor, 400);

		response = httpRequest(context, "GET", root, responsecode);
		
		assertEquals(response, "Invalid method");
		assertEquals(responsecode.valor, 405);
	}
	
	@Test
	void insertTask_AddingAndEditingTasks_PositiveResults() {
		
		ObjectMapper mapper;
		Connection dbConn = null;
		PreparedStatement statement;
		ResultSet result;
		
        try {
			dbConn = DriverManager.getConnection("jdbc:sqlite:servertestdata.db");
			statement = dbConn.prepareStatement("DELETE FROM tasks");
			statement.executeUpdate();
			statement = dbConn.prepareStatement("DELETE FROM events");
			statement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		
		Task task = new Task();
		Task resp = new Task();
		Task taskdb = new Task();
	
		taskGenerico(task);
		
		ObjectNode root = mapper.valueToTree(task);
		
        String response;
        ResponseCode responsecode = new ResponseCode(0);
        
		try {
			
	        String context = "insertTask";
	        
			response = httpRequest(context, "POST", root, responsecode);
	        
	        resp = mapper.readValue(response, Task.class);
	        
	        task.id = resp.id;
	        task.lastEdit = resp.lastEdit;
	        
	        statement = dbConn.prepareStatement("SELECT * FROM tasks WHERE Id = ?");
	        statement.setLong(1, task.id);
	        result = statement.executeQuery();
	        while(result.next()) {
            	taskdb = new Task(result);
            }
	        
	        assertEquals(mapper.writeValueAsString(task), mapper.writeValueAsString(taskdb));
	        assertEquals(mapper.writeValueAsString(task), mapper.writeValueAsString(resp));
	        assertEquals(responsecode.valor, 200);
	        
	        task.title = "TESTE UPDATE EVENTO";
	        task.description = "UPDATE EVENTO";
	        
	        root = mapper.valueToTree(task);
	        
	        response = httpRequest(context, "POST", root, responsecode);
        	
        	resp = mapper.readValue(response.toString(), Task.class);
        	
        	task.lastEdit = resp.lastEdit;
        	
        	statement = dbConn.prepareStatement("SELECT * FROM tasks WHERE Id = ?");
	        statement.setLong(1, task.id);
	        result = statement.executeQuery();
	        while(result.next()) {
            	taskdb = new Task(result);
            }
	        
	        assertEquals(mapper.writeValueAsString(task), mapper.writeValueAsString(taskdb));
        	assertEquals(mapper.writeValueAsString(task), mapper.writeValueAsString(resp));
        	assertEquals(responsecode.valor, 200);
	        
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void insertTask_AddingAndEditingTasks_NegativeResults(){
		ObjectMapper mapper;
		
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        
		Task task = new Task();
		taskGenerico(task);
		
        String context = "insertTask";
        String response;
        ResponseCode responsecode = new ResponseCode(0);
        
		task.userId = "";
		
		ObjectNode root = mapper.valueToTree(task);
        
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Invalid user id");
		assertEquals(responsecode.valor, 401);
		
		root = mapper.createObjectNode();
		root.put("ERRO", "ISTO NAO E UMA MENSAGEM FORMATO JSON");
		
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Invalid input JSON");
		assertEquals(responsecode.valor, 400);
		
		response = httpRequest(context, "GET", root, responsecode);
		
		assertEquals(response, "Invalid method");
		assertEquals(responsecode.valor, 405);
        
		task.id = 5;
		task.userId = "UNIT_TESTS_NEG_SERVIDOR";
		
		root = mapper.valueToTree(task);
		
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Could not update task");
		assertEquals(responsecode.valor, 500);
	}

	@Test
	void deleteTask_DeletingTasks_PositiveResults() {

		ObjectMapper mapper;
		Connection dbConn = null;
		PreparedStatement statement;
		ResultSet result;
		
        try {
			dbConn = DriverManager.getConnection("jdbc:sqlite:servertestdata.db");
			statement = dbConn.prepareStatement("DELETE FROM tasks");
			statement.executeUpdate();
			statement = dbConn.prepareStatement("DELETE FROM events");
			statement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		
		Task task = new Task();
		Task taskdb = new Task();
		taskGenerico(task);
		
        String response;
        ResponseCode responsecode = new ResponseCode(0);
        
        try {
			statement = task.prepareStatement(dbConn);
			statement.executeUpdate();
			result = statement.getGeneratedKeys();
        	if(result != null && result.next()) task.id = result.getLong(1);
        	
        	ObjectNode root = mapper.valueToTree(task);
        	
        	String context = "deleteTask";
        	
        	response = httpRequest(context, "POST", root, responsecode);
        	
        	assertEquals("", response);
        	assertEquals(200, responsecode.valor);
        	
        	statement = dbConn.prepareStatement("SELECT * FROM tasks WHERE Id = ?");
	        statement.setLong(1, task.id);
	        result = statement.executeQuery();
	        while(result != null && result.next()) {
	        	taskdb = new Task(result);
	        }
	        
	        assertEquals(true, taskdb.deleted);
        	
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void deleteTask_DeletingTasks_NegativeResults(){
		ObjectMapper mapper;
		
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        
		Task task = new Task();
		taskGenerico(task);
		
        String context = "deleteTask";
        String response;
        ResponseCode responsecode = new ResponseCode(0);
        
		task.userId = "";
		
		ObjectNode root = mapper.valueToTree(task);
        
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Invalid user id");
		assertEquals(responsecode.valor, 401);
		
		task.id = 0;
		task.userId = "UNIT_TESTS_NEG_SERVIDOR";
		
		root = mapper.valueToTree(task);
		
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Invalid task id");
		assertEquals(responsecode.valor, 400);
        
		task.id = 5;
		
		root = mapper.valueToTree(task);
		
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Could not delete task");
		assertEquals(responsecode.valor, 500);
		
		root = mapper.createObjectNode();
		root.put("ERRO", "ISTO NAO E UMA MENSAGEM FORMATO JSON");
		
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals(response, "Invalid input JSON");
		assertEquals(responsecode.valor, 400);
		
		response = httpRequest(context, "GET", root, responsecode);
		
		assertEquals(response, "Invalid method");
		assertEquals(responsecode.valor, 405);
	}

	@Test
	void get_GetUserEventsAndTasks_PositiveResults(){

		Connection dbConn = null;
		PreparedStatement statement;
		ResultSet result;
		ObjectMapper mapper;
		
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        
        JsonNode eventsNode;
        JsonNode tasksNode;
        ObjectNode root = mapper.createObjectNode();
        
		try {
			dbConn = DriverManager.getConnection("jdbc:sqlite:servertestdata.db");
			statement = dbConn.prepareStatement("DELETE FROM tasks");
			statement.executeUpdate();
			statement = dbConn.prepareStatement("DELETE FROM events");
			statement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		ArrayList<Event> events = new ArrayList<>();
		Event event1 = new Event();
		Event event2 = new Event();
		Event event3 = new Event();
	
		eventoGenerico(event1);
		eventoGenerico(event2);
		eventoGenerico(event3);
		
		ArrayList<Task> tasks = new ArrayList<>();
		Task task1 = new Task();
		Task task2 = new Task();
	
		taskGenerico(task1);
		taskGenerico(task2);
		
        String response;
        ResponseCode responsecode = new ResponseCode(0);
        
        try {
        	String context = "get?userId=UNIT_TESTS_SERVIDOR&since=0";
        	
        	event1.title = "EVENTO1";
	        event1.description = "EVENTO1";
	        statement = event1.prepareStatement(dbConn);
			statement.executeUpdate();
			result = statement.getGeneratedKeys();
        	if(result != null && result.next()) event1.id = result.getLong(1);
	        
	        events.add(event1);
	        
	        event2.title = "EVENTO2";
	        event2.description = "EVENTO2";
	        statement = event2.prepareStatement(dbConn);
			statement.executeUpdate();
			result = statement.getGeneratedKeys();
        	if(result != null && result.next()) event2.id = result.getLong(1);
	        
	        events.add(event2);
        	
        	event3.title = "EVENTO3";
	        event3.description = "EVENTO3";
	        statement = event3.prepareStatement(dbConn);
			statement.executeUpdate();
			result = statement.getGeneratedKeys();
        	if(result != null && result.next()) event3.id = result.getLong(1);
	        
	        events.add(event3);
	        
	        task1.title = "TASK1";
	        task1.description = "TASK1";
	        statement = task1.prepareStatement(dbConn);
			statement.executeUpdate();
			result = statement.getGeneratedKeys();
        	if(result != null && result.next()) task1.id = result.getLong(1);
	        
	        tasks.add(task1);
	        
	        task2.title = "TASK2";
	        task2.description = "TASK2";
	        statement = task2.prepareStatement(dbConn);
			statement.executeUpdate();
			result = statement.getGeneratedKeys();
        	if(result != null && result.next()) task2.id = result.getLong(1);
	        
	        tasks.add(task2);
	        
	        root.put("CONTEUDO", "APENAS CONTEUDO ROOT");
	        
	        response = httpRequest(context, "GET", root, responsecode);
	        
	        root = mapper.createObjectNode();
            eventsNode = mapper.valueToTree(events);
            tasksNode = mapper.valueToTree(tasks);
            root.set("events", eventsNode);
            root.set("tasks", tasksNode);
            
            assertEquals(root.toString(), response);
            
            event1.title = "EVENTO1 APOS UPDATE";
            event1.description = "EVENTO1 APOS UPDATE";
            statement = event1.prepareStatement(dbConn);
			statement.executeUpdate();
            
            long lastEdit = event1.lastEdit - 1;
            
            task1.title = "TASK1 APOS UPDATE";
            task1.description = "TASK1 APOS UPDATE";
            statement = task1.prepareStatement(dbConn);
			statement.executeUpdate();
            
            context = "get?userId=UNIT_TESTS_SERVIDOR&since=" + lastEdit;
            
            response = httpRequest(context, "GET", root, responsecode);
            
            events.clear();
            events.add(event1);
            tasks.clear();
            tasks.add(task1);
            root = mapper.createObjectNode();
            eventsNode = mapper.valueToTree(events);
            tasksNode = mapper.valueToTree(tasks);
            root.set("events", eventsNode);
            root.set("tasks", tasksNode);
            
            assertEquals(root.toString(), response);
	        
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void get_GetUserEventsAndTasks_NegativeResults(){
		ObjectMapper mapper;
		
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        
        ObjectNode root = mapper.createObjectNode();
		root.put("CONTEUDO", "APENAS CONTEUDO ROOT");
        
        ResponseCode responsecode = new ResponseCode(0);
        
		String context = "get";
		String response;
		
		response = httpRequest(context, "GET", root, responsecode);
		
		assertEquals("Missing userId", response);
		assertEquals(403, responsecode.valor);
		
		context = "get?userId=UNIT_TESTS_NEG_SERVIDOR&since=0";
		
		response = httpRequest(context, "POST", root, responsecode);
		
		assertEquals("Invalid method", response);
		assertEquals(405, responsecode.valor);
	}

}
