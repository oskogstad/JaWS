package jaws;

/**
 * WebSocketEventHandler.java
 *
 * To recieve messages from and about clients connected, you must implement this interface, and register the handler with the JaWS-object
 */
public interface WebSocketEventHandler {
    /**
     * Called when the JaWS-server sets up a new connection.
     * @param con The newly created connection.
     */
    public void onConnect(Connection con);

    /**
     * Called when the Connection has recieved a text message from the client.
     * @param con The connection the message came from
     * @param message The message that was recieved
     */
    public void onMessage(Connection con, String message);

    /**
     * Called when a client is disconnected.
     * Note that you should NOT call send on this object now, or later. However, the broadcast-method on the JaWS-object is
     * safe to use if you wish to warn the other connected clients about the disconnection.
     * @param con The connection that just disconnected. Use to identify who disconnected, but do not attempt to interact any further with this object.
     */
    public void onDisconnect(Connection con);

    /**
     * Called when a connection recieves a PONG frame.
     * @param con The connection that recieved the PONG frame
     */
    public void onPong(Connection con);
}

