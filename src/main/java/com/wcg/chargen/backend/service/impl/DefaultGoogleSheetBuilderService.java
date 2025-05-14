package com.wcg.chargen.backend.service.impl;

import com.google.api.services.sheets.v4.model.*;
import com.wcg.chargen.backend.enums.AttributeType;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;

import static com.wcg.chargen.backend.util.GoogleSheetsUtil.GridBuilder.getGridBuilder;
import static com.wcg.chargen.backend.util.GoogleSheetsUtil.RowBuilder.getRowBuilder;

@Service
public class DefaultGoogleSheetBuilderService implements GoogleSheetBuilderService {
    @Autowired
    ProfessionsService professionsService;
    @Autowired
    CharClassesService charClassesService;
    @Autowired
    CommonerService commonerService;
    @Autowired
    RandomNumberService randomNumberService;

    private static final String STATS_SHEET_TITLE = "Stats";
    private static final String SPELLS_SHEET_TITLE = "Spells";
    private static final String FEATURES_SHEET_TITLE = "Class/Species Features";
    private static final String GEAR_SHEET_TITLE = "Gear";
    private static final int NUM_GEAR_ROWS = 10;


    private static Sheet buildSheetWithTitle(String title)
    {
        return new Sheet().setProperties(new SheetProperties().setTitle(title));
    }

    private static String generateTotalModifierFormula(String cellToLeft) {
        return """
                =SUM(C3,IFS(EXACT(%s,"STR"), B9, EXACT(%s,"COR"), B10, EXACT(%s,"STA"), B11, \
                EXACT(%s,"INT"), B12, EXACT(%s,"PER"), B13, EXACT(%s,"PRS"), B14, EXACT(%s,"LUC"), B15))
                """
                .formatted(cellToLeft, cellToLeft, cellToLeft, cellToLeft, cellToLeft, cellToLeft, cellToLeft);
    }

