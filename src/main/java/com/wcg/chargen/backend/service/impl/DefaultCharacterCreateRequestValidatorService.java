package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.AttributeType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;
import com.wcg.chargen.backend.model.Feature;
import com.wcg.chargen.backend.service.*;
import com.wcg.chargen.backend.worker.SkillsProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.wcg.chargen.backend.service.impl.DefaultFeaturesService.CUMULATIVE_TIER_1_FEATURES_PER_LEVEL;
import static com.wcg.chargen.backend.service.impl.DefaultFeaturesService.CUMULATIVE_TIER_2_FEATURES_PER_LEVEL;

@Service
public class DefaultCharacterCreateRequestValidatorService implements CharacterCreateRequestValidatorService {
    @Autowired
    ProfessionsService professionsService;
    @Autowired
    SpeciesService speciesService;
    @Autowired
    SkillsProvider skillsProvider;
    @Autowired
    CharClassesService charClassesService;

    private static final List<Integer> CHALLENGING_ATTRIBUTE_VALUES =
            Arrays.asList(-2, -1, 0, 0, 1, 1, 2);
    private static final List<Integer> HEROIC_ATTRIBUTE_VALUES =
            Arrays.asList(-1, 0, 0, 0, 1, 2, 2);
    private static final int MIN_ATTRIBUTE_VALUE = -3;
    private static final int MAX_ATTRIBUTE_VALUE = 3;

