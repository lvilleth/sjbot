package org.sjb.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sjb.core.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

import static org.sjb.core.utils.Constants.*;

public class JsonConfigurationProvider {

    private final Logger log = LoggerFactory.getLogger(JsonConfigurationProvider.class);
    private final ObjectMapper mapper;

    public JsonConfigurationProvider(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }

    public void config() throws Exception {
        File configFile = Paths.get("").toAbsolutePath().resolve(CONFIG_FILENAME).toFile();
        if(!configFile.exists() || !configFile.canRead()){
            throw new Exception(String.format("File '%s' not found", CONFIG_FILENAME));
        }

        Map<String, Object> json = mapper.readValue(configFile, Map.class);
        Storage.getInstance().set(CONFIG_KEY, json);
        log.debug("Stored configuration successfully");
    }

}
