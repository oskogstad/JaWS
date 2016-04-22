package jaws;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.nio.ByteBuffer;

public class Connection extends Thread {

    final Socket socket;
    final Base64.Decoder b64decoder = Base64.getDecoder();

    final static String[] opcodeNames = new String[] {
        "continuation frame",
        "text frame",
        "binary frame",
        "further non-control frame",
        "further non-control frame",
        "further non-control frame",
        "further non-control frame",
        "further non-control frame",
        "connection close",
        "ping",
        "pong",
        "further control frame",
        "further control frame",
        "further control frame",
        "further control frame",
        "further control frame",
    };

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

            boolean fin = ((int)header[0]&0x80) != 0;
            boolean maskBit = (((int)header[1])&0x80) != 0;
            int opcode = (int)header[0]&0x0F;

            long payloadLen = ((int)header[1])&0x7F;
            if (payloadLen == 126) {
                // Read the next 2 bytes as payload length
                byte[] b = new byte[2];
                input.read(b, 0, b.length);
                ByteBuffer buffer = ByteBuffer.wrap(b);
                payloadLen = buffer.getShort();
            }
            else if (payloadLen == 127) {
                // Read the next 8 bytes as payload length
                byte[] b = new byte[8];
                input.read(b, 0, b.length);
                ByteBuffer buffer = ByteBuffer.wrap(b);
                payloadLen = buffer.getLong();
            }

            byte[] mask = new byte[4];

            if (maskBit) {
                input.read(mask, 0, mask.length);
            }

            // This will fail for messages of size bigger than int max val.
            byte[] payload = new byte[(int)payloadLen];
            input.read(payload, 0, (int)payloadLen);

            String message = new String(payload);
            if (maskBit) {
                message = decode(payload, mask);
            }

            System.out.println("--------------MESSAGE--------------");
            System.out.println("Fin: "+fin);
            System.out.println("Mask: "+maskBit);
            System.out.println("Opcode: "+opcodeNames[opcode]);
            System.out.println(message);
            System.out.println("----------------FIN----------------");

            byte[] out = new byte[2];
            out[0] |= (0x80);
            out[0] |= opcode;
            System.out.println("PayloadLen: "+payloadLen);
            out[1] |= payloadLen;

            System.out.println("out[0]: "+out[0]);
            System.out.println("out[1]: "+out[1]);
            output.write(out[0]);
            output.write(out[1]);
            output.write(message.getBytes());
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private String decode(byte[] payload, byte[] mask) {
        String decoded = "";
        for (int i=0; i<payload.length; i++) {
            decoded += (char)((int)payload[i] ^ (int)mask[i%4]);
        }
        return decoded;
    }
}
