package com.wcg.chargen.backend.util;

import com.wcg.chargen.backend.enums.FeatureAttributeType;
import com.wcg.chargen.backend.model.Feature;
import com.wcg.chargen.backend.model.FeatureAttribute;
import com.wcg.chargen.backend.model.Features;
import com.wcg.chargen.backend.model.FeaturesRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FeatureAttributeUtilTest {
    @ParameterizedTest
    @EnumSource(FeatureAttributeUtil.Tier.class)
    public void getFeatureNameFromRequestWithAttributeType_ReturnsFeatureNameIfFeatureIsPresentInRequest(
            FeatureAttributeUtil.Tier tier) {
        // arrange
        var featureAttributeType = FeatureAttributeType.ADV;
        var tier1FeatureName = "Tier 1 Feature";
        var tier1FeatureAttribute = new FeatureAttribute(featureAttributeType, "Some modifier");
        var tier1Feature = new Feature(tier1FeatureName, List.of(tier1FeatureAttribute));
        var tier2FeatureName = "Tier 2 Feature";
        var tier2FeatureAttribute = new FeatureAttribute(featureAttributeType, "Some modifier");
        var tier2Feature = new Feature(tier2FeatureName, List.of(tier2FeatureAttribute));
        var features = new Features(List.of(tier1Feature), List.of(tier2Feature));

        var expectedFeatureName = (tier == FeatureAttributeUtil.Tier.I) ? tier1FeatureName : tier2FeatureName;

        var featuresRequest = new FeaturesRequest(
                (tier == FeatureAttributeUtil.Tier.I) ? List.of(tier1FeatureName) : Collections.emptyList(),
                (tier == FeatureAttributeUtil.Tier.II) ? List.of(tier2FeatureName) : Collections.emptyList()
        );

        // act
        var actualFeatureName = FeatureAttributeUtil.getFeatureNameFromRequestWithAttributeType(
                features, featuresRequest, featureAttributeType, tier);

        // assert
        assertEquals(expectedFeatureName, actualFeatureName);
    }

    @ParameterizedTest
    @EnumSource(FeatureAttributeUtil.Tier.class)
    public void getFeatureNameFromRequestWithAttributeType_ReturnsNullIfFeatureIsNotPresentInRequest(
            FeatureAttributeUtil.Tier tier) {
        // arrange
        var notPresentFeature = "Random Feature";

        var featureAttributeType = FeatureAttributeType.ADV;
        var tier1FeatureName = "Tier 1 Feature";
        var tier1FeatureAttribute = new FeatureAttribute(featureAttributeType, "Some modifier");
        var tier1Feature = new Feature(tier1FeatureName, List.of(tier1FeatureAttribute));
        var tier2FeatureName = "Tier 2 Feature";
        var tier2FeatureAttribute = new FeatureAttribute(featureAttributeType, "Some modifier");
        var tier2Feature = new Feature(tier2FeatureName, List.of(tier2FeatureAttribute));
        var features = new Features(List.of(tier1Feature), List.of(tier2Feature));

        var featuresRequest = new FeaturesRequest(
                (tier == FeatureAttributeUtil.Tier.I) ? List.of(notPresentFeature) : Collections.emptyList(),
                (tier == FeatureAttributeUtil.Tier.II) ? List.of(notPresentFeature) : Collections.emptyList()
        );

        // act
        var actualFeatureName = FeatureAttributeUtil.getFeatureNameFromRequestWithAttributeType(
                features, featuresRequest, featureAttributeType, tier);

        // assert
        assertNull(actualFeatureName);
    }

    @ParameterizedTest
    @EnumSource(FeatureAttributeUtil.Tier.class)
    public void getFeatureNameFromRequestWithAttributeType_ReturnsNullIfFeatureNamePresentInRequestButWithDifferentAttributeType(
            FeatureAttributeUtil.Tier tier) {
        // arrange
        var featureAttributeType = FeatureAttributeType.ADV;
        var requestFeatureAttributeType = FeatureAttributeType.DADV;

        var tier1FeatureName = "Tier 1 Feature";
        var tier1FeatureAttribute = new FeatureAttribute(featureAttributeType, "Some modifier");
        var tier1Feature = new Feature(tier1FeatureName, List.of(tier1FeatureAttribute));
        var tier2FeatureName = "Tier 2 Feature";
        var tier2FeatureAttribute = new FeatureAttribute(featureAttributeType, "Some modifier");
        var tier2Feature = new Feature(tier2FeatureName, List.of(tier2FeatureAttribute));
        var features = new Features(List.of(tier1Feature), List.of(tier2Feature));

        var featuresRequest = new FeaturesRequest(
                (tier == FeatureAttributeUtil.Tier.I) ? List.of(tier1FeatureName) : Collections.emptyList(),
                (tier == FeatureAttributeUtil.Tier.II) ? List.of(tier2FeatureName) : Collections.emptyList()
        );

        // act
        var actualFeatureName = FeatureAttributeUtil.getFeatureNameFromRequestWithAttributeType(
                features, featuresRequest, requestFeatureAttributeType, tier);

        // assert
        assertNull(actualFeatureName);
    }

    @ParameterizedTest
    @EnumSource(FeatureAttributeType.class)
    public void getAttributeModifierForFeatureAndAttributeType_ReturnsModifierIfFeatureWithSpecifiedNameAndTypeExists(
            FeatureAttributeType featureAttributeType) {
        // arrange
        var expectedModifier = "Some modifier";
        var featureAttribute = new FeatureAttribute(featureAttributeType, expectedModifier);
        var featureName = "Feature Name";
        var feature = new Feature(featureName, List.of(featureAttribute));

        // act
        var actualModifier = FeatureAttributeUtil.getAttributeModifierForFeatureAndAttributeType(
                List.of(feature), featureName, featureAttributeType);

        // assert
        assertEquals(expectedModifier, actualModifier);
    }

    @Test
    public void getAttributeModifierForFeatureAndAttributeType_ReturnsEmptyStringIfNoFeatureWithSpecifiedNameExists() {
        // arrange
        var expectedModifier = "Some modifier";
        var featureAttributeType = FeatureAttributeType.BONUS_HP;
        var featureAttribute = new FeatureAttribute(featureAttributeType, expectedModifier);
        var featureName = "Feature Name";
        var feature = new Feature(featureName, List.of(featureAttribute));

        // act
        var actualModifier = FeatureAttributeUtil.getAttributeModifierForFeatureAndAttributeType(
                List.of(feature), "Nonexistent feature", featureAttributeType);

        // assert
        assertEquals("", actualModifier);
    }

    @Test
    public void getAttributeModifierForFeatureAndAttributeType_ReturnsEmptyStringIfNoFeatureWithSpecifiedTypeExists() {
        // arrange
        var expectedModifier = "Some modifier";
        var featureAttribute = new FeatureAttribute(FeatureAttributeType.BONUS_HP, expectedModifier);
        var featureName = "Feature Name";
        var feature = new Feature(featureName, List.of(featureAttribute));

        // act
        var actualModifier = FeatureAttributeUtil.getAttributeModifierForFeatureAndAttributeType(
                List.of(feature), featureName, FeatureAttributeType.DA_PLUS_1);

        // assert
        assertEquals("", actualModifier);
    }
}

