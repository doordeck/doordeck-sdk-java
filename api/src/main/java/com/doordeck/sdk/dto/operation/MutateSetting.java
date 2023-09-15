package com.doordeck.sdk.dto.operation;

import static com.google.common.base.Preconditions.checkArgument;

import com.doordeck.sdk.dto.device.UnlockBetweenWindow;
import com.doordeck.sdk.util.OptionalUpdate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;
import org.joda.time.Duration;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableMutateSetting.class)
@JsonDeserialize(as = ImmutableMutateSetting.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class MutateSetting implements Operation {

    private static final Duration MAX_UNLOCK_DURATION = Duration.standardSeconds(60);

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
            checkArgument(unlockDuration.getMillis() >= 0,
                    "Min unlock duration must be greater than zero");
            checkArgument(unlockDuration.compareTo(MAX_UNLOCK_DURATION) <= 0,
                    "Unlock duration must be less than 60 seconds");
        }
    }

}
