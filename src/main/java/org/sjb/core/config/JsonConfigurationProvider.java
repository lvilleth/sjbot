package org.sjb.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sjb.core.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.sjb.core.utils.Constants.*;

public class JsonConfigurationProvider {

    private final Logger log = LoggerFactory.getLogger(JsonConfigurationProvider.class);
    private final ObjectMapper mapper;

    public JsonConfigurationProvider(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }

    public void config() throws Exception {
        Properties prop = readProperties();
        String location = Optional.ofNullable((String)prop.get(CONFIG_KEY)).orElse(CONFIG_FILENAME);
        File configFile = Paths.get("").toAbsolutePath().resolve(location).toFile();
        if(!configFile.exists() || !configFile.canRead()){
            throw new Exception(String.format("File '%s' not found", location));
        }

        Map<String, Object> json = mapper.readValue(configFile, Map.class);
        Storage.getInstance().set(CONFIG_KEY, json);
        log.debug("Stored configuration successfully");
    }

    private Properties readProperties(){
        Properties props = new Properties();
        try(InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(PROPERTIES_FILENAME)){
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

}
