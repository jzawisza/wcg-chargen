package com.wcg.chargen.backend.util;

import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.DimensionProperties;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.Sheet;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class GoogleSheetsUtilTest {
    @Test
    public void RowBuilder_AllAddMethodsCorrectlyAddCellToRow() {
        // act
        var rowData = new GoogleSheetsUtil.RowBuilder()
                .addHeaderCell("")
                .addSecondaryHeaderCell("")
                .addCellWithText("")
                .addCellWithNumber(0.0)
                .addCellWithFormula("")
                .addHighlightedCellWithText("")
                .addBaseFeatureCell("")
                .addTier1FeatureCell("")
                .addTier2FeatureCell("")
                .addEmptyCell()
                .build();

        // assert
        assertNotNull(rowData);
        assertNotNull(rowData.getValues());
        assertEquals(10, rowData.getValues().size());
    }

    @Test
    public void RowBuilder_AllMethodsThatAddNonEmptyCellSetFontFamilyToGeorgia() {
        // act
        var rowData = new GoogleSheetsUtil.RowBuilder()
                .addHeaderCell("")
                .addSecondaryHeaderCell("")
                .addCellWithText("")
                .addCellWithNumber(0.0)
                .addCellWithFormula("")
                .addHighlightedCellWithText("")
                .addBaseFeatureCell("")
                .addTier1FeatureCell("")
                .addTier2FeatureCell("")
                .build();

        // assert
        assertNotNull(rowData);
        assertNotNull(rowData.getValues());

        for (var cell : rowData.getValues()) {
            var format = cell.getUserEnteredFormat();
            assertNotNull(format);
            assertNotNull(format.getTextFormat());
            assertEquals("Georgia", format.getTextFormat().getFontFamily());
        }
    }

    @Test
    public void RowBuilder_HeaderCellsDisplayBoldTextWithWordWrap() {
        // act
        var rowData = new GoogleSheetsUtil.RowBuilder()
                .addHeaderCell("")
                .addSecondaryHeaderCell("")
                .build();

        // assert
        assertNotNull(rowData);
        assertNotNull(rowData.getValues());

        for (var cell : rowData.getValues()) {
            var format = cell.getUserEnteredFormat();
            assertNotNull(format);
            assertNotNull(format.getTextFormat());
            assertTrue(format.getTextFormat().getBold());
            assertEquals("WRAP", format.getWrapStrategy());
        }
    }

    @Test
    public void RowBuilder_NonHeaderCellsDisplayNonBoldTextWithWordWrap() {
        // act
        var rowData = new GoogleSheetsUtil.RowBuilder()
                .addCellWithText("")
                .addCellWithNumber(0.0)
                .addCellWithFormula("")
                .addHighlightedCellWithText("")
                .addBaseFeatureCell("")
                .addTier1FeatureCell("")
                .addTier2FeatureCell("")
                .build();

        // assert
        assertNotNull(rowData);
        assertNotNull(rowData.getValues());

        for (var cell : rowData.getValues()) {
            var format = cell.getUserEnteredFormat();
            assertNotNull(format);
            assertNotNull(format.getTextFormat());
            assertNull(format.getTextFormat().getBold());
            assertEquals("WRAP", format.getWrapStrategy());
        }
    }

    @Test
    public void RowBuilder_PrimaryHeaderCellsHaveTopBottomBorders() {
        // act
        var rowData = new GoogleSheetsUtil.RowBuilder()
                .addHeaderCell("")
                .build();

        // assert
        assertNotNull(rowData);
        assertNotNull(rowData.getValues());
        assertEquals(1, rowData.getValues().size());

        var headerCellFormat = rowData.getValues().getFirst().getUserEnteredFormat();
        assertNotNull(headerCellFormat);
        assertNotNull(headerCellFormat.getBorders());
        assertNotNull(headerCellFormat.getBorders().getTop());
        assertNotNull(headerCellFormat.getBorders().getBottom());
        assertNull(headerCellFormat.getBorders().getLeft());
        assertNull(headerCellFormat.getBorders().getRight());
    }

    @Test public void RowBuilder_NonPrimaryHeaderCellsHaveAllBorders() {
        // act
        var rowData = new GoogleSheetsUtil.RowBuilder()
                .addSecondaryHeaderCell("")
                .addCellWithText("")
                .addCellWithNumber(0.0)
                .addCellWithFormula("")
                .addHighlightedCellWithText("")
                .addBaseFeatureCell("")
                .addTier1FeatureCell("")
                .addTier2FeatureCell("")
                .build();

        // assert
        assertNotNull(rowData);
        assertNotNull(rowData.getValues());

        for (var cell : rowData.getValues()) {
            var cellFormat = cell.getUserEnteredFormat();
            assertNotNull(cellFormat);
            assertNotNull(cellFormat.getBorders());
            assertNotNull(cellFormat.getBorders().getTop());
            assertNotNull(cellFormat.getBorders().getBottom());
            assertNotNull(cellFormat.getBorders().getLeft());
            assertNotNull(cellFormat.getBorders().getRight());
        }
    }

    @Test
    public void RowBuilder_CellsWithBackgroundColorsSetColorsCorrectly() {
        // act
        var rowData = new GoogleSheetsUtil.RowBuilder()
                .addHeaderCell("")
                .addSecondaryHeaderCell("")
                .addHighlightedCellWithText("")
                .addBaseFeatureCell("")
                .addTier1FeatureCell("")
                .addTier2FeatureCell("")
                .build();

        // assert
        assertNotNull(rowData);
        assertNotNull(rowData.getValues());
        assertEquals(6, rowData.getValues().size());

        assertColorValue(rowData.getValues().get(0), 0.918f);
        assertColorValue(rowData.getValues().get(1), 0.886f);
        assertColorValue(rowData.getValues().get(2), 0.949f);
        assertColorValue(rowData.getValues().get(3), 0.898f);
        assertColorValue(rowData.getValues().get(4), 0.769f);
        assertColorValue(rowData.getValues().get(5), 0.647f);
    }

    private void assertColorValue(CellData cell, float greenValue) {
        assertNotNull(cell.getUserEnteredFormat());
        assertNotNull(cell.getUserEnteredFormat().getBackgroundColor());
        assertEquals(greenValue, cell.getUserEnteredFormat().getBackgroundColor().getGreen());
    }

    @Test
    public void RowBuilder_CellsWithNoBackgroundColorsHaveNoColorSet() {
        // act
        var rowData = new GoogleSheetsUtil.RowBuilder()
                .addCellWithText("")
                .addCellWithNumber(0.0)
                .addCellWithFormula("")
                .build();

        // assert
        assertNotNull(rowData);
        assertNotNull(rowData.getValues());

        for (var cell : rowData.getValues()) {
            var cellFormat = cell.getUserEnteredFormat();
            assertNull(cellFormat.getBackgroundColor());
        }
    }

    @Test
    public void RowBuilder_EmptyCellsHaveNoFormat() {
        // act
        var rowData = new GoogleSheetsUtil.RowBuilder()
                .addEmptyCell()
                .build();

        // assert
        assertNotNull(rowData);
        assertNotNull(rowData.getValues());
        assertNotNull(rowData.getValues().getFirst());
        assertNull(rowData.getValues().getFirst().getUserEnteredFormat());
    }

    @Test
    public void GridBuilder_AllAddMethodsCorrectlyAddRowToGrid() {
        // arrange
        var row = new GoogleSheetsUtil.RowBuilder()
                .addHeaderCell("Test")
                .build();

        // act
        var grid = new GoogleSheetsUtil.GridBuilder()
                .addRow(row)
                .addEmptyRow()
                .build();

        assertNotNull(grid);
        assertNotNull(grid.getRowData());
        assertEquals(2, grid.getRowData().size());
    }

    @Test
    public void GridBuilder_SettingColumnWidthHasNoEffectIfWithNumColumnsNotCalled() {
        // act
        var grid = new GoogleSheetsUtil.GridBuilder()
                .setColumnWidth(1, 200)
                .build();

        // assert
        assertNotNull(grid);
        assertNotNull(grid.getRowData());
        assertNotNull(grid.getColumnMetadata());
        assertTrue(grid.getColumnMetadata().isEmpty());
    }

    @Test
    public void GridBuilder_SettingColumnWidthDoesNotModifyColumnPropertiesIfColumnNumberIsGreaterThanTotalNumberOfColumns() {
        // act
        var grid = new GoogleSheetsUtil.GridBuilder()
                .withNumColumns(2)
                .setColumnWidth(3, 200)
                .build();

        // assert
        assertNotNull(grid);
        assertNotNull(grid.getRowData());
        assertNotNull(grid.getColumnMetadata());

        for (var dimensionProps: grid.getColumnMetadata()) {
            assertNull(dimensionProps.getPixelSize());
        }
    }

    @Test
    public void GridBuilder_SettingColumnWidthDoesNotModifyColumnPropertiesIfColumnNumberIsNegative() {
        // act
        var grid = new GoogleSheetsUtil.GridBuilder()
                .withNumColumns(2)
                .setColumnWidth(-1, 200)
                .build();

        // assert
        assertNotNull(grid);
        assertNotNull(grid.getRowData());
        assertNotNull(grid.getColumnMetadata());

        for (var dimensionProps: grid.getColumnMetadata()) {
            assertNull(dimensionProps.getPixelSize());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1 })
    public void GridBuilder_SettingColumnWidthDoesNotModifyColumnPropertiesIfColumnWidthIsZeroOrNegative(int width) {
        // act
        var grid = new GoogleSheetsUtil.GridBuilder()
                .withNumColumns(2)
                .setColumnWidth(1, width)
                .build();

        // assert
        assertNotNull(grid);
        assertNotNull(grid.getRowData());
        assertNotNull(grid.getColumnMetadata());

        for (var dimensionProps: grid.getColumnMetadata()) {
            assertNull(dimensionProps.getPixelSize());
        }
    }

    public void GridBuilder_SettingColumnWidthWorksAsExpectedWithColumnNumberLessThanTotalColumnsAndPositiveWidth() {
        // act
        var grid = new GoogleSheetsUtil.GridBuilder()
                .withNumColumns(4)
                .setColumnWidth(0, 200)
                .setColumnWidth(2, 300)
                .setColumnWidth(3, 400)
                .build();

        // assert
        assertNotNull(grid);
        assertNotNull(grid.getRowData());
        assertNotNull(grid.getColumnMetadata());
        assertEquals(4, grid.getColumnMetadata().size());

        assertWidth(grid.getColumnMetadata().get(0), 200);
        assertNull(grid.getColumnMetadata().get(1).getPixelSize());
        assertWidth(grid.getColumnMetadata().get(2), 300);
        assertWidth(grid.getColumnMetadata().get(3), 400);
    }

    private void assertWidth(DimensionProperties dimensionProps, int expectedWidth) {
        assertNotNull(dimensionProps.getPixelSize());
        assertEquals(expectedWidth, dimensionProps.getPixelSize());
    }

    @Test
    public void buildStatsSheet_BuildsSheetWithExpectedTitle() {
        // arrange
        var request = getCharacterCreateRequest(null);

        // act
        var sheet = GoogleSheetsUtil.buildStatsSheet(request);

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
        var sheet = GoogleSheetsUtil.buildStatsSheet(request);

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
            var sheet = GoogleSheetsUtil.buildStatsSheet(request);

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
    public void buildSpellsSheet_BuildsSheetWithExpectedTitle() {
        // act
        var sheet = GoogleSheetsUtil.buildSpellsSheet();

        // assert
        assertNotNull(sheet);
        assertNotNull(sheet.getProperties());
        assertEquals("Spells", sheet.getProperties().getTitle());
    }

    @Test
    public void buildFeaturesSheet_BuildsSheetWithExpectedTitle() {
        // act
        var sheet = GoogleSheetsUtil.buildFeaturesSheet();

        // assert
        assertNotNull(sheet);
        assertNotNull(sheet.getProperties());
        assertEquals("Class/Species Features", sheet.getProperties().getTitle());
    }

    @Test
    public void buildGearSheet_BuildsSheetWithExpectedTitle() {
        // act
        var sheet = GoogleSheetsUtil.buildGearSheet();

        // assert
        assertNotNull(sheet);
        assertNotNull(sheet.getProperties());
        assertEquals("Gear", sheet.getProperties().getTitle());
    }

    private CharacterCreateRequest getCharacterCreateRequest(CharType charType) {
        if (charType == null) {
            // Randomly chosen character type for scenarios where this doesn't matter
            charType = CharType.BERZERKER;
        }

        return new CharacterCreateRequest("Test", charType, SpeciesType.HUMAN);
    }

    private ExtendedValue getCellValueFromSheet(Sheet sheet, int rowIndex, int colIndex) {
        assertNotNull(sheet);
        assertNotNull(sheet.getData());
        assertNotNull(sheet.getData().getFirst());
        assertNotNull(sheet.getData().getFirst().getRowData());
        assertTrue(sheet.getData().getFirst().getRowData().size() >= rowIndex);

        var row = sheet.getData().getFirst().getRowData().get(rowIndex);
        assertNotNull(row);
        assertNotNull(row.getValues());
        assertNotNull(row.getValues().get(colIndex));

        var cellValue = row.getValues().get(colIndex).getUserEnteredValue();
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
