package com.doordeck.sdk.dto.site;

import com.doordeck.sdk.jackson.deserializer.AmericanDayDeseralizer;
import com.doordeck.sdk.jackson.serializer.AmericanDaySeralizer;
import com.doordeck.sdk.util.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;
import org.joda.time.LocalTime;

import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Example output
 *
 * {
 *   "periods": [
 *     {
 *       "close": {
 *         "day": 2,
 *         "time": "0000"
 *       },
 *       "open": {
 *         "day": 1,
 *         "time": "0500"
 *       }
 *     },
 *     {
 *       "close": {
 *         "day": 3,
 *         "time": "0000"
 *       },
 *       "open": {
 *         "day": 2,
 *         "time": "0500"
 *       }
 *     }, etc
 */
@Value.Immutable
@JsonSerialize(as = ImmutableOpenHours.class)
@JsonDeserialize(as = ImmutableOpenHours.class)
@Value.Style(depluralize = true)
public interface OpenHours {

    @Value.NaturalOrder
    SortedSet<Period> periods();

    @Value.Check
    default OpenHours normalize() {
        // Sort periods chronologically, ensure no overlapping entries
        TreeSet<Period> mergedPeriods = new TreeSet<>();

        for (Period currentPeriod : periods()) {
            // First case
            if (mergedPeriods.isEmpty()) {
                mergedPeriods.add(currentPeriod);
            } else {
                // Compare this period to the current top of the stack
                Period lastPeriod = mergedPeriods.last();
                if (lastPeriod.close().get().compareTo(currentPeriod.open()) >= 0) {
                    Period mergedPeriod = ImmutablePeriod.builder()
                        .open(lastPeriod.open())
                        .close(currentPeriod.close().get().compareTo(lastPeriod.close().get()) > 0
                            ? currentPeriod.close()
                            : lastPeriod.close())
                        .build();
                    mergedPeriods.remove(lastPeriod); // Replace with merged entry
                    mergedPeriods.add(mergedPeriod);
                } else {
                    // This is now the latest current period
                    mergedPeriods.add(currentPeriod);
                }
            }
        }

        if (!mergedPeriods.equals(periods())) {
            return ImmutableOpenHours.builder()
                .from(this)
                .periods(mergedPeriods)
                .build();
        }

        return this;
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutablePeriod.class)
    @JsonDeserialize(as = ImmutablePeriod.class)
    interface Period extends Comparable<Period> {
        Entry open();

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        Optional<Entry> close();

        default int compareTo(Period other) {
            // Compare open times, then close times if present
            if (open().compareTo(other.open()) != 0) {
                return open().compareTo(other.open());
            }

            if (close().isPresent() && other.close().isPresent()) {
                return close().get().compareTo(other.close().get());
            }

            if (close().isEmpty() && other.close().isEmpty()) {
                return 0;
            }

            if (close().isEmpty()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableEntry.class)
    @JsonDeserialize(as = ImmutableEntry.class)
    interface Entry extends Comparable<Entry> {
        @Value.Parameter
        @JsonSerialize(using = AmericanDaySeralizer.class)
        @JsonDeserialize(using = AmericanDayDeseralizer.class)
        DayOfWeek day();

        @Value.Parameter
        @JsonFormat(pattern = "HHmm")
        LocalTime time();

        default int compareTo(Entry other) {
            // Adjust day of week to start from Sunday
            int thisDay = day().equals(DayOfWeek.SUNDAY) ?  0 : day().getValue();
            int otherDay = other.day().equals(DayOfWeek.SUNDAY) ? 0 : day().getValue();

            if (thisDay != otherDay) {
                return Integer.compare(thisDay, otherDay);
            } else {
                return time().compareTo(other.time());
            }
        }
    }

}
