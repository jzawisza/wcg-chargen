package com.wcg.chargen.backend.service.impl;

import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.DataValidationRule;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.Sheet;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.*;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.CommonerService;
import com.wcg.chargen.backend.service.GoogleSheetBuilderService;
import com.wcg.chargen.backend.service.ProfessionsService;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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

    private static final String PROFESSION_1_NAME = "Profession1";
    private static final String PROFESSION_2_NAME = "Profession2";
    private static final String PROFESSION_3_NAME = "Profession3";
    @BeforeEach
    public void beforeTest() {
        var profession1 = new Profession(PROFESSION_1_NAME, 0, 3);
        var profession2 = new Profession(PROFESSION_2_NAME, 4, 8);
        var profession3 = new Profession(PROFESSION_3_NAME, 9, 11);
        var professions = new Professions(Arrays.asList(profession1, profession2, profession3));

        var charClass = new CharClass(CharType.WARRIOR.toString(),
                Arrays.asList(1, 2, 3, 4, 5, 6, 7),
                Arrays.asList(10, 11, 12, 13, 14, 15 ,16),
                8,
                4,
                null,
                null);

        var commonerInfo = new Commoner(0, 10);

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
