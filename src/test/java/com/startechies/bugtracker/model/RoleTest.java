package com.startechies.bugtracker.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @ParameterizedTest
    @CsvSource({
        "ProjectManager, PROJECT_MANAGER",
        "project manager, PROJECT_MANAGER",
        "pm,             PROJECT_MANAGER",
        "Developer,      DEVELOPER",
        "developer,      DEVELOPER",
        "Tester,         TESTER",
        "tester,         TESTER"
    })
    void fromDb_recognisesAllAliases(String input, String expected) {
        assertThat(Role.fromDb(input)).isEqualTo(Role.valueOf(expected));
    }

    @Test
    void fromDb_null_returnsNull() {
        assertThat(Role.fromDb(null)).isNull();
    }

    @Test
    void fromDb_unknown_returnsNull() {
        assertThat(Role.fromDb("Wizard")).isNull();
    }

    @Test
    void fromAuthority_roundTrips() {
        for (Role r : Role.values()) {
            assertThat(Role.fromAuthority(r.authority)).isEqualTo(r);
        }
    }

    @Test
    void allRolesHaveDashboardPath() {
        for (Role r : Role.values()) {
            assertThat(r.dashboardPath).isNotBlank();
        }
    }
}
