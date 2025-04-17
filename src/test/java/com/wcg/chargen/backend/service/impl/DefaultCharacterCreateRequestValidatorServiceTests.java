package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DefaultCharacterCreateRequestValidatorServiceTests {
    private static final int RANDOM_STRING_LENGTH = 16;
    @Autowired
    CharacterCreateRequestValidatorService characterCreateRequestValidatorService;

    @Test
    public void validate_ReturnsFailureIfRequestIsNull() {
        var status = characterCreateRequestValidatorService.validate(null);

        assertNotNull(status);
        assertFalse(status.isSuccess());
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void validate_ReturnsFailureIfRequestHasNullOrEmptyCharacterName(String characterName) {
        var request = new CharacterCreateRequestBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(characterName)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
    }

    @Test
    public void validate_ReturnsFailureIfRequestHasNullCharacterType() {
        var request = new CharacterCreateRequestBuilder()
                .withCharacterType(null)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
    }

    @Test
    public void validate_ReturnsFailureIfRequestHasNullSpeciesType() {
        var request = new CharacterCreateRequestBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(null)
                .withCharacterName(getRandomString())
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
    }

    @Test
    public void validate_ReturnsSuccessIfRequestIsValid() {
        var request = new CharacterCreateRequestBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertTrue(status.isSuccess());
    }

    private String getRandomString() {
        return RandomStringUtils.random(RANDOM_STRING_LENGTH);
    }

    private class CharacterCreateRequestBuilder {
        private String characterName = null;
        private CharType characterType = null;
        private SpeciesType speciesType = null;

        public CharacterCreateRequestBuilder withCharacterName(String characterName) {
            this.characterName = characterName;

            return this;
        }

        public CharacterCreateRequestBuilder withCharacterType(CharType characterType) {
            this.characterType = characterType;

            return this;
        }

        public CharacterCreateRequestBuilder withSpeciesType(SpeciesType speciesType) {
            this.speciesType = speciesType;

            return this;
        }

        public CharacterCreateRequest build() {
            return new CharacterCreateRequest(characterName, characterType, speciesType);
        }
    }
}
