package com.wcg.chargen.backend.service.impl;

import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.DataValidationRule;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.Sheet;
import com.wcg.chargen.backend.enums.AttributeType;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.FeatureAttributeType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.*;
import com.wcg.chargen.backend.service.*;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;
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
    private static final int MAX_COMMONER_COPPER = 12;
    private static final int MAX_COMMONER_SILVER = 4;
    private static final int MAX_CLASS_COPPER = 6;
    private static final int MAX_CLASS_SILVER = 10;
    private static final String ITEM_1_NAME = "Item1";
    private static final String ITEM_2_NAME = "Item2";
    private static final String ITEM_3_NAME = "Item3";
    private static final String ITEM_4_NAME = "Item4";
    private static final String ITEM_5_NAME = "Item5";
    private static final String ITEM_6_NAME = "Item6";
    private static final String TIER_1_FEATURE_NAME = "Tier I feature";
    private static final String TIER_2_FEATURE_NAME = "Tier II feature";

    @BeforeEach
    public void beforeTest() {
        var professions = new Professions(List.of(
                new Profession(PROFESSION_1_NAME, 0, 3),
                new Profession(PROFESSION_2_NAME, 4, 8),
                new Profession(PROFESSION_3_NAME, 9, 11)));

        var skills = List.of(SKILL_1_NAME, SKILL_2_NAME, SKILL_3_NAME, SKILL_4_NAME);

        var armorList = List.of(new Armor("Leather Armor", "Light", "3"));
        var weaponsList = List.of(
                new Weapon("Short Sword", "Melee", "1d8"),
                new Weapon("Dagger", "Thrown", "1d6"));
        var itemsList = List.of(ITEM_1_NAME, ITEM_2_NAME, ITEM_3_NAME,
                ITEM_4_NAME, ITEM_5_NAME, ITEM_6_NAME);
        var gear = new Gear(armorList, weaponsList, MAX_CLASS_COPPER, MAX_CLASS_SILVER, itemsList);
        var charClass = new CharClass(CharType.WARRIOR.toString(),
                Arrays.asList(1, 2, 3, 4, 5, 6, 7),
                Arrays.asList(10, 11, 12, 13, 14, 15 ,16),
                TEST_LEVEL_1_HP,
                TEST_MAX_HP_AT_LEVEL_UP,
                skills,
                gear,
                null);

        var commonerInfo = new Commoner(0, 10, MAX_COMMONER_COPPER, MAX_COMMONER_SILVER,
                List.of(ITEM_1_NAME, ITEM_2_NAME));

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

    @ParameterizedTest
    @CsvSource({
        "true, '=SUM(11,B10,1)'",
        "false, '=SUM(11,B10)'"
    })
    public void buildStatsSheet_EvasionIsPopulatedCorrectlyForCharactersWhoseQuickGearIncludesShield(
            boolean useQuickGear, String expectedEvasionFormula) {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.RANGER)
                .withLevel(2)
                .withSpeciesStrength("INT")
                .withBonusSkills(List.of("Healing", "Negotiation"))
                .withUseQuickGear(useQuickGear)
                .build();

        var armor = new Armor("Hoplite Shield", "Shield", "+1 Evasion");
        var gear = new Gear(List.of(armor), Collections.emptyList(), MAX_CLASS_COPPER, MAX_CLASS_SILVER, Collections.emptyList());

        var charClass = new CharClass(CharType.WARRIOR.toString(),
                Arrays.asList(1, 2, 3, 4, 5, 6, 7),
                Arrays.asList(10, 11, 12, 13, 14, 15 ,16),
                TEST_LEVEL_1_HP,
                TEST_MAX_HP_AT_LEVEL_UP,
                Collections.emptyList(),
                gear,
                null);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
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
    public void buildStatsSheet_ContainsExpectedDataValidationRulesForSkillAttributes() {
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
    public void buildStatsSheet_CommonerCharactersHaveNoSkillsOrSkillAttributes() {
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
    public void buildStatsSheet_ExtraRowsGeneratedIfNumberOfSkillsExceedsDefaultNumberOfRows() {
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
    public void buildStatsSheet_CommonerCharactersHaveExpectedMoney() {
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
        var copperValue = getCellValueFromSheet(sheet, 1, 8);
        var silverValue = getCellValueFromSheet(sheet, 2, 8);
        var copper = copperValue.getNumberValue();
        var silver = silverValue.getNumberValue();

        assertNotNull(copper);
        assertNotNull(silver);
        assertTrue(copper >= 1);
        assertTrue(silver >= 1);
        assertTrue(copper <= MAX_COMMONER_COPPER);
        assertTrue(silver <= MAX_COMMONER_SILVER);
    }

    @Test
    public void buildStatsSheet_ClassCharactersHaveNoMoneyIfUseQuickGearIsFalse() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MAGE)
                .withLevel(1)
                .withUseQuickGear(false)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var copperValue = getCellValueFromSheet(sheet, 1, 8);
        var silverValue = getCellValueFromSheet(sheet, 2, 8);
        var copper = copperValue.getNumberValue();
        var silver = silverValue.getNumberValue();

        assertEquals(0.0, copper);
        assertEquals(0.0, silver);
    }

    @Test
    public void buildStatsSheet_ClassCharactersHaveExpectedMoneyIfUseQuickGearIsTrue() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MAGE)
                .withLevel(1)
                .withUseQuickGear(true)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var copperValue = getCellValueFromSheet(sheet, 1, 8);
        var silverValue = getCellValueFromSheet(sheet, 2, 8);
        var copper = copperValue.getNumberValue();
        var silver = silverValue.getNumberValue();

        assertNotNull(copper);
        assertNotNull(silver);
        assertTrue(copper >= 1);
        assertTrue(silver >= 1);
        assertTrue(copper <= MAX_CLASS_COPPER);
        assertTrue(silver <= MAX_CLASS_SILVER);
    }

    @Test
    public void buildStatsSheet_SpecialCaseForShamanClassCharacterCopperWorksAsExpected() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.SHAMAN)
                .withLevel(1)
                .withUseQuickGear(true)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var copperValue = getCellValueFromSheet(sheet, 1, 8);
        var copper = copperValue.getNumberValue();

        assertNotNull(copper);
        // Shamans get 2d12 copper, so we expect a range of 2 to 24
        assertTrue(copper >= 2);
        assertTrue(copper <= 24);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void buildStatsSheet_ClassCharactersThatGet0Or1CopperOrSilverGetExpectedMoney(int expectedMoney) {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MAGE)
                .withLevel(1)
                .withUseQuickGear(true)
                .build();

        var gear = new Gear(Collections.emptyList(), Collections.emptyList(),
                expectedMoney, expectedMoney, null);
        var charClass = new CharClass(CharType.WARRIOR.toString(),
                Arrays.asList(1, 2, 3, 4, 5, 6, 7),
                Arrays.asList(10, 11, 12, 13, 14, 15 ,16),
                TEST_LEVEL_1_HP,
                TEST_MAX_HP_AT_LEVEL_UP,
                Collections.emptyList(),
                gear,
                null);
        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var copperValue = getCellValueFromSheet(sheet, 1, 8);
        var silverValue = getCellValueFromSheet(sheet, 2, 8);
        var copper = copperValue.getNumberValue();
        var silver = silverValue.getNumberValue();

        assertNotNull(copper);
        assertNotNull(silver);
        assertEquals(expectedMoney, copper);
        assertEquals(expectedMoney, silver);
    }

    @Test
    public void buildStatsSheet_CommonerCharactersHave1RowForWeaponsAndArmor() {
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
        var armorWeaponsRow1FirstCellValue = getCellValueFromSheet(sheet, 18, 0);
        assertEquals("", armorWeaponsRow1FirstCellValue.getStringValue());

        // The armor and weapons row should be the last row in the sheet,
        // accounting for one empty row
        assertEquals(19, getNumRowsInSheet(sheet));
    }

    @Test
    public void buildStatsSheet_ClassCharactersWithoutQuickGearHave3RowsForWeaponsAndArmor() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MAGE)
                .withLevel(1)
                .withUseQuickGear(false)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var armorWeaponsRow1FirstCellValue = getCellValueFromSheet(sheet, 18, 0);
        assertEquals("", armorWeaponsRow1FirstCellValue.getStringValue());
        var armorWeaponsRow2FirstCellValue = getCellValueFromSheet(sheet, 19, 0);
        assertEquals("", armorWeaponsRow2FirstCellValue.getStringValue());
        var armorWeaponsRow3FirstCellValue = getCellValueFromSheet(sheet, 20, 0);
        assertEquals("", armorWeaponsRow3FirstCellValue.getStringValue());

        // The armor and weapons rows should be the last rows in the sheet,
        // accounting for one empty row
        assertEquals(21, getNumRowsInSheet(sheet));

    }

    @Test
    public void buildStatsSheet_ClassCharactersWithQuickGearHaveExpectedArmorAndWeaponsRows() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MAGE)
                .withLevel(1)
                .withUseQuickGear(true)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        // First row has armor and weapon
        var armorWeaponsRow1ArmorName = getCellValueFromSheet(sheet, 18, 0);
        assertEquals("Leather Armor", armorWeaponsRow1ArmorName.getStringValue());
        var armorWeaponsRow1ArmorType = getCellValueFromSheet(sheet, 18, 1);
        assertEquals("Light", armorWeaponsRow1ArmorType.getStringValue());
        var armorWeaponsRow1ArmorDa = getCellValueFromSheet(sheet, 18, 2);
        assertEquals("3", armorWeaponsRow1ArmorDa.getStringValue());
        var armorWeaponsRow1WeaponName = getCellValueFromSheet(sheet, 18, 4);
        assertEquals("Short Sword", armorWeaponsRow1WeaponName.getStringValue());
        var armorWeaponsRow1WeaponType = getCellValueFromSheet(sheet, 18, 5);
        assertEquals("Melee", armorWeaponsRow1WeaponType.getStringValue());
        var armorWeaponsRow1WeaponDamage = getCellValueFromSheet(sheet, 18, 7);
        assertEquals("1d8", armorWeaponsRow1WeaponDamage.getStringValue());

        // Second row only has a weapon
        var armorWeaponsRow2ArmorName = getCellValueFromSheet(sheet, 19, 0);
        assertEquals("", armorWeaponsRow2ArmorName.getStringValue());
        var armorWeaponsRow2ArmorType = getCellValueFromSheet(sheet, 19, 1);
        assertEquals("", armorWeaponsRow2ArmorType.getStringValue());
        var armorWeaponsRow2ArmorDa = getCellValueFromSheet(sheet, 19, 2);
        assertEquals("", armorWeaponsRow2ArmorDa.getStringValue());
        var armorWeaponsRow2WeaponName = getCellValueFromSheet(sheet, 19, 4);
        assertEquals("Dagger", armorWeaponsRow2WeaponName.getStringValue());
        var armorWeaponsRow2WeaponType = getCellValueFromSheet(sheet, 19, 5);
        assertEquals("Thrown", armorWeaponsRow2WeaponType.getStringValue());
        var armorWeaponsRow2WeaponDamage = getCellValueFromSheet(sheet, 19, 7);
        assertEquals("1d6", armorWeaponsRow2WeaponDamage.getStringValue());

        // The armor and weapons rows should be the last rows in the sheet,
        // accounting for one empty row
        assertEquals(20, getNumRowsInSheet(sheet));
    }

    @ParameterizedTest
    @MethodSource("shamanAndBonusSkillsAndFeatureAttributes")
    public void buildStatsSheet_FeaturesWithAdvorDadvDisplayCorrectly(
            String skillName, FeatureAttributeType featureAttributeType, int rowIndex) {
        // arrange
        var bonusSkills = List.of("Alchemy", "Stealth");
        var featureName = "Test Feature";
        var shamanSkills = List.of("Animal Expertise", "Arcana", "Healing",
                "Nature", "Religion", "Survival");
        var featureAttribute = new FeatureAttribute(featureAttributeType, skillName);
        var feature = new Feature(featureName, List.of(featureAttribute));

        Features features;
        FeaturesRequest featuresRequest;
        float expectedRedColorValue;
        String expectedNote;
        if (featureAttributeType == FeatureAttributeType.ADV) {
            features = new Features(List.of(feature), Collections.emptyList());
            featuresRequest = new FeaturesRequest(List.of(featureName), Collections.emptyList());
            expectedRedColorValue = 0.576f;
            expectedNote = "Roll with Advantage";
        }
        else {
            features = new Features(Collections.emptyList(), List.of(feature));
            featuresRequest = new FeaturesRequest(Collections.emptyList(), List.of(featureName));
            expectedRedColorValue = 0.463f;
            expectedNote = "Roll with Double Advantage";
        }

        var charClass = new CharClass(CharType.SHAMAN.toString(),
                Arrays.asList(1, 2, 3, 4, 5, 6, 7),
                Arrays.asList(10, 11, 12, 13, 14, 15 ,16),
                TEST_LEVEL_1_HP,
                TEST_MAX_HP_AT_LEVEL_UP,
                shamanSkills,
                null,
                features);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.SHAMAN)
                .withLevel(5)
                .withBonusSkills(bonusSkills)
                .withFeatures(featuresRequest)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var skillCellData = getCellDataFromSheet(sheet, rowIndex, 3);
        assertNotNull(skillCellData.getUserEnteredValue());
        assertEquals(skillName, skillCellData.getUserEnteredValue().getStringValue());

        assertNotNull(skillCellData.getUserEnteredFormat());
        assertNotNull(skillCellData.getUserEnteredFormat().getBackgroundColor());
        assertEquals(expectedRedColorValue, skillCellData.getUserEnteredFormat().getBackgroundColor().getRed());

        assertEquals(expectedNote, skillCellData.getNote());
    }

    static Stream<Arguments> shamanAndBonusSkillsAndFeatureAttributes() {
        return Stream.of(
                Arguments.of("Alchemy", FeatureAttributeType.ADV, 8),
                Arguments.of("Alchemy", FeatureAttributeType.DADV, 8),
                Arguments.of("Animal Expertise", FeatureAttributeType.ADV, 9),
                Arguments.of("Animal Expertise", FeatureAttributeType.DADV, 9),
                Arguments.of("Arcana", FeatureAttributeType.ADV, 10),
                Arguments.of("Arcana", FeatureAttributeType.DADV, 10),
                Arguments.of("Healing", FeatureAttributeType.ADV, 11),
                Arguments.of("Healing", FeatureAttributeType.DADV, 11),
                Arguments.of("Nature", FeatureAttributeType.ADV, 12),
                Arguments.of("Nature", FeatureAttributeType.DADV, 12),
                Arguments.of("Religion", FeatureAttributeType.ADV, 13),
                Arguments.of("Religion", FeatureAttributeType.DADV, 13),
                Arguments.of("Stealth", FeatureAttributeType.ADV, 14),
                Arguments.of("Stealth", FeatureAttributeType.DADV, 14),
                Arguments.of("Survival", FeatureAttributeType.ADV, 15),
                Arguments.of("Survival", FeatureAttributeType.DADV, 15)
        );
    }

    @ParameterizedTest
    @MethodSource("attributesFeatureAttributesAndRowIndexes")
    public void buildStatsSheet_AttributesWithAdvorDadvDisplayCorrectly(
            AttributeType attributeType, String expectedCellText,
            FeatureAttributeType featureAttributeType, int rowIndex) {
        // arrange
        var featureName = "Test Feature";
        var featureAttribute = new FeatureAttribute(featureAttributeType, attributeType.name());
        var feature = new Feature(featureName, List.of(featureAttribute));

        Features features;
        FeaturesRequest featuresRequest;
        float expectedRedColorValue;
        String expectedNote;
        if (featureAttributeType == FeatureAttributeType.ADV) {
            features = new Features(List.of(feature), Collections.emptyList());
            featuresRequest = new FeaturesRequest(List.of(featureName), Collections.emptyList());
            expectedRedColorValue = 0.576f;
            expectedNote = "Roll with Advantage";
        }
        else {
            features = new Features(Collections.emptyList(), List.of(feature));
            featuresRequest = new FeaturesRequest(Collections.emptyList(), List.of(featureName));
            expectedRedColorValue = 0.463f;
            expectedNote = "Roll with Double Advantage";
        }

        var charClass = new CharClass(CharType.SHAMAN.toString(),
                Arrays.asList(1, 2, 3, 4, 5, 6, 7),
                Arrays.asList(10, 11, 12, 13, 14, 15 ,16),
                TEST_LEVEL_1_HP,
                TEST_MAX_HP_AT_LEVEL_UP,
                List.of("Arcana"),
                null,
                features);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.SHAMAN)
                .withLevel(5)
                .withFeatures(featuresRequest)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildStatsSheet(request);

        // assert
        var skillCellData = getCellDataFromSheet(sheet, rowIndex, 0);

        assertNotNull(skillCellData.getUserEnteredFormat());
        assertNotNull(skillCellData.getUserEnteredFormat().getBackgroundColor());
        assertEquals(expectedRedColorValue, skillCellData.getUserEnteredFormat().getBackgroundColor().getRed());

        assertEquals(expectedNote, skillCellData.getNote());
    }

    static Stream<Arguments> attributesFeatureAttributesAndRowIndexes() {
        return Stream.of(
                Arguments.of(AttributeType.STR, "Strength (STR)", FeatureAttributeType.ADV, 8),
                Arguments.of(AttributeType.STR, "Strength (STR)", FeatureAttributeType.DADV, 8),
                Arguments.of(AttributeType.COR, "Coordination (COR)", FeatureAttributeType.ADV, 9),
                Arguments.of(AttributeType.COR, "Coordination (COR)", FeatureAttributeType.DADV, 9),
                Arguments.of(AttributeType.STA, "Stamina (STA)", FeatureAttributeType.ADV, 10),
                Arguments.of(AttributeType.STA, "Stamina (STA)", FeatureAttributeType.DADV, 10),
                Arguments.of(AttributeType.INT, "Intellect (INT)", FeatureAttributeType.ADV, 11),
                Arguments.of(AttributeType.INT, "Intellect (INT)", FeatureAttributeType.DADV, 11),
                Arguments.of(AttributeType.PER, "Perception (PER)", FeatureAttributeType.ADV, 12),
                Arguments.of(AttributeType.PER, "Perception (PER)", FeatureAttributeType.DADV, 12),
                Arguments.of(AttributeType.PRS, "Presence (PRS)", FeatureAttributeType.ADV, 13),
                Arguments.of(AttributeType.PRS, "Presence (PRS)", FeatureAttributeType.DADV, 13),
                Arguments.of(AttributeType.LUC, "Luck (LUC)", FeatureAttributeType.ADV, 14),
                Arguments.of(AttributeType.LUC, "Luck (LUC)", FeatureAttributeType.DADV, 14)
        );
    }

    @Test
    public void buildSpellsSheet_BuildsSheetWithExpectedTitle() {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.MAGE)
                .withLevel(1)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildSpellsSheet(request);

        // assert
        assertNotNull(sheet);
        assertNotNull(sheet.getProperties());
        assertEquals("Spells", sheet.getProperties().getTitle());
    }

    @ParameterizedTest
    @MethodSource("expectedSpellSheetRowsByCharClassAndLevel")
    public void buildSpellsSheet_BuildsSheetWithCorrectNumberOfRowsForCharacterClassAndLevel(
            CharType charType, int level, int expectedRows) {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(charType)
                .withLevel(level)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildSpellsSheet(request);

        // assert
        assertEquals(expectedRows, getNumRowsInSheet(sheet));
    }

    @ParameterizedTest
    @EnumSource(value = CharType.class, names = {"MAGE", "SHAMAN"})
    public void buildSpellsSheet_BuildsSheetWithCorrectDataValidationRules(CharType charType) {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(charType)
                .withLevel(1)
                .build();

        var expectedSpellLevelsList = new ArrayList<String>();
        if (charType == CharType.MAGE) {
            expectedSpellLevelsList.add("Cantrip");
        }
        for (var i = 1; i <= 7; i++) {
            expectedSpellLevelsList.add(String.valueOf(i));
        }

        // act
        var sheet = googleSheetBuilderService.buildSpellsSheet(request);

        // assert
        var numRows = getNumRowsInSheet(sheet);
        // Skip header row
        for (var i = 1; i < numRows; i++) {
            var levelCellData = getCellDataFromSheet(sheet, i, 0);
            assertConditionValueListHasAllValuesFromList(levelCellData.getDataValidation(),
                    expectedSpellLevelsList);;
        }
    }

    @Test
    public void buildFeaturesSheet_BuildsSheetWithExpectedTitle() {
        // act
        var request = CharacterCreateRequestBuilder.getBuilder().build();
        var sheet = googleSheetBuilderService.buildFeaturesSheet(request);

        // assert
        assertNotNull(sheet);
        assertNotNull(sheet.getProperties());
        assertEquals("Class/Species Features", sheet.getProperties().getTitle());
    }

    @Test
    public void buildFeaturesSheet_BuildsSheetWithExpectedTier1andTier2PlaceholdersIfCharacterHasNoFeatures() {
        // arrange
        var expectedTier1RedValue = 0.576f;
        var expectedTier2RedValue = 0.463f;
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(1)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildFeaturesSheet(request);

        // assert
        var tier1CellData = getCellDataFromSheet(sheet, 6, 0);
        var tier1CellFormat = tier1CellData.getUserEnteredFormat();
        assertNotNull(tier1CellFormat);
        assertNotNull(tier1CellFormat.getBackgroundColor());
        assertEquals(expectedTier1RedValue, tier1CellFormat.getBackgroundColor().getRed());
        var tier1Value = tier1CellData.getUserEnteredValue();
        assertNotNull(tier1Value);
        assertEquals("Tier I features in this color", tier1Value.getStringValue());

        var tier2CellData = getCellDataFromSheet(sheet, 7, 0);
        var tier2CellFormat = tier2CellData.getUserEnteredFormat();
        assertNotNull(tier2CellFormat);
        assertNotNull(tier2CellFormat.getBackgroundColor());
        assertEquals(expectedTier2RedValue, tier2CellFormat.getBackgroundColor().getRed());
        var tier2Value = tier2CellData.getUserEnteredValue();
        assertNotNull(tier2Value);
        assertEquals("Tier II features in this color", tier2Value.getStringValue());
    }

    @Test
    public void buildFeaturesSheet_BuildsSheetWithExpectedTier2PlaceholdersIfCharacterHasEmptyTier2FeatureList() {
        // arrange
        var expectedTier2RedValue = 0.463f;
        var featuresRequest = new FeaturesRequest(List.of("test"), Collections.emptyList());
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(1)
                .withFeatures(featuresRequest)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildFeaturesSheet(request);

        // assert
        var tier2CellData = getCellDataFromSheet(sheet, 7, 0);
        var tier2CellFormat = tier2CellData.getUserEnteredFormat();
        assertNotNull(tier2CellFormat);
        assertNotNull(tier2CellFormat.getBackgroundColor());
        assertEquals(expectedTier2RedValue, tier2CellFormat.getBackgroundColor().getRed());
        var tier2Value = tier2CellData.getUserEnteredValue();
        assertNotNull(tier2Value);
        assertEquals("Tier II features in this color", tier2Value.getStringValue());
    }

    @Test
    public void buildFeaturesSheet_BuildsSheetWithExpectedTier1andTier2Features() {
        // arrange
        var expectedTier1RedValue = 0.576f;
        var expectedTier2RedValue = 0.463f;
        // Feature names consist of "test" followed by the row index where they appear,
        // to simplify the assertions below
        var featuresRequest = new FeaturesRequest(List.of("test6", "test7", "test8"),
                List.of("test9", "test10", "test11"));
        // The expected values below are based on the number of Tier I and Tier II features
        // specified in the featuresRequest variable
        var expectedTier1FeatureRowsStart = 6;
        var expectedTier2FeatureRowsStart = 9;
        var expectedNumFeatureRows = 3;
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withLevel(4)
                .withFeatures(featuresRequest)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildFeaturesSheet(request);

        // assert
        for (var i = expectedTier1FeatureRowsStart;
             i < expectedTier1FeatureRowsStart + expectedNumFeatureRows;
             i++) {
            var expectedTier1Value = "test" + i;
            var tier1CellData = getCellDataFromSheet(sheet, i, 0);
            var tier1CellFormat = tier1CellData.getUserEnteredFormat();
            assertNotNull(tier1CellFormat);
            assertNotNull(tier1CellFormat.getBackgroundColor());
            assertEquals(expectedTier1RedValue, tier1CellFormat.getBackgroundColor().getRed());
            var tier1Value = tier1CellData.getUserEnteredValue();
            assertNotNull(tier1Value);
            assertEquals(expectedTier1Value, tier1Value.getStringValue());
        }

        for (var j = expectedTier2FeatureRowsStart;
             j < expectedTier2FeatureRowsStart + expectedNumFeatureRows;
             j++) {
            var expectedTier2Value = "test" + j;
            var tier2CellData = getCellDataFromSheet(sheet, j, 0);
            var tier2CellFormat = tier2CellData.getUserEnteredFormat();
            assertNotNull(tier2CellFormat);
            assertNotNull(tier2CellFormat.getBackgroundColor());
            assertEquals(expectedTier2RedValue, tier2CellFormat.getBackgroundColor().getRed());
            var tier2Value = tier2CellData.getUserEnteredValue();
            assertNotNull(tier2Value);
            assertEquals(expectedTier2Value, tier2Value.getStringValue());
        }
    }

    @Test
    public void buildGearSheet_BuildsSheetWithExpectedTitle() {
        // act
        var sheet = googleSheetBuilderService.buildGearSheet(getCharacterCreateRequest(CharType.BERZERKER));

        // assert
        assertNotNull(sheet);
        assertNotNull(sheet.getProperties());
        assertEquals("Gear", sheet.getProperties().getTitle());
    }

    @Test
    public void buildGearSheet_ContainsExpectedItemsAndRowsForCommonerCharacters() {
        // arrange
        var expectedRows = 1 + 2 + 6; // 1 header row, 2 item rows, 6 blank rows for future

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withProfession("Test")
                .withLevel(0)
                .withSpeciesStrength("INT")
                .withSpeciesWeakness("PRS")
                .build();

        // act
        var sheet = googleSheetBuilderService.buildGearSheet(request);

        // assert
        var item1Value = getCellValueFromSheet(sheet, 1, 0);
        assertEquals(ITEM_1_NAME, item1Value.getStringValue());

        var item2Value = getCellValueFromSheet(sheet, 2, 0);
        assertEquals(ITEM_2_NAME, item2Value.getStringValue());

        assertEquals(expectedRows, getNumRowsInSheet(sheet));
    }

    @Test
    public void buildGearSheet_ContainsExpectedItemsAndRowsForClassCharactersWithQuickGear() {
        // arrange
        var expectedRows = 1 + 6 + 6; // 1 header row, 6 item rows, 6 blank rows for future

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MAGE)
                .withLevel(1)
                .withUseQuickGear(true)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildGearSheet(request);

        // assert
        var item1Value = getCellValueFromSheet(sheet, 1, 0);
        assertEquals(ITEM_1_NAME, item1Value.getStringValue());

        var item2Value = getCellValueFromSheet(sheet, 2, 0);
        assertEquals(ITEM_2_NAME, item2Value.getStringValue());

        var item3Value = getCellValueFromSheet(sheet, 3, 0);
        assertEquals(ITEM_3_NAME, item3Value.getStringValue());

        var item4Value = getCellValueFromSheet(sheet, 4, 0);
        assertEquals(ITEM_4_NAME, item4Value.getStringValue());

        var item5Value = getCellValueFromSheet(sheet, 5, 0);
        assertEquals(ITEM_5_NAME, item5Value.getStringValue());

        var item6Value = getCellValueFromSheet(sheet, 6, 0);
        assertEquals(ITEM_6_NAME, item6Value.getStringValue());

        assertEquals(expectedRows, getNumRowsInSheet(sheet));
    }

    @Test
    public void buildGearSheet_ContainsExpectedItemsAndRowsForClassCharactersWithoutQuickGear() {
        // arrange
        var expectedRows = 1 + 10; // 1 header row, 10 blank rows for future

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(RandomStringUtils.randomAlphabetic(10))
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MAGE)
                .withLevel(1)
                .withUseQuickGear(false)
                .build();

        // act
        var sheet = googleSheetBuilderService.buildGearSheet(request);

        // assert

        // Without quick gear, we expect 10 rows with empty values
        for (var i = 1; i < 11; i++) {
            var itemValue = getCellValueFromSheet(sheet, i, 0);
            assertEquals("", itemValue.getStringValue());
        }

        assertEquals(expectedRows, getNumRowsInSheet(sheet));
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

    private int getNumRowsInSheet(Sheet sheet) {
        assertNotNull(sheet);
        assertNotNull(sheet.getData());
        assertNotNull(sheet.getData().getFirst());
        assertNotNull(sheet.getData().getFirst().getRowData());

        return sheet.getData().getFirst().getRowData().size();
    }

    static Stream<Arguments> getMagicUsersAndExpectedAttributeCells() {
        return Stream.of(
                Arguments.arguments(CharType.MAGE, "B12"),
                Arguments.arguments(CharType.SHAMAN, "B14")
        );
    }

    static Stream<Arguments> expectedSpellSheetRowsByCharClassAndLevel() {
        return Stream.of(
                Arguments.arguments(CharType.MAGE, 1, 6),
                Arguments.arguments(CharType.MAGE, 2, 9),
                Arguments.arguments(CharType.MAGE, 3, 12),
                Arguments.arguments(CharType.MAGE, 4, 15),
                Arguments.arguments(CharType.MAGE, 5, 18),
                Arguments.arguments(CharType.MAGE, 6, 21),
                Arguments.arguments(CharType.MAGE, 7, 24),
                Arguments.arguments(CharType.SHAMAN, 1, 4),
                Arguments.arguments(CharType.SHAMAN, 2, 7),
                Arguments.arguments(CharType.SHAMAN, 3, 10),
                Arguments.arguments(CharType.SHAMAN, 4, 13),
                Arguments.arguments(CharType.SHAMAN, 5, 16),
                Arguments.arguments(CharType.SHAMAN, 6, 19),
                Arguments.arguments(CharType.SHAMAN, 7, 22)
        );
    }
}
