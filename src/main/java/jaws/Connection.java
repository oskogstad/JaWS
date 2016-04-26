package jaws;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class Connection extends Thread {

    final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;

    final Base64.Decoder b64decoder = Base64.getDecoder();
    final JaWS jaws;

    public Connection(JaWS jaws, Socket socket) throws IOException {
        this.jaws = jaws;
        this.socket = socket;

        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            try {
                Frame f = new Frame(input);
                switch(f.opcode) {
                    case PING:
                        output.write(Frame.PONG_FRAME);
                        break;
                    case CONNECTION_CLOSE:
                        this.close();
                        break;
                    case TEXT:
                        jaws.onMessage(this, f.message);
                        break;
                    default:
                        Logger.log("Unhandled message with opcode "+f.opcode, Logger.WS_IO);
                        break;
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String message) {
        new Thread() {
            
            @Override
            public void run() {
                Frame f = new Frame(message);
                try {
                    output.write(f.frameBytes);
                }
                catch(IOException e) {
                    e.printStackTrace(); 
                }
            }

        }.start();
    }

    public void close() {
        try {
            input.close();
            output.close();
            socket.close();

            this.interrupt();
            jaws.onDisconnect(this);
        }
        catch(IOException e) {
            Logger.logErr("Exception thrown while closing connection!", Logger.WS_IO);
            e.printStackTrace();
        }
    }
}

