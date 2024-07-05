package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.model.Skills;
import com.wcg.chargen.backend.service.YamlLoaderService;
import com.wcg.chargen.backend.testUtil.PostConstructUtil;
import com.wcg.chargen.backend.testUtil.SkillsProviderUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultSkillsProviderTests {

    static class InvalidSkillsDataYamlLoaderService implements YamlLoaderService<Skills> {
        public InvalidSkillsDataYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "invalid-data.yml";
        }

        @Override
        public Class<Skills> getObjClass() {
            return Skills.class;
        }
    }


    @Test
    void yamlFile_Without_Valid_Skills_Data_Throws_Exception() {
        var defaultSkillsProvider = new DefaultSkillsProvider(new InvalidSkillsDataYamlLoaderService());
        var exception = assertThrows(InvocationTargetException.class, () -> {
            PostConstructUtil.invokeMethod(DefaultSkillsProvider.class, defaultSkillsProvider);
        });

        var targetException = exception.getTargetException();
        assertEquals(IllegalStateException.class, targetException.getClass());
        assertEquals("Error loading skills YAML file", targetException.getMessage());
    }

    @ParameterizedTest
    @MethodSource("skillsDataProvider")
    void yamlFile_With_Valid_Skills_Data_Behaves_As_Expected(String skillName, boolean shouldBePresent) {
        var defaultSkillsProvider = SkillsProviderUtil.getObject();

        try {
            PostConstructUtil.invokeMethod(DefaultSkillsProvider.class, defaultSkillsProvider);
        }
        catch (Exception e) {
            fail();
        }

        assertEquals(shouldBePresent, defaultSkillsProvider.getByName(skillName) != null);
        assertEquals(shouldBePresent, defaultSkillsProvider.getSkillNameSet().contains(skillName));
    }

    static Stream<Arguments> skillsDataProvider() {
        return Stream.of(
                Arguments.arguments("Alchemy", true),
                Arguments.arguments("Animal Expertise", true),
                Arguments.arguments("Appraisal", true),
                Arguments.arguments("Arcana", true),
                Arguments.arguments("Athletics", true),
                Arguments.arguments("Culture", true),
                Arguments.arguments("Deceit", true),
                Arguments.arguments("Gather Information", true),
                Arguments.arguments("Healing", true),
                Arguments.arguments("History", true),
                Arguments.arguments("Intimidation", true),
                Arguments.arguments("Languages", true),
                Arguments.arguments("Nature", true),
                Arguments.arguments("Negotiation", true),
                Arguments.arguments("Perform", true),
                Arguments.arguments("Precise Tasks", true),
                Arguments.arguments("Religion", true),
                Arguments.arguments("Stealth", true),
                Arguments.arguments("Survival", true),
                Arguments.arguments("Invalid", false)
        );
    }
}
