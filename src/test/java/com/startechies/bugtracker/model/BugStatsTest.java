package com.startechies.bugtracker.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BugStatsTest {

    @Test
    void empty_allZero() {
        BugStats s = BugStats.empty();
        assertThat(s.total()).isZero();
        assertThat(s.open()).isZero();
        assertThat(s.assigned()).isZero();
        assertThat(s.closed()).isZero();
        assertThat(s.critical()).isZero();
    }

    @Test
    void accumulate_openCritical_incrementsBothCounters() {
        BugStats s = BugStats.empty()
                .accumulate(BugStatus.OPEN, Severity.CRITICAL, 3);
        assertThat(s.total()).isEqualTo(3);
        assertThat(s.open()).isEqualTo(3);
        assertThat(s.critical()).isEqualTo(3);
        assertThat(s.assigned()).isZero();
        assertThat(s.closed()).isZero();
    }

    @Test
    void accumulate_closedMajor_onlyIncrementsClosed() {
        BugStats s = BugStats.empty()
                .accumulate(BugStatus.CLOSED, Severity.MAJOR, 5);
        assertThat(s.total()).isEqualTo(5);
        assertThat(s.closed()).isEqualTo(5);
        assertThat(s.critical()).isZero();
    }

    @Test
    void accumulate_nullStatusAndSeverity_onlyIncrementsTotal() {
        BugStats s = BugStats.empty().accumulate(null, null, 2);
        assertThat(s.total()).isEqualTo(2);
        assertThat(s.open()).isZero();
        assertThat(s.critical()).isZero();
    }

    @Test
    void accumulate_multipleRows_sumsCorrectly() {
        BugStats s = BugStats.empty()
                .accumulate(BugStatus.OPEN,     Severity.CRITICAL, 2)
                .accumulate(BugStatus.ASSIGNED,  Severity.MAJOR,    3)
                .accumulate(BugStatus.CLOSED,    Severity.MINOR,    1);
        assertThat(s.total()).isEqualTo(6);
        assertThat(s.open()).isEqualTo(2);
        assertThat(s.assigned()).isEqualTo(3);
        assertThat(s.closed()).isEqualTo(1);
        assertThat(s.critical()).isEqualTo(2);
    }
}
