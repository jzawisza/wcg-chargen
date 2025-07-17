package com.wcg.chargen.backend.util;

import com.google.api.services.sheets.v4.model.*;
import com.wcg.chargen.backend.enums.AttributeType;
import com.wcg.chargen.backend.enums.FeatureAttributeType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Utility methods for building rows and columns of a Google Sheets spreadsheet.
 */
public class GoogleSheetsUtil {
    // Color values are derived from Google Sheets color picker: for more information, see
    // https://spreadsheet.dev/how-to-get-the-hexadecimal-codes-of-colors-in-google-sheets
    private static final Color LIGHT_GREEN_3 = new Color().setRed(0.851f).setGreen(0.918f).setBlue(0.827f);
    private static final Color LIGHT_BLUE_3 = new Color().setRed(0.812f).setGreen(0.886f).setBlue(0.953f);
    private static final Color LIGHT_YELLOW_3 = new Color().setRed(1.0f).setGreen(0.949f).setBlue(0.8f);
    private static final Color LIGHT_YELLOW_2 = new Color().setRed(1.0f).setGreen(0.898f).setBlue(0.6f);
    private static final Color LIGHT_GREEN_1 = new Color().setRed(0.576f).setGreen(0.769f).setBlue(0.49f);
    private static final Color LIGHT_CYAN_1 = new Color().setRed(0.463f).setGreen(0.647f).setBlue(0.686f);

    private static final TextFormat COMMON_TEXT_FORMAT_REGULAR = new TextFormat().setFontFamily("Georgia");
    private static final TextFormat COMMON_TEXT_FORMAT_BOLD = new TextFormat().setFontFamily("Georgia").setBold(true);

    private static final Borders ALL_BORDERS = new Borders()
            .setTop(new Border().setStyle("SOLID"))
            .setBottom(new Border().setStyle("SOLID"))
            .setLeft(new Border().setStyle("SOLID"))
            .setRight(new Border().setStyle("SOLID"));
    private static final Borders TOP_BOTTOM_BORDERS = new Borders()
            .setTop(new Border().setStyle("SOLID"))
            .setBottom(new Border().setStyle("SOLID"));

    private static final String WRAP_TEXT = "WRAP";

    public static class RowBuilder {
        private final RowData row;
        private final List<CellData> rowCells;
        private RowBuilder() {
            row = new RowData();
            rowCells = new ArrayList<>();
        }

        public static RowBuilder getRowBuilder() {
            return new RowBuilder();
        }

        public RowBuilder addHeaderCell(String cellText) {
            var cellFormat = new CellFormat().setBackgroundColor(LIGHT_GREEN_3);
            cellFormat.setBorders(TOP_BOTTOM_BORDERS);
            cellFormat.setTextFormat(COMMON_TEXT_FORMAT_BOLD);
            cellFormat.setWrapStrategy(WRAP_TEXT);

            addCellToList(new ExtendedValue().setStringValue(cellText),
                    cellFormat, null, null);

            return this;
        }

        public RowBuilder addSecondaryHeaderCell(String cellText) {
            var cellFormat = new CellFormat().setBackgroundColor(LIGHT_BLUE_3);
            cellFormat.setBorders(ALL_BORDERS);
            cellFormat.setTextFormat(COMMON_TEXT_FORMAT_BOLD);
            cellFormat.setWrapStrategy(WRAP_TEXT);

            addCellToList(new ExtendedValue().setStringValue(cellText),
                    cellFormat,null, null);

            return this;
        }

        public RowBuilder addCellWithText(String cellText) {
            addCellToList(new ExtendedValue().setStringValue(cellText),
                    null, null, null);

            return this;
        }

        public RowBuilder addCellWithText(String cellText, DataValidationRule dataValidationRule) {
            addCellToList(new ExtendedValue().setStringValue(cellText),
                    null, dataValidationRule, null);

            return this;
        }

        public RowBuilder addCellWithText(String cellText, FeatureAttributeType featureAttributeType) {
            return addCellWithFeatureAttributeType(cellText,
                    featureAttributeType,
                    this::addCellWithText,
                    text -> new ExtendedValue().setStringValue(text));
        }

        public RowBuilder addCellWithNumber(double cellNumber) {
            addCellToList(new ExtendedValue().setNumberValue(cellNumber),
                    null, null, null);

            return this;
        }

        public RowBuilder addCellWithNumber(double cellNumber, DataValidationRule dataValidationRule) {
            addCellToList(new ExtendedValue().setNumberValue(cellNumber),
                    null, dataValidationRule, null);

            return this;
        }

        public RowBuilder addCellWithFormula(String formula) {
            addCellToList(new ExtendedValue().setFormulaValue(formula),
                    null, null, null);

            return this;
        }

        public RowBuilder addCellWithFormula(String formula, FeatureAttributeType featureAttributeType) {
            return addCellWithFeatureAttributeType(formula,
                    featureAttributeType,
                    this::addCellWithFormula,
                    text -> new ExtendedValue().setFormulaValue(text));
        }

        public RowBuilder addCellWithAttributeValue(CharacterCreateRequest request,
                                                    AttributeType attributeType) {
            var attributeTypeStr = attributeType.toString();
            var attributeValue = request.attributes().get(attributeTypeStr);
            if (attributeTypeStr.equals(request.speciesStrength())) {
                var formula = String.format("=SUM(%d,1)", attributeValue);
                addCellWithFormula(formula);
            }
            else if (attributeTypeStr.equals(request.speciesWeakness())) {
                var formula = String.format("=SUM(%d,-1)", attributeValue);
                addCellWithFormula(formula);
            }
            else {
                addCellWithNumber((double)attributeValue);
            }

            return this;
        }

