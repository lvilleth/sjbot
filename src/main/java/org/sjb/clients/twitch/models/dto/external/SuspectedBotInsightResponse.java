package org.sjb.clients.twitch.models.dto.external;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor @AllArgsConstructor
public class SuspectedBotInsightResponse {

    @JsonProperty("_total")
    @JsonAlias("total")
    private Long total;

    @JsonProperty("bots")
    private List<List<Object>> bots;

    /**
     * @return [ [name:string, nChannels:number, lastSeen:number] ]
     */
    public List<List<Object>> getBots() {
        return bots;
    }
}
