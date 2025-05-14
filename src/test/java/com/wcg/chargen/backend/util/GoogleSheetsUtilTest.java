package com.wcg.chargen.backend.util;

import com.google.api.services.sheets.v4.model.*;
import com.wcg.chargen.backend.enums.AttributeType;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.wcg.chargen.backend.util.GoogleSheetsUtil.GridBuilder.getGridBuilder;
import static com.wcg.chargen.backend.util.GoogleSheetsUtil.RowBuilder.getRowBuilder;
import static org.junit.jupiter.api.Assertions.*;

public class GoogleSheetsUtilTest {
    @Test
    public void RowBuilder_AllAddMethodsCorrectlyAddCellToRow() {
        // act
        var rowData = getRowBuilder()
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
        var rowData = getRowBuilder()
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
        var rowData = getRowBuilder()
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
        var rowData = getRowBuilder()
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
        var rowData = getRowBuilder()
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
        var rowData = getRowBuilder()
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
        var rowData = getRowBuilder()
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
        var rowData = getRowBuilder()
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
        var rowData = getRowBuilder()
                .addEmptyCell()
                .build();

        // assert
        assertNotNull(rowData);
        assertNotNull(rowData.getValues());
        assertNotNull(rowData.getValues().getFirst());
        assertNull(rowData.getValues().getFirst().getUserEnteredFormat());
    }

    @Test
    public void RowBuilder_AddCellWithAttributeValueCreatesNumberCellIfAttributeIsNotSpeciesStrengthOrWeakness() {
        // arrange
        var characterCreateRequest = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName("Test")
                .withCharacterType(CharType.WARRIOR)
                .withSpeciesType(SpeciesType.DWARF)
                .withLevel(2)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STA")
                .withSpeciesWeakness("PER")
                .build();

        // act
        var rowData = getRowBuilder()
                .addCellWithAttributeValue(characterCreateRequest, AttributeType.LUC)
                .build();

        // assert
        assertNotNull(rowData);
        assertNotNull(rowData.getValues());
        assertNotNull(rowData.getValues().getFirst());
        assertNotNull(rowData.getValues().getFirst().getUserEnteredValue());
        assertEquals(-2, rowData.getValues().getFirst().getUserEnteredValue().getNumberValue());
    }

    @Test
    public void RowBuilder_AddCellWithAttributeValueCreatesFormulaCellIfAttributeIsSpeciesStrength() {
        // arrange
        var expectedFormula = "=SUM(1,1)";
        var characterCreateRequest = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName("Test")
                .withCharacterType(CharType.WARRIOR)
                .withSpeciesType(SpeciesType.DWARF)
                .withLevel(2)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STA")
                .withSpeciesWeakness("PER")
                .build();

        // act
        var rowData = getRowBuilder()
                .addCellWithAttributeValue(characterCreateRequest, AttributeType.STA)
                .build();

        // assert
        assertNotNull(rowData);
        assertNotNull(rowData.getValues());
        assertNotNull(rowData.getValues().getFirst());
        assertNotNull(rowData.getValues().getFirst().getUserEnteredValue());
        assertEquals(expectedFormula, rowData.getValues().getFirst().getUserEnteredValue().getFormulaValue());
    }

    @Test
    public void RowBuilder_AddCellWithAttributeValueCreatesFormulaCellIfAttributeIsSpeciesWeakness() {
        // arrange
        var expectedFormula = "=SUM(0,-1)";
        var characterCreateRequest = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName("Test")
                .withCharacterType(CharType.WARRIOR)
                .withSpeciesType(SpeciesType.DWARF)
                .withLevel(2)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STA")
                .withSpeciesWeakness("PER")
                .build();

        // act
        var rowData = getRowBuilder()
                .addCellWithAttributeValue(characterCreateRequest, AttributeType.PER)
                .build();

        // assert
        assertNotNull(rowData);
        assertNotNull(rowData.getValues());
        assertNotNull(rowData.getValues().getFirst());
        assertNotNull(rowData.getValues().getFirst().getUserEnteredValue());
        assertEquals(expectedFormula, rowData.getValues().getFirst().getUserEnteredValue().getFormulaValue());
    }

    @Test
    public void GridBuilder_AllAddMethodsCorrectlyAddRowToGrid() {
        // arrange
        var row = getRowBuilder()
                .addHeaderCell("Test")
                .build();

        // act
        var grid = getGridBuilder()
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
        var grid = getGridBuilder()
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
        var grid = getGridBuilder()
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
        var grid = getGridBuilder()
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
        var grid = getGridBuilder()
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

    @Test
    public void GridBuilder_SettingColumnWidthWorksAsExpectedWithColumnNumberLessThanTotalColumnsAndPositiveWidth() {
        // act
        var grid = getGridBuilder()
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
}
