package com.wcg.chargen.backend.service.impl.charCreate;

import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;

import com.wcg.chargen.backend.service.GoogleSheetsApiService;
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
    private static final String STATS_SHEET_TITLE = "Stats";
    private static final String SPELLS_SHEET_TITLE = "Spells";
    private static final String FEATURES_SHEET_TITLE = "Class/Species Features";
    private static final String GEAR_SHEET_TITLE = "Gear";

    private static final List<CharType> MAGIC_USERS_LIST = new ArrayList<CharType>(
            List.of(CharType.MAGE, CharType.SHAMAN, CharType.SKALD)
    );

    private final Logger logger = LoggerFactory.getLogger(GoogleSheetsCharacterCreateService.class);
    @Autowired
    GoogleSheetsApiService googleSheetsApiService;

    @Override
    public CharacterCreateStatus doCreateCharacter(CharacterCreateRequest characterCreateRequest, String bearerToken) {
        try {
            var spreadsheet = buildSpreadsheet(characterCreateRequest);
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
        var title = String.format("%s_%s_%s_%s",
                characterCreateRequest.characterName(),
                characterCreateRequest.species().toString().toUpperCase(),
                characterCreateRequest.characterClass().toString().toUpperCase(),
                currentDateTime.format(dateTimeFormatter));

        var spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties()
                        .setTitle(title));
        spreadsheet.setSheets(buildSheets(characterCreateRequest));

        return spreadsheet;
    }

    private List<Sheet> buildSheets(CharacterCreateRequest characterCreateRequest) {
        var sheetList = new ArrayList<Sheet>();

        var statsSheet = buildSheetWithTitle(STATS_SHEET_TITLE);
        sheetList.add(statsSheet);

        if(MAGIC_USERS_LIST.contains(characterCreateRequest.characterClass())) {
            var spellsSheet = buildSheetWithTitle(SPELLS_SHEET_TITLE);
            sheetList.add(spellsSheet);
        }

        var featuresSheet = buildSheetWithTitle(FEATURES_SHEET_TITLE);
        sheetList.add(featuresSheet);

        var gearSheet = buildSheetWithTitle(GEAR_SHEET_TITLE);
        sheetList.add(gearSheet);

        return sheetList;
    }

    private Sheet buildSheetWithTitle(String title)
    {
        return new Sheet().setProperties(new SheetProperties().setTitle(title));
    }
}
