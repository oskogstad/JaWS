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

            Logger.log("Connection open", Logger.GENERAL);

            while(!isInterrupted) {

                if(input.available() > 0) {
                    Frame f = new Frame(input);
                    switch(f.opcode) {
                        case PING:
                            output.write(f.frameBytes);
                            break;
                        case CONNECTION_CLOSE:
                            isInterrupted = true;
                            break;
                        case TEXT:
                            jaws.onMessage(this, f.message);
                            break;
                        default:
                            Logger.log("Unhandled message with opcode "+f.opcode, Logger.WS_IO);
                            break;
                    }
                }
                synchronized(messageQueue) {
                    if(!messageQueue.isEmpty()) {
                        for (String s : messageQueue) {
                            Logger.log("Sending message "+s, Logger.WS_IO);
                            Frame f = new Frame(s);
                            for (byte b : f.frameBytes) {
                                Logger.log("\t"+b+": "+Integer.toHexString(b), Logger.WS_IO);
                            }
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

        Logger.log("Connection closed", Logger.GENERAL);
        jaws.onDisconnect(this);
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

