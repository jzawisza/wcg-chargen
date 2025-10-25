package com.wcg.chargen.backend.service.impl.charCreate;

import com.wcg.chargen.backend.constants.CharacterSheetConstants;
import com.wcg.chargen.backend.constants.PdfFieldConstants;
import com.wcg.chargen.backend.enums.AttributeType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.PdfCharacterCreateStatus;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import com.wcg.chargen.backend.service.PdfCharacterCreateService;
import com.wcg.chargen.backend.service.SpeciesService;
import com.wcg.chargen.backend.util.PdfUtil;
import com.wcg.chargen.backend.worker.CharacterSheetWorker;
import com.wcg.chargen.backend.worker.impl.DefaultCharacterSheetWorker;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DefaultPdfCharacterCreateService implements PdfCharacterCreateService {
    private static final String PDF_FILE_NAME = "charSheet.pdf";
    private static final int NUM_WEAPONS_ROWS = 3;

    private final Logger logger = LoggerFactory.getLogger(DefaultPdfCharacterCreateService.class);

    @Autowired
    CharacterCreateRequestValidatorService characterCreateRequestValidatorService;
    @Autowired
    SpeciesService speciesService;
    @Autowired
    CharacterSheetWorker characterSheetWorker;
    @Autowired
    CharClassesService charClassesService;

    @Override
    public PdfCharacterCreateStatus createCharacter(CharacterCreateRequest request) {
        var status = characterCreateRequestValidatorService.validate(request);
        if (!status.isSuccess()) {
            // If the request isn't valid, abort here
            return PdfCharacterCreateStatus.error(status.message());
        }

        try (var inputStream = getClass().getClassLoader().getResourceAsStream(PDF_FILE_NAME);
             var pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(inputStream));
             var outputStream = new ByteArrayOutputStream()) {
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.CHARACTER_NAME, request.characterName());
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.LEVEL, String.valueOf(request.level()));
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.SPECIES, request.species().toCharSheetString());

            if (request.isCommoner()) {
                PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.PROFESSION, request.profession());
            }
            else {
                PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.CHARACTER_CLASS,
                        request.characterClass().toCharSheetString());
            }

            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.SPECIES_TRAITS,
                    getSpeciesTraits(request));

            var attributeScores = calculateAttributeScores(request);
            for (var attributeType : AttributeType.values()) {
                var attributeValue = String.valueOf(attributeScores.get(attributeType));
                attributeValue += getAdvOrDadvModifierString(request, attributeType.name());
                PdfUtil.setFieldValue(pdfDocument, attributeType.name(), attributeValue);
            }

            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.FORTUNE_POINTS,
                    String.valueOf(characterSheetWorker.getFortunePoints(request)));

            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.EVASION,
                    getEvasion(request));

            var initiativeStr = getInitiative(request);
            initiativeStr += getAdvOrDadvModifierString(request, CharacterSheetConstants.INITIATIVE);
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.INITIATIVE,
                    initiativeStr);

            for (var i = 0; i < NUM_WEAPONS_ROWS; i++) {
                // The field names in the PDF are 1-based, not 0-based
                var weaponPdfIndex = i + 1;

                var weaponName = characterSheetWorker.getWeaponName(request, i);
                var weaponType = characterSheetWorker.getWeaponType(request, i);
                var weaponDamage = characterSheetWorker.getWeaponDamage(request, i);
                // This will never be null when processing actual requests, but could be
                // null in unit test scenarios
                if (weaponDamage != null) {
                    weaponDamage += getAdvOrDadvModifierString(request, weaponType);
                }

                PdfUtil.setFieldValue(pdfDocument,
                        PdfFieldConstants.WEAPON + weaponPdfIndex,
                        weaponName);
                PdfUtil.setFieldValue(pdfDocument,
                        PdfFieldConstants.WEAPON_TYPE + weaponPdfIndex,
                        weaponType);
                PdfUtil.setFieldValue(pdfDocument,
                        PdfFieldConstants.WEAPON_DAMAGE + weaponPdfIndex,
                        weaponDamage);
            }

            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.ARMOR_TYPE,
                    characterSheetWorker.getArmorType(request, 0));
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.ARMOR_STYLE,
                    characterSheetWorker.getArmorName(request, 0));
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.DAMAGE_ABSORPTION,
                    characterSheetWorker.getArmorDa(request, 0));
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.OFF_HAND_ITEM,
                    getOffHandItem(request));

            var hitPointsStr = String.valueOf(characterSheetWorker.getHitPoints(request));
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.MAX_HIT_POINTS, hitPointsStr);
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.CURRENT_HIT_POINTS, hitPointsStr);

            var equipmentStr = getEquipmentString(request);
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.EQUIPMENT, equipmentStr);
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.CP,
                    String.valueOf(characterSheetWorker.getCopper(request)));
            PdfUtil.setFieldValue(pdfDocument, PdfFieldConstants.SP,
                    String.valueOf(characterSheetWorker.getSilver(request)));

            // Construct and return object representing modified PDF
            pdfDocument.save(outputStream);
            var returnInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            var pdfFileName = characterSheetWorker.generateName(request) + ".pdf";

            return new PdfCharacterCreateStatus(returnInputStream, pdfFileName, null);
        }
        catch (Exception e) {
            logger.error("Error creating PDF character sheet", e);
            return PdfCharacterCreateStatus.error("Error creating PDF character sheet");
        }
    }

    private Map<AttributeType, Integer> calculateAttributeScores(CharacterCreateRequest request) {
        var attributeScores = new HashMap<AttributeType, Integer>();

        for (var attributeType : AttributeType.values()) {
            var attributeValue = request.getAttributeValue(attributeType);
            attributeScores.put(attributeType, attributeValue);
        }

        return attributeScores;
    }

    private String getSpeciesTraits(CharacterCreateRequest request) {
        var species = speciesService.getSpeciesByType(request.species());
        // We need to add an entry to the species traits list for languages,
        // so make a mutable copy of the traits list
        var speciesTraitsList = species.traits() != null ?
                new ArrayList<>(species.traits()) :
                new ArrayList<String>();

        // Generate list of languages as a species trait, since there isn't a section
        // on the character sheet specifically for languages
        var languages = species.languages().stream()
                .collect(Collectors.joining(",", "Languages: ", ""));
        speciesTraitsList.add(languages);

        return String.join("\n", speciesTraitsList);
    }

    private String getEvasion(CharacterCreateRequest request) {
        var baseEvasion = characterSheetWorker.getBaseEvasion(request);
        var evasionBonus = characterSheetWorker.getEvasionBonus(request);

        return String.valueOf(baseEvasion + evasionBonus);
    }

    private String getInitiative(CharacterCreateRequest request) {
        var corScore = request.getAttributeValue(AttributeType.COR);
        var perScore = request.getAttributeValue(AttributeType.PER);
        var initiative = Math.max(corScore, perScore);

        return String.valueOf(initiative);
    }

    private String getAdvOrDadvModifierString(CharacterCreateRequest request, String modifier) {
        var advOrDadv = characterSheetWorker.getAdvOrDadvByModifier(request, modifier);

        if (advOrDadv == null) {
            return "";
        }

        return switch (advOrDadv) {
            case ADV -> " (ADV)";
            case DADV -> " (DADV)";
            default -> "";
        };
    }

    private String getOffHandItem(CharacterCreateRequest request) {
        if (request.isCommoner() || !request.useQuickGear()) {
            return "";
        }

        var charClass = charClassesService.getCharClassByType(request.characterClass());
        var armorList = charClass.gear().armor();
        // We won't have an off-hand item unless there are at least two pieces of armor
        if (armorList.size() < 2) {
            return "";
        }

        var armorType = armorList.get(1).type();
        if (DefaultCharacterSheetWorker.SHIELD.equals(armorType)) {
            return armorList.get(1).name();
        }

        return "";
    }

    private String getEquipmentString(CharacterCreateRequest request) {
        var equipmentList = characterSheetWorker.getEquipmentList(request);

        if (equipmentList == null || equipmentList.isEmpty()) {
            return "";
        }

        return String.join("\n", equipmentList);
    }
}
