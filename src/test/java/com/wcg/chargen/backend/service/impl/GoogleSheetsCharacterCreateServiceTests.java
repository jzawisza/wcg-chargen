package com.wcg.chargen.backend.service.impl;

import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import com.wcg.chargen.backend.service.GoogleSheetsApiService;
import com.wcg.chargen.backend.service.impl.charCreate.GoogleSheetsCharacterCreateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class GoogleSheetsCharacterCreateServiceTests {
    @MockBean
    CharacterCreateRequestValidatorService characterCreateRequestValidatorService;

    @MockBean
    private GoogleSheetsApiService googleSheetsApiService;

    @Autowired
    private GoogleSheetsCharacterCreateService googleSheetsCharacterCreateService;

    @Test
    public void createCharacter_ReturnsFailureIfValidationFails() {
        Mockito.when(characterCreateRequestValidatorService.validate(null))
                .thenReturn(new CharacterCreateStatus(false, "Some error"));

        var status = googleSheetsCharacterCreateService.createCharacter(null, "");

        assertNotNull(status);
        assertFalse(status.isSuccess());
    }

    @Test
    public void createCharacter_ReturnsExpectedErrorIfGoogleSheetsApiServiceReturnsFailure() {
        var request = new CharacterCreateRequest("SomeName", CharType.MYSTIC, SpeciesType.HUMAN);

        Mockito.when(characterCreateRequestValidatorService.validate(request))
                .thenReturn(CharacterCreateStatus.SUCCESS);
        Mockito.when(googleSheetsApiService.createSpreadsheet(any(), any()))
                .thenReturn(null);

        var status = googleSheetsCharacterCreateService.createCharacter(request, "");

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Error creating Google Sheet", status.message());
    }

    @Test
    public void createCharacter_ReturnsExpectedErrorIfExceptionIsThrownDuringProcessing() {
        var request = new CharacterCreateRequest("SomeName", CharType.MYSTIC, SpeciesType.HUMAN);
        var expectedErrMsg = "Exception error";

        Mockito.when(characterCreateRequestValidatorService.validate(request))
                .thenReturn(CharacterCreateStatus.SUCCESS);
        Mockito.when(googleSheetsApiService.createSpreadsheet(any(), any()))
                .thenThrow(new RuntimeException(expectedErrMsg));

        var status = googleSheetsCharacterCreateService.createCharacter(request, "");

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedErrMsg, status.message());
    }

    @Test
    public void createCharacter_GeneratesSpreadsheetWithAppropriateTitle() {
        var request = new CharacterCreateRequest("SomeName", CharType.MYSTIC, SpeciesType.HUMAN);
        // Title should be SomeName_HUMAN_MYSTIC_yyyyMMddHHmmss
        var expectedTitleLength = 36;
        var expectedSpreadsheetId = "aaa-bbb-ccc";

        Mockito.when(characterCreateRequestValidatorService.validate(request))
                .thenReturn(CharacterCreateStatus.SUCCESS);
        Mockito.when(googleSheetsApiService.createSpreadsheet(any(), any()))
                .thenReturn(expectedSpreadsheetId);

        var status = googleSheetsCharacterCreateService.createCharacter(request, "");

        final ArgumentCaptor<Spreadsheet> captor = ArgumentCaptor.forClass(Spreadsheet.class);
        verify(googleSheetsApiService).createSpreadsheet(captor.capture(), any());
        final Spreadsheet spreadsheet = captor.getValue();

        assertTrue(status.isSuccess());
        assertNotNull(spreadsheet);
        assertNotNull(spreadsheet.getProperties());
        assertNotNull(spreadsheet.getProperties().getTitle());

        var title = spreadsheet.getProperties().getTitle();
        assertEquals(expectedTitleLength, title.length());
        assertTrue(title.startsWith("SomeName_HUMAN_MYSTIC"));
        var timestampStr = title.substring(22);
        assertTrue(timestampStr.matches("[0-9]+"));
    }

    @ParameterizedTest
    @MethodSource("charTypesAndExpectedNumberOfSheets")
    public void createCharacter_GeneratesSpreadsheetWithCorrectNumberOfSheets(CharType charType, int expectedNumSheets) {
        var request = new CharacterCreateRequest("SomeName", charType, SpeciesType.HUMAN);

        Mockito.when(characterCreateRequestValidatorService.validate(request))
                .thenReturn(CharacterCreateStatus.SUCCESS);
        Mockito.when(googleSheetsApiService.createSpreadsheet(any(), any()))
                .thenReturn("");

        var status = googleSheetsCharacterCreateService.createCharacter(request, "");

        final ArgumentCaptor<Spreadsheet> captor = ArgumentCaptor.forClass(Spreadsheet.class);
        verify(googleSheetsApiService).createSpreadsheet(captor.capture(), any());
        final Spreadsheet spreadsheet = captor.getValue();

        assertTrue(status.isSuccess());
        assertNotNull(spreadsheet);
        assertNotNull(spreadsheet.getSheets());
        assertEquals(expectedNumSheets, spreadsheet.getSheets().size());
    }

    static Stream<Arguments> charTypesAndExpectedNumberOfSheets() {
        return Stream.of(
                Arguments.arguments(CharType.BERZERKER, 3),
                Arguments.arguments(CharType.MAGE, 4),
                Arguments.arguments(CharType.MYSTIC, 3),
                Arguments.arguments(CharType.RANGER, 3),
                Arguments.arguments(CharType.ROGUE, 3),
                Arguments.arguments(CharType.SHAMAN, 4),
                Arguments.arguments(CharType.SKALD, 4),
                Arguments.arguments(CharType.WARRIOR, 3)
        );
    }
}
