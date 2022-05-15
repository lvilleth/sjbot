package org.sjb.core.exceptions;

import static org.sjb.core.exceptions.CodeErrors.*;

public class ClientAuthorizationError extends BasicException{

    public static final String MESSAGE = "Error during authorization with remote server";

    public ClientAuthorizationError() {
        super(UNAUTHORIZED, MESSAGE);
    }
}
