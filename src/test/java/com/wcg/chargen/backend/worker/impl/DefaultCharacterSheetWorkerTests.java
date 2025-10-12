package com.wcg.chargen.backend.worker.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.FeatureAttributeType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.*;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.CommonerService;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import com.wcg.chargen.backend.worker.CharacterSheetWorker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class DefaultCharacterSheetWorkerTests {
    @Autowired
    CharacterSheetWorker characterSheetWorker;
    @MockBean
    private CharClassesService charClassesService;
    @Autowired
    CommonerService commonerService;

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
        var name = characterSheetWorker.generateName(request);

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
        var name = characterSheetWorker.generateName(request);

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
        var name = characterSheetWorker.generateName(request);

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
        var actualFortunePoints = characterSheetWorker.getFortunePoints(request);

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

    @Test
    public void getBaseEvasion_ReturnsExpectedEvasionForCommonerCharacters() {
        // arrange
        var expectedEvasion = commonerService.getInfo().evasion();
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(0)
                .build();

        // act
        var actualEvasion = characterSheetWorker.getBaseEvasion(request);

        // assert
        assertEquals(expectedEvasion, actualEvasion);
    }

    @Test
    public void getBaseEvasion_ReturnsExpectedEvasionForClassCharacters() {
        // arrange
        var expectedEvasion = 5;
        var evasionModifiers = List.of(expectedEvasion, 6, 7, 8, 9, 10, 11);
        var charClass = new CharClass("Skald", Collections.emptyList(),
                evasionModifiers, 1, 1, null, null, null, null);
        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(1)
                .withCharacterType(CharType.SKALD)
                .build();

        // act
        var actualEvasion = characterSheetWorker.getBaseEvasion(request);

        // assert
        assertEquals(expectedEvasion, actualEvasion);
    }

    @Test
    public void getEvasionBonus_ReturnsZeroForCommonerCharacters() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(0)
                .build();

        // act
        var evasionBonus = characterSheetWorker.getEvasionBonus(request);

        // assert
        assertEquals(0, evasionBonus);
    }

    @ParameterizedTest
    @MethodSource("conditionsForEvasionBonus")
    public void getEvasionBonus_ReturnsExpectedBonusForClassCharactersBasedOnQuickGearAndFeatures(
            boolean useQuickGear, boolean hasTier1EvasionFeature, boolean hasTier2EvasionFeature,
            int expectedEvasionBonus) {
        // arrange
        var armor = new Armor("Hoplite Shield", "Shield", "+1 Evasion");
        var gear = new Gear(List.of(armor), Collections.emptyList(), 0, 0, Collections.emptyList());

        var evasionFeatureName = "Evasion test";
        var evasionFeatureAttribute = new FeatureAttribute(FeatureAttributeType.EV_PLUS_1, "");
        var evasionFeature = new Feature(evasionFeatureName, List.of(evasionFeatureAttribute));
        var evasionFeatureList = List.of(evasionFeature);
        var features = new Features(
                hasTier1EvasionFeature ? evasionFeatureList : Collections.emptyList(),
                hasTier2EvasionFeature ? evasionFeatureList : Collections.emptyList()
        );

        var charClass = new CharClass(CharType.RANGER.toString(),
                Collections.emptyList(),
                Collections.emptyList(),
                0,
                0,
                Collections.emptyList(),
                gear,
                null,
                features);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var featuresRequest = new FeaturesRequest(
                hasTier1EvasionFeature ? List.of(evasionFeatureName) : Collections.emptyList(),
                hasTier2EvasionFeature ? List.of(evasionFeatureName) : Collections.emptyList()
        );
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.RANGER)
                .withLevel(4)
                .withUseQuickGear(useQuickGear)
                .withFeatures(featuresRequest)
                .build();

        // act
        var actualEvasionBonus = characterSheetWorker.getEvasionBonus(request);

        // assert
        assertEquals(expectedEvasionBonus, actualEvasionBonus);
    }

    static Stream<Arguments> conditionsForEvasionBonus() {
        return Stream.of(
                Arguments.arguments(false, false, false, 0),
                Arguments.arguments(true, false, false, 1),
                Arguments.arguments(false, true, false, 1),
                Arguments.arguments(false, false, true, 1),
                Arguments.arguments(true, true, false, 2),
                Arguments.arguments(true, false, true, 2),
                Arguments.arguments(false, true, true, 2),
                Arguments.arguments(true, true, true, 3)
        );
    }
}
