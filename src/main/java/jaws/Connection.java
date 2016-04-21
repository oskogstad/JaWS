package jaws;

import java.io.*;
import java.net.*;

public class Connection extends Thread {

    final Socket socket;

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
            byte[] header = new byte[2];
            input.read(header, 0, header.length);

            boolean fin = (header[0]>>7) != 0;

        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}

