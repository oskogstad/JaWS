JaWS
====


JaWS is a Java Websocket Server implemented for an assignment in a network programming course at NTNU.

Demo Install Guide
-------------

1. Install Java JDK from http://www.oracle.com/technetwork/java/javase/downloads/index.html
2. Install Gradle from http://gradle.org/gradle-download/

    2.1. ```gradle build```

    2.2. ```./run```

    NOTE: On Windwos use an IDE to run the file or run the app (.jar file)
3. In your webapp create a websocket on ```localhost:40506```

Implementation Guide
--------------------

1. Still need Java and Gradle
2. Implement the interface ```WebSocketEventHandler```
3. Simple echo server example, listening on port 40506:
``` java
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
}```
