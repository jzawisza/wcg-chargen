package com.wcg.chargen.backend.service.impl;

import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.wcg.chargen.backend.model.GoogleSheetsApiResponse;
import com.wcg.chargen.backend.service.GoogleSheetsApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * This class manages the connection to the Google Sheets REST API.
 * I had originally planned to use Google's own OAuth classes from their online examples,
 * but those classes are poorly documented and unsuited to the use case here,
 * i.e. a one-time use of an access token without need for periodic refresh.
 * The approach taken here actually works, and has the benefit of being much simpler
 * than Google's examples.
 */
@Service
public class DefaultGoogleSheetsApiService implements GoogleSheetsApiService {
    private final RestClient restClient;
    private final Logger logger = LoggerFactory.getLogger(DefaultGoogleSheetsApiService.class);

    private static final String GOOGLE_SHEETS_URL = "https://sheets.googleapis.com/v4/spreadsheets?fields=spreadsheetId";

    public DefaultGoogleSheetsApiService() {
        restClient = RestClient.builder()
                .baseUrl(GOOGLE_SHEETS_URL)
                .build();
    }
    @Override
    public String createSpreadsheet(Spreadsheet spreadsheet, String bearerToken) {
        try {
            var responseEntity = restClient
                    .post()
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .body(spreadsheet)
                    .retrieve()
                    .toEntity(GoogleSheetsApiResponse.class);
            if (responseEntity.hasBody() && responseEntity.getBody() != null) {
                return responseEntity.getBody().spreadsheetId();
            }
        }
        catch (Exception e) {
            logger.error("Error creating Google Sheet", e);
        }

        return null;
    }
}
