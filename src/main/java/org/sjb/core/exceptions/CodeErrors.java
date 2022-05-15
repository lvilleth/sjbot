package org.sjb.core.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public enum CodeErrors {

    UNAUTHORIZED(1, 401),
    API_EXCEPTION(2, 400)
    ;

    private final int code;
    private final int httpStatus;

}
