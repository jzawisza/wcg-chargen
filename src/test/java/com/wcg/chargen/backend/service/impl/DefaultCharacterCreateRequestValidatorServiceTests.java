package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.AttributeType;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.*;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import com.wcg.chargen.backend.service.SpeciesService;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import com.wcg.chargen.backend.worker.SkillsProvider;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class DefaultCharacterCreateRequestValidatorServiceTests {
    private static final int RANDOM_STRING_LENGTH = 16;
    private static final String VALID_PROFESSION = "Forester";
    private static final String TIER1_FEATURE = "Tier I Feature";
    private static final String TIER2_FEATURE = "Tier II Feature";

    @Autowired
    CharacterCreateRequestValidatorService characterCreateRequestValidatorService;
    @MockBean
    SpeciesService speciesService;
    @Autowired
    SkillsProvider skillsProvider;
    @MockBean
    CharClassesService charClassesService;

    @BeforeEach
    public void setup() {
        var dwarfStrengths = Arrays.asList("STR", "STA");
        var dwarfWeaknesses = Arrays.asList("PRS", "LUC");
        var dwarfSkills = Arrays.asList("Appraisal", "Athletics", "Intimidation");
        var species = new Species("dwarf", dwarfStrengths, dwarfWeaknesses, dwarfSkills, null, Collections.emptyList());

        Mockito.when(speciesService.getSpeciesByType(any())).thenReturn(species);

        var tier1Feature = new Feature(TIER1_FEATURE, null);
        var tier2Feature = new Feature(TIER2_FEATURE, null);
        var features = new Features(List.of(tier1Feature), List.of(tier2Feature));
        var charClass = new CharClass(null, null, null, null, null,
                null, null, null, features);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);
    }

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
                .withBonusSkills(Arrays.asList("Healing", "History"))
                .withUseQuickGear(false)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertTrue(status.isSuccess());
    }

    @Test
    public void validate_ReturnsFailureIfSpeciesStrengthIsNotInSpeciesStrengthsListForNonHumanSpecies() {
        var requestSpeciesStrength = "LUC";
        var expectedMsg = String.format("Species strength %s is not valid for species dwarf",
                requestSpeciesStrength);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength(requestSpeciesStrength)
                .withSpeciesWeakness("PRS")
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedMsg, status.message());
    }

    @Test
    public void validate_ReturnsFailureIfSpeciesWeaknessIsNotInSpeciesWeaknessesListForNonHumanSpecies() {
        var requestSpeciesWeakness = "INT";
        var expectedMsg = String.format("Species weakness %s is not valid for species dwarf",
                requestSpeciesWeakness);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness(requestSpeciesWeakness)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedMsg, status.message());
    }

    @Test
    public void validate_ReturnsFailureIfSpeciesSkillIsNotInSpeciesSkillsListForNonHumanSpecies() {
        var invalidSkill = "Invalid";
        var expectedMsg = String.format("Species skill %s is not valid for species dwarf", invalidSkill);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withSpeciesSkill(invalidSkill)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedMsg, status.message());
    }

    @Test
    public void validate_ReturnsFailureIfClassCharacterBonusSkillsAreNull() {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withSpeciesSkill("Athletics")
                .withBonusSkills(null)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Bonus skills cannot be null for characters Level 1 and above", status.message());
    }
    @ParameterizedTest
    @MethodSource("speciesAndBonusSkillsTestCases")
    public void validate_ReturnsExpectedStatusBasedOnNumberOfBonusSkillsPerSpecies(
            SpeciesType speciesType, List<String> bonusSkillsList, boolean expectedStatus) {
        var expectedMsg = "";
        if (!expectedStatus) {
            var expectedBonusSkills = (speciesType == SpeciesType.HUMAN) ? 2 : 1;
            var speciesClass = (speciesType == SpeciesType.HUMAN) ? "human" : "non-human";
            expectedMsg = String.format("Expected %d bonus skills for %s species, got %d",
                    expectedBonusSkills, speciesClass, bonusSkillsList.size());
        }

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(speciesType)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withSpeciesSkill("Athletics")
                .withBonusSkills(bonusSkillsList)
                .withUseQuickGear(false)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertEquals(expectedStatus, status.isSuccess());
        assertEquals(expectedMsg, status.message());
    }

    static Stream<Arguments> speciesAndBonusSkillsTestCases() {
        return Stream.of(
                Arguments.arguments(SpeciesType.DWARF, List.of("Healing"), true),
                Arguments.arguments(SpeciesType.DWARF, Collections.emptyList(), false),
                Arguments.arguments(SpeciesType.DWARF, List.of("Healing", "History"), false),
                Arguments.arguments(SpeciesType.ELF, List.of("Healing"), true),
                Arguments.arguments(SpeciesType.ELF, Collections.emptyList(), false),
                Arguments.arguments(SpeciesType.ELF, List.of("Healing", "History"), false),
                Arguments.arguments(SpeciesType.HALFLING, List.of("Healing"), true),
                Arguments.arguments(SpeciesType.HALFLING, Collections.emptyList(), false),
                Arguments.arguments(SpeciesType.HALFLING, List.of("Healing", "History"), false),
                Arguments.arguments(SpeciesType.HUMAN, List.of("Healing", "History"), true),
                Arguments.arguments(SpeciesType.HUMAN, Collections.emptyList(), false),
                Arguments.arguments(SpeciesType.HUMAN, List.of("Healing"), false),
                Arguments.arguments(SpeciesType.HUMAN, List.of("Healing", "History", "Languages"), false)
        );
    }

    @Test
    public void validate_ReturnsFailureIfBonusSkillIsNotInMasterSkillsList() {
        var invalidBonusSkill = "Invalid";
        var expectedMsg = String.format("Bonus skill %s is not a valid skill", invalidBonusSkill);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withSpeciesSkill("Athletics")
                .withBonusSkills(List.of(invalidBonusSkill))
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedMsg, status.message());
    }

    @Test
    public void validate_ReturnsFailureIfUseQuickGearIsNotSpecifiedForCharacterClass() {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withSpeciesSkill("Athletics")
                .withBonusSkills(List.of("Intimidation"))
                .withUseQuickGear(null)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Use quick gear field must be specified for class characters", status.message());
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
                .withSpeciesSkill("Athletics")
                .withBonusSkills(List.of("Healing"))
                .withUseQuickGear(true)
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
                .withSpeciesSkill("Athletics")
                .withBonusSkills(List.of("Healing"))
                .withUseQuickGear(false)
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

    @Test
    public void validate_ReturnsFailureIfFeaturesSpecifiedForCommonerCharacter() {
        var featuresRequest = new FeaturesRequest(Collections.emptyList(), Collections.emptyList());

        var requestCommoner = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withProfession(VALID_PROFESSION)
                .withLevel(0)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withFeatures(featuresRequest)
                .build();

        var status = characterCreateRequestValidatorService.validate(requestCommoner);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Features cannot be specified for commoner or Level 1 characters", status.message());
    }

    @Test
    public void validate_ReturnsFailureIfFeaturesSpecifiedForLevel1Character() {
        var featuresRequest = new FeaturesRequest(Collections.emptyList(), Collections.emptyList());

        var requestLevel1 = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(1)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withSpeciesSkill("Appraisal")
                .withBonusSkills(List.of("Healing"))
                .withFeatures(featuresRequest)
                .build();

        var status = characterCreateRequestValidatorService.validate(requestLevel1);
        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Features cannot be specified for commoner or Level 1 characters", status.message());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6, 7})
    public void validate_ReturnsFailureIfFeaturesInformationIsNullForClassCharactersLevel2AndAbove(int level) {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(level)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withSpeciesSkill("Appraisal")
                .withBonusSkills(List.of("Healing"))
                .withFeatures(null)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Features must be specified for characters Level 2 and above", status.message());

    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6, 7})
    public void validate_ReturnsFailureIfTier1FeaturesAreNullForClassCharactersLevel2AndAbove(int level) {
        var featuresRequest = new FeaturesRequest(null, Collections.emptyList());

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(level)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withSpeciesSkill("Appraisal")
                .withBonusSkills(List.of("Healing"))
                .withFeatures(featuresRequest)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Tier I features for characters level 2 and above cannot be null", status.message());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6, 7})
    public void validate_ReturnsFailureIfTier2FeaturesAreNullForClassCharactersLevel2AndAbove(int level) {
        var featuresRequest = new FeaturesRequest(Collections.emptyList(), null);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(level)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withSpeciesSkill("Appraisal")
                .withBonusSkills(List.of("Healing"))
                .withFeatures(featuresRequest)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals("Tier II features for characters level 2 and above cannot be null", status.message());
    }

    @ParameterizedTest
    @MethodSource("levelsAndExpectedNumberOfTier1AndTier2Features")
    public void validate_ReturnsFailureIfIncorrectNumberOfTier1FeaturesForClassCharactersLevel2AndAbove(
            int level, int expectedNumTier1Features) {
        var featuresRequest = new FeaturesRequest(List.of("feature1", "feature2"),
                Collections.emptyList());
        var expectedMsg = String.format("Expected %d tier I features for level %d, got 2",
                expectedNumTier1Features, level);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(level)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withSpeciesSkill("Appraisal")
                .withBonusSkills(List.of("Healing"))
                .withFeatures(featuresRequest)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedMsg, status.message());
    }

    @ParameterizedTest
    @MethodSource("levelsAndExpectedNumberOfTier1AndTier2Features")
    public void validate_ReturnsFailureIfIncorrectNumberOfTier2FeaturesForClassCharactersLevel2AndAbove(
            int level, int expectedNumTier1Features, int expectedNumTier2Features) {
        var tier1FeatureList = new ArrayList<>(Collections.nCopies(expectedNumTier1Features, "tier1feature"));
        var invalidNumTier2Features = 5;
        var tier2FeatureList = new ArrayList<>(Collections.nCopies(invalidNumTier2Features, "tier2feature"));
        var featuresRequest = new FeaturesRequest(tier1FeatureList, tier2FeatureList);
        var expectedMsg = String.format("Expected %d tier II features for level %d, got %d",
                expectedNumTier2Features, level, invalidNumTier2Features);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(level)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withSpeciesSkill("Appraisal")
                .withBonusSkills(List.of("Healing"))
                .withFeatures(featuresRequest)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedMsg, status.message());
    }

    @ParameterizedTest
    @MethodSource("levelsAndExpectedNumberOfTier1AndTier2Features")
    public void validate_ReturnsFailureIfTier1FeatureIsInvalidForClassCharactersLevel2AndAbove(
            int level, int expectedNumTier1Features, int expectedNumTier2Features) {
        var invalidFeature = "Invalid feature";
        var tier1FeatureList = new ArrayList<>(Collections.nCopies(expectedNumTier1Features - 1,
                TIER1_FEATURE));
        tier1FeatureList.add(invalidFeature);
        var tier2FeatureList = new ArrayList<>(Collections.nCopies(expectedNumTier2Features, TIER2_FEATURE));
        var featuresRequest = new FeaturesRequest(tier1FeatureList, tier2FeatureList);
        var expectedMsg = String.format("Tier I feature %s is not valid for class %s",
                invalidFeature, CharType.BERZERKER);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(level)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withSpeciesSkill("Appraisal")
                .withBonusSkills(List.of("Healing"))
                .withFeatures(featuresRequest)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedMsg, status.message());
    }

    @ParameterizedTest
    @MethodSource("levelsAndExpectedNumberOfTier1AndTier2Features")
    public void validate_ReturnsFailureIfTier2FeatureIsInvalidForClassCharactersLevel2AndAbove(
            int level, int expectedNumTier1Features, int expectedNumTier2Features) {
        if (expectedNumTier2Features == 0) {
            // Skip this test if we have no Tier II features for this level
            return;
        }

        var invalidFeature = "Invalid feature";
        var tier1FeatureList = new ArrayList<>(Collections.nCopies(expectedNumTier1Features, TIER1_FEATURE));
        var tier2FeatureList = new ArrayList<>(Collections.nCopies(expectedNumTier2Features - 1,
                TIER2_FEATURE));
        tier2FeatureList.add(invalidFeature);
        var featuresRequest = new FeaturesRequest(tier1FeatureList, tier2FeatureList);
        var expectedMsg = String.format("Tier II feature %s is not valid for class %s",
                invalidFeature, CharType.BERZERKER);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withCharacterName(getRandomString())
                .withLevel(level)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength("STR")
                .withSpeciesWeakness("LUC")
                .withSpeciesSkill("Appraisal")
                .withBonusSkills(List.of("Healing"))
                .withFeatures(featuresRequest)
                .build();

        var status = characterCreateRequestValidatorService.validate(request);

        assertNotNull(status);
        assertFalse(status.isSuccess());
        assertEquals(expectedMsg, status.message());
    }

    static Stream<Arguments> levelsAndExpectedNumberOfTier1AndTier2Features() {
        return Stream.of(
                Arguments.arguments(2, 1, 0),
                Arguments.arguments(3, 3, 0),
                Arguments.arguments(4, 3, 1),
                Arguments.arguments(5, 3, 2),
                Arguments.arguments(6, 4, 3),
                Arguments.arguments(7, 5, 4)
        );
    }


    private String getRandomString() {
        return RandomStringUtils.random(RANDOM_STRING_LENGTH);
    }
}
