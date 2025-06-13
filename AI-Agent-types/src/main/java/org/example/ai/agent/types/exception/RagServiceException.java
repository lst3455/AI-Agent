package org.example.ai.agent.types.exception;

public class RagServiceException extends RuntimeException {
    /**
     * Error code
     */
    private String code;

    public RagServiceException(String code) {
        super();
        this.code = code;
    }

    public RagServiceException(String code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public RagServiceException(String code, String message) {
        super(message);
        this.code = code;
    }

    public RagServiceException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}