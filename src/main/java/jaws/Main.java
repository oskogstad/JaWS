package jaws;
import java.io.*;
import java.util.ArrayList;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.*;

public class Main implements WebSocketEventHandler {
    private JaWS jaws;
    private JsonParser jsonParser;
    private StringEscapeUtils stringEscape;
    private int numberOfConnections;
    private ArrayList<String> chatlogArray;
    private File chatlog;
    private BufferedReader reader;

    public Main() {
        jaws = new JaWS(40506);
        jaws.setEventHandler(this);
        jaws.start();
        jsonParser = new JsonParser();
        stringEscape = new StringEscapeUtils();
        numberOfConnections = 0;

        // Read chatlog.txt to chatlogArray
        chatlogArray = new ArrayList();
        try {
            chatlog = new File("chatlog.txt");
            reader = new BufferedReader(new FileReader(chatlog));
            String text = null;
            while ((text = reader.readLine()) != null) {
                chatlogArray.add(text);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        // ShutdownHook, catches any interrupt signal and closes all threads
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                jaws.close();
            }
        }));
    }

    @Override
    public void onMessage(Connection con, String message) {
        JsonElement jsonElem = jsonParser.parse(message);
        if (jsonElem instanceof JsonObject) {
            JsonObject json = (JsonObject)jsonElem;
            // escape all text from client
            json.addProperty("name", StringEscapeUtils.escapeHtml4(json.get("name").getAsString()));
            json.addProperty("msg", StringEscapeUtils.escapeHtml4(json.get("msg").getAsString()));
            json.addProperty("timestamp", StringEscapeUtils.escapeHtml4(json.get("timestamp").getAsString()));

            // send to all clients
            jaws.broadcast(json.toString());
        }

    }

    @Override
    public void onConnect(Connection con) {
        for (String s: chatlogArray) {
            Logger.log(s, Logger.GENERAL);
        }

        Logger.log("New connection", Logger.GENERAL);
        numberOfConnections++;

        // Broadcast JSON with new numberOfConnections
        JsonObject json = new JsonObject();
        json.addProperty("numberOfCon", numberOfConnections);
        jaws.broadcast(json.toString());

        Logger.log("Number of connections: " + numberOfConnections, Logger.GENERAL);
    }

    @Override
    public void onDisconnect(Connection con) {
        Logger.log("Connection disconnected", Logger.GENERAL);
        numberOfConnections--;

        // Broadcast JSON with new numberOfConnections
        JsonObject json = new JsonObject();
        json.addProperty("numberOfCon", numberOfConnections);
        jaws.broadcast(json.toString());

        Logger.log("Number of connections: " + numberOfConnections, Logger.GENERAL);
    }

    public void writeToChatlog(String text) {
        // make new thread, write to array and file
    }

    public static void main(String[] args) {
<<<<<<< HEAD
        Logger.logLevel = Logger.ALL;
=======
        Logger.logLevel = Logger.ALL & ~(Logger.WS_PARSE | Logger.WS_IO);

>>>>>>> dd0faa1392d47997273db8ef05a92d65f15e9f61
        new Main();
    }
}
