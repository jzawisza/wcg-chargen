package com.wcg.chargen.backend.service.impl.charCreate;

import com.google.api.services.sheets.v4.model.*;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;

import com.wcg.chargen.backend.service.*;
import com.wcg.chargen.backend.worker.CharacterSheetWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    @Autowired
    CharacterSheetWorker characterSheetWorker;

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
        var title = characterSheetWorker.generateName(characterCreateRequest);

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

        if(characterSheetWorker.hasMagic(characterCreateRequest)) {
            var spellsSheet = googleSheetBuilderService.buildSpellsSheet(characterCreateRequest);
            sheetList.add(spellsSheet);
        }

        var featuresSheet = googleSheetBuilderService.buildFeaturesSheet(characterCreateRequest);
        sheetList.add(featuresSheet);

        var gearSheet = googleSheetBuilderService.buildGearSheet(characterCreateRequest);
        sheetList.add(gearSheet);

        return sheetList;
    }
}
