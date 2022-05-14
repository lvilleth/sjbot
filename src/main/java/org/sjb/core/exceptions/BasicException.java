package org.sjb.core.exceptions;

import lombok.Getter;

@Getter
public class BasicException extends RuntimeException {

    private final int httpStatus;
    private final int code;
    private final String message;

    public BasicException(CodeErrors codeErrors, String message) {
        super(message);
        this.httpStatus = codeErrors.getHttpStatus();
        this.code = codeErrors.getCode();
        this.message = message;
    }
}
