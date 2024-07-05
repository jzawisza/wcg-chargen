package com.wcg.chargen.backend.testUtil;

import com.wcg.chargen.backend.service.impl.yaml.CharClassYamlLoaderService;

public class TestInvalidYamlLoaderServices
{
    public static class InvalidCharClassDataYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "invalid-data.yml";
        }
    }

    public static class InvalidCharTypeYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "invalid-class.yml";
        }
    }

    public static class NoFeaturesYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "no-features.yml"; }
    }

    public static class NoTier1OrTier2FeaturesYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "no-tier1-or-tier2-features.yml"; }
    }

    public static class TooFewFeaturesYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "too-few-features.yml"; }
    }

    public static class TooManyFeaturesYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "too-many-features.yml"; }
    }

    public static class FeatureBlankDescriptionYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-blank-description.yml"; }
    }

    public static class InvalidAttrPlusOneModifierYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-attr-plus-one-modifier.yml"; }
    }

    public static class InvalidBonusHpModifierYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-bonus-hp-modifier.yml"; }
    }

    public static class InvalidDaPlusOneModifierYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-da-plus-one-modifier.yml"; }
    }

    public static class InvalidSkillForSkaldYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-skill-for-skald.yml"; }
    }

    public static class InvalidSkillAttributeForMageYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-skill-for-mage-attribute.yml"; }
    }

    public static class InvalidSkillRandomStringForMageYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-skill-for-mage-random-string.yml"; }
    }

    public static class InvalidSkillForBerzerkerYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-skill-for-berzerker.yml"; }
    }

    public static class NoTier2FeaturesYamlLoaderService extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "no-tier2-features.yml"; }
    }

    public static class InvalidAdvModifierSkill extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "feature-invalid-adv-modifier-skill.yml";
        }
    }

    public static class InvalidDadvModifierSkill extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "feature-invalid-dadv-modifier-skill.yml";
        }
    }

    public static class InvalidAdvModifierForgottenLore extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "feature-invalid-adv-modifier-forgotten-lore.yml";
        }
    }

    public static class InvalidDadvModifierForgottenLore extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "feature-invalid-dadv-modifier-forgotten-lore.yml";
        }
    }

    public static class InvalidAdvModifierUnarmedDamage extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "feature-invalid-adv-modifier-unarmed-damage.yml";
        }
    }

    public static class InvalidDadvModifierUnarmedDamage extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "feature-invalid-dadv-modifier-unarmed-damage.yml";
        }
    }

    public static class InvalidAdvModifierAny extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "feature-invalid-adv-modifier-any.yml";
        }
    }

    public static class InvalidDadvModifierAny extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "feature-invalid-dadv-modifier-any.yml";
        }
    }
}
