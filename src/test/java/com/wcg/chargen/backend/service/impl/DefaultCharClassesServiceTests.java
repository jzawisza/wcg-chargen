package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.service.impl.yaml.CharClassYamlLoaderService;
import com.wcg.chargen.backend.testUtil.PostConstructUtil;
import com.wcg.chargen.backend.testUtil.SkillsProviderUtil;
import com.wcg.chargen.backend.testUtil.TestInvalidYamlLoaderServices;
import com.wcg.chargen.backend.testUtil.TestYamlLoaderServices;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultCharClassesServiceTests {
    @ParameterizedTest
    @MethodSource("yamlServicesWithBadDataProvider")
    void test_yamlFiles_With_Invalid_Data_Throw_Exception(List<CharClassYamlLoaderService> yamlLoaderServiceList, String expectedMsg) {
        var defaultCharClassesService = new DefaultCharClassesService(yamlLoaderServiceList,
                SkillsProviderUtil.getObject());

        // When reflection is used, the top-level exception is InvocationTargetException
        var exception = assertThrows(InvocationTargetException.class, () -> {
            PostConstructUtil.invokeMethod(DefaultCharClassesService.class, defaultCharClassesService);
        });

        var targetException = exception.getTargetException();
        assertEquals(IllegalStateException.class, targetException.getClass());
        assertEquals(expectedMsg, targetException.getMessage());
    }

    @ParameterizedTest
    @EnumSource(CharType.class)
    void test_yamlFiles_Representing_All_Classes_Loads_Successfully(CharType charType) {
        var defaultCharClassesService = new DefaultCharClassesService(TestYamlLoaderServices.getAllTestCharClassesList(),
                SkillsProviderUtil.getObject());

        try {
            PostConstructUtil.invokeMethod(DefaultCharClassesService.class, defaultCharClassesService);
        }
        catch (Exception e) {
            fail();
        }
        
        var charClass = defaultCharClassesService.getCharClassByType(charType);
        assertNotNull(charClass);
    }

    static Stream<Arguments> yamlServicesWithBadDataProvider() {
        return Stream.of(
          Arguments.arguments(
                  Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidCharClassDataYamlLoaderService()),
                  "Error loading character class YAML file invalid-data.yml"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidCharTypeYamlLoaderService()),
                    "Character class type invalid found in YAML file invalid-class.yml is not valid"),
            Arguments.arguments(
                    new ArrayList<CharClassYamlLoaderService>(
                            // Randomly chosen selection of character classes
                            List.of(new TestYamlLoaderServices.TestBerzerkerYamlLoaderService(),
                                    new TestYamlLoaderServices.TestRangerYamlLoaderService(),
                                    new TestYamlLoaderServices.TestSkaldYamlLoaderService())),
                    "No entry for character type mage in character class type map"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.NoFeaturesYamlLoaderService()),
                    "Character class type berzerker has null feature data"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.NoTier1OrTier2FeaturesYamlLoaderService()),
                   "Character class type berzerker has invalid Tier I feature data: Null feature list"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.TooFewFeaturesYamlLoaderService()),
                    "Character class type berzerker has invalid Tier I feature data: Expected between 8 and 9 features in list, but got 2"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.TooManyFeaturesYamlLoaderService()),
                    "Character class type berzerker has invalid Tier I feature data: Expected between 8 and 9 features in list, but got 12"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.FeatureBlankDescriptionYamlLoaderService()),
                    "Character class type berzerker has invalid Tier I feature data: Feature with blank description found"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidAttrPlusOneModifierYamlLoaderService()),
                    "Character class type berzerker has invalid Tier I feature data: Error reading modifier INVALID for ATTR_PLUS_1 value type"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidBonusHpModifierYamlLoaderService()),
                    "Character class type berzerker has invalid Tier I feature data: Modifier NOT_A_NUMBER for BONUS_HP value type must be an integer"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidDaPlusOneModifierYamlLoaderService()),
                    "Character class type berzerker has invalid Tier I feature data: Error reading modifier INVALID for DA_PLUS_1 value type"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidSkillForSkaldYamlLoaderService()),
                    "Character class type skald has invalid Tier I feature data: Expected blank string for SKILL modifier for skald, but found FOO"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidSkillAttributeForMageYamlLoaderService()),
                    "Character class type mage has invalid Tier I feature data: Expected INT for SKILL modifier for mage, but found STR"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidSkillRandomStringForMageYamlLoaderService()),
                    "Character class type mage has invalid Tier I feature data: Error reading modifier FOO for SKILL value type for mage, expected INT"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidSkillForBerzerkerYamlLoaderService()),
                    "Character class type berzerker has invalid Tier I feature data: Found SKILL attribute with unexpected character type berzerker"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.NoTier2FeaturesYamlLoaderService()),
                    "Character class type berzerker has invalid Tier II feature data: Null feature list"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidAdvModifierSkill()),
                    "Character class type berzerker has invalid Tier I feature data: Unexpected modifier Not A Skill found for ADV/DADV value type"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidDadvModifierSkill()),
                    "Character class type berzerker has invalid Tier II feature data: Unexpected modifier Not A Skill found for ADV/DADV value type"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidAdvModifierForgottenLore()),
                    "Character class type berzerker has invalid Tier I feature data: Unexpected modifier Forgotten Lore found for ADV/DADV value type"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidDadvModifierForgottenLore()),
                    "Character class type berzerker has invalid Tier II feature data: Unexpected modifier Forgotten Lore found for ADV/DADV value type"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidAdvModifierUnarmedDamage()),
                    "Character class type berzerker has invalid Tier I feature data: Unexpected modifier Unarmed found for ADV/DADV value type"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidDadvModifierUnarmedDamage()),
                    "Character class type berzerker has invalid Tier II feature data: Unexpected modifier Unarmed found for ADV/DADV value type"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidAdvModifierAny()),
                    "Character class type berzerker has invalid Tier I feature data: Unexpected modifier Any found for ADV/DADV value type"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidDadvModifierAny()),
                    "Character class type berzerker has invalid Tier II feature data: Unexpected modifier Any found for ADV/DADV value type"
            ),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.NoAttackModifiers()),
                    "Character class type berzerker has null or empty attack modifier list"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.EmptyAttackModifiers()),
                    "Character class type berzerker has null or empty attack modifier list"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.TooFewAttackModifiers()),
                    "Character class type berzerker attack modifier list has 4 elements: expected 7"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.TooManyAttackModifiers()),
                    "Character class type berzerker attack modifier list has 8 elements: expected 7"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.NoEvasionModifiers()),
                    "Character class type berzerker has null or empty evasion modifier list"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.EmptyEvasionModifiers()),
                    "Character class type berzerker has null or empty evasion modifier list"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.TooFewEvasionModifiers()),
                    "Character class type berzerker evasion modifier list has 4 elements: expected 7"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.TooManyEvasionModifiers()),
                    "Character class type berzerker evasion modifier list has 8 elements: expected 7"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.NoLevel1Hp()),
                    "Character class type berzerker has null level 1 HP"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.NoMaxHpAtLevelUp()),
                    "Max HP at level up for character class type berzerker is null: expected 3 or 4"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidMaxHpAtLevelUp()),
                    "Max HP at level up for character class type berzerker is 2: expected 3 or 4"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.NoSkills()),
                    "Character class type berzerker has null or empty skills list"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.EmptySkills()),
                    "Character class type berzerker has null or empty skills list"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.InvalidSkill()),
                    "Character class type berzerker has unknown skill NotASkill"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.NoGear()),
                    "Character class type berzerker has null gear data"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.EmptyGear()),
                    "Character class type berzerker has null gear data"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.GearNoArmor()),
                    "Character class type berzerker has null or missing armor information in gear"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.GearEmptyArmor()),
                    "Character class type berzerker has null or missing armor information in gear"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.GearNoWeapons()),
                    "Character class type berzerker has null or missing weapon information in gear"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.GearEmptyWeapons()),
                    "Character class type berzerker has null or missing weapon information in gear"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.GearNoMaxCopper()),
                    "Character class type berzerker has null or missing max copper information in gear"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.GearEmptyMaxCopper()),
                    "Character class type berzerker has null or missing max copper information in gear"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.GearNoMaxSilver()),
                    "Character class type berzerker has null or missing max silver information in gear"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.GearEmptyMaxSilver()),
                    "Character class type berzerker has null or missing max silver information in gear"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.GearNoItems()),
                    "Character class type berzerker has null or missing item information in gear"),
            Arguments.arguments(
                    Collections.singletonList(new TestInvalidYamlLoaderServices.GearEmptyItems()),
                    "Character class type berzerker has null or missing item information in gear")
        );
    }
}
