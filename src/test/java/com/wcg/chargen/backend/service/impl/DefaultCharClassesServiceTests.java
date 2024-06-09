package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.service.impl.yaml.CharClassYamlLoaderService;
import com.wcg.chargen.backend.testUtil.PostConstructUtil;
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
        var defaultCharClassesService = new DefaultCharClassesService(yamlLoaderServiceList);

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
        var defaultCharClassesService = new DefaultCharClassesService(TestYamlLoaderServices.getAllTestCharClassesList());

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
            )
        );
    }
}
