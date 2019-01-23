package com.doordeck.sdk.core.dto;

import com.doordeck.sdk.core.util.OptionalUpdate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.time.Duration;
import java.util.Optional;

import static com.doordeck.sdk.core.util.Preconditions.checkArgument;

@Value.Immutable
@JsonSerialize(as = ImmutableMutateSetting.class)
@JsonDeserialize(as = ImmutableMutateSetting.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface MutateSetting extends Operation {

    Duration MAX_UNLOCK_DURATION = Duration.ofSeconds(60);

    Optional<Duration> unlockDuration();

    @Value.Default
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    default OptionalUpdate<UnlockBetweenWindow> unlockBetween() {
        return OptionalUpdate.preserve();
    }

    @Value.Check
    default void validate() {
        unlockDuration().ifPresent(unlockDuration -> checkArgument(!unlockDuration.isNegative() && !unlockDuration.isZero(),
                "Min unlock duration must be greater than zero"));

        unlockDuration().ifPresent(unlockDuration -> checkArgument(unlockDuration.compareTo(MAX_UNLOCK_DURATION) <= 0,
                "Unlock duration must be less than 60 seconds"));
    }

}
