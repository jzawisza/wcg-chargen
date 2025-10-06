package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.constants.PdfFieldConstants;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import com.wcg.chargen.backend.service.PdfCharacterCreateService;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import com.wcg.chargen.backend.util.PdfUtil;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class DefaultPdfCharacterCreateServiceTests {
    @Autowired
    private PdfCharacterCreateService pdfCharacterCreateService;
    @MockBean
    CharacterCreateRequestValidatorService characterCreateRequestValidatorService;

    private static final String CHARACTER_NAME = "SomeName";
    private static final int CHARACTER_LEVEL = 1;

    private static final CharacterCreateRequest DEFAULT_CLASS_CHARACTER_REQUEST = CharacterCreateRequestBuilder
            .getBuilder()
            .withCharacterName(CHARACTER_NAME)
            .withCharacterType(CharType.MYSTIC)
            .withSpeciesType(SpeciesType.HUMAN)
            .withProfession(null)
            .withLevel(CHARACTER_LEVEL)
            .build();

    @BeforeEach
    public void beforeTest() {
        Mockito.when(characterCreateRequestValidatorService.validate(any()))
                .thenReturn(CharacterCreateStatus.SUCCESS);
    }

    @Test
    public void createCharacter_ReturnsFailureIfValidationFails() {
        // arrange
        var expectedErrMsg = "Some error";

        Mockito.when(characterCreateRequestValidatorService.validate(null))
                .thenReturn(new CharacterCreateStatus(false, expectedErrMsg));

        // act
        var status = pdfCharacterCreateService.createCharacter(null);

        // assert
        assertNotNull(status);
        assertEquals(expectedErrMsg, status.errMsg());
    }

    @Test
    public void createCharacter_ReturnsPdfDataIfValidationIsSuccessful() {
        // act
        var status = pdfCharacterCreateService.createCharacter(DEFAULT_CLASS_CHARACTER_REQUEST);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());
    }

    @Test
    public void createCharacter_ReturnsExpectedPdfNameOnSuccess() {
        // arrange
        // File name should be SomeName_HUMAN_MYSTIC_yyyyMMddHHmmss.pdf
        var expectedFileNameLength = 40;

        // act
        var status = pdfCharacterCreateService.createCharacter(DEFAULT_CLASS_CHARACTER_REQUEST);

        // assert
        assertNotNull(status);
        assertNotNull(status.fileName());
        assertEquals(expectedFileNameLength, status.fileName().length());
        assertTrue(status.fileName().startsWith("SomeName_HUMAN_MYSTIC_"));
        var timestampStr = status.fileName().substring(22, 36);
        assertTrue(timestampStr.matches("[0-9]+"));
        assertTrue(status.fileName().endsWith(".pdf"));
    }

    @Test
    public void createCharacter_ReturnsPdfWithCorrectCharacterName() throws Exception {
        // act
        var status = pdfCharacterCreateService.createCharacter(DEFAULT_CLASS_CHARACTER_REQUEST);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualCharacterName = PdfUtil.getFieldValue(pdfDocument, PdfFieldConstants.CHARACTER_NAME);

            assertEquals(CHARACTER_NAME, actualCharacterName);
        }
    }

    @Test
    public void createCharacter_ReturnsPdfWithCorrectLevel() throws Exception {
        // act
        var status = pdfCharacterCreateService.createCharacter(DEFAULT_CLASS_CHARACTER_REQUEST);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualLevel = PdfUtil.getFieldValue(pdfDocument, PdfFieldConstants.LEVEL);

            assertEquals(String.valueOf(CHARACTER_LEVEL), actualLevel);
        }
    }

    @ParameterizedTest
    @EnumSource(SpeciesType.class)
    public void createCharacter_ReturnsPdfWithCorrectSpecies(SpeciesType speciesType) throws Exception {
        // arrange
        var request = CharacterCreateRequestBuilder
                .getBuilder()
                .withCharacterType(CharType.MYSTIC)
                .withSpeciesType(speciesType)
                .withProfession(null)
                .withLevel(CHARACTER_LEVEL)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualSpecies = PdfUtil.getFieldValue(pdfDocument, PdfFieldConstants.SPECIES);

            assertEquals(speciesType.toCharSheetString(), actualSpecies);
        }
    }
}
