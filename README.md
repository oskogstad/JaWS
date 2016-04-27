JaWS
====


JaWS is a Java Websocket Server implemented for an assignment in a network programming course at NTNU.

Missing WebSocket support
---------------
* Binary frames
* Continuation frames should be supported, but not tested.

Implementation Guide
--------------------

* Install Java JDK from http://www.oracle.com/technetwork/java/javase/downloads/index.html
* Install Gradle from http://gradle.org/gradle-download/

    - ```gradle build```

    - ```gradle javadoc``` if you want javadoc.

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
