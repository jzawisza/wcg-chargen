package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import com.wcg.chargen.backend.service.PdfCharacterCreateService;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DefaultPdfCharacterCreateServiceTests {
    @Autowired
    private PdfCharacterCreateService pdfCharacterCreateService;
    @MockBean
    CharacterCreateRequestValidatorService characterCreateRequestValidatorService;

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
        // arrange
        Mockito.when(characterCreateRequestValidatorService.validate(DEFAULT_CLASS_CHARACTER_REQUEST))
                .thenReturn(CharacterCreateStatus.SUCCESS);

        // act
        var status = pdfCharacterCreateService.createCharacter(DEFAULT_CLASS_CHARACTER_REQUEST);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());
    }
}
