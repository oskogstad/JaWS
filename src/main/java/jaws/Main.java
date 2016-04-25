package jaws;
import java.io.*;
import com.google.gson.*;

public class Main implements WebSocketEventHandler {

    private JaWS jaws;
    //private JsonParser jsonParser;

    public Main() {
        jaws = new JaWS(40506);
        jaws.setEventHandler(this);
        jaws.start();
        //jsonParser = new JsonParser();
        // ShutdownHook, catches any interrupt signal and closes all threads
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                jaws.close();
            }
        }));
    }

    @Override
    public void onMessage(Connection con, String message) {
        System.out.println(message);
        //JsonElement json = jsonParser.parse(message);
        //System.out.println(json.toString());
        jaws.broadcast(message);
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
