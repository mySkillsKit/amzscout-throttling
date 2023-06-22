package net.amzscout.exception;

public class RequestLimitException extends RuntimeException {

    private final String id;

    public RequestLimitException(String msg, String id) {
        super(msg);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}