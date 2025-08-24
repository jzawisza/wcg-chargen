package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.constants.FeatureConstants;
import com.wcg.chargen.backend.enums.*;
import com.wcg.chargen.backend.model.CharClass;
import com.wcg.chargen.backend.model.Feature;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.SkillsProvider;
import com.wcg.chargen.backend.service.impl.yaml.CharClassYamlLoaderService;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class DefaultCharClassesService implements CharClassesService {
    private final List<CharClassYamlLoaderService> charClassYamlLoaderServiceList;

    private final SkillsProvider skillsProvider;

    private final HashMap<CharType, CharClass> charClassTypeMap = new HashMap<CharType, CharClass>();

    private static final String INITIATIVE = "Initiative";

    private static final String SKALD_FORGOTTEN_LORE = "Forgotten Lore";

    private static final String MYSTIC_UNARMED = "Unarmed";

    private static final String ROGUE_ANY = "Any";

    private static final int NUM_LEVELS = 7;

    private static final int HP_PER_LEVEL_VALUE_1 = 3;
    private static final int HP_PER_LEVEL_VALUE_2 = 4;

    @Autowired
    public DefaultCharClassesService(List<CharClassYamlLoaderService> charClassYamlLoaderServiceList,
                                     SkillsProvider skillsProvider) {
        this.charClassYamlLoaderServiceList = charClassYamlLoaderServiceList;
        this.skillsProvider = skillsProvider;
    }

    @PostConstruct
    private void postConstruct() {
        for (var yamlLoaderService : charClassYamlLoaderServiceList) {
            var charClass = yamlLoaderService.loadFromYaml();
            var yamlFile = yamlLoaderService.getYamlFile();

            if (charClass == null) {
                throw new IllegalStateException("Error loading character class YAML file " + yamlFile);
            }

            try {
                var charType = CharType.valueOf(charClass.type().toUpperCase());
                charClassTypeMap.put(charType, charClass);

                // Validate that lists of attack and evasion modifiers have correct number of elements
                if (charClass.attackModifiers() == null || charClass.attackModifiers().isEmpty()) {
                    throw new IllegalStateException("Character class type " + charClass.type()
                            + " has null or empty attack modifier list");
                }
                if (charClass.attackModifiers().size() != NUM_LEVELS) {
                    throw new IllegalStateException("Character class type " + charClass.type() +
                            " attack modifier list has " + charClass.attackModifiers().size()
                            + " elements: expected " + NUM_LEVELS);
                }

                if (charClass.evasionModifiers() == null || charClass.evasionModifiers().isEmpty()) {
                    throw new IllegalStateException("Character class type " + charClass.type()
                            + " has null or empty evasion modifier list");
                }
                if (charClass.evasionModifiers().size() != NUM_LEVELS) {
                    throw new IllegalStateException("Character class type " + charClass.type() +
                            " evasion modifier list has " + charClass.evasionModifiers().size()
                            + " elements: expected " + NUM_LEVELS);
                }

                // Validate that level 1 HP is not null
                if (charClass.level1Hp() == null) {
                    throw new IllegalStateException("Character class type " + charClass.type()
                            + " has null level 1 HP");
                }

                // Validate that max HP per level up is one of 2 known values
                if (charClass.maxHpAtLevelUp() == null || (
                        charClass.maxHpAtLevelUp() != HP_PER_LEVEL_VALUE_1
                        && charClass.maxHpAtLevelUp() != HP_PER_LEVEL_VALUE_2)) {
                    System.out.println("Got here");
                    throw new IllegalStateException("Max HP at level up for character class type "
                        + charClass.type() + " is " + charClass.maxHpAtLevelUp()
                        + ": expected " + HP_PER_LEVEL_VALUE_1 + " or " + HP_PER_LEVEL_VALUE_2);
                }

                // Validate that skills list is not empty and that all listed skills are valid
                if (charClass.skills() == null || charClass.skills().isEmpty()) {
                    throw new IllegalStateException("Character class type " + charClass.type()
                            + " has null or empty skills list");
                }

                for (var charSkill : charClass.skills()) {
                    if (skillsProvider.getByName(charSkill) == null) {
                        throw new IllegalStateException("Character class type " + charClass.type()
                                + " has unknown skill " + charSkill);
                    }
                }

                // Validate that quick gear has expected data
                if (charClass.gear() == null) {
                    throw new IllegalStateException(
                            String.format("Character class type %s has null gear data",
                                    charClass.type()));
                }

                if (charClass.gear().armor() == null ||
                    charClass.gear().armor().isEmpty()) {
                    throw new IllegalStateException(
                            String.format("Character class type %s has null or missing armor information in gear",
                                    charClass.type()));
                }

                if (charClass.gear().weapons() == null ||
                        charClass.gear().weapons().isEmpty()) {
                    throw new IllegalStateException(
                            String.format("Character class type %s has null or missing weapon information in gear",
                                    charClass.type()));
                }

                if (charClass.gear().maxCopper() == null) {
                    throw new IllegalStateException(
                            String.format("Character class type %s has null or missing max copper information in gear",
                                    charClass.type()));
                }

                if (charClass.gear().maxSilver() == null) {
                    throw new IllegalStateException(
                            String.format("Character class type %s has null or missing max silver information in gear",
                                    charClass.type()));
                }

                if (charClass.gear().items() == null ||
                        charClass.gear().items().isEmpty()) {
                    throw new IllegalStateException(
                            String.format("Character class type %s has null or missing item information in gear",
                                    charClass.type()));
                }

                // Validate that all features have correct attribute data
                if (charClass.features() == null) {
                    throw new IllegalStateException("Character class type " + charClass.type()
                            + " has null feature data");
                }

                var tier1ErrMsg = checkFeaturesForErrors(charClass.features().tier1(), charType);
                if (tier1ErrMsg != null) {
                    throw new IllegalStateException("Character class type " + charClass.type()
                            + " has invalid Tier I feature data: " + tier1ErrMsg);
                }

                var tier2ErrMsg = checkFeaturesForErrors(charClass.features().tier2(), charType);
                if (tier2ErrMsg != null) {
                    throw new IllegalStateException("Character class type " + charClass.type()
                            + " has invalid Tier II feature data: " + tier2ErrMsg);
                }
            }
            catch (IllegalArgumentException e) {
                throw new IllegalStateException("Character class type " + charClass.type()
                        + " found in YAML file " + yamlFile + " is not valid");
            }
        }

        // Ensure that we have one character class for each character type
        for (var charType : CharType.values()) {
            if (charClassTypeMap.get(charType) == null) {
                throw new IllegalStateException("No entry for character type " + charType.toString()
                        + " in character class type map");
            }
        }
    }

    /**
     * Check to see if there are any errors with the feature lists.
     *
     * @param features List of features
     * @param charType Character class
     * @return A descriptive error message if errors are found, null if there are no errors.
     */
    private String checkFeaturesForErrors(List<Feature> features, CharType charType) {
        if (features == null) {
            return "Null feature list";
        }

        // We should have a certain number of features for each class
        var numFeatures = features.size();
        if (numFeatures < FeatureConstants.NUM_FEATURES_MIN || numFeatures > FeatureConstants.NUM_FEATURES_MAX) {
            return String.format("Expected between %d and %d features in list, but got %d",
                    FeatureConstants.NUM_FEATURES_MIN,
                    FeatureConstants.NUM_FEATURES_MAX,
                    numFeatures);
        }

        for (var feature : features) {
            // Make sure the description is valid
            if (StringUtils.isBlank(feature.description())) {
                return "Feature with blank description found";
            }
            // If the feature has attributes, verify the attribute data
            for (var attr : feature.attributes()) {
                var modifier = attr.modifier();

                switch (attr.type()) {
                    case ADV, DADV -> {
                        var advDadvMessage = checkAdvDadvAttribute(modifier, charType);
                        if (advDadvMessage != null) {
                            return advDadvMessage;
                        }
                    }
                    case ATTR_PLUS_1 -> {
                        try {
                            AttrPlusOneModifier.valueOf(modifier.toUpperCase());
                        }
                        catch (IllegalArgumentException e) {
                            return String.format("Error reading modifier %s for ATTR_PLUS_1 value type",
                                    modifier);
                        }
                    }
                    case BONUS_HP -> {
                        // Modifier must be an integer
                        try {
                            Integer.valueOf(modifier);
                        }
                        catch (NumberFormatException e) {
                            return String.format("Modifier %s for BONUS_HP value type must be an integer",
                                    modifier);
                        }
                    }
                    case DA_PLUS_1 -> {
                        try {
                            DaPlusOneModifier.valueOf(modifier.toUpperCase());
                        }
                        catch (IllegalArgumentException e) {
                            return String.format("Error reading modifier %s for DA_PLUS_1 value type",
                                    modifier);
                        }
                    }
                    case SKILL -> {
                        // Skill modifier should be empty for skalds and INT for mages
                        if (charType == CharType.SKALD) {
                            if (!StringUtils.isBlank(modifier)) {
                                return String.format(
                                        "Expected blank string for SKILL modifier for skald, but found %s",
                                        modifier);
                            }
                        }
                        else if (charType == CharType.MAGE) {
                            try {
                                var charAttribute = AttributeType.valueOf(modifier.toUpperCase());
                                if (charAttribute != AttributeType.INT) {
                                    return String.format(
                                            "Expected INT for SKILL modifier for mage, but found %s",
                                            modifier);
                                }
                            }
                            catch (IllegalArgumentException e) {
                                return String.format("Error reading modifier %s for SKILL value type for mage, expected INT",
                                        modifier);
                            }
                        }
                        else {
                            // If we get here, we have invalid data
                            return String.format("Found SKILL attribute with unexpected character type %s",
                                    charType);
                        }
                    }
                    case UNARMED_BONUS -> {
                        // Unarmed bonus modifier can vary, but must be populated
                        if (StringUtils.isBlank(modifier)) {
                            return "Unarmed bonus modifier cannot be null or empty";
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Check that the modifier for the ADV or DADV attribute types is correct.
     *
     * @param modifier Modifier for ADV or DADV
     * @param charType Character type
     * @return Null if there are no errors, an error message otherwise
     */
    private String checkAdvDadvAttribute(String modifier, CharType charType) {
        var hasAttribute = true;

        // First check to see if modifier is a skill
        var skill = skillsProvider.getByName(modifier);
        if (skill != null) {
            return null;
        }

        // Next, check to see if it's an attribute
        try {
            AttributeType.valueOf(modifier.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            hasAttribute = false;
        }

        if (hasAttribute) {
            return null;
        }

        // "Initiative" is a valid modifier as well
        if (modifier.equals(INITIATIVE)) {
            return null;
        }

        // For skalds, "Forgotten Lore" is a valid modifier
        if (charType == CharType.SKALD && modifier.equals(SKALD_FORGOTTEN_LORE)) {
            return null;
        }

        // For mystics, "Unarmed" is a valid modifier
        if (charType == CharType.MYSTIC && modifier.equals(MYSTIC_UNARMED)) {
            return null;
        }

        // For rogues, "Any" is a valid modifier
        if (charType == CharType.ROGUE && modifier.equals(ROGUE_ANY)) {
            return null;
        }

        // If no valid arguments are found, return an error
        return String.format("Unexpected modifier %s found for ADV/DADV value type", modifier);
    }

    @Override
    public CharClass getCharClassByType(CharType charType) {
        return charClassTypeMap.get(charType);
    }
}
