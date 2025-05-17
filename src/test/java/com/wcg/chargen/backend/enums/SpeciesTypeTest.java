package com.wcg.chargen.backend.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpeciesTypeTest {
    @ParameterizedTest
    @MethodSource("speciesAndExpectedStringValues")
    public void toStringMethods_ReturnExpectedValues(SpeciesType species, String toStringRepresentation,
                                                     String toCharSheetStringRepresentation) {
        assertEquals(toStringRepresentation, species.toString());
        assertEquals(toCharSheetStringRepresentation, species.toCharSheetString());
    }

    @ParameterizedTest
    @EnumSource(SpeciesType.class)
    public void isHuman_returnsExpectedValue(SpeciesType species) {
        var expectedIsHuman = (species == SpeciesType.HUMAN);
        assertEquals(expectedIsHuman, species.isHuman());
    }

    static Stream<Arguments> speciesAndExpectedStringValues() {
        return Stream.of(
            Arguments.arguments(SpeciesType.DWARF, "dwarf", "Dwarf"),
            Arguments.arguments(SpeciesType.ELF, "elf", "Elf"),
            Arguments.arguments(SpeciesType.HALFLING, "halfling", "Halfling"),
            Arguments.arguments(SpeciesType.HUMAN, "human", "Human")
        );
    }
}
