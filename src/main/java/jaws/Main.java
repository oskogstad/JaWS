package jaws;
import java.io.*;

public class Main implements WebSocketEventHandler {
    public Main() {
        JaWS jaws = new JaWS(40506);
        jaws.setEventHandler(this);
        jaws.start();

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
