package com.wcg.chargen.backend.util;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
}
