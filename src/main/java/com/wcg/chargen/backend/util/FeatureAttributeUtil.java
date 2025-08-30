package com.wcg.chargen.backend.util;

import com.wcg.chargen.backend.enums.FeatureAttributeType;
import com.wcg.chargen.backend.model.Feature;
import com.wcg.chargen.backend.model.FeatureAttribute;
import com.wcg.chargen.backend.model.Features;
import com.wcg.chargen.backend.model.FeaturesRequest;

import java.util.List;

public class FeatureAttributeUtil {
    public enum Tier {
        I,
        II
    };

    /**
     * Determine if the features from a character creation request include any that have
     * a specified attribute type.
     *
     * @param features  Set of features associated with a given character class
     * @param featuresRequest   Character creation request
     * @param attributeType Attribute that should be associated with the feature
     * @param tier  Specify whether to search Tier I or Tier II features
     * @return The name of the feature from the request that has the specified attribute type,
     * or null if none found
     */
    public static String getFeatureNameFromRequestWithAttributeType(Features features,
                                                                  FeaturesRequest featuresRequest,
                                                                  FeatureAttributeType attributeType,
                                                                  Tier tier) {
        if (featuresRequest == null) {
            return null;
        }

        var classFeatureList = (tier == Tier.I) ? features.tier1() : features.tier2();
        var requestFeatureList = (tier == Tier.I) ? featuresRequest.tier1() : featuresRequest.tier2();

        // Get all class features with the specified attribute type
        var classFeatureNamesWithAttributeType = classFeatureList.stream()
                .filter(f -> f.attributes().stream()
                        .anyMatch(a -> a.type() == attributeType))
                .map(Feature::description)
                .toList();

        // See if the features from the request include any of those features
        for (var requestFeatureName : requestFeatureList) {
            if (classFeatureNamesWithAttributeType.contains(requestFeatureName)) {
                return requestFeatureName;
            }
        }

        return null;
    }

    public static String getAttributeModifierForFeatureAndAttributeType(List<Feature> featureList,
                                                                        String feature,
                                                                        FeatureAttributeType featureAttributeType) {
        var modifierOptional = featureList.stream()
                .filter(f -> f.description().equals(feature))
                .map(Feature::attributes)
                .flatMap(List::stream)
                .filter(a -> a.type() == featureAttributeType)
                .map(FeatureAttribute::modifier)
                .findFirst();

        return modifierOptional.orElse("");
    }
}
