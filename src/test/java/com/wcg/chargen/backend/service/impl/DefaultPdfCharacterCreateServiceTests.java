package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.constants.PdfFieldConstants;
import com.wcg.chargen.backend.enums.AttributeType;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.FeatureAttributeType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.*;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import com.wcg.chargen.backend.service.PdfCharacterCreateService;
import com.wcg.chargen.backend.service.SpeciesService;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import com.wcg.chargen.backend.util.PdfUtil;
import com.wcg.chargen.backend.worker.CharacterSheetWorker;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
public class DefaultPdfCharacterCreateServiceTests {
    @Autowired
    private PdfCharacterCreateService pdfCharacterCreateService;
    @MockBean
    CharacterCreateRequestValidatorService characterCreateRequestValidatorService;
    @MockBean
    private SpeciesService speciesService;
    @MockBean
    CharacterSheetWorker characterSheetWorker;
    @MockBean
    CharClassesService charClassesService;

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
        var species = new Species(SpeciesType.HUMAN.toCharSheetString(), null, null, null, null, Collections.emptyList());

        Mockito.when(characterCreateRequestValidatorService.validate(any()))
                .thenReturn(CharacterCreateStatus.SUCCESS);
        Mockito.when(speciesService.getSpeciesByType(any())).thenReturn(species);
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
        Mockito.when(characterSheetWorker.generateName(any())).thenReturn("test");

        // act
        var status = pdfCharacterCreateService.createCharacter(DEFAULT_CLASS_CHARACTER_REQUEST);

        // assert
        assertNotNull(status);
        assertNotNull(status.fileName());
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

