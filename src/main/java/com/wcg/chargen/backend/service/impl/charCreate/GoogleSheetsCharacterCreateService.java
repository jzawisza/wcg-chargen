package com.wcg.chargen.backend.service.impl.charCreate;

import com.google.api.services.sheets.v4.model.*;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;

import com.wcg.chargen.backend.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleSheetsCharacterCreateService extends BaseCharacterCreateService {
    private final Logger logger = LoggerFactory.getLogger(GoogleSheetsCharacterCreateService.class);
    @Autowired
    GoogleSheetsApiService googleSheetsApiService;
    @Autowired
    GoogleSheetBuilderService googleSheetBuilderService;
    @Autowired
    ProfessionsService professionsService;
    @Autowired
    CharClassesService charClassesService;
    @Autowired
    CommonerService commonerService;

    @Override
    public CharacterCreateStatus doCreateCharacter(CharacterCreateRequest characterCreateRequest, String bearerToken) {
        try {
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
        var currentDateTime = LocalDateTime.now();
        var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        var classOrProfession = (characterCreateRequest.level() > 0) ?
                characterCreateRequest.characterClass().toString().toUpperCase() :
                characterCreateRequest.profession().toUpperCase();
        var title = String.format("%s_%s_%s_%s",
                characterCreateRequest.characterName(),
                characterCreateRequest.species().toString().toUpperCase(),
                classOrProfession,
                currentDateTime.format(dateTimeFormatter));

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

        if(characterCreateRequest.characterClass() != null &&
                characterCreateRequest.characterClass().isMagicUser()) {
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
