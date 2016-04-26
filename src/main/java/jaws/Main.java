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

    public Main() {
        jaws = new JaWS(40506);
        jaws.setEventHandler(this);
        jaws.start();
        jsonParser = new JsonParser();
        stringEscape = new StringEscapeUtils();

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

    }

    @Override
    public void onDisconnect(Connection con) {

    }

    public static void main(String[] args) {
        new Main();
    }
}
