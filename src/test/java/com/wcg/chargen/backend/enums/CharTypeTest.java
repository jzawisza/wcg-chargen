package com.wcg.chargen.backend.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class CharTypeTest {
    @ParameterizedTest
    @MethodSource("charTypesAndExpectedMagicUserValues")
    public void isMagicUser_ReturnsExpectedValues(CharType charType, boolean isMagicUser) {
        Assertions.assertEquals(isMagicUser, charType.isMagicUser());
    }

    static Stream<Arguments> charTypesAndExpectedMagicUserValues() {
        return Stream.of(
                Arguments.arguments(CharType.BERZERKER, false),
                Arguments.arguments(CharType.MAGE, true),
                Arguments.arguments(CharType.MYSTIC, false),
                Arguments.arguments(CharType.RANGER, false),
                Arguments.arguments(CharType.ROGUE, false),
                Arguments.arguments(CharType.SHAMAN, true),
                Arguments.arguments(CharType.SKALD, false),
                Arguments.arguments(CharType.WARRIOR, false)
        );
    }
}
