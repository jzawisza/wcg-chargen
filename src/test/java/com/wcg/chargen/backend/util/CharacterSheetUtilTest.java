package com.wcg.chargen.backend.util;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterSheetUtilTest {
    @Test
    public void generateName_ReturnsExpectedNameForClassCharacters() {
        // arrange
        var characterName = "SomeName";
        var expectedNameStart = characterName + "_DWARF_BERZERKER_";
        var expectedTitleLength = expectedNameStart.length() + 14; // 14 = YYYYMMDDHHMMSS

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(characterName)
                .withSpeciesType(SpeciesType.DWARF)
                .withLevel(1)
                .withCharacterType(CharType.BERZERKER)
                .build();

        // act
        var name = CharacterSheetUtil.generateName(request);

        // assert
        assertNotNull(name);
        assertEquals(expectedTitleLength, name.length());
        assertTrue(name.startsWith(expectedNameStart));
        var timestampStr = name.substring(expectedNameStart.length());
        assertTrue(timestampStr.matches("[0-9]+"));
    }

    @ParameterizedTest
    @CsvSource({"Potter, POTTER", "Rat Catcher, RAT_CATCHER", "Healer/Herbalist, HEALER_HERBALIST"})
    public void generateName_ReturnsExpectedNameForCommonerCharacters(String originalProfession,
                                                                      String formattedProfession)
    {
        // arrange
        var characterName = "SomeName";
        var expectedNameStart = characterName + "_DWARF_" + formattedProfession.toUpperCase() + "_";
        var expectedTitleLength = expectedNameStart.length() + 14; // 14 = YYYYMMDDHHMMSS

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(characterName)
                .withSpeciesType(SpeciesType.DWARF)
                .withLevel(0)
                .withProfession(originalProfession)
                .build();

        // act
        var name = CharacterSheetUtil.generateName(request);

        // assert
        assertNotNull(name);
        assertEquals(expectedTitleLength, name.length());
        assertTrue(name.startsWith(expectedNameStart));
        var timestampStr = name.substring(expectedNameStart.length());
        assertTrue(timestampStr.matches("[0-9]+"));
    }

    @ParameterizedTest
    @CsvSource({
            "RegularName, RegularName",
            "Name With Spaces, Name_With_Spaces",
            "NameWith:Colon, NameWith_Colon",
            "NameWith/Slash, NameWith_Slash",
            "NameWith\\Backslash, NameWith_Backslash",
            "NameWith*Asterisk, NameWith_Asterisk",
            "NameWith?Question, NameWith_Question",
            "NameWith\"Quote, NameWith_Quote",
            "NameWith<LessThan, NameWith_LessThan",
            "NameWith>GreaterThan, NameWith_GreaterThan",
            "NameWith|Pipe, NameWith_Pipe"})
    public void generateName_RemovesNonFilenameCharactersFromCharacterName(String originalName,
                                                                           String formattedName)
    {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(originalName)
                .withSpeciesType(SpeciesType.DWARF)
                .withLevel(1)
                .withCharacterType(CharType.BERZERKER)
                .build();

        // act
        var name = CharacterSheetUtil.generateName(request);

        // assert
        assertNotNull(name);
        assertTrue(name.startsWith(formattedName));
    }

    @ParameterizedTest
    @MethodSource("expectedFortunePoints")
    public void getFortunePoints_ReturnsExpectedPoints(int level, int luckAttributeValue,
                                                       SpeciesType speciesType,
                                                       String speciesStrength,
                                                       String speciesWeakness,
                                                       int expectedFortunePoints) {
        // arrange
        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(
                0, 0, 0, 0, 0, 0, luckAttributeValue);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(level)
                .withSpeciesType(speciesType)
                .withAttributes(attributesMap)
                .withSpeciesStrength(speciesStrength)
                .withSpeciesWeakness(speciesWeakness)
                .build();

        // act
        var actualFortunePoints = CharacterSheetUtil.getFortunePoints(request);

        assertEquals(expectedFortunePoints, actualFortunePoints);
    }

    // Conditions tested:
    // 1) Base fortune points are derived from level plus LUC modifier
    // 2) Halflings get 1 extra fortune point
    // 3) If the species strength is LUC, fortune points increase by 1
    // 4) If the species weakness is LUC, fortune points decrease by 1
    // 5) Fortune points can never go below 0
    static Stream<Arguments> expectedFortunePoints() {
        return Stream.of(
                Arguments.of(0, 1, SpeciesType.HUMAN, "INT", null, 1),
                Arguments.of(0, 1, SpeciesType.HALFLING, "COR", "STR", 2),
                Arguments.of(1, 1, SpeciesType.HUMAN, "INT", null, 2),
                Arguments.of(1, 1, SpeciesType.HALFLING, "COR", "STR", 3),
                Arguments.of(1, 1, SpeciesType.HALFLING, "LUC", "STR", 4),
                Arguments.of(1, 1, SpeciesType.DWARF, "STR", "LUC", 1),
                Arguments.of(5, -1, SpeciesType.HUMAN, "INT", null, 4),
                Arguments.of(5, -1, SpeciesType.HALFLING, "COR", "STR", 5),
                Arguments.of(1, -2, SpeciesType.HUMAN, "INT", null, 0),
                Arguments.of(1, -2, SpeciesType.HALFLING, "COR", "STR", 0)
        );
    }
}
