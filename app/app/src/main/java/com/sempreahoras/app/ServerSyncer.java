package com.sempreahoras.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.nio.charset.StandardCharsets;

public class ServerSyncer {
    private final String host = "https://sempreahoras.herokuapp.com/";
    private final String lastEditTag = "lastEdit";

    private RequestQueue queue;
    private SharedPreferences prefs;
    private ObjectMapper mapper;

    private Repository repo;

    Context context;

    ServerSyncer(Context context) {
        this.context = context;

        prefs = context.getSharedPreferences("data", Context.MODE_PRIVATE);

        JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        queue = Volley.newRequestQueue(context);

        repo = new Repository(context);
    }

    private String getUrl(String path) {
        return host + path;
    }

    void fetchNewData(SimpleCallback post) {
        if(isNetworkAvailable(context)) {
            StringRequest request = new StringRequest(Request.Method.GET,
                                                      getUrl("get?userId=" + MainActivity.userId +
                                                             "&since=" + prefs.getLong(lastEditTag+MainActivity.userId, 0)),
                    response -> {
                        try {
                            JsonNode root = mapper.readTree(response);
                            Event[] events = mapper.treeToValue(root.get("events"), Event[].class);
                            Task[] tasks = mapper.treeToValue(root.get("tasks"), Task[].class);

                            long newLastEdit = prefs.getLong(lastEditTag+MainActivity.userId, 0);

                            if(events.length > 0) {
                                for (Event event : events) {
                                    repo.insertEvent(event);
                                    if (event.lastEdit > newLastEdit) {
                                        newLastEdit = event.lastEdit;
                                    }
                                }
                            }

                            if(tasks.length > 0) {
                                for (Task task : tasks) {
                                    repo.insertTask(task);
                                    if (task.lastEdit > newLastEdit) {
                                        newLastEdit = task.lastEdit;
                                    }
                                }
                            }

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putLong(lastEditTag+MainActivity.userId, newLastEdit);
                            editor.apply();

                            post.run(null);
                        } catch (JsonProcessingException e) {
                            post.run("Could not parse JSON");
                        }
                    },
                    error -> post.run(error.toString()));

            queue.add(request);
        }
    }

    void sendNewEvent(Event event, EventCallback post) {
        event.ensureConsistency();

        if(isNetworkAvailable(context)) {
            try {
                final String result = mapper.writeValueAsString(event);

                StringRequest request = new StringRequest(Request.Method.POST, getUrl("insertEvent"),
                        response -> {
                            try {
                                Event finalEvent = mapper.readValue(response, Event.class);
                                post.run(finalEvent, null);
                            }
                            catch (JsonProcessingException e) {
                                post.run(null, e.toString());
                            }
                        },
                        error -> post.run(null, error.toString()))
                {
                    @Override
                    public byte[] getBody() {
                        return result.getBytes(StandardCharsets.UTF_8);
                    }
                };

                queue.add(request);
            }
            catch (JsonProcessingException e) {
                post.run(null, "Could not convert event to JSON");
            }
        }
        else {
            post.run(null, "You need to be connected to the Internet for editing events!");
        }
    }

    void deleteEvent(long eventId, String userId, SimpleCallback post) {
        if(isNetworkAvailable(context)) {
            StringRequest request = new StringRequest(Request.Method.POST, getUrl("deleteEvent"),
                    response -> post.run(null),
                    error -> post.run(error.toString()))
            {
                @Override
                public byte[] getBody() {
                    return ("{\"id\":" + eventId + ",\"userId\":\"" + userId + "\"}").getBytes(StandardCharsets.UTF_8);
                }
            };

            queue.add(request);
        }
        else {
            post.run("You need to be connected to the Internet for deleting events!");
        }
    }

    void sendNewTask(Task task, TaskCallback post) {
        if(isNetworkAvailable(context)) {
            try {
                final String result = mapper.writeValueAsString(task);

                StringRequest request = new StringRequest(Request.Method.POST, getUrl("insertTask"),
                        response -> {
                            try {
                                Task finalTask = mapper.readValue(response, Task.class);
                                post.run(finalTask, null);
                            }
                            catch (JsonProcessingException e) {
                                post.run(null, e.toString());
                            }
                        },
                        error -> post.run(null, error.toString()))
                {
                    @Override
                    public byte[] getBody() {
                        return result.getBytes(StandardCharsets.UTF_8);
                    }
                };

                queue.add(request);
            }
            catch (JsonProcessingException e) {
                post.run(null, "Could not convert task to JSON");
            }
        }
        else {
            post.run(null, "You need to be connected to the Internet for editing tasks!");
        }
    }

    void deleteTask(long taskId, String userId, SimpleCallback post) {
        if(isNetworkAvailable(context)) {
            StringRequest request = new StringRequest(Request.Method.POST, getUrl("deleteTask"),
                    response -> post.run(null),
                    error -> post.run(error.toString()))
            {
                @Override
                public byte[] getBody() {
                    return ("{\"id\":" + taskId + ",\"userId\":\"" + userId + "\"}").getBytes(StandardCharsets.UTF_8);
                }
            };

            queue.add(request);
        }
        else {
            post.run("You need to be connected to the Internet for deleting tasks!");
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public interface SimpleCallback {
        void run(String error);
    }

    public interface EventCallback {
        void run(Event e, String error);
    }

    public interface TaskCallback {
        void run(Task t, String error);
    }
}
