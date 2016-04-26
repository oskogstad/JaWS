package jaws;

import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.io.DataInputStream;
import java.io.IOException;


public class Frame {

    public final OpCode opcode;

    public final String message;

    public final int messageLength;

    public final byte[] frameBytes;

    private static final Charset utf8 = StandardCharsets.UTF_8;

    public final byte[] mask;

    public final boolean fin;

    public final static byte[] PONG_FRAME;
    static {
        PONG_FRAME = new byte[3];
        PONG_FRAME[0] = (byte)0x8A; // Set fin flag (0x80) and opcode PONG (0x0A)
        PONG_FRAME[1] = (byte)0x01; // Set mask bit to 0 and payload length to 1
        PONG_FRAME[2] = (byte)'!';  // Set dummy payload to '!'
    }

    public Frame(String message) {
        this.message = message;
        this.mask = null;
        this.fin = true;

        opcode = OpCode.TEXT;
        byte[] messageBytes = message.getBytes(utf8);
        messageLength = messageBytes.length;

        this.frameBytes = pack(messageBytes, this.opcode.code, null);
    }

    public Frame(DataInputStream input) throws IOException {
            byte[] header = new byte[2];
            input.read(header, 0, header.length);
            this.fin = ((int)header[0]&0x80) != 0;
            boolean maskBit = (((int)header[1])&0x80) != 0;
            int op = (int)header[0]&0x0F;
            long payloadLen = ((int)header[1])&0x7F;

            this.opcode = OpCode.getOpcode(op);
            //TODO: Handle opcode = null

            if (payloadLen == 126) {
                // Read the next 2 bytes as payload length
                byte[] b = new byte[2];
                input.read(b, 0, b.length);

                payloadLen |= b[1];
                payloadLen |= (b[0]<<8);
            }
            else if (payloadLen == 127) {
                // Read the next 8 bytes as payload length
                byte[] b = new byte[8];
                input.read(b, 0, b.length);
                payloadLen |= b[3];
                payloadLen |= (b[2]<<8);
                payloadLen |= (b[1]<<16);
                payloadLen |= (b[0]<<24);
            }

            if (maskBit) {
                this.mask = new byte[4];
                input.read(mask, 0, mask.length);
            }
            else {
                this.mask = null;
            }

            // This will fail for messages of size bigger than int max val.
            byte[] payload = new byte[(int)payloadLen];
            input.read(payload, 0, (int)payloadLen);

            if (maskBit) {
                this.message = decode(payload, this.mask);
            }
            else {
                this.message = new String(payload);
            }

            byte[] messageBytes = this.message.getBytes(utf8);
            this.messageLength = messageBytes.length;

            int frameBytesLength = 2+this.messageLength;
            if(maskBit) {
                frameBytesLength += 4;
            }
            if(payloadLen >= 32*1024) {
                frameBytesLength += 8;
            }
            else if (payloadLen >= 126) {
                frameBytesLength += 2;
            }

            this.frameBytes = pack(messageBytes, op, this.mask);
    }

    private String decode(byte[] payload, byte[] mask) {
       byte[] decoded = new byte[payload.length];
       for (int i=0; i<payload.length; i++) {
           decoded[i] = (byte)((int)payload[i] ^ (int)mask[i%4]);
       }
       return new String(decoded, utf8);
    }

    private byte[] pack(byte[] messageBytes, int op, byte[] mask) {

        int framelength = 2+messageLength;

        /*
         * 0 for single byte length,
         * 1 for 2 bytes,
         * and 2 for 8 bytes of paylod length
         */
        int length = 0;

        if (this.messageLength > 32768) {
            length = 2;
            framelength += 8;
        }
        else if (this.messageLength > 125) {
            length = 1;
            framelength += 2;
        }

        byte[] bytes = new byte[framelength];
        int pointer = 2; // This will tell us what index in the array we will begin writing the message

        // Writing payload length
        bytes[0] = (byte)0x81; // 0x80 is the fin flag, 0x01 is opcode TEXT
        switch(length) {
            case 0:
                bytes[1] = (byte)this.messageLength;
                pointer = 2;
                break;
            case 1:
                bytes[1] = (byte)126;
                bytes[2] = (byte)(messageLength>>8);
                bytes[3] = (byte)(messageLength&0xFF);
                pointer = 4;
                break;
            case 2: // Due to limitations in java, we cannot fully support 8 bytes of payload length
                bytes[1] = (byte)127;
                bytes[9] = (byte)(messageLength&0xFF);
                bytes[8] = (byte)(messageLength&0xFF00);
                bytes[7] = (byte)(messageLength&0xFF0000);
                bytes[6] = (byte)(messageLength&0xFF000000);

                pointer = 10;
                break;
            default:
                break;
        }

        // Writing the message as payload data
        for (int i=0; i<messageLength; i++) {
            bytes[pointer+i] = messageBytes[i];
        }

        return bytes;
    }

    @Override
    public String toString() {
        return "WEBSOCKET FRAME:\nOpCode: "+opcode+
            "\nmessage: "+message+
            "\nEND";
    }

    enum OpCode {
        CONTINUATION(0),
        TEXT(1),
        BINARY(2),
        FURTHER_NON_CONTROL(3), // 3-7
        CONNECTION_CLOSE(8),
        PING(9),
        PONG(10),
        FURTHER_CONTROL_FRAME(11); // 11-15

        public final int code;

        OpCode(int code) {
            this.code = code;
        }

        public static OpCode getOpcode(int code) {
            if (code <= 15 && code >= 0) {
                if(code == 0) {
                    return CONTINUATION;
                }
                else if (code == 1) {
                    return TEXT;
                }
                else if (code == 2) {
                    return BINARY;
                }
                else if (code >= 3 && code <= 7) {
                    return FURTHER_NON_CONTROL;
                }
                else if (code == 8) {
                    return CONNECTION_CLOSE;
                }
                else if (code == 9) {
                    return PING;
                }
                else if (code == 10) {
                    return PONG;
                }
                else {
                    return FURTHER_CONTROL_FRAME;
                }
            }
            return null;
        }
    }
}