    @Test
    public void createCharacter_ReturnsPdfWithCorrectAttributeValues() throws Exception {
        // arrange
        var speciesStrength = "STR";
        var speciesWeakness = "PRS";

        var request = CharacterCreateRequestBuilder
                .getBuilder()
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.DWARF)
                .withLevel(CHARACTER_LEVEL)
                .withAttributes(CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP)
                .withSpeciesStrength(speciesStrength)
                .withSpeciesWeakness(speciesWeakness)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            for (var attribute : AttributeType.values()) {
                var attributeName = attribute.name();
                var expectedValue = CharacterCreateRequestBuilder.VALID_ATTRIBUTES_MAP.get(
                        attributeName);
                // Adjust expected value for species strength or weakness
                if (attributeName.equals(speciesStrength)) {
                    expectedValue += 1;
                }
                else if (attributeName.equals(speciesWeakness)) {
                    expectedValue -= 1;
                }
                var actualValue = PdfUtil.getFieldValue(pdfDocument, attributeName);

                assertEquals(String.valueOf(expectedValue), actualValue);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = FeatureAttributeType.class, names = {"ADV", "DADV"})
    public void createCharacter_DisplaysAdvOrDadvCorrectlyForAttributes(
            FeatureAttributeType featureAttributeType) throws Exception {
        // arrange
        var attributeType = AttributeType.STR;
        // We set the attribute map to all zeroes, so this attribute string will be true
        // for any attribute type
        var expectedAttributeString = "0 (" + featureAttributeType.name() + ")";

        var attributeMap = CharacterCreateRequestBuilder.getAttributesMap(
                0, 0, 0, 0, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MAGE)
                .withAttributes(attributeMap)
                .build();

        Mockito.when(characterSheetWorker.getAdvOrDadvByModifier(any(), eq(attributeType.name())))
                .thenReturn(featureAttributeType);

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualAttributeString = PdfUtil.getFieldValue(pdfDocument, attributeType.name());
            assertEquals(expectedAttributeString, actualAttributeString);
        }
    }

    @Test
    public void createCharacter_ReturnsPdfWithProfessionAndNotCharacterClassForCommonerCharacters()
            throws Exception {
        // arrange
        var expectedProfession = "Farmer";
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withProfession(expectedProfession)
                .withLevel(0)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualProfession = PdfUtil.getFieldValue(pdfDocument, PdfFieldConstants.PROFESSION);
            assertEquals(expectedProfession, actualProfession);

            var charClass = PdfUtil.getFieldValue(pdfDocument, PdfFieldConstants.CHARACTER_CLASS);
            assertEquals("", charClass);
        }
    }

    @Test
    public void createCharacter_ReturnsPdfWithCharacterClassAndNotProfessionForClassCharacters()
            throws Exception {
        // arrange
        var expectedCharClass = CharType.BERZERKER;
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterType(expectedCharClass)
                .withSpeciesType(SpeciesType.HUMAN)
                .withLevel(1)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualCharClass = PdfUtil.getFieldValue(pdfDocument, PdfFieldConstants.CHARACTER_CLASS);
            assertEquals(expectedCharClass.toCharSheetString(), actualCharClass);

            var profession = PdfUtil.getFieldValue(pdfDocument, PdfFieldConstants.PROFESSION);
            assertEquals("", profession);
        }
    }

    @Test
    public void createCharacter_ReturnsLanguagesAsOnlySpeciesTraitIfNoOtherTraitsExist() throws Exception {
        // arrange
        var expectedSpeciesTraitString = "Languages: Human,Common";
        var languages = List.of("Human", "Common");
        var species = new Species(SpeciesType.HUMAN.toCharSheetString(), null, null,
                null, null, languages);

        Mockito.when(speciesService.getSpeciesByType(any())).thenReturn(species);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withLevel(0)
                .withProfession("Profession")
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualSpeciesTraitString = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.SPECIES_TRAITS);
            assertEquals(expectedSpeciesTraitString, actualSpeciesTraitString);
        }
    }

    @Test
    public void createCharacter_ReturnsSpeciesTraitsPlusLanguageInformationIfSpeciesTraitsExist() throws Exception {
        // arrange
        var expectedSpeciesTraitString = "Lowlight Vision\nAura Sense\nLanguages: Elven,Common";
        var traits = List.of("Lowlight Vision", "Aura Sense");
        var languages = List.of("Elven", "Common");
        var species = new Species(SpeciesType.ELF.toCharSheetString(), null, null,
                null, traits, languages);

        Mockito.when(speciesService.getSpeciesByType(any())).thenReturn(species);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.ELF)
                .withLevel(1)
                .withCharacterType(CharType.RANGER)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualSpeciesTraitString = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.SPECIES_TRAITS);
            assertEquals(expectedSpeciesTraitString, actualSpeciesTraitString);
        }
    }

    @Test
    public void createCharacter_ReturnsExpectedEvasion() throws Exception {
        // arrange
        Mockito.when(characterSheetWorker.getBaseEvasion(any())).thenReturn(10);
        Mockito.when(characterSheetWorker.getEvasionBonus(any())).thenReturn(2);
        var expectedEvasionString = "13"; // 10 base + 1 COR (below) + 2 bonus

        // act
        var attributeMap = CharacterCreateRequestBuilder.getAttributesMap(
                0, 1, 0, 0, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MAGE)
                .withLevel(2)
                .withAttributes(attributeMap)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualEvasionString = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.EVASION);
            assertEquals(expectedEvasionString, actualEvasionString);
        }
    }

    @Test
    public void createCharacter_ReturnsErrorStatusIfExceptionIsThrown() {
        // arrange
        Mockito.when(characterCreateRequestValidatorService.validate(any()))
                .thenReturn(CharacterCreateStatus.SUCCESS);
        Mockito.when(speciesService.getSpeciesByType(any()))
                .thenThrow(new RuntimeException("Some exception"));

        // act
        var status = pdfCharacterCreateService.createCharacter(DEFAULT_CLASS_CHARACTER_REQUEST);

        // assert
        assertNotNull(status);
        assertNotNull(status.errMsg());
        assertTrue(status.errMsg().contains("Error creating PDF character sheet"));
    }

    @ParameterizedTest
    @CsvSource({
            "2, 1, 2",
            "1, 3, 3",
            "1, 1, 1"
    })
    public void createCharacter_ReturnsPdfWithExpectedInitiative(int corScore, int perScore,
                                                                 int expectedInitiative)
            throws Exception {
        // arrange
        var attributeMap = CharacterCreateRequestBuilder.getAttributesMap(
                0, corScore, 0, perScore, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MAGE)
                .withAttributes(attributeMap)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualInitiativeString = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.INITIATIVE);
            assertEquals(Integer.toString(expectedInitiative), actualInitiativeString);
        }
    }

    @ParameterizedTest
    @EnumSource(value = FeatureAttributeType.class, names = {"ADV", "DADV"})
    public void createCharacter_DisplaysAdvOrDadvCorrectlyForInitiative(
            FeatureAttributeType featureAttributeType) throws Exception {
        // arrange
        var perScore = 1;
        var expectedInitiativeString = Integer.toString(perScore) + " (" +
                featureAttributeType.name() + ")";

        var attributeMap = CharacterCreateRequestBuilder.getAttributesMap(
                0, 0, 0, perScore, 0, 0, 0);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MAGE)
                .withAttributes(attributeMap)
                .build();

        Mockito.when(characterSheetWorker.getAdvOrDadvByModifier(any(), any()))
                .thenReturn(featureAttributeType);

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualInitiativeString = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.INITIATIVE);
            assertEquals(expectedInitiativeString, actualInitiativeString);
        }
    }

    @Test
    public void createCharacter_ReturnsExpectedMaxHpAndCurrentHp() throws Exception {
        // arrange
        var expectedHitPoints = 10;

        Mockito.when(characterSheetWorker.getHitPoints(any())).thenReturn(expectedHitPoints);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MAGE)
                .withLevel(1)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualMaxHpString = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.MAX_HIT_POINTS);
            assertEquals(Integer.toString(expectedHitPoints), actualMaxHpString);

            var actualCurrentHpString = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.CURRENT_HIT_POINTS);
            assertEquals(Integer.toString(expectedHitPoints), actualCurrentHpString);
        }
    }

    @Test
    public void createCharacter_ReturnsExpectedWeaponInfo() throws Exception {
        // arrange
        var weaponIndex = 0;
        var expectedWeaponName = "Sword";
        var expectedWeaponType = "Melee";
        var expectedWeaponDamage = "1d8";

        Mockito.when(characterSheetWorker.getWeaponName(any(), eq(weaponIndex)))
                .thenReturn(expectedWeaponName);
        Mockito.when(characterSheetWorker.getWeaponType(any(), eq(weaponIndex)))
                .thenReturn(expectedWeaponType);
        Mockito.when(characterSheetWorker.getWeaponDamage(any(), eq(weaponIndex)))
                .thenReturn(expectedWeaponDamage);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.WARRIOR)
                .withLevel(1)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualWeaponName = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.WEAPON + (weaponIndex + 1));
            assertEquals(expectedWeaponName, actualWeaponName);

            var actualWeaponType = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.WEAPON_TYPE + (weaponIndex + 1));
            assertEquals(expectedWeaponType, actualWeaponType);

            var actualWeaponDamage = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.WEAPON_DAMAGE + (weaponIndex + 1));
            assertEquals(expectedWeaponDamage, actualWeaponDamage);

            // Remaining weapons rows should be empty
            actualWeaponName = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.WEAPON + (weaponIndex + 2));
            assertEquals("", actualWeaponName);

            actualWeaponType = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.WEAPON_TYPE + (weaponIndex + 2));
            assertEquals("", actualWeaponType);

            actualWeaponDamage = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.WEAPON_DAMAGE + (weaponIndex + 2));
            assertEquals("", actualWeaponDamage);

            actualWeaponName = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.WEAPON + (weaponIndex + 3));
            assertEquals("", actualWeaponName);

            actualWeaponType = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.WEAPON_TYPE + (weaponIndex + 3));
            assertEquals("", actualWeaponType);

            actualWeaponDamage = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.WEAPON_DAMAGE + (weaponIndex + 3));
            assertEquals("", actualWeaponDamage);
        }
    }

    @ParameterizedTest
    @EnumSource(value = FeatureAttributeType.class, names = {"ADV", "DADV"})
    public void createCharacter_DisplaysAdvOrDadvCorrectlyForWeaponDamage(
            FeatureAttributeType featureAttributeType) throws Exception {
        // arrange
        var weaponIndex = 0;
        var weaponType = "Unarmed";
        var weaponDamage = "1d6";
        var expectedCharSheetWeaponDamage = weaponDamage + " (" + featureAttributeType.name() + ")";

        Mockito.when(characterSheetWorker.getWeaponType(any(), eq(weaponIndex)))
                .thenReturn(weaponType);
        Mockito.when(characterSheetWorker.getWeaponDamage(any(), eq(weaponIndex)))
                .thenReturn(weaponDamage);

        Mockito.when(characterSheetWorker.getAdvOrDadvByModifier(any(), eq(weaponType)))
                .thenReturn(featureAttributeType);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MYSTIC)
                .withLevel(1)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualWeaponDamage = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.WEAPON_DAMAGE + (weaponIndex + 1));
            assertEquals(expectedCharSheetWeaponDamage, actualWeaponDamage);
        }
    }

    @Test
    public void createCharacter_ReturnsExpectedArmorInfo() throws Exception {
        // arrange
        var expectedArmorName = "Leather";
        var expectedArmorType = "Light";
        var expectedArmorDa = "3";

        Mockito.when(characterSheetWorker.getArmorName(any(), eq(0))).thenReturn(expectedArmorName);
        Mockito.when(characterSheetWorker.getArmorType(any(), eq(0))).thenReturn(expectedArmorType);
        Mockito.when(characterSheetWorker.getArmorDa(any(), eq(0))).thenReturn(expectedArmorDa);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.MYSTIC)
                .withLevel(1)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualArmorName = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.ARMOR_STYLE);
            assertEquals(expectedArmorName, actualArmorName);

            var actualArmorType = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.ARMOR_TYPE);
            assertEquals(expectedArmorType, actualArmorType);

            var actualArmorDa = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.DAMAGE_ABSORPTION);
            assertEquals(expectedArmorDa, actualArmorDa);
        }
    }

    @Test
    public void createCharacter_ReturnsEmptyStringForOffHandItemForCommonerCharacters() throws Exception {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withLevel(0)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualOffHandItem = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.OFF_HAND_ITEM);
            assertEquals("", actualOffHandItem);
        }
    }

    @Test
    public void createCharacter_ReturnsEmptyStringForOffHandItemForCharactersWithoutQuickGear()
            throws Exception {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.SKALD)
                .withLevel(1)
                .withUseQuickGear(false)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualOffHandItem = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.OFF_HAND_ITEM);
            assertEquals("", actualOffHandItem);
        }
    }

    @Test
    public void createCharacter_ReturnsEmptyStringForOffHandItemForCharactersWithOnePieceOfAmor()
            throws Exception {
        // arrange
        var armor = new Armor("Leather", "Light", "3");
        var gear = new Gear(List.of(armor), null, 0, 0, null);
        var charClass = new CharClass(CharType.SKALD.toString(),
                null,
                null,
                0,
                0,
                null,
                gear,
                null,
                null);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.SKALD)
                .withLevel(1)
                .withUseQuickGear(true)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualOffHandItem = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.OFF_HAND_ITEM);
            assertEquals("", actualOffHandItem);
        }
    }

    @Test
    public void createCharacter_ReturnsExpectedOffHandItemForCharactersWithShieldAsSecondPieceOfArmor()
            throws Exception {
        // arrange
        var expectedOffHandItem = "Hoplite Shield";
        var armor = new Armor("Leather", "Light", "3");
        var shield = new Armor(expectedOffHandItem, "Shield", "All from one hit");
        var gear = new Gear(List.of(armor, shield), null, 0, 0, null);
        var charClass = new CharClass(CharType.SKALD.toString(),
                null,
                null,
                0,
                0,
                null,
                gear,
                null,
                null);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.SKALD)
                .withLevel(1)
                .withUseQuickGear(true)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualOffHandItem = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.OFF_HAND_ITEM);
            assertEquals(expectedOffHandItem, actualOffHandItem);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void createCharacter_ReturnsEmptyStringForEquipmentListIfListIsNullOrEmpty(
            List<String> equipmentList) throws Exception {
        // arrange
        Mockito.when(characterSheetWorker.getEquipmentList(any())).thenReturn(equipmentList);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.SKALD)
                .withLevel(1)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualEquipmentStr = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.EQUIPMENT);
            assertEquals("", actualEquipmentStr);
        }
    }

    @Test
    public void createCharacter_ReturnsExpectedEquipmentListStringIfListHasItems() throws Exception {
        // arrange
        var equipmentList = List.of("Rope", "Torch", "Backpack");
        var expectedEquipmentStr = String.join("\n", equipmentList);
        Mockito.when(characterSheetWorker.getEquipmentList(any())).thenReturn(equipmentList);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.SKALD)
                .withLevel(1)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualEquipmentStr = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.EQUIPMENT);
            assertEquals(expectedEquipmentStr, actualEquipmentStr);
        }
    }

    @Test
    public void createCharacter_ReturnsExpectedCopperAndSilver() throws Exception {
        // arrange
        var expectedCopper = 10;
        var expectedSilver = 2;
        Mockito.when(characterSheetWorker.getCopper(any())).thenReturn(expectedCopper);
        Mockito.when(characterSheetWorker.getSilver(any())).thenReturn(expectedSilver);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.SKALD)
                .withLevel(1)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualCopperStr = PdfUtil.getFieldValue(pdfDocument, PdfFieldConstants.CP);
            var actualSilverStr = PdfUtil.getFieldValue(pdfDocument, PdfFieldConstants.SP);

            assertEquals(String.valueOf(expectedCopper), actualCopperStr);
            assertEquals(String.valueOf(expectedSilver), actualSilverStr);
        }
    }

    @Test
    public void createCharacter_ReturnsEmptyStringForSpellModForNonMagicUsingCharacters()
        throws Exception {
        // arrange
        Mockito.when(characterSheetWorker.hasMagic(any())).thenReturn(false);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.WARRIOR)
                .withLevel(1)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualSpellMod = PdfUtil.getFieldValue(pdfDocument, PdfFieldConstants.SPELL_MOD);
            assertEquals("", actualSpellMod);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "MAGE, '+2'",
            "SHAMAN, '-1'",
            "SKALD, '+2 (Mage), -1 (Shaman)'"
    })
    public void createCharacter_ReturnsExpectedSpellModForMagicUsingCharacters(CharType charType,
                                                                               String expectedSpellMod)
        throws Exception {
        // arrange
        var intModifier = 1;
        var prsModifier = -2;
        var attackModifier = 1;

        var charClass = new CharClass(charType.toString(),
                List.of(0, attackModifier, 0, 0, 0, 0, 0),
                null,
                0,
                0,
                null,
                null,
                null,
                null);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);
        Mockito.when(characterSheetWorker.hasMagic(any())).thenReturn(true);

        var attributeMap = CharacterCreateRequestBuilder.getAttributesMap(
                0, 0, 0, 0, intModifier, prsModifier, 0);
        // Level must be 2 to match the charClass attack modifier list, which has zeroes
        // for every element except the second one
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(charType)
                .withAttributes(attributeMap)
                .withLevel(2)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualSpellMod = PdfUtil.getFieldValue(pdfDocument, PdfFieldConstants.SPELL_MOD);
            assertEquals(expectedSpellMod, actualSpellMod);
        }
    }

    @Test
    public void createCharacter_ReturnsEmptyStringForClassFeaturesForCommonerCharacters()
        throws Exception {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withLevel(0)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualClassFeatures = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.CLASS_FEATURES);
            assertEquals("", actualClassFeatures);
        }
    }

    @Test
    public void createCharacter_ReturnsExpectedClassFeaturesForClassCharacters()
        throws Exception {
        // arrange
        var abilitiesList = List.of("Test Feature 1", "Test Feature 2", "Test Feature 3");
        var expectedClassFeatures = String.join("\n", abilitiesList);

        var charClass = new CharClass(CharType.BERZERKER.toString(),
                null,
                null,
                0,
                0,
                null,
                null,
                abilitiesList,
                null);

        Mockito.when(charClassesService.getCharClassByType(any())).thenReturn(charClass);

        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.BERZERKER)
                .withLevel(1)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualClassFeatures = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.CLASS_FEATURES);
            assertEquals(expectedClassFeatures, actualClassFeatures);
        }
    }

    @Test
    public void createCharacter_ReturnsEmptyStringForAdvancedFeaturesIfRequestHasNullFeatures()
            throws Exception {
        // arrange
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.SHAMAN)
                .withLevel(1)
                .withFeatures(null)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var advancedFeatures = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.TIER_I_II_FEATURES);
            assertEquals("", advancedFeatures);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void createCharacter_ReturnsEmptyStringForAdvancedFeaturesIfRequestHasNullOrEmptyTierIAndTierIIFeatures(
            List<String> features) throws Exception {
        // arrange
        var featuresRequest = new FeaturesRequest(features, features);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.SHAMAN)
                .withLevel(1)
                .withFeatures(featuresRequest)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var advancedFeatures = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.TIER_I_II_FEATURES);
            assertEquals("", advancedFeatures);
        }
    }

    @Test
    public void createCharacter_ReturnsExpectedAdvancedFeaturesStringForTierIAndTierIIFeatures()
            throws Exception {
        // arrange
        var expectedAdvancedFeatures = "Tier I Features:\n- Tier 1 Feature A\n- Tier 1 Feature B\n" +
                "Tier II Features:\n- Tier 2 Feature C\n- Tier 2 Feature D\n";
        var tier1FeatureList = List.of("Tier 1 Feature A", "Tier 1 Feature B");
        var tier2FeatureList = List.of("Tier 2 Feature C", "Tier 2 Feature D");
        var featuresRequest = new FeaturesRequest(tier1FeatureList, tier2FeatureList);
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withSpeciesType(SpeciesType.HUMAN)
                .withCharacterType(CharType.SHAMAN)
                .withLevel(4)
                .withFeatures(featuresRequest)
                .build();

        // act
        var status = pdfCharacterCreateService.createCharacter(request);

        // assert
        assertNotNull(status);
        assertNotNull(status.pdfStream());

        try (var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(status.pdfStream()))) {
            var actualAdvancedFeatures = PdfUtil.getFieldValue(pdfDocument,
                    PdfFieldConstants.TIER_I_II_FEATURES);
            assertEquals(expectedAdvancedFeatures, actualAdvancedFeatures);
        }
    }

}
