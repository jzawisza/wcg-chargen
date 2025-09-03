package com.wcg.chargen.backend.testUtil;

import com.wcg.chargen.backend.service.impl.yaml.CharClassYamlLoaderService;

public class TestInvalidYamlLoaderServices
{
    public static class InvalidCharClassData extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "invalid-data.yml";
        }
    }

    public static class InvalidCharType extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "invalid-class.yml";
        }
    }

    public static class NoFeatures extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "no-features.yml"; }
    }

    public static class NoTier1OrTier2Features extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "no-tier1-or-tier2-features.yml"; }
    }

    public static class TooFewFeatures extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "too-few-features.yml"; }
    }

    public static class TooManyFeatures extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "too-many-features.yml"; }
    }

    public static class FeatureBlankDescription extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-blank-description.yml"; }
    }

    public static class InvalidAttrPlusOneModifier extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-attr-plus-one-modifier.yml"; }
    }

    public static class InvalidBonusHpModifier extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-bonus-hp-modifier.yml"; }
    }

    public static class InvalidDaPlusOneModifier extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-da-plus-one-modifier.yml"; }
    }

    public static class InvalidSkillForSkald extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-skill-for-skald.yml"; }
    }

    public static class InvalidSkillAttributeForMage extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-skill-for-mage-attribute.yml"; }
    }

    public static class InvalidSkillRandomStringForMage extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-skill-for-mage-random-string.yml"; }
    }

    public static class InvalidSkillForBerzerker extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-invalid-skill-for-berzerker.yml"; }
    }

    public static class NoTier2Features extends CharClassYamlLoaderService {
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

    public static class NoAttackModifiers extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "no-attack-modifiers.yml";
        }
    }

    public static class EmptyAttackModifiers extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "empty-attack-modifiers.yml";
        }
    }

    public static class TooFewAttackModifiers extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "too-few-attack-modifiers.yml";
        }
    }

    public static class TooManyAttackModifiers extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "too-many-attack-modifiers.yml";
        }
    }

    public static class NoEvasionModifiers extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "no-evasion-modifiers.yml";
        }
    }

    public static class EmptyEvasionModifiers extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "empty-evasion-modifiers.yml";
        }
    }

    public static class TooFewEvasionModifiers extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "too-few-evasion-modifiers.yml";
        }
    }

    public static class TooManyEvasionModifiers extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "too-many-evasion-modifiers.yml";
        }
    }

    public static class NoLevel1Hp extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "no-level-1-hp.yml";
        }
    }

    public static class NoMaxHpAtLevelUp extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "no-max-hp-at-level-up.yml";
        }
    }

    public static class InvalidMaxHpAtLevelUp extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "invalid-max-hp-at-level-up.yml";
        }
    }

    public static class NoSkills extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "no-skills.yml";
        }
    }

    public static class EmptySkills extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "empty-skills.yml";
        }
    }

    public static class InvalidSkill extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "invalid-skill.yml";
        }
    }

    public static class NoGear extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "no-gear.yml";
        }
    }

    public static class EmptyGear extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "empty-gear.yml";
        }
    }

    public static class GearNoArmor extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "gear-no-armor.yml";
        }
    }

    public static class GearEmptyArmor extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "gear-empty-armor.yml";
        }
    }

    public static class GearNoWeapons extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "gear-no-weapons.yml";
        }
    }

    public static class GearEmptyWeapons extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "gear-empty-weapons.yml";
        }
    }

    public static class GearNoMaxCopper extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "gear-no-max-copper.yml";
        }
    }

    public static class GearEmptyMaxCopper extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "gear-empty-max-copper.yml";
        }
    }

    public static class GearNoMaxSilver extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "gear-no-max-silver.yml";
        }
    }

    public static class GearEmptyMaxSilver extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "gear-empty-max-silver.yml";
        }
    }

    public static class GearNoItems extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "gear-no-items.yml";
        }
    }

    public static class GearEmptyItems extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() {
            return "gear-empty-items.yml";
        }
    }

    public static class MissingUnarmedBonusModifier extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "feature-missing-unarmed-bonus-modifier.yml"; }
    }

    public static class NoAbilities extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "no-abilities.yml"; }
    }

    public static class EmptyAbilities extends CharClassYamlLoaderService {
        @Override
        public String getYamlFile() { return "empty-abilities.yml"; }
    }
}