    public CharacterCreateStatus validate(CharacterCreateRequest characterCreateRequest) {
        if (characterCreateRequest == null) {
            return failedStatus("Invalid object");
        }
        if (StringUtils.isEmpty(characterCreateRequest.characterName())) {
            return failedStatus("Missing character name");
        }
        if (characterCreateRequest.species() == null) {
            return failedStatus("Missing species");
        }
        if (characterCreateRequest.level() == null) {
            return failedStatus("Missing level");
        }

        var level = characterCreateRequest.level();
        if (level < 0 || level > 7) {
            return failedStatus("Level must be between 0 and 7");
        }
        if (characterCreateRequest.isCommoner()) {
            // A commoner character should have a profession and not a class
            if (characterCreateRequest.characterClass() != null) {
                return failedStatus("Level 0 characters cannot have a character class");
            }

            var profession = characterCreateRequest.profession();
            if (StringUtils.isEmpty(profession)) {
                return failedStatus("Level 0 characters must have a profession");
            }

            var isProfessionValid = professionsService.getAllProfessions().professions().stream()
                    .anyMatch(x -> x.name().equals(profession));
            if (!isProfessionValid) {
                return failedStatus("Profession " + profession + " is not a valid profession");
            }
        }
        else if (characterCreateRequest.characterClass() == null) {
            // Characters with levels 1-7 must have a class
            return failedStatus("Missing character class");
        }

        var attributesMap = characterCreateRequest.attributes();
        for (var attributeName : AttributeType.values()) {
            if (!attributesMap.containsKey(attributeName.toString())) {
                return failedStatus("Attributes object is missing required attribute " + attributeName);
            }
        }

        // For class characters, the values from the attributes object should match either the
        // Challenging or Heroic attribute array
        // For commoner characters, they should all be between -3 and 3
        if (characterCreateRequest.isCommoner()) {
            for (var attributeKey : attributesMap.keySet()) {
                var attributeValue  = attributesMap.get(attributeKey);
                if (attributeValue < MIN_ATTRIBUTE_VALUE || attributeValue > MAX_ATTRIBUTE_VALUE) {
                    return failedStatus(
                            String.format("Attribute %s has invalid value %d which is not between %d and %d",
                            attributeKey, attributeValue, MIN_ATTRIBUTE_VALUE, MAX_ATTRIBUTE_VALUE));
                }
            }
        }
        else {
            var attributeValuesList = attributesMap.values().stream().sorted().toList();

            if (!(CHALLENGING_ATTRIBUTE_VALUES.equals(attributeValuesList) ||
                    HEROIC_ATTRIBUTE_VALUES.equals(attributeValuesList))) {
                return failedStatus("Attribute values do not match challenging or heroic attribute arrays");
            }
        }

        try {
            AttributeType.valueOf(characterCreateRequest.speciesStrength());
        }
        catch (IllegalArgumentException e) {
            return failedStatus("Species strength value " + characterCreateRequest.speciesStrength()
            + " is not a valid attribute type");
        }

        var isHuman = characterCreateRequest.species().isHuman();

        if (!isHuman && StringUtils.isEmpty(characterCreateRequest.speciesWeakness())) {
            // Non-human characters must have a species weakness specified
            return failedStatus("Non-human characters must specify a species weakness");
        }

        try {
            if (!isHuman) {
                AttributeType.valueOf(characterCreateRequest.speciesWeakness());
            }
        }
        catch (IllegalArgumentException e) {
            return failedStatus("Species weakness value " + characterCreateRequest.speciesWeakness()
                    + " is not a valid attribute type");
        }

        // For non-human characters, the species strengths and weaknesses from the request
        // must sync up with the values from the species YAML files, and their bonus skill
        // must be included in the species YAML file
        if (!isHuman) {
            var speciesType = characterCreateRequest.species();
            var species = speciesService.getSpeciesByType(speciesType);

            if (!species.strengths().contains(characterCreateRequest.speciesStrength())) {
                return failedStatus(String.format("Species strength %s is not valid for species %s",
                        characterCreateRequest.speciesStrength(), speciesType));
            }

            if (!species.weaknesses().contains(characterCreateRequest.speciesWeakness())) {
                return failedStatus(String.format("Species weakness %s is not valid for species %s",
                        characterCreateRequest.speciesWeakness(), speciesType));
            }

            if (!characterCreateRequest.isCommoner() &&
                    !species.skills().contains(characterCreateRequest.speciesSkill())) {
                return failedStatus(String.format("Species skill %s is not valid for species %s",
                        characterCreateRequest.speciesSkill(), speciesType));
            }
        }

        if (!characterCreateRequest.isCommoner()) {
            if (characterCreateRequest.bonusSkills() == null) {
                return failedStatus("Bonus skills cannot be null for characters Level 1 and above");
            }

            // Humans are allowed 2 bonus skills, while non-humans only get 1,
            // and the bonus skills must all be valid
            var expectedBonusSkills = isHuman ? 2 : 1;
            var speciesClass = isHuman ? "human" : "non-human";
            if (characterCreateRequest.bonusSkills().size() != expectedBonusSkills) {
                return failedStatus(String.format("Expected %d bonus skills for %s species, got %d",
                        expectedBonusSkills, speciesClass, characterCreateRequest.bonusSkills().size()));
            }

            for (var skill : characterCreateRequest.bonusSkills()) {
                if (skillsProvider.getByName(skill) == null) {
                    return failedStatus(String.format("Bonus skill %s is not a valid skill", skill));
                }
            }

            if (characterCreateRequest.useQuickGear() == null) {
                return failedStatus("Use quick gear field must be specified for class characters");
            }
        }

        if (characterCreateRequest.level() <= 1 && characterCreateRequest.features() != null) {
            return failedStatus("Features cannot be specified for commoner or Level 1 characters");
        }
        else if (characterCreateRequest.level() > 1) {
            if (characterCreateRequest.features() == null) {
                return failedStatus("Features must be specified for characters Level 2 and above");
            }
            if (characterCreateRequest.features().tier1() == null) {
                return failedStatus("Tier I features for characters level 2 and above cannot be null");
            }
            if (characterCreateRequest.features().tier2() == null) {
                return failedStatus("Tier II features for characters level 2 and above cannot be null");
            }

            // Make sure we have the correct number of Tier I and Tier II features
            var numAllowedTier1Features =
                    CUMULATIVE_TIER_1_FEATURES_PER_LEVEL[characterCreateRequest.level() - 1];
            if (characterCreateRequest.features().tier1().size() != numAllowedTier1Features) {
                return failedStatus(String.format("Expected %d tier I features for level %d, got %d",
                        numAllowedTier1Features,
                        characterCreateRequest.level(),
                        characterCreateRequest.features().tier1().size()));
            }

            var numAllowedTier2Features =
                    CUMULATIVE_TIER_2_FEATURES_PER_LEVEL[characterCreateRequest.level() - 1];
            if (characterCreateRequest.features().tier2().size() != numAllowedTier2Features) {
                return failedStatus(String.format("Expected %d tier II features for level %d, got %d",
                        numAllowedTier2Features,
                        characterCreateRequest.level(),
                        characterCreateRequest.features().tier2().size()));
            }

            // Check that the features are valid for the character class
            var charClass = charClassesService.getCharClassByType(characterCreateRequest.characterClass());
            var tier1FeatureDescriptions = charClass.features().tier1().stream()
                    .map(Feature::description)
                    .toList();
            for (var featureDesc : characterCreateRequest.features().tier1()) {
                if (!tier1FeatureDescriptions.contains(featureDesc)) {
                    return failedStatus(String.format("Tier I feature %s is not valid for class %s",
                            featureDesc, characterCreateRequest.characterClass()));
                }
            }

            var tier2FeatureDescriptions = charClass.features().tier2().stream()
                    .map(Feature::description)
                    .toList();
            for (var featureDesc : characterCreateRequest.features().tier2()) {
                if (!tier2FeatureDescriptions.contains(featureDesc)) {
                    return failedStatus(String.format("Tier II feature %s is not valid for class %s",
                            featureDesc, characterCreateRequest.characterClass()));
                }
            }
        }

        return CharacterCreateStatus.SUCCESS;
    }

    private CharacterCreateStatus failedStatus(String msg) {
        return new CharacterCreateStatus(false, msg);
    }
}
