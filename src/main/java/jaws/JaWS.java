package jaws;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.*;
import java.security.*;

public class JaWS extends Thread {
    private final int PORT;
    private final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private final ServerSocket SOCKET_SERVER;

    private WebSocketEventHandler wsHandler;
    private Base64.Encoder b64encoder;
    private MessageDigest sha1digester;
    private ArrayList<Connection> connections;

    public JaWS(WebSocketEventHandler wsHandler, int port) throws IOException {
        this.PORT = port;
        this.wsHandler = wsHandler;
        SOCKET_SERVER = new ServerSocket(PORT);
        connections = new ArrayList();

        // Utilities
        b64encoder = Base64.getEncoder();
        try {
            sha1digester = MessageDigest.getInstance("SHA-1");
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // ShutdownHook, catches any interrupt signal and closes all threads
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    // Close all threads
                    System.out.println("Number of threads: " + connections.size());
                    for (Connection c : connections) {
                        c.interrupt();
                    }
                    if (SOCKET_SERVER != null) SOCKET_SERVER.close();
                    System.out.println("All done. Bye!");
                } catch(Exception e) {
                    System.out.println("Thread shutdown failed ...");
                    e.printStackTrace();
                }
            }
        }));
    }

    @Override
    public void run() {
        while(true) {
            try {
                System.out.println("Server now listening on port " + PORT);

                // Waiting for connections
                Socket socket = SOCKET_SERVER.accept();
				System.out.println("Incomming connection ...");

				ArrayList<String> httpReq = new ArrayList();

                try {
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                    PrintWriter out = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    // Adding httpReq to string array
                    String s;
                    while((s=in.readLine()) != null) {
                        if(s.isEmpty()) {
                            break;
                        }
                        httpReq.add(s);
                    }

                    String upgrade = null;
                    String connection = null;
                    String wsKey = null;
                    String[] wsProtocol = null;
                    int wsVersion = -1;
                    for (String line : httpReq) {
                        System.out.println(line);

                        String[] parts = line.split(": ");
                        if (parts.length == 1) {
                            // Should we check the GET ... line here?
                        }
                        else {
                            String key = parts[0];
                            String val = parts[1];

                            if(key.toLowerCase().contains("upgrade")) {
                                upgrade = val;
                            }
                            else if(key.equalsIgnoreCase("connection")) {
                                connection = val;
                            }
                            else if(key.equalsIgnoreCase("sec-websocket-key")) {
                                wsKey = val;
                            }
                            else if(key.equalsIgnoreCase("sec-websocket-protocol")) {
                                wsProtocol = val.split(",");
                            }
                            else if(key.equalsIgnoreCase("sec-websocket-version")) {
                                wsVersion = Integer.parseInt(val);
                            }
                        }
                    }

                    boolean websocket = false;
                    if (
                            upgrade != null && upgrade.equalsIgnoreCase("websocket") &&
                            connection != null && connection.toLowerCase().contains("upgrade") &&
                            wsKey != null)
                    {
                        websocket = true;
                        // Send handshake response


                        String acceptKey = b64encoder.encodeToString(
                                sha1digester.digest((wsKey+GUID).getBytes()));

                        out.write(
                                "HTTP/1.1 101 Switching Protocols\r\n"+
                                "Upgrade: websocket\r\n"+
                                "Connection: Upgrade\r\n"+
                                "Sec-WebSocket-Accept: "+acceptKey+
                                "\r\n\r\n");
                        out.flush();

                        System.out.println("Handshake sent");
                    }
                    else {
                        out.write(
                                "HTTPS/1.1 503 Connection Refused\r\n"+
                                "\r\n\r\n"
                                );
                        out.flush();

                        System.out.println("Connection refused");
                    }

                    if(websocket) {
                        Connection con= new Connection(socket);
                        connections.add(con);
                        con.start();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    System.out.println("IO error on socket creation");
                }
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("Socket accept failed");
            }
        }
    }
}
