package com.wcg.chargen.backend.worker.impl;

import com.wcg.chargen.backend.enums.AttributeType;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.FeatureAttributeType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.Feature;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.CommonerService;
import com.wcg.chargen.backend.util.FeatureAttributeUtil;
import com.wcg.chargen.backend.worker.CharacterSheetWorker;
import com.wcg.chargen.backend.worker.RandomNumberWorker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Helper methods for creating character sheets that are used to generate both
 * Google Sheets and PDF sheets.
 */
@Component
public class DefaultCharacterSheetWorker implements CharacterSheetWorker {
    private static final String UNARMED_WEAPON_TYPE = "Unarmed";

    private final Logger logger = LoggerFactory.getLogger(DefaultCharacterSheetWorker.class);

    @Autowired
    CharClassesService charClassesService;
    @Autowired
    CommonerService commonerService;
    @Autowired
    RandomNumberWorker randomNumberWorker;

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

    public int getHitPoints(CharacterCreateRequest characterCreateRequest) {
        var staminaScore = characterCreateRequest.getAttributeValue(AttributeType.STA);
        if (characterCreateRequest.isCommoner()) {
            var d4Roll = randomNumberWorker.getIntFromRange(1, 4);

            return d4Roll + 1 + staminaScore;
        }
        else {
            var charType = characterCreateRequest.characterClass();
            var charClass = charClassesService.getCharClassByType(charType);

            // Base hit points for level 1 character
            var hitPoints = charClass.level1Hp() + staminaScore;
            // Add hit points for each level above 1
            for (int i = 1; i < characterCreateRequest.level(); i++) {
                hitPoints += randomNumberWorker.getIntFromRange(1, charClass.maxHpAtLevelUp());
            }

            // Check for features that increase hit points
            logger.info("Hit points before checking for BONUS_HP features: {}", hitPoints);

            var tier1BonusHpFeatureName = FeatureAttributeUtil.getFeatureNameFromRequestWithAttributeType(
                    charClass.features(),
                    characterCreateRequest.features(),
                    FeatureAttributeType.BONUS_HP,
                    FeatureAttributeUtil.Tier.I);
            if (tier1BonusHpFeatureName != null) {
                hitPoints += getHitPointsForFeature(charClass.features().tier1(), tier1BonusHpFeatureName);
            }

            var tier2BonusHpFeatureName = FeatureAttributeUtil.getFeatureNameFromRequestWithAttributeType(
                    charClass.features(),
                    characterCreateRequest.features(),
                    FeatureAttributeType.BONUS_HP,
                    FeatureAttributeUtil.Tier.II);
            if (tier2BonusHpFeatureName != null) {
                hitPoints += getHitPointsForFeature(charClass.features().tier2(), tier2BonusHpFeatureName);
            }

            logger.info("Final hit points: {}", hitPoints);

            return hitPoints;
        }
    }

    private int getHitPointsForFeature(List<Feature> featureList, String featureName) {
        var hitPoints = 0;
        var hitPointsStr = "";

        try {
            hitPointsStr = FeatureAttributeUtil.getAttributeModifierForFeatureAndAttributeType(
                    featureList, featureName, FeatureAttributeType.BONUS_HP);
            hitPoints = Integer.parseInt(hitPointsStr);
        }
        catch (NumberFormatException e) {
            logger.error("Error parsing BONUS_HP value {} for feature {}",
                    hitPointsStr, featureName, e);
        }

        return hitPoints;
    }

    public String getWeaponName(CharacterCreateRequest request, int index) {
        if (request.isCommoner()) {
            return "";
        }

        if (!request.useQuickGear()) {
            if (request.characterClass() == CharType.MYSTIC && index == 0) {
                // Mystics always have an unarmed attack, even without quick gear
                return "Fists";
            }

            return "";
        }

        var charClass = charClassesService.getCharClassByType(request.characterClass());
        var gear = charClass.gear();

        return (index < gear.weapons().size()) ? gear.weapons().get(index).name() : "";
    }

    public String getWeaponType(CharacterCreateRequest request, int index) {
        if (request.isCommoner()) {
            return "";
        }

        if (!request.useQuickGear()) {
            if (request.characterClass() == CharType.MYSTIC && index == 0) {
                // Mystics always have an unarmed attack, even without quick gear
                return UNARMED_WEAPON_TYPE;
            }

            return "";
        }

        var charClass = charClassesService.getCharClassByType(request.characterClass());
        var gear = charClass.gear();

        return (index < gear.weapons().size()) ? gear.weapons().get(index).type() : "";
    }

    public String getWeaponDamage(CharacterCreateRequest request, int index) {
        if (request.isCommoner()) {
            return "";
        }

        var mysticWithoutQuickGear = false;
        if (!request.useQuickGear()) {
            if (request.characterClass() == CharType.MYSTIC && index == 0) {
                // Mystics always have an unarmed attack, even without quick gear
                mysticWithoutQuickGear = true;
            } else {
                return "";
            }
        }

        var charClass = charClassesService.getCharClassByType(request.characterClass());
        var baseDamage = "";
        if (mysticWithoutQuickGear) {
            baseDamage = "1d6";
        }
        else {
            var gear = charClass.gear();

            if (index >= gear.weapons().size()) {
                return "";
            }

            baseDamage = gear.weapons().get(index).damage();
        }

        // Check for features that boost unarmed damage, and apply them if we're generating
        // the damage for unarmed attacks
        String improvedDamage = null;
        var weaponType = getWeaponType(request, index);

        if (UNARMED_WEAPON_TYPE.equals(weaponType)) {
            var tier1UnarmedBonusFeatureName = FeatureAttributeUtil.getFeatureNameFromRequestWithAttributeType(
                    charClass.features(),
                    request.features(),
                    FeatureAttributeType.UNARMED_BONUS,
                    FeatureAttributeUtil.Tier.I);
            if (tier1UnarmedBonusFeatureName != null) {
                var unarmedBonusModifier = FeatureAttributeUtil.getAttributeModifierForFeatureAndAttributeType(
                        charClass.features().tier1(),
                        tier1UnarmedBonusFeatureName,
                        FeatureAttributeType.UNARMED_BONUS);
                if (!StringUtils.isBlank(unarmedBonusModifier)) {
                    improvedDamage = unarmedBonusModifier;
                }
            }

            var tier2UnarmedBonusFeatureName = FeatureAttributeUtil.getFeatureNameFromRequestWithAttributeType(
                    charClass.features(),
                    request.features(),
                    FeatureAttributeType.UNARMED_BONUS,
                    FeatureAttributeUtil.Tier.II);
            if (tier2UnarmedBonusFeatureName != null) {
                var unarmedBonusModifier = FeatureAttributeUtil.getAttributeModifierForFeatureAndAttributeType(
                        charClass.features().tier2(),
                        tier2UnarmedBonusFeatureName,
                        FeatureAttributeType.UNARMED_BONUS);
                if (!StringUtils.isBlank(unarmedBonusModifier)) {
                    improvedDamage = unarmedBonusModifier;
                }
            }
        }

        return (improvedDamage != null) ? improvedDamage : baseDamage;
    }
}
