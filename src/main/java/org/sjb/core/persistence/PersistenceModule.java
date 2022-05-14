package org.sjb.core.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.sjb.core.CoreModule;
import org.sjb.core.config.AppConfiguration;
import org.sjb.core.config.DatabaseConfiguration;

import java.util.Map;

public class PersistenceModule extends AbstractModule {

    @Override
    protected void configure() {
        Injector in = Guice.createInjector(new CoreModule());
        DatabaseConfiguration dbConfig = in.getInstance(AppConfiguration.class).getDb();

        Map<String, String> props = Map.of(
                "javax.persistence.schema-generation.database.action", dbConfig.getAction(),
                "hibernate.connection.username", dbConfig.getUsername(),
                "hibernate.connection.password", dbConfig.getPassword(),
                "hibernate.connection.url", dbConfig.getUrl()
        );

        JpaPersistModule persistModule = new JpaPersistModule(dbConfig.getPersistenceUnit());
        persistModule.properties(props);
        install(persistModule);
    }

}
