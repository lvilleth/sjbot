package org.sjb;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.sjb.clients.ClientManager;
import org.sjb.core.auth.AuthController;
import org.sjb.core.config.JsonConfigurationProvider;
import spark.Spark;

import java.net.http.HttpClient;
import java.time.Duration;

public class Main {

    public static void main(String[] args) throws Exception {
        spark();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(60))
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JsonConfigurationProvider jcp = new JsonConfigurationProvider(objectMapper);
        jcp.config();

        ClientManager clientManager = new ClientManager(httpClient, objectMapper);
        AuthController auth = new AuthController(httpClient, objectMapper, clientManager);
        auth.setup();
    }

    private static void spark(){
        Spark.port(8844);
        Spark.init();
    }

}
