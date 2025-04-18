package com.wcg.chargen.backend.service.impl.charCreate;

import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;

import com.wcg.chargen.backend.service.GoogleSheetsApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GoogleSheetsCharacterCreateService extends BaseCharacterCreateService {
    private final Logger logger = LoggerFactory.getLogger(GoogleSheetsCharacterCreateService.class);
    @Autowired
    GoogleSheetsApiService googleSheetsApiService;

    @Override
    public CharacterCreateStatus doCreateCharacter(CharacterCreateRequest characterCreateRequest, String bearerToken) {
        try {
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

            var spreadsheetId = googleSheetsApiService.createSpreadsheet(spreadsheet, bearerToken);

            if (spreadsheetId == null) {
                logger.error("Error when creating Google Sheet");
                return new CharacterCreateStatus(false, "Error creating Google Sheet");
            }
            else {
                logger.info("Spreadsheet ID {} created for spreadsheet {}", spreadsheetId, title);
            }

            return CharacterCreateStatus.SUCCESS;
        }
        catch (Exception e) {
            logger.error("Exception thrown when creating Google Sheet", e);
            return new CharacterCreateStatus(false, e.getMessage());
        }
    }
}
