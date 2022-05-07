package org.sjb.core.commands;

import java.util.concurrent.Future;

public interface AsyncCommand<R> {

    Future<R> execute();

}
