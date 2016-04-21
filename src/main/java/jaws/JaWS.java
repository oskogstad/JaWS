package jaws;
import java.net.ServerSocket;
import java.net.Socket;

public class JaWS {
    public static final int PORT=80;

    public static void main(String[] args) {

        // list of all connections
        ArrayList<Connection> connections = new ArrayList();

        ServerSocket socketServer = new ServerSocket(PORT);
        System.out.println("Server now listening on port " + PORT);

        // ShutdownHook, catches any interrupt signal and closes all threads
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    // Close all threads
                    System.out.println("Number of threads: " + connections.size());

                    for (Connection c : connections) {
                        c.interrupt();
                    }

                    System.out.println("All done. Bye!");

                } catch(Exception e) {
                    System.out.println("Thread shutdown failed ...");
                    e.printStackTrace();
                }
            }
        }));

        while(true) {
            try {
                // Waiting for connections
                Socket socket = socketServer.accept();
				System.out.println("Incomming connection ...");

				ArrayList<String> httpReq = new ArrayList();

                try {
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                    PrintWriter out = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                } catch(Exception e) {
                    e.printStackTrace();
                    System.out.println("IO error on socket creation");
                }

                // Adding httpReq to string array
				String s;
				while((s=in.readLine()) != null) {
					System.out.println(s);
					if(s.isEmpty()) {
						break;
					}
					httpReq.add(s);
				}
                // todo: check for websocket headers and stuff
                // send http response


                // check for websocket, start new connection and add to thread array
                //if(websocket) {
                    Connection connection = new Connection(socket);
                    connections.add(connection);
                    connection.start();
                //}
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("Socket accept failed");
            }
        }
    }
}
