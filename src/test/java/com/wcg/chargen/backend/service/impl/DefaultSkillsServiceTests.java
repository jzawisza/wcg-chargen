package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharClass;
import com.wcg.chargen.backend.model.Skill;
import com.wcg.chargen.backend.model.Skills;
import com.wcg.chargen.backend.model.Species;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.SpeciesService;
import com.wcg.chargen.backend.service.YamlLoaderService;
import com.wcg.chargen.backend.testUtil.PostConstructUtil;

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
    CharClassesService charClassesServiceMock;

    @Mock
    SpeciesService speciesServiceMock;

    private final List<String> DEFAULT_CLASS_SKILL_NAME_LIST = new ArrayList<>(List.of(
            "Alchemy",
            "Arcana"));

    private final List<String> DEFAULT_SPECIES_SKILL_NAME_LIST = new ArrayList<>(List.of(
            "Appraisal",
            "Arcana"
    ));

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
        var defaultSkillsService = new DefaultSkillsService(new InvalidSkillsDataYamlLoaderService(), null, null);
        var exception = assertThrows(InvocationTargetException.class, () -> {
            PostConstructUtil.invokeMethod(DefaultSkillsService.class, defaultSkillsService);
        });

        var targetException = exception.getTargetException();
        assertEquals(IllegalStateException.class, targetException.getClass());
        assertEquals("Error loading skills YAML file", targetException.getMessage());
    }

    @Test
    void test_getSkills_Correctly_Sets_Class_Skills_And_Bonus_Skills() {
        var defaultSkillsService = getConfiguredDefaultSkillsService(true, true);
        if (defaultSkillsService == null) {
            fail();
        }

        // Arcana is excluded from the species skill list because it's a class skill:
        // class skills need to be excluded from the species skill list
        var expectedSpeciesSkillNameList = new ArrayList<>(List.of("Appraisal"));

        // Any non-class skills, including species skills, should be in the bonus skill list
        var expectedBonusSkillNameList = new ArrayList<>(List.of("Animal Expertise",
                "Appraisal",
                "Athletics",
                "Culture",
                "Deceit"));

        // Parameter values don't matter: mock controls logic
        var skillsResponse = defaultSkillsService.getSkills(CharType.ROGUE, SpeciesType.ELF);

        // Get names of skills returned
        var actualClassSkillNameList = skillsResponse.getClassSkills().stream().map(Skill::name).toList();
        var actualSpeciesSkillNameList = skillsResponse.getSpeciesSkills().stream().map(Skill::name).toList();
        var actualBonusSkillNameList = skillsResponse.getBonusSkills().stream().map(Skill::name).toList();

        assertEquals(DEFAULT_CLASS_SKILL_NAME_LIST, actualClassSkillNameList);
        assertEquals(expectedSpeciesSkillNameList, actualSpeciesSkillNameList);
        assertEquals(expectedBonusSkillNameList, actualBonusSkillNameList);
    }

    @Test
    void test_getSkills_Returns_Empty_Response_If_Class_Skill_Is_Missing_From_Master_Skill_List() {
        var defaultSkillsService = getConfiguredDefaultSkillsService(false, true);
        if (defaultSkillsService == null) {
            fail();
        }

        // Parameter values don't matter: mock controls logic
        var skillsResponse = defaultSkillsService.getSkills(CharType.SHAMAN, SpeciesType.DWARF);

        assertNotNull(skillsResponse);
        assertNotNull(skillsResponse.getClassSkills());
        assertNotNull(skillsResponse.getBonusSkills());
        assertTrue(skillsResponse.getClassSkills().isEmpty());
        assertTrue(skillsResponse.getBonusSkills().isEmpty());
    }

    @Test
    void test_getSkills_Returns_Empty_Response_If_Species_Skill_Is_Missing_From_Master_Skill_List() {
        var defaultSkillsService = getConfiguredDefaultSkillsService(true, false);
        if (defaultSkillsService == null) {
            fail();
        }

        // Parameter values don't matter: mock controls logic
        var skillsResponse = defaultSkillsService.getSkills(CharType.RANGER, SpeciesType.HALFLING);

        assertNotNull(skillsResponse);
        assertNotNull(skillsResponse.getSpeciesSkills());
        assertNotNull(skillsResponse.getBonusSkills());
        assertTrue(skillsResponse.getSpeciesSkills().isEmpty());
        assertTrue(skillsResponse.getBonusSkills().isEmpty());
    }

    private DefaultSkillsService getConfiguredDefaultSkillsService(boolean hasValidClassSkills,
                                                                   boolean hasValidSpeciesSkills) {
        final var invalidSkillsList = Collections.singletonList("Invalid Skill");
        var classSkillList = hasValidClassSkills ?
                DEFAULT_CLASS_SKILL_NAME_LIST :
                invalidSkillsList;
        var speciesSkillList = hasValidSpeciesSkills ?
                DEFAULT_SPECIES_SKILL_NAME_LIST :
                invalidSkillsList;
        var charClass = new CharClass("test", classSkillList);
        var species = new Species("test", speciesSkillList);

        when(charClassesServiceMock.getCharClassByType(any(CharType.class))).thenReturn(charClass);
        when(speciesServiceMock.getSpeciesByType(any(SpeciesType.class))).thenReturn(species);

        var skillsService = new DefaultSkillsService(new ValidSkillsDataYamlLoaderService(),
                charClassesServiceMock, speciesServiceMock);

        // Call @PostConstruct method
        try {
            PostConstructUtil.invokeMethod(DefaultSkillsService.class, skillsService);
        }
        catch (Exception e) {
            return null;
        }

        return skillsService;
    }
}
