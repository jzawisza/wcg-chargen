package com.wcg.chargen.backend.worker.impl;

import com.wcg.chargen.backend.enums.AttributeType;
import com.wcg.chargen.backend.enums.FeatureAttributeType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.CommonerService;
import com.wcg.chargen.backend.util.FeatureAttributeUtil;
import com.wcg.chargen.backend.worker.CharacterSheetWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Helper methods for creating character sheets that are used to generate both
 * Google Sheets and PDF sheets.
 */
@Component
public class DefaultCharacterSheetWorker implements CharacterSheetWorker {
    @Autowired
    CharClassesService charClassesService;
    @Autowired
    CommonerService commonerService;

    private static final String SHIELD = "Shield";

    /**
     * Generate a name for the character sheet, i.e. the Google Sheets title or PDF file name.
     *
     * @param request Character create request
     * @return Name for the character sheet
     */
    public String generateName(CharacterCreateRequest request) {
        var currentDateTime = LocalDateTime.now();
        var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // For character names, replace spaces and characters that are not allowed in file names
        // with underscores
        var characterName = (request.characterName() != null) ?
                request.characterName().replaceAll("[\\s\\\\/:*?\"<>|]", "_") :
                "";

        // For professions, replace spaces and slashes with underscores
        // to simplify their representation
        var classOrProfession = request.isCommoner() ?
                request.profession().replaceAll("\\s|/", "_").toUpperCase() :
                request.characterClass().toString().toUpperCase();

        return String.format("%s_%s_%s_%s",
                characterName,
                request.species().toString().toUpperCase(),
                classOrProfession,
                currentDateTime.format(dateTimeFormatter));
    }

    public int getFortunePoints(CharacterCreateRequest request) {
        var luckScore = request.getAttributeValue(AttributeType.LUC);
        // Level 1 characters start with 1 fortune point, and you get 1 more per level
        var fortunePoints = request.level();

        // Halflings get 1 extra fortune point
        if (request.species() == SpeciesType.HALFLING) {
            fortunePoints++;
        }

        // Fortune points can never go below 0
        return Math.max(0, fortunePoints + luckScore);
    }

    public int getBaseEvasion(CharacterCreateRequest request) {
        if (request.isCommoner()) {
            return commonerService.getInfo().evasion();
        }
        else {
            var charClass = charClassesService.getCharClassByType(request.characterClass());
            return charClass.evasionModifiers().get(request.level() - 1);
        }
    }

    public int getEvasionBonus(CharacterCreateRequest request) {
        if (request.isCommoner()) {
            // Commoners don't have anything that would give a bonus to evasion
            return 0;
        }

        var bonusEvasion = 0;
        var charClass = charClassesService.getCharClassByType(request.characterClass());

        // If a character has a shield, they get a +1 bonus to evasion
        if(request.useQuickGear() &&
                charClass.gear().armor().stream()
                        .anyMatch(a -> a.type().equals(SHIELD))) {
            bonusEvasion += 1;
        }

        // If a character has a feature that gives them a bonus to evasion,
        // take that into account
        if (FeatureAttributeUtil.getFeatureNameFromRequestWithAttributeType(charClass.features(),
                request.features(),
                FeatureAttributeType.EV_PLUS_1,
                FeatureAttributeUtil.Tier.I) != null) {
            bonusEvasion += 1;
        }

        if (FeatureAttributeUtil.getFeatureNameFromRequestWithAttributeType(charClass.features(),
                request.features(),
                FeatureAttributeType.EV_PLUS_1,
                FeatureAttributeUtil.Tier.II) != null) {
            bonusEvasion += 1;
        }

        return bonusEvasion;
    }

    /**
     * Returns the ADV or DADV type for a given modifier string, if it exists in the character's features.
     * If no matching feature is found, returns null.
     */
    public FeatureAttributeType getAdvOrDadvByModifier(CharacterCreateRequest request,
                                                        String modifierStr) {
        if (request.features() == null) {
            return null;
        }

        FeatureAttributeType featureAttributeType = null;
        var charClass = charClassesService.getCharClassByType(request.characterClass());

        // Process Tier I and then Tier II features to account for the fact that some classes
        // may give ADV to something as a Tier I feature and DADV as a Tier II feature
        // (e.g. Healing for a shaman), and we want to show DADV in that case
        for (var tier1FeatureName : request.features().tier1()) {
            var tier1FeatureAttributes = charClass.features().tier1().stream()
                    .filter(f -> f.description().equals(tier1FeatureName))
                    .findFirst();

            if (tier1FeatureAttributes.isPresent()) {
                for (var attribute : tier1FeatureAttributes.get().attributes()) {
                    if (attribute.type() == FeatureAttributeType.ADV &&
                            attribute.modifier().equals(modifierStr)) {
                        featureAttributeType = FeatureAttributeType.ADV;
                    }
                    else if (attribute.type() == FeatureAttributeType.DADV &&
                            attribute.modifier().equals(modifierStr)) {
                        featureAttributeType = FeatureAttributeType.DADV;
                    }
                }
            }
        }

        for (var tier2FeatureName : request.features().tier2()) {
            var tier2FeatureAttributes = charClass.features().tier2().stream()
                    .filter(f -> f.description().equals(tier2FeatureName))
                    .findFirst();

            if (tier2FeatureAttributes.isPresent()) {
                for (var attribute : tier2FeatureAttributes.get().attributes()) {
                    if (attribute.type() == FeatureAttributeType.ADV &&
                            attribute.modifier().equals(modifierStr)) {
                        featureAttributeType = FeatureAttributeType.ADV;
                    }
                    else if (attribute.type() == FeatureAttributeType.DADV &&
                            attribute.modifier().equals(modifierStr)) {
                        featureAttributeType = FeatureAttributeType.DADV;
                    }
                }
            }
        }

        return featureAttributeType;
    }
}
