public class Frame {
    public enum opCode {
        CONTINUATION,
        TEXT,
        BINARY,
        FURTHER_NON_CONTROL,
        CONNECTION_CLOSE,
        PING,
        PONG,
        FURTHER_CONTROL_FRAME
    }
}