    private DataValidationRule buildSpeciesDataValidation() {
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

    private DataValidationRule buildLevelDataValidation(int level) {
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

    private DataValidationRule buildProfessionDataValidation() {
        var condition = new BooleanCondition();
        condition.setType("ONE_OF_LIST");
        var professionValues = professionsService.getAllProfessions().professions().stream()
                .map(x -> new ConditionValue().setUserEnteredValue(x.name()))
                .toList();
        condition.setValues(professionValues);

        return buildDataValidationRuleWithCondition(condition);
    }

    private DataValidationRule buildCharClassDataValidation() {
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

    private DataValidationRule buildDataValidationRuleWithCondition(BooleanCondition condition) {
        var dataValidationRule = new DataValidationRule();
        dataValidationRule.setShowCustomUi(true);
        dataValidationRule.setCondition(condition);

        return dataValidationRule;
    }

    private int getAttack(CharacterCreateRequest characterCreateRequest) {
        var level = characterCreateRequest.level();
        if (level > 0) {
            var charClass = charClassesService.getCharClassByType(characterCreateRequest.characterClass());
            return charClass.attackModifiers().get(level - 1);
        }
        else {
            return commonerService.getInfo().attack();
        }
    }

    private String getEvasionFormula(CharacterCreateRequest characterCreateRequest) {
        var level = characterCreateRequest.level();
        var evasion = -1;
        if (level > 0) {
            var charClass = charClassesService.getCharClassByType(characterCreateRequest.characterClass());
            evasion = charClass.evasionModifiers().get(level - 1);
        }
        else {
            evasion = commonerService.getInfo().evasion();
        }

        return String.format("=SUM(%d,B10)", evasion);
    }

    private int getFortunePoints(CharacterCreateRequest characterCreateRequest) {
        var luckScore = characterCreateRequest.getAttributeValue(AttributeType.LUC);
        // Level 1 characters start with 1 fortune point, and you get 1 more per level
        var fortunePoints = characterCreateRequest.level();

        // Fortune points can never go below 0
        return Math.max(0, fortunePoints + luckScore);
    }

    private int getHitPoints(CharacterCreateRequest characterCreateRequest) {
        var staminaScore = characterCreateRequest.getAttributeValue(AttributeType.STA);
        if (characterCreateRequest.level() == 0) {
            var d4Roll = randomNumberService.getIntFromRange(1, 4);

            return d4Roll + 1 + staminaScore;
        }
        else {
            var charType = characterCreateRequest.characterClass();
            var charClass = charClassesService.getCharClassByType(charType);

            // Base hit points for level 1 character
            var hitPoints = charClass.level1Hp() + staminaScore;
            // Add hit points for each level above 1
            for (int i = 1; i < characterCreateRequest.level(); i++) {
                hitPoints += randomNumberService.getIntFromRange(1, charClass.maxHpAtLevelUp());
            }

            return hitPoints;
        }
    }

    public Sheet buildStatsSheet(CharacterCreateRequest characterCreateRequest) {
        var sheet = buildSheetWithTitle(STATS_SHEET_TITLE);
        var isClassCharacter = (characterCreateRequest.level() > 0);

        // Block with basic information and money sections
        var row1 = getRowBuilder()
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

        var row2 = getRowBuilder()
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
        var row3 = getRowBuilder()
                .addCellWithText(characterCreateRequest.characterName())
                .addCellWithText(characterCreateRequest.species().toCharSheetString(),
                        buildSpeciesDataValidation())
                .addCellWithNumber(characterCreateRequest.level(),
                        buildLevelDataValidation(characterCreateRequest.level()))
                .addCellWithText(profession,
                        buildProfessionDataValidation())
                .addCellWithText(charClass,
                        buildCharClassDataValidation())
                .addCellWithText("")
                .addEmptyCell()
                .addHighlightedCellWithText("SP")
                .addCellWithText("")
                .build();

        var row4 = getRowBuilder()
                .addSecondaryHeaderCell("Initiative")
                .addSecondaryHeaderCell("Attack")
                .addSecondaryHeaderCell("Evasion")
                .addSecondaryHeaderCell("Fortune Points")
                .addSecondaryHeaderCell("Current HP")
                .addSecondaryHeaderCell("Max HP")
                .addEmptyCell()
                .addHighlightedCellWithText("Other")
                .addCellWithText("")
                .build();

        var hitPoints = getHitPoints(characterCreateRequest);
        var row5 = getRowBuilder()
                .addCellWithFormula("=MAX(B10,B13)")
                .addCellWithNumber(getAttack(characterCreateRequest))
                .addCellWithFormula(getEvasionFormula(characterCreateRequest))
                .addCellWithNumber(getFortunePoints(characterCreateRequest))
                .addCellWithNumber(hitPoints)
                .addCellWithNumber(hitPoints)
                .build();

        // Block with attributes, skills, attack, and damage
        var row6 = getRowBuilder()
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

        var row7 = getRowBuilder()
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

        var row8 = getRowBuilder()
                .addHighlightedCellWithText("Strength (STR)")
                .addCellWithAttributeValue(characterCreateRequest, AttributeType.STR)
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E9"))
                .addEmptyCell()
                .addSecondaryHeaderCell("Ranged")
                .addCellWithFormula("=SUM(B5,B10)")
                .build();

        var row9Builder = getRowBuilder()
                .addHighlightedCellWithText("Coordination (COR)")
                .addCellWithAttributeValue(characterCreateRequest, AttributeType.COR)
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E10"))
                .addEmptyCell();

        var row10Builder = getRowBuilder()
                .addHighlightedCellWithText("Stamina (STA)")
                .addCellWithAttributeValue(characterCreateRequest, AttributeType.STA)
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E11"))
                .addEmptyCell();

        var row11Builder = getRowBuilder()
                .addHighlightedCellWithText("Intellect (INT)")
                .addCellWithAttributeValue(characterCreateRequest, AttributeType.INT)
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E12"))
                .addEmptyCell();

        var row12Builder = getRowBuilder()
                .addHighlightedCellWithText("Perception (PER)")
                .addCellWithAttributeValue(characterCreateRequest, AttributeType.PER)
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E13"))
                .addEmptyCell();

        var row13Builder = getRowBuilder()
                .addHighlightedCellWithText("Presence (PRS)")
                .addCellWithAttributeValue(characterCreateRequest, AttributeType.PRS)
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E14"))
                .addEmptyCell();

        var row14 = getRowBuilder()
                .addHighlightedCellWithText("Luck (LUC)")
                .addCellWithAttributeValue(characterCreateRequest, AttributeType.LUC)
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithFormula(generateTotalModifierFormula("E15"))
                .build();

        var row15 = getRowBuilder()
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
        var row16 = getRowBuilder()
                .addHeaderCell("ARMOR")
                .addHeaderCell("")
                .addHeaderCell("")
                .addEmptyCell()
                .addHeaderCell("WEAPONS")
                .addHeaderCell("")
                .addHeaderCell("")
                .addHeaderCell("")
                .build();

        var row17 = getRowBuilder()
                .addSecondaryHeaderCell("Armor")
                .addSecondaryHeaderCell("Type")
                .addSecondaryHeaderCell("Damage Absorption")
                .addEmptyCell()
                .addSecondaryHeaderCell("Weapon")
                .addSecondaryHeaderCell("Type")
                .addSecondaryHeaderCell("Attack Bonus")
                .addSecondaryHeaderCell("Total Damage")
                .build();

        var row18 = getRowBuilder()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithText("")
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithText("")
                .build();

        var row19 = getRowBuilder()
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
        var row20 = getRowBuilder()
                .addEmptyCell()
                .addEmptyCell()
                .addEmptyCell()
                .addEmptyCell()
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithText("")
                .addCellWithText("")
                .build();

        var gridData = getGridBuilder()
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

    public Sheet buildSpellsSheet() {
        var sheet = buildSheetWithTitle(SPELLS_SHEET_TITLE);

        var headerRow = getRowBuilder()
                .addSecondaryHeaderCell("Level")
                .addSecondaryHeaderCell("Spell")
                .addSecondaryHeaderCell("Notes")
                .build();

        var gridDataBuilder = getGridBuilder()
                .addRow(headerRow);

        // Do 2 rows for now: vary this by level later
        var spellRow = getRowBuilder()
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

    public Sheet buildFeaturesSheet(CharacterCreateRequest characterCreateRequest) {
        var sheet = buildSheetWithTitle(FEATURES_SHEET_TITLE);

        var speciesLanguageHeaderRow = getRowBuilder()
                .addHeaderCell("SPECIES FEATURES")
                .addEmptyCell()
                .addHeaderCell("LANGUAGES")
                .build();

        var speciesLanguageRow = getRowBuilder()
                .addCellWithText("")
                .addEmptyCell()
                .addCellWithText("")
                .build();

        var speciesRow = getRowBuilder().addCellWithText("").build();

        var classFeaturesHeaderRow = getRowBuilder().addHeaderCell("CLASS FEATURES").build();

        var baseFeaturesRow = getRowBuilder().addBaseFeatureCell("Base features in this color").build();

        var tier1FeaturesRow = getRowBuilder().addTier1FeatureCell("Tier I features in this color").build();

        var tier2FeaturesRow = getRowBuilder().addTier2FeatureCell("Tier II features in this color").build();

        var gridData = getGridBuilder()
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

    public Sheet buildGearSheet(CharacterCreateRequest characterCreateRequest) {
        var sheet = buildSheetWithTitle(GEAR_SHEET_TITLE);

        var headerRow = getRowBuilder()
                .addHeaderCell("EQUIPMENT")
                .addEmptyCell()
                .addHeaderCell("SPECIAL ITEMS")
                .build();

        var gridDataBuilder = getGridBuilder()
                .addRow(headerRow);

        var gearRow = getRowBuilder()
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
}
