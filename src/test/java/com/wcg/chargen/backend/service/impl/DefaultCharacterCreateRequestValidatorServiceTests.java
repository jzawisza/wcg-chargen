package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DefaultCharacterCreateRequestValidatorServiceTests {
    private static final int RANDOM_STRING_LENGTH = 16;
    private static final String VALID_PROFESSION = "Forester";
    @Autowired
    CharacterCreateRequestValidatorService characterCreateRequestValidatorService;

    @Test
    public void validate_ReturnsFailureIfRequestIsNull() {
        var status = characterCreateRequestValidatorService.validate(null);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Invalid object", status.message());
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void validate_ReturnsFailureIfRequestHasNullOrEmptyCharacterName(String characterName) {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(characterName)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Missing character name", status.message());
    }

    @Test
    public void validate_ReturnsFailureIfRequestHasNullSpeciesType() {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(null)
                .withCharacterName(getRandomString())
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Missing species", status.message());
    }

    @Test
    public void validate_ReturnsFailureIfRequestHasNullLevel() {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.MYSTIC)
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterName("Test")
                .withLevel(null)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Missing level", status.message());
    }

    @ParameterizedTest
    @ValueSource(ints = {-5, -1, 8, 12})
    public void validate_ReturnsFailureIfRequestHasInvalidLevel(int level) {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(level)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Level must be between 0 and 7", status.message());
    }

    @Test
    public void validate_ReturnsFailureIfRequestHasCharacterTypeForCommonerCharacter() {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withProfession(VALID_PROFESSION)
                .withLevel(0)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Level 0 characters cannot have a character class", status.message());
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void validate_ReturnsFailureIfRequestHasNullOrEmptyProfessionForCommonerCharacter(String profession) {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withProfession(profession)
                .withLevel(0)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Level 0 characters must have a profession", status.message());
    }

    @Test
    public void validate_ReturnsFailureIfRequestHasInvalidProfession() {
        var invalidProfession = "Invalid";
        var expectedMessage = "Profession " + invalidProfession + " is not a valid profession";
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withProfession(invalidProfession)
                .withLevel(0)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedMessage, status.message());
    }

    @Test
    public void validate_ReturnsFailureIfRequestHasNullCharacterTypeForClassCharacter() {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(null)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Missing character class", status.message());
    }

    @Test
    public void validate_ReturnsSuccessIfRequestIsValidClassCharacter() {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertTrue(status.isSuccess());
    }

    @Test
    public void validate_ReturnsSuccessIfRequestIsValidCommonerCharacter() {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withProfession(VALID_PROFESSION)
                .withLevel(0)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);
        System.out.println(status.message());

        assertNotNull(status);
        assertTrue(status.isSuccess());
    }

    private String getRandomString() {
        return RandomStringUtils.random(RANDOM_STRING_LENGTH);
    }
}
