JaWS
====

JaWS is a Java Websocket server implemented as an assignment in a network programming course at NTNU.

It provides a simple, event based interface. All write calls are asynchronous, so JaWS will never hog the main thread.
Every connection gets a thread for reading, and on write calls, a thread is created to handle the write.

Messages are sent to clients by calling ```send()``` on a Connection object, or ```broadcast()``` on the JaWS object.

Supported WebSocket features
----------------------------
* Text frames. Tested with strings up to 300MB long
* Ping/Pong. Not tested
* Proper closing. Reasons are sent from the server, but reasons sent from the client are ignored.
* Continuation frames should be supported, but not tested.

Missing WebSocket features
--------------------------
* Binary frames

Building
--------
* Install Java JDK from http://www.oracle.com/technetwork/java/javase/downloads/index.html
* Install Gradle from http://gradle.org/gradle-download/

    - ```gradle build```

    - ```gradle javadoc``` if you want javadoc.


Implementation Guide
--------------------

* Simple echo server example, listening on port 40506:

``` java
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
```
