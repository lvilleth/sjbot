package org.sjb.clients.twitch.models;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(builderMethodName = "")
@Data
@Entity
@Table(name = "twitch")
public class TwitchEntity {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    @Column(name = "id", length = 36)
    private UUID id;

    @NonNull
    @Column(name = "login", unique = true,  nullable = false, length = 128)
    private String login;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "updated", nullable = false, insertable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private Instant updated;

    @Column(name = "created", nullable = false, insertable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private Instant created;


    @PreUpdate
    private void preUpdate() {
        this.updated = Instant.now(Clock.systemUTC());
    }

    public static TwitchEntityBuilder builder(@NonNull String login){
        return  new TwitchEntityBuilder().login(login);
    }

}
