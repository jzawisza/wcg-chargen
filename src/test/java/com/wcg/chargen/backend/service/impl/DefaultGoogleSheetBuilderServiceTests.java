package com.wcg.chargen.backend.service.impl;

import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.DataValidationRule;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.Sheet;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.*;
import com.wcg.chargen.backend.service.*;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class DefaultGoogleSheetBuilderServiceTests {
    @Autowired
    GoogleSheetBuilderService googleSheetBuilderService;
    @MockBean
    ProfessionsService professionsService;
    @MockBean
    CharClassesService charClassesService;
    @MockBean
    CommonerService commonerService;
    @Autowired
    RandomNumberService randomNumberService;
    @Autowired
    SkillsProvider skillsProvider;

    private static final String PROFESSION_1_NAME = "Profession1";
    private static final String PROFESSION_2_NAME = "Profession2";
    private static final String PROFESSION_3_NAME = "Profession3";
    private static final String SKILL_1_NAME = "Arcana";
    private static final String SKILL_2_NAME = "Athletics";
    private static final String SKILL_3_NAME = "Deceit";
    private static final String SKILL_4_NAME = "Stealth";
    private static final int TEST_LEVEL_1_HP = 8;
    private static final int TEST_MAX_HP_AT_LEVEL_UP = 4;

    @BeforeEach
    public void beforeTest() {
        var professions = new Professions(List.of(
                new Profession(PROFESSION_1_NAME, 0, 3),
                new Profession(PROFESSION_2_NAME, 4, 8),
                new Profession(PROFESSION_3_NAME, 9, 11)));

        var skills = List.of(SKILL_1_NAME, SKILL_2_NAME, SKILL_3_NAME, SKILL_4_NAME);

        var charClass = new CharClass(CharType.WARRIOR.toString(),
                Arrays.asList(1, 2, 3, 4, 5, 6, 7),
                Arrays.asList(10, 11, 12, 13, 14, 15 ,16),
                TEST_LEVEL_1_HP,
                TEST_MAX_HP_AT_LEVEL_UP,
                skills,
                null,
                null);

        var commonerInfo = new Commoner(0, 10, 12, 4, null);

        Mockito.when(professionsService.getAllProfessions()).thenReturn(professions);
        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);
        Mockito.when(commonerService.getInfo()).thenReturn(commonerInfo);
    }
    @Test
    public void buildStatsSheet_BuildsSheetWithExpectedTitle() {
        // arrange
        var request = getCharacterCreateRequest(CharType.BERZERKER);

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        assertNotNull(sheet);
        assertNotNull(sheet.getProperties());
        assertEquals("Stats", sheet.getProperties().getTitle());
    }

    @ParameterizedTest
    @MethodSource("getMagicUsersAndExpectedAttributeCells")
    public void buildStatsSheet_MagicUsersHaveSpellAttackModifierWithCorrectFormulaForClass(
            CharType charType, String cellName) {
        // arrange
        var request = getCharacterCreateRequest(charType);
        var expectedFormula = "=SUM(B5," + cellName + ")";

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var spellTextValue = getCellValueFromSheet(sheet, 9, 7);
        assertEquals("Spell", spellTextValue.getStringValue());

        var spellFormulaValue = getCellValueFromSheet(sheet,9, 8);
        assertEquals(expectedFormula, spellFormulaValue.getFormulaValue());

        var damageModifiersValue = getCellValueFromSheet(sheet, 11, 7);
        assertEquals("DAMAGE MODIFIERS", damageModifiersValue.getStringValue());

        var meleeValue =  getCellValueFromSheet(sheet, 12, 7);
        assertEquals("Melee", meleeValue.getStringValue());

        var rangedValue =  getCellValueFromSheet(sheet, 13, 7);
        assertEquals("Ranged", rangedValue.getStringValue());
    }

    @ParameterizedTest
    @EnumSource(CharType.class)
    public void buildStatsSheet_NonMagicUsersDoNotHaveSpellAttackModifier(CharType charType) {
        if (!charType.isMagicUser()) {
            // arrange
            var request = getCharacterCreateRequest(charType);

            // act
            var sheet = googleSheetBuilderService.buildStatsSheet(request);

            // assert
            var damageModifiersValue = getCellValueFromSheet(sheet, 10, 7);
            assertEquals("DAMAGE MODIFIERS", damageModifiersValue.getStringValue());

            var meleeValue =  getCellValueFromSheet(sheet, 11, 7);
            assertEquals("Melee", meleeValue.getStringValue());

            var rangedValue =  getCellValueFromSheet(sheet, 12, 7);
            assertEquals("Ranged", rangedValue.getStringValue());
        }
    }

    @Test
    public void buildStatsSheet_FirstDataRowIsPopulatedCorrectlyForClassCharacter() {
        // arrange
        var expectedCharName = "TestName";
        var expectedLevel = 3;
        var expectedSpecies = SpeciesType.ELF;
        var expectedCharType = CharType.SKALD;

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(expectedCharName)
                .withSpeciesType(expectedSpecies)
                .withCharacterType(expectedCharType)
                .withLevel(expectedLevel)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var charNameValue = getCellValueFromSheet(sheet, 2, 0);
        assertEquals(expectedCharName, charNameValue.getStringValue());

        var speciesValue = getCellValueFromSheet(sheet, 2, 1);
        assertEquals(expectedSpecies.toCharSheetString(), speciesValue.getStringValue());

        var levelValue = getCellValueFromSheet(sheet, 2, 2);
        assertEquals(expectedLevel, levelValue.getNumberValue());

        var professionValue = getCellValueFromSheet(sheet, 2, 3);
        assertEquals("", professionValue.getStringValue());

        var charClassValue = getCellValueFromSheet(sheet, 2, 4);
        assertEquals(expectedCharType.toCharSheetString(), charClassValue.getStringValue());
    }

    @Test
    public void buildStatsSheet_FirstDataRowIsPopulatedCorrectlyForCommonerCharacter() {
        // arrange
        var expectedCharName = "TestName";
        var expectedLevel = 0;
        var expectedSpecies = SpeciesType.ELF;
        var expectedProfession = "Cartwright";

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(expectedCharName)
                .withSpeciesType(expectedSpecies)
                .withProfession(expectedProfession)
                .withLevel(expectedLevel)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var charNameValue = getCellValueFromSheet(sheet, 2, 0);
        assertEquals(expectedCharName, charNameValue.getStringValue());

        var speciesValue = getCellValueFromSheet(sheet, 2, 1);
        assertEquals(expectedSpecies.toCharSheetString(), speciesValue.getStringValue());

        var levelValue = getCellValueFromSheet(sheet, 2, 2);
        assertEquals(expectedLevel, levelValue.getNumberValue());

        var professionValue = getCellValueFromSheet(sheet, 2, 3);
        assertEquals(expectedProfession, professionValue.getStringValue());

        var charClassValue = getCellValueFromSheet(sheet, 2, 4);
        assertEquals("", charClassValue.getStringValue());
    }

    @Test
    public void buildStatsSheet_FirstDataRowHasExpectedDataValidationsForClassCharacter() {
        // arrange
        var expectedSpeciesValuesList = Arrays.stream(SpeciesType.values())
                .map(SpeciesType::toCharSheetString)
                .toList();
        var expectedProfessionValuesList = Arrays.asList(PROFESSION_1_NAME, PROFESSION_2_NAME, PROFESSION_3_NAME);
        var expectedCharClassValuesList = Arrays.stream(CharType.values())
                .map(CharType::toCharSheetString)
                .toList();

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(1)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var speciesCellData = getCellDataFromSheet(sheet, 2, 1);
        assertConditionValueListHasAllValuesFromList(speciesCellData.getDataValidation(),
                expectedSpeciesValuesList);

        var levelCellData = getCellDataFromSheet(sheet, 2, 2);
        assertNotNull(levelCellData.getDataValidation());
        assertNotNull(levelCellData.getDataValidation().getCondition());
        var levelConditionValuesList = levelCellData.getDataValidation().getCondition().getValues();
        assertNotNull(levelConditionValuesList);
        assertEquals(2, levelConditionValuesList.size());
        assertEquals("1", levelConditionValuesList.getFirst().getUserEnteredValue());
        assertEquals("7", levelConditionValuesList.getLast().getUserEnteredValue());

        var professionCellData = getCellDataFromSheet(sheet, 2, 3);
        assertConditionValueListHasAllValuesFromList(professionCellData.getDataValidation(),
                expectedProfessionValuesList);

        var charClassCellData = getCellDataFromSheet(sheet, 2, 4);
        assertConditionValueListHasAllValuesFromList(charClassCellData.getDataValidation(),
                expectedCharClassValuesList);
    }

    @Test
    public void buildStatsSheet_FirstDataRowHasExpectedDataValidationsForCommonerCharacter() {
        // arrange
        var expectedSpeciesValuesList = Arrays.stream(SpeciesType.values())
                .map(SpeciesType::toCharSheetString)
                .toList();
        var expectedProfessionValuesList = Arrays.asList(PROFESSION_1_NAME, PROFESSION_2_NAME, PROFESSION_3_NAME);
        var expectedCharClassValuesList = Arrays.stream(CharType.values())
                .map(CharType::toCharSheetString)
                .toList();

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withProfession(PROFESSION_1_NAME)
                .withLevel(0)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var speciesCellData = getCellDataFromSheet(sheet, 2, 1);
        assertConditionValueListHasAllValuesFromList(speciesCellData.getDataValidation(),
                expectedSpeciesValuesList);

        var levelCellData = getCellDataFromSheet(sheet, 2, 2);
        assertNotNull(levelCellData.getDataValidation());
        assertNotNull(levelCellData.getDataValidation().getCondition());
        var levelConditionValuesList = levelCellData.getDataValidation().getCondition().getValues();
        assertNotNull(levelConditionValuesList);
        assertEquals(2, levelConditionValuesList.size());
        assertEquals("0", levelConditionValuesList.getFirst().getUserEnteredValue());
        assertEquals("7", levelConditionValuesList.getLast().getUserEnteredValue());

        var professionCellData = getCellDataFromSheet(sheet, 2, 3);
        assertConditionValueListHasAllValuesFromList(professionCellData.getDataValidation(),
                expectedProfessionValuesList);

        // We include character class validations for Level 0 characters because
        // they can get promoted to class characters...if they live long enough
        var charClassCellData = getCellDataFromSheet(sheet, 2, 4);
        assertConditionValueListHasAllValuesFromList(charClassCellData.getDataValidation(),
                expectedCharClassValuesList);
    }

    @Test
    public void buildStatsSheet_AttackAndEvasionArePopulatedCorrectly() {
        // arrange
        var expectedAttack = 3;
        var expectedEvasion = 12;
        var expectedEvasionFormula = String.format("=SUM(%d,B10)", expectedEvasion);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(3)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var attackValue = getCellValueFromSheet(sheet, 4, 1);
        assertEquals((double)expectedAttack, attackValue.getNumberValue());

        var evasionValue = getCellValueFromSheet(sheet, 4, 2);
        assertEquals(expectedEvasionFormula, evasionValue.getFormulaValue());
    }

    private void assertConditionValueListHasAllValuesFromList(DataValidationRule dataValidationRule,
                                                              List<String> expectedValues) {
        assertNotNull(dataValidationRule);
        var condition = dataValidationRule.getCondition();
        assertNotNull(condition);
        var conditionValueList = condition.getValues();
        assertNotNull(conditionValueList);

        var expectedValueSet = new HashSet<>(expectedValues);
        for (var conditionValue : conditionValueList) {
            var valueStr = conditionValue.getUserEnteredValue();
            var containedValue = expectedValueSet.remove(valueStr);
            if (!containedValue) {
                fail("Data validation rule condition missing expected value " + valueStr);
            }
        }

        assertTrue(expectedValueSet.isEmpty());
    }

    @Test
    public void buildStatsSheet_FortunePointsArePopulatedCorrectlyIfLuckIsNotSpeciesStrengthOrWeakness() {
        // arrange
        var luckValue = 1;
        var level = 3;
        var expectedFortunePoints = level + luckValue;

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, 0, 0, 0, 0, luckValue);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(level)
                .withAttributes(attributesMap)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("PER")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var fortunePointsValue = getCellValueFromSheet(sheet, 4, 3);
        assertEquals(expectedFortunePoints, fortunePointsValue.getNumberValue());
    }

    @Test
    public void buildStatsSheet_FortunePointsArePopulatedCorrectlyIfLuckIsSpeciesStrength() {
        // arrange
        var luckValue = 1;
        var level = 3;
        // Extra +1 since luck is species strength
        var expectedFortunePoints = level + luckValue + 1;

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, 0, 0, 0, 0, luckValue);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(level)
                .withAttributes(attributesMap)
                .withSpeciesStrength("LUC")
                .withSpeciesWeakness("PER")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var fortunePoints = getCellValueFromSheet(sheet, 4, 3);
        assertEquals(expectedFortunePoints, fortunePoints.getNumberValue());
    }

    @Test
    public void buildStatsSheet_FortunePointsArePopulatedCorrectlyIfLuckIsSpeciesWeakness() {
        // arrange
        var luckValue = 1;
        var level = 3;
        // -1 since luck is species weakness
        var expectedFortunePoints = level + luckValue - 1;

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, 0, 0, 0, 0, luckValue);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(level)
                .withAttributes(attributesMap)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var fortunePoints = getCellValueFromSheet(sheet, 4, 3);
        assertEquals(expectedFortunePoints, fortunePoints.getNumberValue());
    }

    @Test
    public void buildStatsSheet_FortunePointsAreNotLessThanZero() {
        // arrange
        var luckValue = -2;
        var level = 1;

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, 0, 0, 0, 0, luckValue);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(level)
                .withAttributes(attributesMap)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("PER")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var fortunePoints = getCellValueFromSheet(sheet, 4, 3);
        assertEquals(0, fortunePoints.getNumberValue());
    }

    @Test
    public void buildStatsSheet_HitPointsAreInExpectedRangeForCommonerCharacterWhereStaminaIsNotSpeciesStrengthOrWeakness() {
        // arrange
        var staValue = 1;
        // min d4+1
        var minHp = staValue + 1 + 1;
        // max d4+1
        var maxHp = staValue + 4 + 1;

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, staValue, 0, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withProfession("Test")
                .withLevel(0)
                .withAttributes(attributesMap)
                .withSpeciesStrength("INT")
                .withSpeciesWeakness("PRS")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var currentHpValue = getCellValueFromSheet(sheet, 4, 4);
        assertNotNull(currentHpValue.getNumberValue());
        assertTrue(currentHpValue.getNumberValue() >= minHp);
        assertTrue(currentHpValue.getNumberValue() <= maxHp);

        var maxHpValue = getCellValueFromSheet(sheet, 4, 5);
        assertEquals(currentHpValue.getNumberValue(), maxHpValue.getNumberValue());
    }

    @Test
    public void buildStatsSheet_HitPointsAreInExpectedRangeForCommonerCharacterWhereStaminaIsSpeciesStrength() {
        // arrange
        var staValue = 1;
        // min d4+1 + 1 for species strength
        var minHp = staValue + 1 + 1 + 1;
        // max d4+1 + 1 for species strength
        var maxHp = staValue + 4 + 1 + 1;

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, staValue, 0, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withProfession("Test")
                .withLevel(0)
                .withAttributes(attributesMap)
                .withSpeciesStrength("STA")
                .withSpeciesWeakness("PRS")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var currentHpValue = getCellValueFromSheet(sheet, 4, 4);
        assertNotNull(currentHpValue.getNumberValue());
        assertTrue(currentHpValue.getNumberValue() >= minHp);
        assertTrue(currentHpValue.getNumberValue() <= maxHp);

        var maxHpValue = getCellValueFromSheet(sheet, 4, 5);
        assertEquals(currentHpValue.getNumberValue(), maxHpValue.getNumberValue());
    }

    @Test
    public void buildStatsSheet_HitPointsAreInExpectedRangeForCommonerCharacterWhereStaminaIsSpeciesWeakness() {
        // arrange
        var staValue = 1;
        // min d4+1 - 1 for species weakness
        var minHp = staValue + 1 + 1 - 1;
        // max d4+1 - 1 for species weakness
        var maxHp = staValue + 4 + 1 - 1;

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, staValue, 0, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withProfession("Test")
                .withLevel(0)
                .withAttributes(attributesMap)
                .withSpeciesStrength("INT")
                .withSpeciesWeakness("STA")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var currentHpValue = getCellValueFromSheet(sheet, 4, 4);
        assertNotNull(currentHpValue.getNumberValue());
        assertTrue(currentHpValue.getNumberValue() >= minHp);
        assertTrue(currentHpValue.getNumberValue() <= maxHp);

        var maxHpValue = getCellValueFromSheet(sheet, 4, 5);
        assertEquals(currentHpValue.getNumberValue(), maxHpValue.getNumberValue());
    }

    @Test
    public void buildStatsSheet_HitPointsArePopulatedCorrectlyForLevel1CharacterWhereStaminaIsNotSpeciesStrengthOrWeakness() {
        // arrange
        var staValue = 1;
        var expectedHitPoints = TEST_LEVEL_1_HP + staValue;

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, staValue, 0, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(1)
                .withAttributes(attributesMap)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("PER")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var currentHpValue = getCellValueFromSheet(sheet, 4, 4);
        assertEquals(expectedHitPoints, currentHpValue.getNumberValue());

        var maxHpValue = getCellValueFromSheet(sheet, 4, 5);
        assertEquals(expectedHitPoints, maxHpValue.getNumberValue());
    }

    @Test
    public void buildStatsSheet_HitPointsArePopulatedCorrectlyForLevel1CharacterWhereStaminaIsSpeciesStrength() {
        // arrange
        var staValue = 1;
        var expectedHitPoints = TEST_LEVEL_1_HP + staValue + 1;

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, staValue, 0, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(1)
                .withAttributes(attributesMap)
                .withSpeciesStrength("STA")
                .withSpeciesWeakness("PER")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var currentHpValue = getCellValueFromSheet(sheet, 4, 4);
        assertEquals(expectedHitPoints, currentHpValue.getNumberValue());

        var maxHpValue = getCellValueFromSheet(sheet, 4, 5);
        assertEquals(expectedHitPoints, maxHpValue.getNumberValue());
    }

    @Test
    public void buildStatsSheet_HitPointsArePopulatedCorrectlyForLevel1CharacterWhereStaminaIsSpeciesWeakness() {
        // arrange
        var staValue = 1;
        var expectedHitPoints = TEST_LEVEL_1_HP + staValue - 1;

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, staValue, 0, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(1)
                .withAttributes(attributesMap)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("STA")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var currentHpValue = getCellValueFromSheet(sheet, 4, 4);
        assertEquals(expectedHitPoints, currentHpValue.getNumberValue());

        var maxHpValue = getCellValueFromSheet(sheet, 4, 5);
        assertEquals(expectedHitPoints, maxHpValue.getNumberValue());
    }

    @ParameterizedTest
    @CsvSource({
            "2, 9, 13",
            "3, 10, 17",
            "4, 11, 21",
            "5, 12, 25",
            "6, 13, 29",
            "7, 14, 33"
    })
    public void buildStatsSheet_HitPointsAreInExpectedRangeForHigherLevelCharacterWhereStaminaIsNotSpeciesStrengthOrWeakness(
            int level, int minHp, int maxHp
    ) {
        // arrange
        var staValue = 1;

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, staValue, 0, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(level)
                .withAttributes(attributesMap)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("PER")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var currentHpValue = getCellValueFromSheet(sheet, 4, 4);
        assertNotNull(currentHpValue.getNumberValue());
        assertTrue(currentHpValue.getNumberValue() >= minHp);
        assertTrue(currentHpValue.getNumberValue() <= maxHp);

        var maxHpValue = getCellValueFromSheet(sheet, 4, 5);
        assertEquals(currentHpValue.getNumberValue(), maxHpValue.getNumberValue());
    }

    @ParameterizedTest
    @CsvSource({
            "2, 10, 14",
            "3, 11, 18",
            "4, 12, 22",
            "5, 13, 26",
            "6, 14, 30",
            "7, 15, 34"
    })
    public void buildStatsSheet_HitPointsAreInExpectedRangeForHigherLevelCharacterWhereStaminaIsSpeciesStrength(
            int level, int minHp, int maxHp
    ) {
        // arrange
        var staValue = 1;

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, staValue, 0, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(level)
                .withAttributes(attributesMap)
                .withSpeciesStrength("STA")
                .withSpeciesWeakness("PER")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var currentHpValue = getCellValueFromSheet(sheet, 4, 4);
        assertNotNull(currentHpValue.getNumberValue());
        assertTrue(currentHpValue.getNumberValue() >= minHp);
        assertTrue(currentHpValue.getNumberValue() <= maxHp);

        var maxHpValue = getCellValueFromSheet(sheet, 4, 5);
        assertEquals(currentHpValue.getNumberValue(), maxHpValue.getNumberValue());
    }

    @ParameterizedTest
    @CsvSource({
            "2, 8, 12",
            "3, 9, 16",
            "4, 10, 20",
            "5, 11, 24",
            "6, 12, 28",
            "7, 13, 32"
    })
    public void buildStatsSheet_HitPointsAreInExpectedRangeForHigherLevelCharacterWhereStaminaIsSpeciesWeakness(
            int level, int minHp, int maxHp
    ) {
        // arrange
        var staValue = 1;

        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, staValue, 0, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(level)
                .withAttributes(attributesMap)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("STA")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var currentHpValue = getCellValueFromSheet(sheet, 4, 4);
        assertNotNull(currentHpValue.getNumberValue());

        assertTrue(currentHpValue.getNumberValue() >= minHp);
        assertTrue(currentHpValue.getNumberValue() <= maxHp);

        var maxHpValue = getCellValueFromSheet(sheet, 4, 5);
        assertEquals(currentHpValue.getNumberValue(), maxHpValue.getNumberValue());
    }

    @Test
    public void buildStatsSheet_ContainsExpectedSkillsAndSkillAttributesForNonHumanCharacter() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterType(CharType.RANGER)
                .withLevel(1)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("PRS")
                .withSpeciesSkill("Intimidation")
                .withBonusSkills(List.of("Healing"))
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var skill1NameValue = getCellValueFromSheet(sheet, 8, 3);
        assertEquals(SKILL_1_NAME, skill1NameValue.getStringValue());
        var skill2NameValue = getCellValueFromSheet(sheet, 9, 3);
        assertEquals(SKILL_2_NAME, skill2NameValue.getStringValue());
        var skill3NameValue = getCellValueFromSheet(sheet, 10, 3);
        assertEquals(SKILL_3_NAME, skill3NameValue.getStringValue());
        var skill4NameValue = getCellValueFromSheet(sheet, 11, 3);
        assertEquals("Healing", skill4NameValue.getStringValue());
        var skill5NameValue = getCellValueFromSheet(sheet, 12, 3);
        assertEquals("Intimidation", skill5NameValue.getStringValue());
        var skill6NameValue = getCellValueFromSheet(sheet, 13, 3);
        assertEquals(SKILL_4_NAME, skill6NameValue.getStringValue());
        // Last attribute row should have no skill name
        var skill7NameValue = getCellValueFromSheet(sheet, 14, 3);
        assertEquals("", skill7NameValue.getStringValue());

        var skill1AttributeValue = getCellValueFromSheet(sheet, 8, 4);
        assertEquals("INT", skill1AttributeValue.getStringValue());
        var skill2AttributeValue = getCellValueFromSheet(sheet, 9, 4);
        assertEquals("STR", skill2AttributeValue.getStringValue());
        var skill3AttributeValue = getCellValueFromSheet(sheet, 10, 4);
        assertEquals("PRS", skill3AttributeValue.getStringValue());
        var skill4AttributeValue = getCellValueFromSheet(sheet, 11, 4);
        assertEquals("INT", skill4AttributeValue.getStringValue());
        var skill5AttributeValue = getCellValueFromSheet(sheet, 12, 4);
        assertEquals("STR", skill5AttributeValue.getStringValue());
        var skill6AttributeValue = getCellValueFromSheet(sheet, 13, 4);
        assertEquals("COR", skill6AttributeValue.getStringValue());
        // Last attribute row should have no skill attribute
        var skill7AttributeValue = getCellValueFromSheet(sheet, 14, 4);
        assertEquals("", skill7AttributeValue.getStringValue());
    }

    @Test
    public void buildStatsSheet_ContainsExpectedSkillsAndSkillAttributesForHumanCharacter() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.RANGER)
                .withLevel(1)
                .withSpeciesStrength("INT")
                .withBonusSkills(List.of("Healing", "Negotiation"))
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var skill1NameValue = getCellValueFromSheet(sheet, 8, 3);
        assertEquals(SKILL_1_NAME, skill1NameValue.getStringValue());
        var skill2NameValue = getCellValueFromSheet(sheet, 9, 3);
        assertEquals(SKILL_2_NAME, skill2NameValue.getStringValue());
        var skill3NameValue = getCellValueFromSheet(sheet, 10, 3);
        assertEquals(SKILL_3_NAME, skill3NameValue.getStringValue());
        var skill4NameValue = getCellValueFromSheet(sheet, 11, 3);
        assertEquals("Healing", skill4NameValue.getStringValue());
        var skill5NameValue = getCellValueFromSheet(sheet, 12, 3);
        assertEquals("Negotiation", skill5NameValue.getStringValue());
        var skill6NameValue = getCellValueFromSheet(sheet, 13, 3);
        assertEquals(SKILL_4_NAME, skill6NameValue.getStringValue());
        // Last attribute row should have no skill name
        var skill7NameValue = getCellValueFromSheet(sheet, 14, 3);
        assertEquals("", skill7NameValue.getStringValue());

        var skill1AttributeValue = getCellValueFromSheet(sheet, 8, 4);
        assertEquals("INT", skill1AttributeValue.getStringValue());
        var skill2AttributeValue = getCellValueFromSheet(sheet, 9, 4);
        assertEquals("STR", skill2AttributeValue.getStringValue());
        var skill3AttributeValue = getCellValueFromSheet(sheet, 10, 4);
        assertEquals("PRS", skill3AttributeValue.getStringValue());
        var skill4AttributeValue = getCellValueFromSheet(sheet, 11, 4);
        assertEquals("INT", skill4AttributeValue.getStringValue());
        var skill5AttributeValue = getCellValueFromSheet(sheet, 12, 4);
        assertEquals("PRS", skill5AttributeValue.getStringValue());
        var skill6AttributeValue = getCellValueFromSheet(sheet, 13, 4);
        assertEquals("COR", skill6AttributeValue.getStringValue());
        // Last attribute row should have no skill attribute
        var skill7AttributeValue = getCellValueFromSheet(sheet, 14, 4);
        assertEquals("", skill7AttributeValue.getStringValue());
    }

    @Test
    public void buildStats_ContainsExpectedDataValidationRulesForSkillAttributes() {
        // arrange
        var expectedAttributeValuesList = List.of("STR", "COR", "STA");
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.RANGER)
                .withLevel(1)
                .withSpeciesStrength("INT")
                .withBonusSkills(List.of("Healing", "Negotiation"))
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var singleAttributeValueCellData = getCellDataFromSheet(sheet, 8, 4);
        // The data validation for the Arcana skill should be null, since Arcana only uses INT
        assertNull(singleAttributeValueCellData.getDataValidation());

        var multiAttributeValueCellData = getCellDataFromSheet(sheet, 9, 4);
        // Athletics has 3 possible skills that it uses, so we should have a data validation rule
        assertConditionValueListHasAllValuesFromList(multiAttributeValueCellData.getDataValidation(),
                expectedAttributeValuesList);
    }

    @Test
    public void buildStats_CommonerCharactersHaveNoSkillsOrSkillAttributes() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withProfession("Test")
                .withLevel(0)
                .withSpeciesStrength("INT")
                .withSpeciesWeakness("PRS")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var startIndex = 8;
        var numAttributeRows = 7;
        for (int i = startIndex; i < startIndex + numAttributeRows; i++) {
            var skillNameValue = getCellValueFromSheet(sheet, i, 3);
            assertEquals("", skillNameValue.getStringValue());

            var skillAttributeCellData = getCellDataFromSheet(sheet, i,4 );
            assertNotNull(skillAttributeCellData.getUserEnteredValue());
            assertEquals("", skillAttributeCellData.getUserEnteredValue().getStringValue());
            assertNull(skillAttributeCellData.getDataValidation());
        }
    }

    @Test
    public void buildStats_ExtraRowsGeneratedIfNumberOfSkillsExceedsDefaultNumberOfRows() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.RANGER)
                .withLevel(1)
                .withSpeciesStrength("INT")
                .withBonusSkills(List.of("Healing", "History", "Intimidation", "Languages", "Negotiation"))
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var skill1NameValue = getCellValueFromSheet(sheet, 8, 3);
        assertEquals(SKILL_1_NAME, skill1NameValue.getStringValue());
        var skill2NameValue = getCellValueFromSheet(sheet, 9, 3);
        assertEquals(SKILL_2_NAME, skill2NameValue.getStringValue());
        var skill3NameValue = getCellValueFromSheet(sheet, 10, 3);
        assertEquals(SKILL_3_NAME, skill3NameValue.getStringValue());
        var skill4NameValue = getCellValueFromSheet(sheet, 11, 3);
        assertEquals("Healing", skill4NameValue.getStringValue());
        var skill5NameValue = getCellValueFromSheet(sheet, 12, 3);
        assertEquals("History", skill5NameValue.getStringValue());
        var skill6NameValue = getCellValueFromSheet(sheet, 13, 3);
        assertEquals("Intimidation", skill6NameValue.getStringValue());
        var skill7NameValue = getCellValueFromSheet(sheet, 14, 3);
        assertEquals("Languages", skill7NameValue.getStringValue());
        var skill8NameValue = getCellValueFromSheet(sheet, 15, 3);
        assertEquals("Negotiation", skill8NameValue.getStringValue());
        var skill9NameValue = getCellValueFromSheet(sheet, 16, 3);
        assertEquals(SKILL_4_NAME, skill9NameValue.getStringValue());
    }

    @Test
    public void buildSpellsSheet_BuildsSheetWithExpectedTitle() {
        // act
        var sheet = googleSheetBuilderService.buildSpellsSheet();

        // assert
        assertNotNull(sheet);
        assertNotNull(sheet.getProperties());
        assertEquals("Spells", sheet.getProperties().getTitle());
    }

    @Test
    public void buildFeaturesSheet_BuildsSheetWithExpectedTitle() {
        // act
        var sheet = googleSheetBuilderService.buildFeaturesSheet(null);

        // assert
        assertNotNull(sheet);
        assertNotNull(sheet.getProperties());
        assertEquals("Class/Species Features", sheet.getProperties().getTitle());
    }

    @Test
    public void buildGearSheet_BuildsSheetWithExpectedTitle() {
        // act
        var sheet = googleSheetBuilderService.buildGearSheet(null);

        // assert
        assertNotNull(sheet);
        assertNotNull(sheet.getProperties());
        assertEquals("Gear", sheet.getProperties().getTitle());
    }

    private CharacterCreateRequest getCharacterCreateRequest(CharType charType) {
        return CharacterCreateRequestBuilder
                .getBuilder()
                .withCharacterName("Test")
                .withCharacterType(charType)
                .withSpeciesType(SpeciesType.HUMAN)
                .withProfession(null)
                .withLevel(1)
                .build();
    }

    private CellData getCellDataFromSheet(Sheet sheet, int rowIndex, int colIndex) {
        assertNotNull(sheet);
        assertNotNull(sheet.getData());
        assertNotNull(sheet.getData().getFirst());
        assertNotNull(sheet.getData().getFirst().getRowData());
        assertTrue(sheet.getData().getFirst().getRowData().size() >= rowIndex);

        var row = sheet.getData().getFirst().getRowData().get(rowIndex);
        assertNotNull(row);
        assertNotNull(row.getValues());
        assertNotNull(row.getValues().get(colIndex));

        var cellData = row.getValues().get(colIndex);
        assertNotNull(cellData);

        return cellData;
    }

    private ExtendedValue getCellValueFromSheet(Sheet sheet, int rowIndex, int colIndex) {
        var cellData = getCellDataFromSheet(sheet, rowIndex, colIndex);
        var cellValue = cellData.getUserEnteredValue();
        assertNotNull(cellValue);

        return cellValue;
    }

    static Stream<Arguments> getMagicUsersAndExpectedAttributeCells() {
        return Stream.of(
                Arguments.arguments(CharType.MAGE, "B12"),
                Arguments.arguments(CharType.SHAMAN, "B14")
        );
    }
}
