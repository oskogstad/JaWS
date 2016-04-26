package jaws;
import java.io.*;
import com.google.gson.*;

public class Main implements WebSocketEventHandler {

    private JaWS jaws;
    private JsonParser jsonParser;

    public Main() {
        jaws = new JaWS(40506);
        jaws.setEventHandler(this);
        jaws.start();
        jsonParser = new JsonParser();

        // ShutdownHook, catches any interrupt signal and closes all threads
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                jaws.close();
            }
        }));
    }

    @Override
    public void onMessage(Connection con, String message) {
        Logger.log(message, Logger.GENERAL);
        JsonElement json = jsonParser.parse(message);
        Logger.log(json.toString(), Logger.JSON);
        
        jaws.broadcast(message);
    }

    @Override
    public void onConnect(Connection con) {

    }

    @Override
    public void onDisconnect(Connection con) {
        Logger.log("Connection disconnected", Logger.GENERAL);
    }

    public static void main(String[] args) {
        Logger.logLevel = Logger.JSON | Logger.GENERAL;

        new Main();
    }
}
