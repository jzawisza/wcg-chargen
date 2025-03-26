package com.wcg.chargen.backend.service;

import com.google.api.services.sheets.v4.model.Spreadsheet;

public interface GoogleSheetsApiService {
    String createSpreadsheet(Spreadsheet spreadsheet, String bearerToken);
}
