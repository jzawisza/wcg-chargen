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
import org.junit.jupiter.params.provider.*;
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
    @MockBean
    CommonerService commonerService;
    @MockBean
    RandomNumberWorker randomNumberWorker;

    private static final String TEST_MODIFIER = "Test Modifier";
    private static final int TEST_LEVEL_1_HP = 8;
    private static final int TEST_MAX_HP_AT_LEVEL_UP = 4;

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
        var expectedEvasion = 2;
        var commoner = new Commoner(0, 2, 0, 0, null);

        Mockito.when(commonerService.getInfo()).thenReturn(commoner);

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
        var staValue = 1;
        var bonusTier1HitPoints = 2;
        var bonusTier2HitPoints = 3;
        var expectedHitPoints = TEST_LEVEL_1_HP + staValue + bonusTier1HitPoints + bonusTier2HitPoints;

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
                TEST_LEVEL_1_HP,
                TEST_MAX_HP_AT_LEVEL_UP,
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

    @Test
    public void getWeaponMethods_ReturnEmptyStringForCommonerCharacters() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(0)
                .build();

        // act
        var weaponName = characterSheetWorker.getWeaponName(request, 0);
        var weaponType = characterSheetWorker.getWeaponType(request, 0);
        var weaponDamage = characterSheetWorker.getWeaponDamage(request, 0);

        // assert
        assertEquals("", weaponName);
        assertEquals("", weaponType);
        assertEquals("", weaponDamage);
    }

    @ParameterizedTest
    @CsvSource({
            "BERZERKER, '', '', ''",
            "MYSTIC, 'Fists', 'Unarmed', '1d6'"
    })
    public void getWeaponMethods_ReturnExpectedValueIfQuickGearIsNotSelected(CharType charType,
                                                                             String expectedWeaponName,
                                                                             String expectedWeaponType,
                                                                             String expectedWeaponDamage) {
        // arrange
        var charClass = new CharClass(CharType.BERZERKER.toString(),
                null,
                null,
                0,
                0,
                null,
                null,
                null,
                new Features(Collections.emptyList(), Collections.emptyList()));
        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(charType)
                .withLevel(1)
                .withUseQuickGear(false)
                .build();

        // act
        var actualWeaponName = characterSheetWorker.getWeaponName(request, 0);
        var actualWeaponType = characterSheetWorker.getWeaponType(request, 0);
        var actualWeaponDamage = characterSheetWorker.getWeaponDamage(request, 0);

        // assert
        assertEquals(expectedWeaponName, actualWeaponName);
        assertEquals(expectedWeaponType, actualWeaponType);
        assertEquals(expectedWeaponDamage, actualWeaponDamage);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 'Weapon Name', 'Weapon Type', '1d8'",
            "1, '', '', ''"
    })
    public void getWeaponMethods_ReturnExpectedValuesIfQuickGearIsSelected(int index,
                                                                           String expectedWeaponName,
                                                                           String expectedWeaponType,
                                                                           String expectedWeaponDamage) {
        // arrange
        var weapon = new Weapon(expectedWeaponName, expectedWeaponType, expectedWeaponDamage);
        // We only have a single weapon in the list, so we expect empty values for index 1 and higher
        var gear = new Gear(null, List.of(weapon), 0, 0, null);
        var charClass = new CharClass(CharType.MAGE.toString(),
                null,
                null,
                0,
                0,
                null,
                gear,
                null,
                null);
        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.MAGE)
                .withLevel(1)
                .withUseQuickGear(true)
                .build();

        // act
        var actualWeaponName = characterSheetWorker.getWeaponName(request, index);
        var actualWeaponType = characterSheetWorker.getWeaponType(request, index);
        var actualWeaponDamage = characterSheetWorker.getWeaponDamage(request, index);

        // assert
        assertEquals(expectedWeaponName, actualWeaponName);
        assertEquals(expectedWeaponType, actualWeaponType);
        assertEquals(expectedWeaponDamage, actualWeaponDamage);
    }

    @ParameterizedTest
    @CsvSource({
            "true, true",
            "false, true",
            "true, false",
            "false, false"
    })
    public void getWeaponDamage_ReturnsExpectedValuesIfDamageIsBoostedByFeatures(
            boolean tier2FeatureForUnarmedDamage, boolean hasQuickGear) {
        // arrange
        var featureName = "Test Feature";
        var boostedUnarmedDamage = "1d8";
        var featureAttribute = new FeatureAttribute(FeatureAttributeType.UNARMED_BONUS, boostedUnarmedDamage);
        var feature = new Feature(featureName, List.of(featureAttribute));
        var regularUnarmedDamage = "1d6";
        var unarmedWeapon = new Weapon("Fists", "Unarmed", regularUnarmedDamage);
        var gear = new Gear(Collections.emptyList(), List.of(unarmedWeapon),0, 0, null);

        var features = tier2FeatureForUnarmedDamage ?
                new Features(Collections.emptyList(), List.of(feature)) :
                new Features(List.of(feature), Collections.emptyList());
        var featuresRequest = tier2FeatureForUnarmedDamage ?
                new FeaturesRequest(Collections.emptyList(), List.of(featureName)) :
                new FeaturesRequest(List.of(featureName), Collections.emptyList());

        var charClass = new CharClass(CharType.MYSTIC.toString(),
                Arrays.asList(1, 2, 3, 4, 5, 6, 7),
                Arrays.asList(10, 11, 12, 13, 14, 15 ,16),
                TEST_LEVEL_1_HP,
                TEST_MAX_HP_AT_LEVEL_UP,
                Collections.emptyList(),
                gear,
                null,
                features);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MYSTIC)
                .withLevel(5)
                .withFeatures(featuresRequest)
                .withUseQuickGear(hasQuickGear)
                .build();

        // act
        var actualWeaponDamage = characterSheetWorker.getWeaponDamage(request, 0);

        // assert
        assertEquals(boostedUnarmedDamage, actualWeaponDamage);
    }

    @Test
    public void getArmorMethods_ReturnEmptyStringForCommonerCharacters() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(0)
                .build();

        // act
        var armorName = characterSheetWorker.getArmorName(request, 0);
        var armorType = characterSheetWorker.getArmorType(request, 0);
        var armorDa = characterSheetWorker.getArmorDa(request, 0);

        // assert
        assertEquals("", armorName);
        assertEquals("", armorType);
        assertEquals("", armorDa);
    }

    @ParameterizedTest
    @EnumSource(value = CharType.class, names = {"BERZERKER", "MYSTIC"})
    public void getArmorMethods_ReturnEmptyStringIfQuickGearIsNotSelected(CharType charType) {
        // arrange
        var gear = new Gear(Collections.emptyList(), null, 0, 0, null);
        var charClass = new CharClass(charType.toString(),
                null,
                null,
                0,
                0,
                null,
                gear,
                null,
                new Features(Collections.emptyList(), Collections.emptyList()));
        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(charType)
                .withLevel(1)
                .withUseQuickGear(false)
                .build();

        // act
        var actualArmorName = characterSheetWorker.getArmorName(request, 0);
        var actualArmorType = characterSheetWorker.getArmorType(request, 0);
        var actualArmorDa = characterSheetWorker.getArmorDa(request, 0);

        // assert
        assertEquals("", actualArmorName);
        assertEquals("", actualArmorType);
        assertEquals("", actualArmorDa);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 'Leather Armor', 'Light', '3'",
            "1, '', '', ''"
    })
    public void getArmorMethods_ReturnExpectedValuesIfQuickGearIsSelected(int index,
                                                                          String expectedArmorName,
                                                                          String expectedArmorType,
                                                                          String expectedArmorDa) {
        // arrange
        var armor = new Armor(expectedArmorName, expectedArmorType, expectedArmorDa);
        var gear = new Gear(List.of(armor), null, 0, 0, null);
        var charClass = new CharClass(CharType.MAGE.toString(),
                null,
                null,
                0,
                0,
                null,
                gear,
                null,
                new Features(Collections.emptyList(), Collections.emptyList()));
        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.MAGE)
                .withLevel(1)
                .withUseQuickGear(true)
                .build();

        // act
        var actualArmorName = characterSheetWorker.getArmorName(request, index);
        var actualArmorType = characterSheetWorker.getArmorType(request, index);
        var actualArmorDa = characterSheetWorker.getArmorDa(request, index);

        // assert
        assertEquals(expectedArmorName, actualArmorName);
        assertEquals(expectedArmorType, actualArmorType);
        assertEquals(expectedArmorDa, actualArmorDa);
    }

    @ParameterizedTest
    @MethodSource("armorDaBoostScenarios")
    public void getArmorDa_ReturnsExpectedValueIfDaIsBoostedByFeatures(CharType charType,
                                                                       boolean useQuickGear,
                                                                       int baseDa,
                                                                       boolean useTier1Feature,
                                                                       boolean useTier2Feature,
                                                                       String expectedArmorDa) {
        // arrange
        var baseDaStr = Integer.toString(baseDa);

        var tier1FeatureName = "Tier I DA_PLUS_1";
        var tier1FeatureAttribute = new FeatureAttribute(FeatureAttributeType.DA_PLUS_1, "NO_HEAVY_ARMOR");
        var tier2FeatureName = "Tier II DA_PLUS_1";
        var tier2FeatureAttribute = new FeatureAttribute(FeatureAttributeType.DA_PLUS_1, "ANY");
        var tier1Feature = new Feature(tier1FeatureName, List.of(tier1FeatureAttribute));
        var tier2Feature = new Feature(tier2FeatureName, List.of(tier2FeatureAttribute));
        var features = new Features(
                List.of(tier1Feature),
                List.of(tier2Feature));

        var armor = new Armor("Leather Armor", "Light", baseDaStr);
        var gear = new Gear(List.of(armor),
                Collections.emptyList(),
                0,
                0,
                Collections.emptyList());

        var charClass = new CharClass(charType.toString(),
                null,
                null,
                0,
                0,
                null,
                gear,
                null,
                features);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var featuresRequest = new FeaturesRequest(
                useTier1Feature ? List.of(tier1FeatureName) : Collections.emptyList(),
                useTier2Feature ? List.of(tier2FeatureName) : Collections.emptyList()
        );
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(charType)
                .withLevel(5)
                .withFeatures(featuresRequest)
                .withUseQuickGear(useQuickGear)
                .build();

        // act
        var actualArmorDa = characterSheetWorker.getArmorDa(request, 0);

        // assert
        assertEquals(expectedArmorDa, actualArmorDa);
    }

    static Stream<Arguments> armorDaBoostScenarios() {
        return Stream.of(
                Arguments.arguments(CharType.BERZERKER, true, 3, false, false, "3"),
                Arguments.arguments(CharType.BERZERKER, true, 3, true, false, "4"),
                Arguments.arguments(CharType.BERZERKER, true, 3, false, true, "4"),
                Arguments.arguments(CharType.BERZERKER, true, 3, true, true, "5"),
                Arguments.arguments(CharType.MYSTIC, true, 3, false, false, "3"),
                Arguments.arguments(CharType.MYSTIC, true, 3, true, false, "4"),
                Arguments.arguments(CharType.MYSTIC, true, 3, false, true, "4"),
                Arguments.arguments(CharType.MYSTIC, true, 3, true, true, "5"),
                Arguments.arguments(CharType.MYSTIC, false, 3, false, false, ""),
                Arguments.arguments(CharType.MYSTIC, false, 3, true, false, "1"),
                Arguments.arguments(CharType.MYSTIC, false, 3, false, true, "1"),
                Arguments.arguments(CharType.MYSTIC, false, 3, true, true, "2")
        );
    }

    @Test
    public void getEquipmentList_ReturnsExpectedInformationForCommonerCharacters() {
        // arrange
        var item1 = "Item 1";
        var item2 = "Item 2";
        var itemList = List.of(item1, item2);
        var commoner = new Commoner(0, 0, 0, 0, itemList);

        Mockito.when(commonerService.getInfo()).thenReturn(commoner);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(0)
                .build();

        // act
        var equipmentList = characterSheetWorker.getEquipmentList(request);

        // assert
        assertNotNull(equipmentList);
        assertEquals(2, equipmentList.size());
        assertEquals(item1, equipmentList.get(0));
        assertEquals(item2, equipmentList.get(1));
    }

    @Test
    public void getEquipmentList_ReturnsExpectedInformationForClassCharactersWithQuickGear() {
        // arrange
        var item1 = "Item A";
        var item2 = "Item B";
        var item3 = "Item C";
        var itemList = List.of(item1, item2, item3);
        var gear = new Gear(null, null, 0, 0, itemList);
        var charClass = new CharClass(CharType.RANGER.toString(),
                null,
                null,
                0,
                0,
                null,
                gear,
                null,
                null);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(1)
                .withUseQuickGear(true)
                .build();

        // act
        var equipmentList = characterSheetWorker.getEquipmentList(request);

        // assert
        assertNotNull(equipmentList);
        assertEquals(3, equipmentList.size());
        assertEquals(item1, equipmentList.get(0));
        assertEquals(item2, equipmentList.get(1));
        assertEquals(item3, equipmentList.get(2));
    }

    @Test
    public void getEquipmentList_ReturnsNullForClassCharactersWithoutQuickGear() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(1)
                .withUseQuickGear(false)
                .build();

        // act
        var equipmentList = characterSheetWorker.getEquipmentList(request);

        // assert
        assertNull(equipmentList);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1, 1, 1",
            "5, 3, 3, 2"
    })
    public void getMoneyMethods_ReturnExpectedValuesForCommonerCharacters(int maxCopper,
                                                                          int maxSilver,
                                                                          int expectedCopper,
                                                                          int expectedSilver) {
        // arrange
        var commonerInfo = new Commoner(0, 0, maxCopper, maxSilver, null);

        Mockito.when(commonerService.getInfo()).thenReturn(commonerInfo);
        Mockito.when(randomNumberWorker.getIntFromRange(1, maxCopper)).thenReturn(expectedCopper);
        Mockito.when(randomNumberWorker.getIntFromRange(1, maxSilver)).thenReturn(expectedSilver);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(0)
                .build();

        // act
        var actualCopper = characterSheetWorker.getCopper(request);
        var actualSilver = characterSheetWorker.getSilver(request);

        // assert
        assertEquals(expectedCopper, actualCopper);
        assertEquals(expectedSilver, actualSilver);
    }

    @Test
    public void getMoneyMethods_ReturnZeroForClassCharactersWithoutQuickGear() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(1)
                .withUseQuickGear(false)
                .build();

        // act
        var actualCopper = characterSheetWorker.getCopper(request);
        var actualSilver = characterSheetWorker.getSilver(request);

        // assert
        assertEquals(0, actualCopper);
        assertEquals(0, actualSilver);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1, 1, 1",
            "5, 3, 3, 2"
    })
    public void getMoneyMethods_ReturnExpectedValuesForClassCharactersWithQuickGear(int maxCopper,
                                                                                    int maxSilver,
                                                                                    int expectedCopper,
                                                                                     int expectedSilver) {
        // arrange
        var gear = new Gear(null, null, maxCopper, maxSilver, null);
        var charClass = new CharClass(CharType.RANGER.toString(),
                null,
                null,
                0,
                0,
                null,
                gear,
                null,
                null);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);
        Mockito.when(randomNumberWorker.getIntFromRange(1, maxCopper)).thenReturn(expectedCopper);
        Mockito.when(randomNumberWorker.getIntFromRange(1, maxSilver)).thenReturn(expectedSilver);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(1)
                .withUseQuickGear(true)
                .build();

        // act
        var actualCopper = characterSheetWorker.getCopper(request);
        var actualSilver = characterSheetWorker.getSilver(request);

        // assert
        assertEquals(expectedCopper, actualCopper);
        assertEquals(expectedSilver, actualSilver);
    }

    public void getCopper_ReturnsExpectedValueForShamansWithQuickGear() {
        // arrange
        // For shamans, we roll 2d12 for copper, so the expected copper is double the random roll
        var randomRoll = 7;
        var expectedCopper = randomRoll * 2;

        // maxCopper and maxSilver values aren't actually used for shamans
        var gear = new Gear(null, null, 0, 0, null);
        var charClass = new CharClass(CharType.RANGER.toString(),
                null,
                null,
                0,
                0,
                null,
                gear,
                null,
                null);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);
        Mockito.when(randomNumberWorker.getIntFromRange(1, 12)).thenReturn(expectedCopper);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(1)
                .withCharacterType(CharType.SHAMAN)
                .withUseQuickGear(true)
                .build();

        // act
        var actualCopper = characterSheetWorker.getCopper(request);

        // assert
        assertEquals(expectedCopper, actualCopper);
    }

    @Test
    public void hasMagic_ReturnsFalseForCommonerCharacters() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(0)
                .build();

        // act
        var hasMagic = characterSheetWorker.hasMagic(request);

        // assert
        assertFalse(hasMagic);
    }

    @ParameterizedTest
    @EnumSource(value = CharType.class, names = {"MAGE", "SHAMAN"})
    public void hasMagic_ReturnsTrueForMagicUsers(CharType charType) {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(1)
                .withCharacterType(charType)
                .build();

        // act
        var hasMagic = characterSheetWorker.hasMagic(request);

        // assert
        assertTrue(hasMagic);
    }

    @ParameterizedTest
    @MethodSource("skaldFeaturesAndExpectedHasMagic")
    public void hasMagic_ReturnsTrueForSkaldsWithFeaturesThatAllowMagic(
            boolean hasMagicTier1Feature, boolean hasMagicTier2Feature, boolean expectedHasMagic) {
        // arrange
        var magicTier1FeatureName = "Magic Feature Tier I";
        var magicTier2FeatureName = "Magic Feature Tier II";
        var magicFeatureAttribute = new FeatureAttribute(FeatureAttributeType.MAGIC, "");
        var magicTier1Feature = new Feature(magicTier1FeatureName, List.of(magicFeatureAttribute));
        var magicTier2Feature = new Feature(magicTier2FeatureName, List.of(magicFeatureAttribute));
        var features = new Features(List.of(magicTier1Feature), List.of(magicTier2Feature));

        var charClass = new CharClass(CharType.SKALD.toString(),
                null,
                null,
                0,
                0,
                null,
                null,
                null,
                features);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var featuresRequest = new FeaturesRequest(
                hasMagicTier1Feature ? List.of(magicTier1FeatureName) : Collections.emptyList(),
                hasMagicTier2Feature ? List.of(magicTier2FeatureName) : Collections.emptyList()
        );

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.SKALD)
                .withLevel(4)
                .withFeatures(featuresRequest)
                .build();

        // act
        var actualHasMagic = characterSheetWorker.hasMagic(request);

        // assert
        assertEquals(expectedHasMagic, actualHasMagic);
    }

    static Stream<Arguments> skaldFeaturesAndExpectedHasMagic() {
        return Stream.of(
                Arguments.arguments(true, true, true),
                Arguments.arguments(true, false, true),
                Arguments.arguments(false, true, true),
                Arguments.arguments(false, false, false));
    }
}
