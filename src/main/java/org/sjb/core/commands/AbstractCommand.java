package org.sjb.core.commands;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommand {

    protected final Logger log = LoggerFactory.getLogger(AbstractCommand.class);

    @Getter
    protected String name;

    @Override
    public String toString() {
        return "Command{'name='" + name + "}";
    }
}
