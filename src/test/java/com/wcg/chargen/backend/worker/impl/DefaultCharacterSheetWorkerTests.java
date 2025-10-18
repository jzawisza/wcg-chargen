package com.wcg.chargen.backend.worker.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.FeatureAttributeType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.*;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.CommonerService;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import com.wcg.chargen.backend.util.FeatureAttributeUtil;
import com.wcg.chargen.backend.worker.CharacterSheetWorker;
import com.wcg.chargen.backend.worker.RandomNumberWorker;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class DefaultCharacterSheetWorkerTests {
    @Autowired
    CharacterSheetWorker characterSheetWorker;
    @MockBean
    CharClassesService charClassesService;
    @Autowired
    CommonerService commonerService;
    @MockBean
    RandomNumberWorker randomNumberWorker;

    private static final String TEST_MODIFIER = "Test Modifier";

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

    @Test
    public void getAdvOrDadvByModifier_ReturnsNullWhenNoMatchingFeature() {
        // arrange
        var charClass = new CharClass(CharType.BERZERKER.toString(),
                Collections.emptyList(),
                Collections.emptyList(),
                0,
                0,
                Collections.emptyList(),
                null,
                null,
                new Features(Collections.emptyList(), Collections.emptyList()));
        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.BERZERKER)
                .withLevel(1)
                .withFeatures(new FeaturesRequest(Collections.emptyList(), Collections.emptyList()))
                .build();

        // act
        var advOrDadv = characterSheetWorker.getAdvOrDadvByModifier(request, TEST_MODIFIER);

        // assert
        assertNull(advOrDadv);
    }

    @ParameterizedTest
    @MethodSource("advDadvScenarios")
    public void getAdvOrDadvByModifier_ReturnsExpectedAdvOrDadvBasedOnFeatures(
            FeatureAttributeUtil.Tier tier, FeatureAttributeType expectedAdvDadv) {
        // arrange
        var featureName = "Test Feature";
        var featureAttribute = new FeatureAttribute(expectedAdvDadv, TEST_MODIFIER);
        var feature = new Feature(featureName, List.of(featureAttribute));

        var features = new Features(
                tier == FeatureAttributeUtil.Tier.I ? List.of(feature) : Collections.emptyList(),
                tier == FeatureAttributeUtil.Tier.II ? List.of(feature) : Collections.emptyList()
        );
        var charClass = new CharClass(CharType.BERZERKER.toString(),
                Collections.emptyList(),
                Collections.emptyList(),
                0,
                0,
                Collections.emptyList(),
                null,
                null,
                features);
        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var featuresRequest = new FeaturesRequest(
                tier == FeatureAttributeUtil.Tier.I ? List.of(featureName) : Collections.emptyList(),
                tier == FeatureAttributeUtil.Tier.II ? List.of(featureName) : Collections.emptyList()
        );
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.BERZERKER)
                .withLevel(1)
                .withFeatures(featuresRequest)
                .build();

        // act
        var actualAdvDadv = characterSheetWorker.getAdvOrDadvByModifier(request, TEST_MODIFIER);

        // assert
        assertEquals(expectedAdvDadv, actualAdvDadv);
    }

    static Stream<Arguments> advDadvScenarios() {
        return Stream.of(
                Arguments.arguments(FeatureAttributeUtil.Tier.I,
                        FeatureAttributeType.ADV),
                Arguments.arguments(FeatureAttributeUtil.Tier.I,
                        FeatureAttributeType.DADV),
                Arguments.arguments(FeatureAttributeUtil.Tier.II,
                        FeatureAttributeType.ADV),
                Arguments.arguments(FeatureAttributeUtil.Tier.II,
                        FeatureAttributeType.ADV)
        );
    }

    @Test
    public void getAdvOrDadvByModifier_ReturnsDadvForModifierWithAdvAsTier1AndDadvAsTier2() {
        // arrange
        var advFeatureName = "Adv Feature";
        var advFeatureAttribute = new FeatureAttribute(FeatureAttributeType.ADV, TEST_MODIFIER);
        var advFeature = new Feature(advFeatureName, List.of(advFeatureAttribute));

        var dadvFeatureName = "Dadv Feature";
        var dadvFeatureAttribute = new FeatureAttribute(FeatureAttributeType.DADV, TEST_MODIFIER);
        var dadvFeature = new Feature(dadvFeatureName, List.of(dadvFeatureAttribute));

        var charClass = new CharClass(CharType.BERZERKER.toString(),
                Collections.emptyList(),
                Collections.emptyList(),
                0,
                0,
                Collections.emptyList(),
                null,
                null,
                new Features(List.of(advFeature), List.of(dadvFeature)));
        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.BERZERKER)
                .withLevel(1)
                .withFeatures(new FeaturesRequest(
                        List.of(advFeatureName),
                        List.of(dadvFeatureName)))
                .build();

        // act
        var advOrDadv = characterSheetWorker.getAdvOrDadvByModifier(request, TEST_MODIFIER);

        // assert
        assertEquals(FeatureAttributeType.DADV, advOrDadv);
    }

    @ParameterizedTest
    @MethodSource("commonerHitPointScenarios")
    public void getHitPoints_ReturnsExpectedResultsForCommonerCharacters(int staValue,
                                                                         int randomRoll,
                                                                         String speciesStrength,
                                                                         String speciesWeakness,
                                                                         int expectedHitPoints) {
        // arrange
        Mockito.when(randomNumberWorker.getIntFromRange(1, 4)).thenReturn(randomRoll);

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, staValue, 0, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(0)
                .withAttributes(attributesMap)
                .withSpeciesStrength(speciesStrength)
                .withSpeciesWeakness(speciesWeakness)
                .build();

        // act
        var actualHitPoints = characterSheetWorker.getHitPoints(request);

        // assert
        assertEquals(expectedHitPoints, actualHitPoints);
    }

    static Stream<Arguments> commonerHitPointScenarios() {
        // Commoner characters have 1d4 + 1 + STA hit points
        return Stream.of(
                Arguments.arguments(1, 1, "INT", "PRS", 3),
                Arguments.arguments(1, 4, "INT", "PRS", 6),
                Arguments.arguments(1, 1, "STA", "PRS", 4),
                Arguments.arguments(1, 4, "STA", "PRS", 7),
                Arguments.arguments(1, 1, "INT", "STA", 2),
                Arguments.arguments(1, 4, "INT", "STA", 5)
        );
    }

    @ParameterizedTest
    @MethodSource("classHitPointScenarios")
    public void getHitPoints_ReturnsExpectedResultsForClassCharacters(int level,
                                                                      int level1Hp,
                                                                      int staValue,
                                                                      int randomRoll,
                                                                      String speciesStrength,
                                                                      String speciesWeakness,
                                                                      int expectedHitPoints) {
        // arrange
        var maxHpPerLevel = 4;
        var charClass = new CharClass(CharType.BERZERKER.toString(),
                null,
                null,
                level1Hp,
                maxHpPerLevel,
                null,
                null,
                null,
                null);
        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        // This is only needed for characters level 2 and up: it doesn't apply to level 1 characters
        Mockito.when(randomNumberWorker.getIntFromRange(1, maxHpPerLevel)).thenReturn(randomRoll);

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, staValue, 0, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(level)
                .withAttributes(attributesMap)
                .withSpeciesStrength(speciesStrength)
                .withSpeciesWeakness(speciesWeakness)
                .build();

        // act
        var actualHitPoints = characterSheetWorker.getHitPoints(request);

        // assert
        assertEquals(expectedHitPoints, actualHitPoints);
    }

    static Stream<Arguments> classHitPointScenarios() {
        // Class characters get their level 1 HP plus STA at level 1,
        // and 1d3 or 1d4 HP per level depending on class
        return Stream.of(
                Arguments.arguments(1, 8, 1, 0, "INT", "PRS", 9),
                Arguments.arguments(1, 8, 1, 0, "STA", "PRS", 10),
                Arguments.arguments(1, 8, 1, 0, "INT", "STA", 8),
                Arguments.arguments(7, 8, 1, 1, "INT", "PRS", 15),
                Arguments.arguments(7, 8, 1, 4, "INT", "PRS", 33),
                Arguments.arguments(7, 8, 1, 1, "STA", "PRS", 16),
                Arguments.arguments(7, 8, 1, 4, "STA", "PRS", 34),
                Arguments.arguments(7, 8, 1, 1, "INT", "STA", 14),
                Arguments.arguments(7, 8, 1, 4, "INT", "STA", 32)
        );
    }

    @Test
    public void getHitPoints_ReturnsExpectedResultsIfBonusHpFeaturesAreSelected() {
        // arrange
        var level1Hp = 8;
        var maxHpAtLevelUp = 4;
        var staValue = 1;
        var bonusTier1HitPoints = 2;
        var bonusTier2HitPoints = 3;
        var expectedHitPoints = level1Hp + staValue + bonusTier1HitPoints + bonusTier2HitPoints;

        var bonusHpTier1FeatureName = "Tier I bonus HP test";
        var bonusHpTier1FeatureAttribute = new FeatureAttribute(FeatureAttributeType.BONUS_HP,
                Integer.toString(bonusTier1HitPoints));
        var bonusHpTier1Feature = new Feature(bonusHpTier1FeatureName,
                List.of(bonusHpTier1FeatureAttribute));
        var bonusHpTier2FeatureName = "Tier II bonus HP test";
        var bonusHpTier2FeatureAttribute = new FeatureAttribute(FeatureAttributeType.BONUS_HP,
                Integer.toString(bonusTier2HitPoints));
        var bonusHpTier2Feature = new Feature(bonusHpTier2FeatureName,
                List.of(bonusHpTier2FeatureAttribute));
        var features = new Features(
                List.of(bonusHpTier1Feature),
                List.of(bonusHpTier2Feature)
        );

        var charClass = new CharClass(CharType.RANGER.toString(),
                Arrays.asList(1, 2, 3, 4, 5, 6, 7),
                Arrays.asList(10, 11, 12, 13, 14, 15 ,16),
                level1Hp,
                maxHpAtLevelUp,
                List.of(""),
                null,
                null,
                features);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, staValue, 0, 0, 0, 0);
        var featuresRequest = new FeaturesRequest(
                List.of(bonusHpTier1FeatureName),
                List.of(bonusHpTier2FeatureName)
        );
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(1)
                .withAttributes(attributesMap)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("PER")
                .withFeatures(featuresRequest)
                .build();

        // act
        var actualHitPoints = characterSheetWorker.getHitPoints(request);

        // assert
        assertEquals(expectedHitPoints, actualHitPoints);
    }
}
