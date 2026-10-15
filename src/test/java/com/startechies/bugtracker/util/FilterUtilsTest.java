package com.startechies.bugtracker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class FilterUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = { "all", "ALL", "All", " ", "" })
    void unfiltered_returnsTrue(String value) {
        assertThat(FilterUtils.isUnfiltered(value)).isTrue();
    }

    @Test
    void null_returnsTrue() {
        assertThat(FilterUtils.isUnfiltered(null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "Open", "Closed", "critical", "major" })
    void filtered_returnsFalse(String value) {
        assertThat(FilterUtils.isUnfiltered(value)).isFalse();
    }
}
