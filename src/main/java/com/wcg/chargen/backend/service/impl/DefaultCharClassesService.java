package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.constants.FeatureConstants;
import com.wcg.chargen.backend.enums.*;
import com.wcg.chargen.backend.model.CharClass;
import com.wcg.chargen.backend.model.Feature;
import com.wcg.chargen.backend.model.Features;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.impl.yaml.CharClassYamlLoaderService;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class DefaultCharClassesService implements CharClassesService {
    private final List<CharClassYamlLoaderService> charClassYamlLoaderServiceList;

    private final HashMap<CharType, CharClass> charClassTypeMap = new HashMap<CharType, CharClass>();

    private final Logger logger = LoggerFactory.getLogger(DefaultCharClassesService.class);

    @Autowired
    public DefaultCharClassesService(List<CharClassYamlLoaderService> charClassYamlLoaderServiceList) {
        this.charClassYamlLoaderServiceList = charClassYamlLoaderServiceList;
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

                // Validate that all features have correct attribute data
                if (charClass.features() == null) {
                    throw new IllegalStateException("Character class type " + charClass.type() + " has null feature data");
                }

                var tier1ErrMsg = checkForErrors(charClass.features().tier1(), charType);
                if (tier1ErrMsg != null) {
                    throw new IllegalStateException("Character class type " + charClass.type() + " has invalid Tier I feature data");
                }

                var tier2ErrMsg = checkForErrors(charClass.features().tier2(), charType);
                if (tier2ErrMsg != null) {
                    throw new IllegalStateException("Character class type " + charClass.type() + " has invalid Tier II feature data");
                }
            }
            catch (IllegalArgumentException e) {
                throw new IllegalStateException("Character class type " + charClass.type() + " found in YAML file " + yamlFile + " is not valid");
            }
        }

        // Ensure that we have one character class for each character type
        for (var charType : CharType.values()) {
            if (charClassTypeMap.get(charType) == null) {
                throw new IllegalStateException("No entry for character type " + charType.toString() + " in character class type map");
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
    private String checkForErrors(List<Feature> features, CharType charType) {
        if (features == null) {
            return "Null feature list";
        }

        // We should have a certain number of features for each class
        var numFeatures = features.size();
        if (!(numFeatures >= FeatureConstants.NUM_FEATURES_MIN || numFeatures <= FeatureConstants.NUM_FEATURES_MAX)) {
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
                        // TODO: add check here (will require some refactoring for skills)
                    }
                    case ATTR_PLUS_1 -> {
                        try {
                            AttrPlusOneModifier.valueOf(modifier);
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
                            DaPlusOneModifier.valueOf(modifier);
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
                                var charAttribute = AttributeType.valueOf(modifier);
                                if (charAttribute != AttributeType.INT) {
                                    return String.format(
                                            "Expected INT for SKILL modifier for mage, but found %s",
                                            modifier);
                                }
                            }
                            catch (IllegalArgumentException e) {
                                return String.format("Error reading modifier %s for SKILL value type: expected attribute",
                                        modifier);
                            }
                        }
                        else {
                            // If we get here, we have invalid data
                            return String.format("Found SKILL attribute with unexpected character type %s",
                                    charType);
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public CharClass getCharClassByType(CharType charType) {
        return charClassTypeMap.get(charType);
    }
}
