package com.startechies.bugtracker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordValidatorTest {

    // ── Weak passwords ───────────────────────────────────────────────────────

    @Test void null_isNotStrong()               { assertThat(PasswordValidator.isStrong(null)).isFalse(); }
    @Test void blank_isNotStrong()              { assertThat(PasswordValidator.isStrong("")).isFalse(); }
    @Test void tooShort_isNotStrong()           { assertThat(PasswordValidator.isStrong("Pa1!")).isFalse(); }
    @Test void noDigit_isNotStrong()            { assertThat(PasswordValidator.isStrong("Password!")).isFalse(); }
    @Test void noSpecialChar_isNotStrong()      { assertThat(PasswordValidator.isStrong("Password1")).isFalse(); }
    @Test void noLetter_isNotStrong()           { assertThat(PasswordValidator.isStrong("12345678!")).isFalse(); }
    @Test void exactly7chars_isNotStrong()      { assertThat(PasswordValidator.isStrong("Pass1!x")).isFalse(); }

    // ── Strong passwords ─────────────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {
        "Password1!",       // classic mixed
        "password1!",       // lowercase letter counts as a letter
        "PASSWORD1!",       // uppercase letter counts as a letter
        "abc123@xyz",       // @ as special char
        "Hello$World9",     // $ as special char
        "x1!xxxxxxx",       // exactly 10 chars, meets all criteria
        "UPPER1lower!",     // full mix
        "12abcdef#A"        // digit-heavy but still has letter + special
    })
    void strongPasswords_returnTrue(String pw) {
        assertThat(PasswordValidator.isStrong(pw)).isTrue();
    }

    @Test void exactly8chars_withAllCriteria_isStrong() {
        assertThat(PasswordValidator.isStrong("Abcd1!ef")).isTrue();
    }
}
