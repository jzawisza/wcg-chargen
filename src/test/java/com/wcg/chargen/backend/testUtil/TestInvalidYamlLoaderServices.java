package com.wcg.chargen.backend.testUtil;

import com.wcg.chargen.backend.service.impl.yaml.CharClassYamlLoaderService;

public class TestInvalidYamlLoaderServices
{
    public static class InvalidCharClassDataYamlLoaderService extends CharClassYamlLoaderService {
        public InvalidCharClassDataYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "invalid-data.yml";
        }
    }

    public static class InvalidCharTypeYamlLoaderService extends CharClassYamlLoaderService {
        public InvalidCharTypeYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "invalid-class.yml";
        }
    }

    public static class NoFeaturesYamlLoaderService extends CharClassYamlLoaderService {
        public NoFeaturesYamlLoaderService() {}

        @Override
        public String getYamlFile() { return "no-features.yml"; }
    }

    public static class NoTier1OrTier2FeaturesYamlLoaderService extends CharClassYamlLoaderService {
        public NoTier1OrTier2FeaturesYamlLoaderService() {}

        @Override
        public String getYamlFile() { return "no-tier1-or-tier2-features.yml"; }
    }

    public static class TooFewFeaturesYamlLoaderService extends CharClassYamlLoaderService {
        public TooFewFeaturesYamlLoaderService() {}

        @Override
        public String getYamlFile() { return "too-few-features.yml"; }
    }

    public static class TooManyFeaturesYamlLoaderService extends CharClassYamlLoaderService {
        public TooManyFeaturesYamlLoaderService() {}

        @Override
        public String getYamlFile() { return "too-many-features.yml"; }
    }

    public static class FeatureBlankDescriptionYamlLoaderService extends CharClassYamlLoaderService {
        public FeatureBlankDescriptionYamlLoaderService() {}

        @Override
        public String getYamlFile() { return "feature-blank-description.yml"; }
    }

    public static class InvalidAttrPlusOneModifierYamlLoaderService extends CharClassYamlLoaderService {
        public InvalidAttrPlusOneModifierYamlLoaderService() {}

        @Override
        public String getYamlFile() { return "feature-invalid-attr-plus-one-modifier.yml"; }
    }

    public static class InvalidBonusHpModifierYamlLoaderService extends CharClassYamlLoaderService {
        public InvalidBonusHpModifierYamlLoaderService() {}

        @Override
        public String getYamlFile() { return "feature-invalid-bonus-hp-modifier.yml"; }
    }

    public static class InvalidDaPlusOneModifierYamlLoaderService extends CharClassYamlLoaderService {
        public InvalidDaPlusOneModifierYamlLoaderService() {}

        @Override
        public String getYamlFile() { return "feature-invalid-da-plus-one-modifier.yml"; }
    }

    public static class InvalidSkillForSkaldYamlLoaderService extends CharClassYamlLoaderService {
        public InvalidSkillForSkaldYamlLoaderService() {}

        @Override
        public String getYamlFile() { return "feature-invalid-skill-for-skald.yml"; }
    }

    public static class InvalidSkillAttributeForMageYamlLoaderService extends CharClassYamlLoaderService {
        public InvalidSkillAttributeForMageYamlLoaderService() {}

        @Override
        public String getYamlFile() { return "feature-invalid-skill-for-mage-attribute.yml"; }
    }

    public static class InvalidSkillRandomStringForMageYamlLoaderService extends CharClassYamlLoaderService {
        public InvalidSkillRandomStringForMageYamlLoaderService() {}

        @Override
        public String getYamlFile() { return "feature-invalid-skill-for-mage-random-string.yml"; }
    }

    public static class InvalidSkillForBerzerkerYamlLoaderService extends CharClassYamlLoaderService {
        public InvalidSkillForBerzerkerYamlLoaderService() {}

        @Override
        public String getYamlFile() { return "feature-invalid-skill-for-berzerker.yml"; }
    }

    public static class NoTier2FeaturesYamlLoaderService extends CharClassYamlLoaderService {
        public NoTier2FeaturesYamlLoaderService() {}

        @Override
        public String getYamlFile() { return "no-tier2-features.yml"; }
    }
}
