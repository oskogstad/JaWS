package jaws;
import java.io.*;

public class Main implements WebSocketEventHandler {
    public Main() {
        try {
            JaWS jaws = new JaWS(this, 40506);
            jaws.start();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main();
    }

    @Override
    public void onMessage() {
        System.out.println("overwritted");
    }
}
