package com.wcg.chargen.backend.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CharTypeTest {
    @ParameterizedTest
    @MethodSource("charTypesAndExpectedMagicUserValues")
    public void isMagicUser_ReturnsExpectedValues(CharType charType, boolean isMagicUser) {
        assertEquals(isMagicUser, charType.isMagicUser());
    }

    @ParameterizedTest
    @MethodSource("charTypesAndExpectedStringValues")
    public void toStringMethods_ReturnExpectedValues(CharType charType, String toStringRepresentation,
                                                     String toCharSheetStringRepresentation) {
        assertEquals(toStringRepresentation, charType.toString());
        assertEquals(toCharSheetStringRepresentation, charType.toCharSheetString());
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

    static Stream<Arguments> charTypesAndExpectedStringValues() {
        return Stream.of(
                Arguments.arguments(CharType.BERZERKER, "berzerker", "Berzerker"),
                Arguments.arguments(CharType.MAGE, "mage", "Mage"),
                Arguments.arguments(CharType.MYSTIC, "mystic", "Mystic"),
                Arguments.arguments(CharType.RANGER, "ranger", "Ranger"),
                Arguments.arguments(CharType.ROGUE, "rogue", "Rogue"),
                Arguments.arguments(CharType.SHAMAN, "shaman", "Shaman"),
                Arguments.arguments(CharType.SKALD, "skald", "Skald"),
                Arguments.arguments(CharType.WARRIOR, "warrior", "Warrior")
        );
    }
}
