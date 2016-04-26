package jaws;
import java.io.*;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.*;

public class Main implements WebSocketEventHandler {

    private JaWS jaws;
    private JsonParser jsonParser;
    private StringEscapeUtils stringEscape;
    private int numberOfConnections;

    public Main() {
        jaws = new JaWS(40506);
        jaws.setEventHandler(this);
        jaws.start();
        jsonParser = new JsonParser();
        stringEscape = new StringEscapeUtils();
        numberOfConnections = 0;

        // ShutdownHook, catches any interrupt signal and closes all threads
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                jaws.close();
            }
        }));
    }

    @Override
    public void onMessage(Connection con, String message) {
        JsonObject json = (JsonObject) jsonParser.parse(message);

        // escape all text from client
        json.addProperty("name", StringEscapeUtils.escapeHtml4(json.get("name").getAsString()));
        json.addProperty("msg", StringEscapeUtils.escapeHtml4(json.get("msg").getAsString()));
        json.addProperty("timestamp", StringEscapeUtils.escapeHtml4(json.get("timestamp").getAsString()));

        // send to all clients
        jaws.broadcast(json.toString());
    }

    @Override
    public void onConnect(Connection con) {
        numberOfConnections++;
        // make json and broadcast change to chat.tilfeldig.info
        System.out.println("number of con: " + numberOfConnections);
    }

    @Override
    public void onDisconnect(Connection con) {
        Logger.log("Connection disconnected", Logger.GENERAL);
        numberOfConnections--;
        // make json and broadcast change to chat.tilfeldig.info
        Logger.log("number of con: " + numberOfConnections, Logger.GENERAL);
    }

    public static void main(String[] args) {
        Logger.logLevel = Logger.ALL;

        new Main();
    }
}
