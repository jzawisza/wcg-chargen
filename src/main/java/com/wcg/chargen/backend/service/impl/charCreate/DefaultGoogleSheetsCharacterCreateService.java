package com.wcg.chargen.backend.service.impl.charCreate;

import com.google.api.services.sheets.v4.model.*;
import com.wcg.chargen.backend.enums.FeatureAttributeType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;

import com.wcg.chargen.backend.service.*;
import com.wcg.chargen.backend.util.CharacterSheetUtil;
import com.wcg.chargen.backend.util.FeatureAttributeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultGoogleSheetsCharacterCreateService implements GoogleSheetsCharacterCreateService {
    private final Logger logger = LoggerFactory.getLogger(DefaultGoogleSheetsCharacterCreateService.class);
    @Autowired
    GoogleSheetsApiService googleSheetsApiService;
    @Autowired
    GoogleSheetBuilderService googleSheetBuilderService;
    @Autowired
    CharClassesService charClassesService;
    @Autowired
    CharacterCreateRequestValidatorService characterCreateRequestValidatorService;

    @Override
    public CharacterCreateStatus createCharacter(CharacterCreateRequest characterCreateRequest, String bearerToken) {
        try {
            var status = characterCreateRequestValidatorService.validate(characterCreateRequest);
            if (!status.isSuccess()) {
                // If the request isn't valid, abort here
                return status;
            }

            var spreadsheet = buildSpreadsheet(characterCreateRequest);
            logger.debug("Spreadsheet to create = {}", spreadsheet);
            var spreadsheetId = googleSheetsApiService.createSpreadsheet(spreadsheet, bearerToken);

            if (spreadsheetId == null) {
                logger.error("Error when creating Google Sheet");
                return new CharacterCreateStatus(false, "Error creating Google Sheet");
            }
            else {
                logger.info("Spreadsheet ID {} created for spreadsheet {}",
                        spreadsheetId, spreadsheet.getProperties().getTitle());
            }

            return CharacterCreateStatus.SUCCESS;
        }
        catch (Exception e) {
            logger.error("Exception thrown when creating Google Sheet", e);
            return new CharacterCreateStatus(false, e.getMessage());
        }
    }

    private Spreadsheet buildSpreadsheet(CharacterCreateRequest characterCreateRequest) {
        var title = CharacterSheetUtil.generateName(characterCreateRequest);

        var spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties()
                        .setTitle(title));
        spreadsheet.setSheets(buildSheets(characterCreateRequest));

        return spreadsheet;
    }

    private List<Sheet> buildSheets(CharacterCreateRequest characterCreateRequest) {
        var sheetList = new ArrayList<Sheet>();

        var statsSheet = googleSheetBuilderService.buildStatsSheet(characterCreateRequest);
        sheetList.add(statsSheet);

        if(shouldAddSpellsSheet(characterCreateRequest)) {
            var spellsSheet = googleSheetBuilderService.buildSpellsSheet(characterCreateRequest);
            sheetList.add(spellsSheet);
        }

        var featuresSheet = googleSheetBuilderService.buildFeaturesSheet(characterCreateRequest);
        sheetList.add(featuresSheet);

        var gearSheet = googleSheetBuilderService.buildGearSheet(characterCreateRequest);
        sheetList.add(gearSheet);

        return sheetList;
    }

    private boolean shouldAddSpellsSheet(CharacterCreateRequest characterCreateRequest) {
        if(characterCreateRequest.characterClass() == null) {
            return false;
        }

        if (characterCreateRequest.characterClass().isMagicUser()) {
            return true;
        }

        // Check if a feature has been selected that allows a character not otherwise
        // considered a magic user to cast spells
        var charClass = charClassesService.getCharClassByType(characterCreateRequest.characterClass());
        if (FeatureAttributeUtil.getFeatureNameFromRequestWithAttributeType(charClass.features(),
                characterCreateRequest.features(),
                FeatureAttributeType.MAGIC,
                FeatureAttributeUtil.Tier.I) != null) {
            return true;
        }

        if (FeatureAttributeUtil.getFeatureNameFromRequestWithAttributeType(charClass.features(),
                characterCreateRequest.features(),
                FeatureAttributeType.MAGIC,
                FeatureAttributeUtil.Tier.II) != null) {
            return true;
        }

        return false;
    }
}
