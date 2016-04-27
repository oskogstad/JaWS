package jaws.demo;

import jaws.*;

public class Main implements WebSocketEventHandler {
    private JaWS jaws;

    public Main() {
        jaws = new JaWS(40506);
        jaws.setEventHandler(this);
        jaws.start();
    }

    @Override
    public void onMessage(Connection con, String message) {
        jaws.broadcast(message);
    }

    @Override
    public void onConnect(Connection con) {

    }

    @Override
    public void onDisconnect(Connection con) {

    }

    @Override
    public void onPong(Connection con) {

    }

    public static void main(String[] args) {
        new Main();
    }
}
