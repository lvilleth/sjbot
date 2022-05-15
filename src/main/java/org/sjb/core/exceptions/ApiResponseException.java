package org.sjb.core.exceptions;

import static java.lang.String.format;
import static org.sjb.core.exceptions.CodeErrors.*;

public class ApiResponseException extends BasicException {

    public static final String MESSAGE = "Unexpected response from %s %s";

    public ApiResponseException(String api, String response) {
        super(API_EXCEPTION, format(MESSAGE, api, response));
    }
}
