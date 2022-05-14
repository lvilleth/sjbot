package org.sjb.clients.twitch.models;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "twitch_bot_account")
public class TwitchBotAccountEntity {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 128)
    private String username;

    @Column(name = "created", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private Instant created;

    @Column(name = "updated", nullable = false, insertable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private Instant updated;

    @PreUpdate
    private void preUpdate() {
        this.updated = Instant.now(Clock.systemUTC());
    }
}
