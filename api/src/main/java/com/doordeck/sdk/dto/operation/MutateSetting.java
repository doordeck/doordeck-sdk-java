package com.doordeck.sdk.dto.operation;

import com.doordeck.sdk.dto.device.UnlockBetweenWindow;
import com.doordeck.sdk.util.OptionalUpdate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.time.Duration;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableMutateSetting.class)
@JsonDeserialize(as = ImmutableMutateSetting.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class MutateSetting implements Operation {

    private static final Duration MAX_UNLOCK_DURATION = Duration.ofSeconds(60);

    public abstract Optional<Duration> unlockDuration();

    @Value.Default
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public OptionalUpdate<UnlockBetweenWindow> unlockBetween() {
        return OptionalUpdate.preserve();
    }

    @Value.Check
    protected void validate() {
        if (unlockDuration().isPresent()) {
            Duration unlockDuration = unlockDuration().get();
            if (unlockDuration.toMillis() <= 0) {
                throw new IllegalArgumentException("Unlock duration must be greater than zero");
            }

            if (unlockDuration.compareTo(MAX_UNLOCK_DURATION) > 0) {
                throw new IllegalArgumentException("Unlock duration must be less than 60 seconds");
            }
        }
    }

}
