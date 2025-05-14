package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.AttributeType;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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
    public void validate_ReturnsFailureIfAttributeMapIsEmpty() {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(new HashMap<>())
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Attributes object is missing required attribute STR", status.message());
    }

    @ParameterizedTest
    @EnumSource(AttributeType.class)
    public void validate_ReturnsFailureIfAttributeMapIsMissingRequiredAttribute(AttributeType attributeType) {
        var expectedMsg = String.format("Attributes object is missing required attribute %s",
                attributeType.name());
        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, 0 , 0, 0, 0 ,0);
        attributesMap.remove(attributeType.name());

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(attributesMap)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedMsg, status.message());
    }

    @ParameterizedTest
    @EnumSource(AttributeType.class)
    public void validate_ReturnsFailureIfCommonerCharacterAttributeValueIsTooLow(AttributeType attributeType) {
        validate_ReturnsFailureIfCommonerCharacterAttributeValueIsInvalid(attributeType, -4);
    }

    @ParameterizedTest
    @EnumSource(AttributeType.class)
    public void validate_ReturnsFailureIfCommonerCharacterAttributeValueIsTooHigh(AttributeType attributeType) {
        validate_ReturnsFailureIfCommonerCharacterAttributeValueIsInvalid(attributeType, 4);
    }

    private void validate_ReturnsFailureIfCommonerCharacterAttributeValueIsInvalid(
            AttributeType attributeType, int invalidAttributeValue) {
        var expectedMsg = String.format("Attribute %s has invalid value %d which is not between -3 and 3",
                attributeType.name(), invalidAttributeValue);
        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, 0 , 0, 0, 0 ,0);
        attributesMap.put(attributeType.name(), invalidAttributeValue);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withProfession(VALID_PROFESSION)
                .withLevel(0)
                .withAttributes(attributesMap)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedMsg, status.message());
    }

    @Test
    public void validate_ReturnsFailureIfCLassCharacterAttributeValuesDoNotMatchExpectedValues() {
        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(0, 0, 0 , 0, 0, 0 ,0);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(attributesMap)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Attribute values do not match challenging or heroic attribute arrays", status.message());
    }

    @Test
    public void validate_ReturnsFailureIfSpeciesStrengthIsSetToInvalidValue() {
        var invalidSpeciesStrength = "Not an attribute";
        var expectedMsg = String.format("Species strength value %s is not a valid attribute type",
                invalidSpeciesStrength);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength(invalidSpeciesStrength)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedMsg, status.message());
    }

    @ParameterizedTest
    @MethodSource("nonHumanSpecies")
    public void validate_ReturnsFailureIfSpeciesWeaknessIsNotSetForNonHumanSpecies(SpeciesType speciesType) {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("COR")
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Non-human characters must specify a species weakness", status.message());
    }

    static Stream<Arguments> nonHumanSpecies() {
        return Stream.of(
                Arguments.arguments(SpeciesType.DWARF),
                Arguments.arguments(SpeciesType.ELF),
                Arguments.arguments(SpeciesType.HALFLING)
        );
    }

    @Test
    public void validate_ReturnsFailureIfSpeciesWeaknessIsSetToInvalidValueForNonHumanSpecies() {
        var invalidSpeciesWeakness = "Not an attribute";
        var expectedMsg = String.format("Species weakness value %s is not a valid attribute type",
                invalidSpeciesWeakness);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("LUC")
                .withSpeciesWeakness(invalidSpeciesWeakness)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedMsg, status.message());
    }

    @Test
    public void validate_ReturnsSuccessIfSpeciesWeaknessIsSetToInvalidValueForHumanSpecies() {
        var invalidSpeciesWeakness = "Not an attribute";
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("LUC")
                .withSpeciesWeakness(invalidSpeciesWeakness)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertTrue(status.isSuccess());
    }

    @Test
    public void validate_ReturnsSuccessIfRequestIsValidClassCharacterWithHeroicArray() {
        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(2, 1, 2, 0, -1, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(attributesMap)
                .withSpeciesStrength(AttributeType.STA.name())
                .withSpeciesWeakness(AttributeType.LUC.name())
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertTrue(status.isSuccess());
    }

    @Test
    public void validate_ReturnsSuccessIfRequestIsValidClassCharacterWithChallengingArray() {
        var attributesMap = CharacterCreateRequestBuilder.getAttributesMap(2, 1, -2, 0, -1, 1, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(attributesMap)
                .withSpeciesStrength(AttributeType.STA.name())
                .withSpeciesWeakness(AttributeType.LUC.name())
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
                .withSpeciesStrength(AttributeType.STA.name())
                .withSpeciesWeakness(AttributeType.LUC.name())
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertTrue(status.isSuccess());
    }

    private String getRandomString() {
        return RandomStringUtils.random(RANDOM_STRING_LENGTH);
    }
}