        public RowBuilder addHighlightedCellWithText(String cellText) {
            addCellToList(new ExtendedValue().setStringValue(cellText),
                    getCellFormatWithColor(LIGHT_YELLOW_3),
                    null, null);

            return this;
        }

        public RowBuilder addHighlightedCellWithText(String cellText, FeatureAttributeType featureAttributeType) {
            return addCellWithFeatureAttributeType(cellText,
                    featureAttributeType,
                    this::addHighlightedCellWithText,
                    text -> new ExtendedValue().setStringValue(text));
        }

        private RowBuilder addCellWithFeatureAttributeType(String cellText,
                                                           FeatureAttributeType featureAttributeType,
                                                           Function<String, RowBuilder> noFeatureAttributeFunc,
                                                           Function<String, ExtendedValue> cellValueFunc) {
            if (featureAttributeType == null) {
                return noFeatureAttributeFunc.apply(cellText);
            }

            switch (featureAttributeType) {
                case ADV:
                    addCellToList(cellValueFunc.apply(cellText),
                            getCellFormatWithColor(LIGHT_GREEN_1),
                            null, "Roll with Advantage");
                    break;
                case DADV:
                    addCellToList(cellValueFunc.apply(cellText),
                            getCellFormatWithColor(LIGHT_CYAN_1),
                            null, "Roll with Double Advantage");
                    break;
                default:
                    return noFeatureAttributeFunc.apply(cellText);
            }

            return this;
        }

        public RowBuilder addBaseFeatureCell(String cellText) {
            addCellToList(new ExtendedValue().setStringValue(cellText),
                    getCellFormatWithColor(LIGHT_YELLOW_2),
                    null, null);

            return this;
        }

        public RowBuilder addTier1FeatureCell(String cellText) {
            addCellToList(new ExtendedValue().setStringValue(cellText),
                    getCellFormatWithColor(LIGHT_GREEN_1),
                    null, null);

            return this;
        }

        public RowBuilder addTier2FeatureCell(String cellText) {
            addCellToList(new ExtendedValue().setStringValue(cellText),
                    getCellFormatWithColor(LIGHT_CYAN_1),
                    null, null);

            return this;
        }

        public RowBuilder addEmptyCell() {
            rowCells.add(new CellData());

            return this;
        }

        private CellFormat getCellFormatWithColor(Color color) {
            var cellFormat = new CellFormat();
            cellFormat.setBackgroundColor(color);
            cellFormat.setBorders(ALL_BORDERS);
            cellFormat.setTextFormat(COMMON_TEXT_FORMAT_REGULAR);
            cellFormat.setWrapStrategy(WRAP_TEXT);

            return cellFormat;
        }

        private void addCellToList(ExtendedValue cellValue, CellFormat cellFormat,
                                   DataValidationRule dataValidationRule, String note) {
            var newCell = new CellData().setUserEnteredValue(cellValue);

            CellFormat formatToApply;
            if (cellFormat == null) {
                formatToApply = new CellFormat();
                // Default to all borders and Georgia font for cells with no other particular format
                formatToApply.setBorders(ALL_BORDERS);
                formatToApply.setTextFormat(COMMON_TEXT_FORMAT_REGULAR);
                formatToApply.setWrapStrategy(WRAP_TEXT);
            }
            else {
                formatToApply = cellFormat;
            }

            newCell.setUserEnteredFormat(formatToApply);
            newCell.setDataValidation(dataValidationRule);
            newCell.setNote(note);

            rowCells.add(newCell);
        }

        public RowData build() {
            row.setValues(rowCells);

            return row;
        }

    }

    public static class GridBuilder {
        private final GridData gridData;
        private final List<RowData> rowDataList;
        private int numColumns;
        private int[] columnWidths;

        private GridBuilder() {
            gridData = new GridData();
            rowDataList = new ArrayList<>();
            numColumns = 0;
        }

        public static GridBuilder getGridBuilder() {
            return new GridBuilder();
        }

        public GridBuilder withNumColumns(int numColumns) {
            this.numColumns = numColumns;
            columnWidths = new int[numColumns];

            return this;
        }

        public GridBuilder addRow(RowData row) {
            rowDataList.add(row);

            return this;
        }

        public GridBuilder addEmptyRow() {
            rowDataList.add(new RowData());

            return this;
        }

        /**
         * Set the width of a specified column.
         * If the column specified is greater than the number of columns,
         * this method does nothing.
         *
         * @param column 0-based column index
         * @param width Width of column in pixels
         * @return Builder instance
         */
        public GridBuilder setColumnWidth(int column, int width) {
            if (column >= 0 && column < numColumns && width > 0) {
                columnWidths[column] = width;
            }

            return this;
        }

        public GridData build() {
            List<DimensionProperties> columnMetadataList = new ArrayList<>();
            for (int i = 0; i < numColumns; i++) {
                var dimensionProperties = new DimensionProperties();
                if (columnWidths[i] > 0) {
                    dimensionProperties.setPixelSize(columnWidths[i]);
                }
                columnMetadataList.add(dimensionProperties);
            }
            gridData.setColumnMetadata(columnMetadataList);

            gridData.setRowData(rowDataList);

            return gridData;
        }
    }
}
