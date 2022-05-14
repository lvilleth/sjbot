package org.sjb;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.sjb.clients.twitch.TwitchModule;
import org.sjb.core.CoreModule;
import org.sjb.core.auth.AuthController;
import org.sjb.core.persistence.PersistenceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.util.UUID;

public class Main {

    private final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        bootstrap();
    }

    private static void spark(){
        Spark.port(8844);
        Spark.init();
    }

    private static void bootstrap(){
        // Injector with all the necessary dependencies
        Injector injector = Guice.createInjector(
                new CoreModule(),
                new PersistenceModule(),
                new TwitchModule()
        );
        // Bootstrap the application
        // start the server
        injector.getInstance(Main.class).start(injector);
    }

    public void start(Injector injector){
        log.info("Application Bootstrapped with ID " + UUID.randomUUID());
        spark();
        injector.getInstance(AuthController.class).setup();
    }

}
