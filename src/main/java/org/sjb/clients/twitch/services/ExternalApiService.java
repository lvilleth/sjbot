package org.sjb.clients.twitch.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sjb.clients.twitch.models.dto.external.SuspectedBotInsightResponse;
import org.sjb.core.exceptions.ApiResponseException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Singleton
public class ExternalApiService {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    @Inject
    public ExternalApiService(HttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    public SuspectedBotInsightResponse getSuspectedBotAccountList() throws IOException, InterruptedException {
        String url = "https://api.twitchinsights.net/v1/bots/all";
        HttpRequest request = HttpRequest.newBuilder()
                .header("User-Agent", "NBSChatBot")
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200){
            throw new ApiResponseException(url, response.body());
        }
        return mapper.readValue(response.body(), SuspectedBotInsightResponse.class);
    }


}
