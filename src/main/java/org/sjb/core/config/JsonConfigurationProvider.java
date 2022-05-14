package org.sjb.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sjb.core.Storage;
import org.sjb.core.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.sjb.core.utils.Constants.CONFIG_KEY;

@Singleton
public class JsonConfigurationProvider {

    private final Logger log = LoggerFactory.getLogger(JsonConfigurationProvider.class);
    private final ObjectMapper mapper;
    private final AppConfiguration appConfiguration;

    @Inject
    public JsonConfigurationProvider(ObjectMapper objectMapper, AppConfiguration appConfiguration) {
        this.mapper = objectMapper;
        this.appConfiguration = appConfiguration;
    }

    public void config() throws IOException {
        File configFile = FileUtils.readFromInstallDir(appConfiguration.getConfig());
        Map<String, Object> json = mapper.readValue(configFile, Map.class);
        Storage.getInstance().set(CONFIG_KEY, json);
        log.debug("Stored configuration successfully");
    }

}
