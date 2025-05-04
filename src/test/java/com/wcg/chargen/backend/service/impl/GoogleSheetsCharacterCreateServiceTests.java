package com.wcg.chargen.backend.service.impl;

import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import com.wcg.chargen.backend.service.GoogleSheetsApiService;
import com.wcg.chargen.backend.service.impl.charCreate.GoogleSheetsCharacterCreateService;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
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

    private static final CharacterCreateRequest DEFAULT_CLASS_CHARACTER_REQUEST = CharacterCreateRequestBuilder
            .getBuilder()
            .withCharacterName("SomeName")
            .withCharacterType(CharType.MYSTIC)
            .withSpeciesType(SpeciesType.HUMAN)
            .withProfession(null)
            .withLevel(1)
            .build();

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
        Mockito.when(characterCreateRequestValidatorService.validate(DEFAULT_CLASS_CHARACTER_REQUEST))
                .thenReturn(CharacterCreateStatus.SUCCESS);
        Mockito.when(googleSheetsApiService.createSpreadsheet(any(), any()))
                .thenReturn(null);

        var status = googleSheetsCharacterCreateService.createCharacter(DEFAULT_CLASS_CHARACTER_REQUEST, "");

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Error creating Google Sheet", status.message());
    }

    @Test
    public void createCharacter_ReturnsExpectedErrorIfExceptionIsThrownDuringProcessing() {
        var expectedErrMsg = "Exception error";

        Mockito.when(characterCreateRequestValidatorService.validate(DEFAULT_CLASS_CHARACTER_REQUEST))
                .thenReturn(CharacterCreateStatus.SUCCESS);
        Mockito.when(googleSheetsApiService.createSpreadsheet(any(), any()))
                .thenThrow(new RuntimeException(expectedErrMsg));

        var status = googleSheetsCharacterCreateService.createCharacter(DEFAULT_CLASS_CHARACTER_REQUEST, "");

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedErrMsg, status.message());
    }

    @Test
    public void createCharacter_GeneratesSpreadsheetWithAppropriateTitleForClassCharacters() {
        // Title should be SomeName_HUMAN_MYSTIC_yyyyMMddHHmmss
        var expectedTitleLength = 36;
        var expectedSpreadsheetId = "aaa-bbb-ccc";

        Mockito.when(characterCreateRequestValidatorService.validate(DEFAULT_CLASS_CHARACTER_REQUEST))
                .thenReturn(CharacterCreateStatus.SUCCESS);
        Mockito.when(googleSheetsApiService.createSpreadsheet(any(), any()))
                .thenReturn(expectedSpreadsheetId);

        var status = googleSheetsCharacterCreateService.createCharacter(DEFAULT_CLASS_CHARACTER_REQUEST, "");

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

    @Test
    public void createCharacter_GeneratesSpreadsheetWithAppropriateTitleForCommonerCharacters() {
        // Title should be SomeName_HUMAN_CARPENTER_yyyyMMddHHmmss
        var expectedTitleLength = 39;
        var expectedSpreadsheetId = "aaa-bbb-ccc";
        var request = CharacterCreateRequestBuilder
                .getBuilder()
                .withCharacterName("SomeName")
                .withCharacterType(null)
                .withSpeciesType(SpeciesType.HUMAN)
                .withProfession("CARPENTER")
                .withLevel(0)
                .build();

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
        assertTrue(title.startsWith("SomeName_HUMAN_CARPENTER"));
        var timestampStr = title.substring(25);
        assertTrue(timestampStr.matches("[0-9]+"));
    }

    @ParameterizedTest
    @MethodSource("charTypesAndExpectedNumberOfSheets")
    public void createCharacter_GeneratesSpreadsheetWithCorrectNumberOfSheets(CharType charType, int expectedNumSheets) {
        var level = (charType != null) ? 1 : 0;
        var profession = (charType == null) ? "Carpenter" : null;
        var request = CharacterCreateRequestBuilder
                .getBuilder()
                .withCharacterName("SomeName")
                .withCharacterType(charType)
                .withSpeciesType(SpeciesType.HUMAN)
                .withProfession(profession)
                .withLevel(level)
                .build();

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
                Arguments.arguments(null, 3),
                Arguments.arguments(CharType.BERZERKER, 3),
                Arguments.arguments(CharType.MAGE, 4),
                Arguments.arguments(CharType.MYSTIC, 3),
                Arguments.arguments(CharType.RANGER, 3),
                Arguments.arguments(CharType.ROGUE, 3),
                Arguments.arguments(CharType.SHAMAN, 4),
                Arguments.arguments(CharType.SKALD, 3),
                Arguments.arguments(CharType.WARRIOR, 3)
        );
    }
}
