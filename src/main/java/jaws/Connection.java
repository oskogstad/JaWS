package jaws;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class Connection extends Thread {

    final Socket socket;
    final Base64.Decoder b64decoder = Base64.getDecoder();
    private LinkedList<String> messageQueue;
    final JaWS jaws;
    private boolean isInterrupted;

    public Connection(JaWS jaws, Socket socket) {
        this.jaws = jaws;
        this.socket = socket;
        messageQueue = new LinkedList<String>();
    }

    @Override
    public void run() {
        try(
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
           )
        {

            System.out.println("Connection open");

            while(!isInterrupted) {

                if(input.available() > 0) {
                    Frame f = new Frame(input);

                    // Send pong if message is Ping
                    if(f.isPing) {
                        output.write(f.frameBytes);
                    } else {
                        jaws.onMessage(this, f.message);
                    }
                }
                synchronized(messageQueue) {
                    if(!messageQueue.isEmpty()) {
                        for (String s : messageQueue) {
                            Frame f = new Frame(s);
                            output.write(f.frameBytes);
                            output.flush();
                        }
                        messageQueue.clear();
                    }
                }

                try {
                    Thread.sleep(20);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        synchronized(messageQueue) {
            messageQueue.add(message);
        }
    }

    @Override
    public void interrupt() {
        this.isInterrupted = true;
    }
}
