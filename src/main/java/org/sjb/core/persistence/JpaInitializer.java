package org.sjb.core.persistence;

import com.google.inject.persist.PersistService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JpaInitializer {

    @Inject
    JpaInitializer(PersistService persistService){
        persistService.start();

    }

}
