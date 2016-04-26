package jaws;

public class Logger {

    public static final int NONE = 0x00;
    public static final int WS_IO = 0x01;
    public static final int JSON = 0x02;
    public static final int GENERAL = 0x04;
    public static final int WS_PARSE = 0x08;
    public static final int ALL = 0xFF;

    public static int logLevel = 0;

    public static void log(String message, int category) {
        if ((logLevel & category) != 0) {
            System.out.println(message);
        }
    }

    public static void logErr(String message, int category) {
        if ((logLevel & category) != 0) {
            System.err.println(message);
        }
    }
}

