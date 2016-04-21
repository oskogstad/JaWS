package jaws;

import java.io.*;
import java.net.*;
import java.util.Base64;

public class Connection extends Thread {

    final Socket socket;
    final Base64.Decoder b64decoder = Base64.getDecoder();

    public Connection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try(
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
           )
        {
            System.out.println("Connection open");

            byte[] header = new byte[2];
            input.read(header, 0, header.length);

            boolean fin = (header[0]>>7) != 0;
            boolean maskBit = (((int)header[1])&0x80) != 0;

            int payloadLen = ((int)header[1])&0x7F;
            if (payloadLen == 126) {
                // Read the next 2 bytes as payload length
            }
            else if (payloadLen == 127) {
                // Read the next 8 bytes as payload length
            }

            byte[] mask = new byte[4];

            if (maskBit) {
                input.read(mask, 0, mask.length); 
            }

            byte[] payload = new byte[payloadLen];
            input.read(payload, 0, payloadLen);
            if (maskBit) {
                decode(payload, mask);
            }
            
            String message = new String(payload, "UTF-8");

            System.out.println("--------------MESSAGE--------------");
            System.out.println("Fin: "+fin);
            System.out.println("Mask: "+maskBit);
            System.out.println(payload);
            System.out.println("--------------FIN--------------");

        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void decode(byte[] payload, byte[] mask) {
        for (int i=0; i<payload.length; i++) {
            payload[i] = (byte)((int)payload[i] ^ (int)mask[i%4]);
        }
    }
}

