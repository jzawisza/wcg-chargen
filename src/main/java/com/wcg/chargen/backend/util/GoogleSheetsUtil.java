package com.wcg.chargen.backend.util;

import com.google.api.services.sheets.v4.model.*;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateInfo;
import com.wcg.chargen.backend.model.CharacterCreateRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleSheetsUtil {
    private static final String STATS_SHEET_TITLE = "Stats";
    private static final String SPELLS_SHEET_TITLE = "Spells";
    private static final String FEATURES_SHEET_TITLE = "Class/Species Features";
    private static final String GEAR_SHEET_TITLE = "Gear";

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

    private static final int NUM_GEAR_ROWS = 10;

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
        private RowData row;
        private List<CellData> rowCells;
        public RowBuilder() {
            row = new RowData();
            rowCells = new ArrayList<>();
        }

        public RowBuilder addHeaderCell(String cellText) {
            var cellFormat = new CellFormat().setBackgroundColor(LIGHT_GREEN_3);
            cellFormat.setBorders(TOP_BOTTOM_BORDERS);
            cellFormat.setTextFormat(COMMON_TEXT_FORMAT_BOLD);
            cellFormat.setWrapStrategy(WRAP_TEXT);

            addCellToList(new ExtendedValue().setStringValue(cellText), cellFormat, null);

            return this;
        }

        public RowBuilder addSecondaryHeaderCell(String cellText) {
            var cellFormat = new CellFormat().setBackgroundColor(LIGHT_BLUE_3);
            cellFormat.setBorders(ALL_BORDERS);
            cellFormat.setTextFormat(COMMON_TEXT_FORMAT_BOLD);
            cellFormat.setWrapStrategy(WRAP_TEXT);

            addCellToList(new ExtendedValue().setStringValue(cellText), cellFormat, null);

            return this;
        }

        public RowBuilder addCellWithText(String cellText) {
            addCellToList(new ExtendedValue().setStringValue(cellText), null, null);

            return this;
        }

        public RowBuilder addCellWithText(String cellText, DataValidationRule dataValidationRule) {
            addCellToList(new ExtendedValue().setStringValue(cellText), null, dataValidationRule);

            return this;
        }

        public RowBuilder addCellWithNumber(double cellNumber) {
            addCellToList(new ExtendedValue().setNumberValue(cellNumber), null, null);

            return this;
        }

        public RowBuilder addCellWithNumber(double cellNumber, DataValidationRule dataValidationRule) {
            addCellToList(new ExtendedValue().setNumberValue(cellNumber), null, dataValidationRule);

            return this;
        }

        public RowBuilder addCellWithFormula(String formula) {
            addCellToList(new ExtendedValue().setFormulaValue(formula), null, null);

            return this;
        }

        public RowBuilder addHighlightedCellWithText(String cellText) {
            var cellFormat = new CellFormat().setBackgroundColor(LIGHT_YELLOW_3);
            cellFormat.setBorders(ALL_BORDERS);
            cellFormat.setTextFormat(COMMON_TEXT_FORMAT_REGULAR);
            cellFormat.setWrapStrategy(WRAP_TEXT);

            addCellToList(new ExtendedValue().setStringValue(cellText), cellFormat, null);

            return this;
        }

        public RowBuilder addBaseFeatureCell(String cellText) {
            var cellFormat = new CellFormat().setBackgroundColor(LIGHT_YELLOW_2);
            cellFormat.setBorders(ALL_BORDERS);
            cellFormat.setTextFormat(COMMON_TEXT_FORMAT_REGULAR);
            cellFormat.setWrapStrategy(WRAP_TEXT);

            addCellToList(new ExtendedValue().setStringValue(cellText), cellFormat, null);

            return this;
        }

        public RowBuilder addTier1FeatureCell(String cellText) {
            var cellFormat = new CellFormat().setBackgroundColor(LIGHT_GREEN_1);
            cellFormat.setBorders(ALL_BORDERS);
            cellFormat.setTextFormat(COMMON_TEXT_FORMAT_REGULAR);
            cellFormat.setWrapStrategy(WRAP_TEXT);

            addCellToList(new ExtendedValue().setStringValue(cellText), cellFormat, null);

            return this;
        }

        public RowBuilder addTier2FeatureCell(String cellText) {
            var cellFormat = new CellFormat().setBackgroundColor(LIGHT_CYAN_1);
            cellFormat.setBorders(ALL_BORDERS);
            cellFormat.setTextFormat(COMMON_TEXT_FORMAT_REGULAR);
            cellFormat.setWrapStrategy(WRAP_TEXT);

            addCellToList(new ExtendedValue().setStringValue(cellText), cellFormat, null);

            return this;
        }

        public RowBuilder addEmptyCell() {
            rowCells.add(new CellData());

            return this;
        }

        private void addCellToList(ExtendedValue cellValue, CellFormat cellFormat,
                                   DataValidationRule dataValidationRule) {
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

            rowCells.add(newCell);
        }

        public RowData build() {
            row.setValues(rowCells);

            return row;
        }

    }

    public static class GridBuilder {
        private GridData gridData;
        private List<RowData> rowDataList;
        private int numColumns;
        private int[] columnWidths;

        public GridBuilder() {
            gridData = new GridData();
            rowDataList = new ArrayList<>();
            numColumns = 0;
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

    /****************************
     METHODS FOR BUILDING SHEETS
     ****************************/

    public static Sheet buildStatsSheet(CharacterCreateInfo characterCreateInfo) {
        var sheet = buildSheetWithTitle(STATS_SHEET_TITLE);
        var characterCreateRequest = characterCreateInfo.characterCreateRequest();
        var isClassCharacter = (characterCreateRequest.level() > 0);

        // Block with basic information and money sections
        var row1 = new RowBuilder()
                .addHeaderCell("BASIC INFORMATION")
                .addHeaderCell("")
                .addHeaderCell("")
                .addHeaderCell("")
                .addHeaderCell("")
                .addHeaderCell("")
                .addEmptyCell()
                .addHeaderCell("MONEY")
                .addHeaderCell("")
                .build();

        var row2 = new RowBuilder()
                .addSecondaryHeaderCell("Character Name")
                .addSecondaryHeaderCell("Species")
                .addSecondaryHeaderCell("Level")
                .addSecondaryHeaderCell("Profession")
                .addSecondaryHeaderCell("Class")
                .addSecondaryHeaderCell("XP")
                .addEmptyCell()
                .addHighlightedCellWithText("CP")
                .addCellWithText("")
                .build();

        var profession = isClassCharacter ? "" : characterCreateRequest.profession();
        var charClass = isClassCharacter ? characterCreateRequest.characterClass().toCharSheetString() : "";
        var row3 = new RowBuilder()
                .addCellWithText(characterCreateRequest.characterName())
                .addCellWithText(characterCreateRequest.species().toCharSheetString(),
                        buildSpeciesDataValidation())
                .addCellWithNumber(characterCreateRequest.level(),
                        buildLevelDataValidation(characterCreateRequest.level()))
                .addCellWithText(profession,
                        buildProfessionDataValidation(characterCreateInfo.professions()))
                .addCellWithText(charClass,
                        buildCharClassDataValidation())
                .addCellWithText("")
                .addEmptyCell()
                .addHighlightedCellWithText("SP")
                .addCellWithText("")
                .build();

        var row4 = new RowBuilder()
                .addSecondaryHeaderCell("Initiative")
                .addSecondaryHeaderCell("Attack")
                .addSecondaryHeaderCell("Evasion")
                .addSecondaryHeaderCell("Critical Hit")
                .addSecondaryHeaderCell("Fortune Points")
                .addSecondaryHeaderCell("Hit Points")
                .addEmptyCell()
                .addHighlightedCellWithText("Other")
                .addCellWithText("")
                .build();

        var evasionFormula = String.format("=SUM(%d,B10)", characterCreateInfo.evasion());
        var row5 = new RowBuilder()
                .addCellWithFormula("=MAX(B10,B13)")
                .addCellWithNumber(characterCreateInfo.attack())
                .addCellWithFormula(evasionFormula)
                .addCellWithText("20")  // Hardcode this for now
                .addCellWithText("")
                .addCellWithText("")
                .build();

        // Block with attributes, skills, attack, and damage
        var row6 = new RowBuilder()
                .addHeaderCell("ATTRIBUTES")
                .addHeaderCell("")
                .addEmptyCell()
                .addHeaderCell("TRAINED SKILLS")
                .addHeaderCell("")
                .addHeaderCell("")
                .addEmptyCell()
                .addHeaderCell("ATTACK MODIFIERS")
                .addHeaderCell("")
                .build();

        var row7 = new RowBuilder()
                .addSecondaryHeaderCell("")
                .addSecondaryHeaderCell("Modifier")
                .addEmptyCell()
                .addSecondaryHeaderCell("Skill")
                .addSecondaryHeaderCell("Ability Modifier")
                .addSecondaryHeaderCell("Total Modifier")
                .addEmptyCell()
                .addSecondaryHeaderCell("Melee")
                .addCellWithFormula("=SUM(B5,B9)")
                .build();

        var row8 = new RowBuilder()
                .addHighlightedCellWithText("Strength (STR)")
                .addCellWithText("")
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E9"))
                .addEmptyCell()
                .addSecondaryHeaderCell("Ranged")
                .addCellWithFormula("=SUM(B5,B10)")
                .build();

        var row9Builder = new RowBuilder()
                .addHighlightedCellWithText("Coordination (COR)")
                .addCellWithText("")
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E10"))
                .addEmptyCell();

        var row10Builder = new RowBuilder()
                .addHighlightedCellWithText("Stamina (STA)")
                .addCellWithText("")
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E11"))
                .addEmptyCell();

        var row11Builder = new RowBuilder()
                .addHighlightedCellWithText("Intellect (INT)")
                .addCellWithText("")
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E12"))
                .addEmptyCell();

        var row12Builder = new RowBuilder()
                .addHighlightedCellWithText("Perception (PER)")
                .addCellWithText("")
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E13"))
                .addEmptyCell();

        var row13Builder = new RowBuilder()
                .addHighlightedCellWithText("Presence (PRS)")
                .addCellWithText("")
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E14"))
                .addEmptyCell();

        var row14 = new RowBuilder()
                .addHighlightedCellWithText("Luck (LUC)")
                .addCellWithText("")
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E15"))
                .build();

        var row15 = new RowBuilder()
                .addEmptyCell()
                .addEmptyCell()
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E16"))
                .build();

        // If the character is a magic user, we have Spell under the list of attack modifiers.
        // Otherwise, we just have Melee and Ranged.
        var isMagicUser = characterCreateRequest.characterClass() != null &&
                characterCreateRequest.characterClass().isMagicUser();
        if (isMagicUser) {
            row9Builder = row9Builder
                    .addSecondaryHeaderCell("Spell");

            // Mages use INT for spells; Shamans use PRS
            var spellAttributeScoreCell = (characterCreateRequest.characterClass() == CharType.MAGE) ?
                    "B12" : "B14";
            var spellFormula = "=SUM(B5," + spellAttributeScoreCell + ")";

            row9Builder.addCellWithFormula(spellFormula);

            row10Builder.addEmptyCell().addEmptyCell();

            row11Builder.addHeaderCell("DAMAGE MODIFIERS").addHeaderCell("");

            row12Builder.addSecondaryHeaderCell("Melee").addCellWithFormula("=B9");

            row13Builder.addSecondaryHeaderCell("Ranged").addCellWithFormula("=B10");
        }
        else {
            row9Builder.addEmptyCell().addEmptyCell();

            row10Builder.addHeaderCell("DAMAGE MODIFIERS").addHeaderCell("");

            row11Builder.addSecondaryHeaderCell("Melee").addCellWithFormula("=B9");

            row12Builder.addSecondaryHeaderCell("Ranged").addCellWithFormula("=B10");

            row13Builder.addEmptyCell().addEmptyCell();
        }

        // Block with armor and weapons
        var row16 = new RowBuilder()
                .addHeaderCell("ARMOR")
                .addHeaderCell("")
                .addHeaderCell("")
                .addEmptyCell()
                .addHeaderCell("WEAPONS")
                .addHeaderCell("")
                .addHeaderCell("")
                .addHeaderCell("")
                .build();

        var row17 = new RowBuilder()
                .addSecondaryHeaderCell("Armor")
                .addSecondaryHeaderCell("Type")
                .addSecondaryHeaderCell("Damage Absorption")
                .addEmptyCell()
                .addSecondaryHeaderCell("Weapon")
                .addSecondaryHeaderCell("Type")
                .addSecondaryHeaderCell("Attack Bonus")
                .addSecondaryHeaderCell("Total Damage")
                .build();

        var row18 = new RowBuilder()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithText("")
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithText("")
                .build();

        var row19 = new RowBuilder()
                .addEmptyCell()
                .addEmptyCell()
                .addEmptyCell()
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithText("")
                .build();

        // This duplicates row19 for now, but that one may be populated during character creation,
        // while this one will most likely not be
        var row20 = new RowBuilder()
                .addEmptyCell()
                .addEmptyCell()
                .addEmptyCell()
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithText("")
                .build();

        var gridData = new GridBuilder()
                .withNumColumns(9)
                .addRow(row1)
                .addRow(row2)
                .addRow(row3)
                .addRow(row4)
                .addRow(row5)
                .addEmptyRow()
                .addRow(row6)
                .addRow(row7)
                .addRow(row8)
                .addRow(row9Builder.build())
                .addRow(row10Builder.build())
                .addRow(row11Builder.build())
                .addRow(row12Builder.build())
                .addRow(row13Builder.build())
                .addRow(row14)
                .addRow(row15)
                .addEmptyRow()
                .addRow(row16)
                .addRow(row17)
                .addRow(row18)
                .addRow(row19)
                .addRow(row20)
                .setColumnWidth(0, 225)
                .setColumnWidth(3, 125)
                .setColumnWidth(4, 150)
                .setColumnWidth(5, 150)
                .build();

        sheet.setData(Collections.singletonList(gridData));

        return sheet;
    }

    private static DataValidationRule buildSpeciesDataValidation() {
        var condition = new BooleanCondition();
        condition.setType("ONE_OF_LIST");
        var speciesValues = new ArrayList<ConditionValue>();
        for (var species : SpeciesType.values()) {
            var conditionValue = new ConditionValue();
            conditionValue.setUserEnteredValue(species.toCharSheetString());
            speciesValues.add(conditionValue);
        }
        condition.setValues(speciesValues);

        var dataValidationRule = new DataValidationRule();
        dataValidationRule.setShowCustomUi(true);
        dataValidationRule.setCondition(condition);

        return dataValidationRule;
    }

    private static DataValidationRule buildLevelDataValidation(int level) {
        var condition = new BooleanCondition();
        condition.setType("NUMBER_BETWEEN");
        var levelValues = new ArrayList<ConditionValue>();
        // Don't allow class characters to select level 0 as an option
        var minAllowedLevel = (level > 0) ? "1" : "0";
        levelValues.add(new ConditionValue().setUserEnteredValue(minAllowedLevel));
        levelValues.add(new ConditionValue().setUserEnteredValue("7"));
        condition.setValues(levelValues);

        return buildDataValidationRuleWithCondition(condition);
    }

    private static DataValidationRule buildProfessionDataValidation(List<String> professions) {
        var condition = new BooleanCondition();
        condition.setType("ONE_OF_LIST");
        var professionValues = professions.stream()
                        .map(x -> new ConditionValue().setUserEnteredValue(x))
                        .toList();
        condition.setValues(professionValues);

        return buildDataValidationRuleWithCondition(condition);
    }

    private static DataValidationRule buildCharClassDataValidation() {
        var condition = new BooleanCondition();
        condition.setType("ONE_OF_LIST");
        var charClassValues = new ArrayList<ConditionValue>();
        for (var charClass : CharType.values()) {
            var conditionValue = new ConditionValue();
            conditionValue.setUserEnteredValue(charClass.toCharSheetString());
            charClassValues.add(conditionValue);
        }
        condition.setValues(charClassValues);

        return buildDataValidationRuleWithCondition(condition);
    }

    private static DataValidationRule buildDataValidationRuleWithCondition(BooleanCondition condition) {
        var dataValidationRule = new DataValidationRule();
        dataValidationRule.setShowCustomUi(true);
        dataValidationRule.setCondition(condition);

        return dataValidationRule;
    }

    public static Sheet buildSpellsSheet() {
        var sheet = buildSheetWithTitle(SPELLS_SHEET_TITLE);

        var headerRow = new RowBuilder()
                .addSecondaryHeaderCell("Level")
                .addSecondaryHeaderCell("Spell")
                .addSecondaryHeaderCell("Notes")
                .build();

        var gridDataBuilder = new GridBuilder()
                .addRow(headerRow);

        // Do 2 rows for now: vary this by level later
        var spellRow = new RowBuilder()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithText("")
                .build();
        for (int i = 0; i < 2; i++) {
            gridDataBuilder = gridDataBuilder.addRow(spellRow);
        }

        sheet.setData(Collections.singletonList(gridDataBuilder.build()));

        return sheet;
    }

    public static Sheet buildFeaturesSheet() {
        var sheet = buildSheetWithTitle(FEATURES_SHEET_TITLE);

        var speciesLanguageHeaderRow = new RowBuilder()
                .addHeaderCell("SPECIES FEATURES")
                .addEmptyCell()
                .addHeaderCell("LANGUAGES")
                .build();

        var speciesLanguageRow = new RowBuilder()
                .addCellWithText("")
                .addEmptyCell()
                .addCellWithText("")
                .build();

        var speciesRow = new RowBuilder().addCellWithText("").build();

        var classFeaturesHeaderRow = new RowBuilder().addHeaderCell("CLASS FEATURES").build();

        var baseFeaturesRow = new RowBuilder().addBaseFeatureCell("Base features in this color").build();

        var tier1FeaturesRow = new RowBuilder().addTier1FeatureCell("Tier I features in this color").build();

        var tier2FeaturesRow = new RowBuilder().addTier2FeatureCell("Tier II features in this color").build();

        var gridData = new GridBuilder()
                .withNumColumns(3)
                .addRow(speciesLanguageHeaderRow)
                .addRow(speciesLanguageRow)
                .addRow(speciesRow)
                .addEmptyRow()
                .addRow(classFeaturesHeaderRow)
                .addRow(baseFeaturesRow)
                .addRow(tier1FeaturesRow)
                .addRow(tier2FeaturesRow)
                .setColumnWidth(0, 450)
                .setColumnWidth(2, 200)
                .build();

        sheet.setData(Collections.singletonList(gridData));

        return sheet;
    }

    public static Sheet buildGearSheet() {
        var sheet = buildSheetWithTitle(GEAR_SHEET_TITLE);

        var headerRow = new RowBuilder()
                .addHeaderCell("EQUIPMENT")
                .addEmptyCell()
                .addHeaderCell("SPECIAL ITEMS")
                .build();

        var gridDataBuilder = new GridBuilder()
                .addRow(headerRow);

        var gearRow = new RowBuilder()
                .addCellWithText("")
                .addEmptyCell()
                .addCellWithText("")
                .build();
        for (int i = 0; i < NUM_GEAR_ROWS; i++) {
            gridDataBuilder.addRow(gearRow);
        }

        gridDataBuilder.withNumColumns(3).setColumnWidth(0, 225).setColumnWidth(2, 225);

        sheet.setData(Collections.singletonList(gridDataBuilder.build()));

        return sheet;
    }

    private static String generateTotalModifierFormula(String cellToLeft) {
        return """
                =SUM(C3,IFS(EXACT(%s,"STR"), B9, EXACT(%s,"COR"), B10, EXACT(%s,"STA"), B11, \
                EXACT(%s,"INT"), B12, EXACT(%s,"PER"), B13, EXACT(%s,"PRS"), B14, EXACT(%s,"LUC"), B15))
                """
                .formatted(cellToLeft, cellToLeft, cellToLeft, cellToLeft, cellToLeft, cellToLeft, cellToLeft);
    }

    private static Sheet buildSheetWithTitle(String title)
    {
        return new Sheet().setProperties(new SheetProperties().setTitle(title));
    }
}
