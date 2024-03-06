package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.model.CharClass;
import com.wcg.chargen.backend.model.Skill;
import com.wcg.chargen.backend.model.Skills;
import com.wcg.chargen.backend.service.YamlLoaderService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultSkillsServiceTests {
    @Mock
    DefaultCharClassesService charClassesServiceMock;

    private List<String> DEFAULT_CLASS_SKILL_NAME_LIST = new ArrayList<>(List.of("Alchemy",
            "Appraisal",
            "Arcana"));

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
    static class ValidSkillsDataYamlLoaderService implements YamlLoaderService<Skills> {
        public ValidSkillsDataYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "test-skills.yml";
        }

        @Override
        public Class<Skills> getObjClass() {
            return Skills.class;
        }
    }

    @Test
    void test_yamlFile_Without_Valid_Skills_Data_Throws_Exception() {
        var defaultSkillsService = new DefaultSkillsService(new InvalidSkillsDataYamlLoaderService(), null);
        var exception = assertThrows(InvocationTargetException.class, () -> {
            // Use reflection to invoke @PostConstruct annotated method directly rather than via Spring framework
            var postConstructMethod = DefaultSkillsService.class.getDeclaredMethod("postConstruct");
            postConstructMethod.setAccessible(true);
            postConstructMethod.invoke(defaultSkillsService);
        });

        var targetException = exception.getTargetException();
        assertEquals(IllegalStateException.class, targetException.getClass());
        assertEquals("Error loading skills YAML file", targetException.getMessage());
    }

    @Test
    void test_getSkills_Correctly_Sets_Class_Skills_And_Bonus_Skills() {
        var defaultSkillsService = getConfiguredDefaultSkillsService(true);
        if (defaultSkillsService == null) {
            fail();
        }

        var expectedBonusSkillNameList = new ArrayList<>(List.of("Animal Expertise",
                "Athletics"));

        // CharType value doesn't matter here, as values are set by mock
        var skillsResponse = defaultSkillsService.getSkills(CharType.ROGUE);

        // Get names of skills returned
        var actualClassSkillNameList = skillsResponse.getClassSkills().stream().map(Skill::name).toList();
        var actualBonusSkillNameList = skillsResponse.getBonusSkills().stream().map(Skill::name).toList();

        assertEquals(DEFAULT_CLASS_SKILL_NAME_LIST, actualClassSkillNameList);
        assertEquals(expectedBonusSkillNameList, actualBonusSkillNameList);
    }

    @Test
    void test_getSkills_Returns_Empty_Response_If_Class_Skill_Is_Missing_From_Master_Skill_List() {
        var defaultSkillsService = getConfiguredDefaultSkillsService(false);
        if (defaultSkillsService == null) {
            fail();
        }

        // CharType value doesn't matter here, as values are set by mock
        var skillsResponse = defaultSkillsService.getSkills(CharType.SHAMAN);

        assertNotNull(skillsResponse);
        assertNotNull(skillsResponse.getClassSkills());
        assertNotNull(skillsResponse.getBonusSkills());
        assertTrue(skillsResponse.getClassSkills().isEmpty());
        assertTrue(skillsResponse.getBonusSkills().isEmpty());
    }

    private DefaultSkillsService getConfiguredDefaultSkillsService(boolean hasValidClassSkills) {
        var skillList = hasValidClassSkills ?
                DEFAULT_CLASS_SKILL_NAME_LIST :
                Collections.singletonList("Invalid Skill");
        var charClass = new CharClass("test", skillList);
        when(charClassesServiceMock.getCharClassByType(any(CharType.class))).thenReturn(charClass);

        var skillsService = new DefaultSkillsService(new ValidSkillsDataYamlLoaderService(),
                charClassesServiceMock);

        // Call @PostConstruct method
        try {
            var postConstructMethod = DefaultSkillsService.class.getDeclaredMethod("postConstruct");
            postConstructMethod.setAccessible(true);
            postConstructMethod.invoke(skillsService);
        }
        catch (Exception e) {
            return null;
        }

        return skillsService;
    }
}
