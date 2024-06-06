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

    private static final List<String> DEFAULT_SPECIES_SKILL_NAME_LIST = new ArrayList<>(List.of(
            "Appraisal",
            "Arcana"
    ));

    private static final List<String> INVALID_SKILL_NAME_LIST = Collections.singletonList("Invalid Skill");

    private enum SpeciesSkillsStatus {
        VALID(DEFAULT_SPECIES_SKILL_NAME_LIST),
        INVALID(INVALID_SKILL_NAME_LIST),
        NULL(null);

        private final List<String> skillsList;

        SpeciesSkillsStatus(List<String> skillsList) {
            this.skillsList = skillsList;
        }

        public List<String> getSkillsList() { return skillsList; }
    }

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
    void yamlFile_Without_Valid_Skills_Data_Throws_Exception() {
        var defaultSkillsService = new DefaultSkillsService(new InvalidSkillsDataYamlLoaderService(), null, null);
        var exception = assertThrows(InvocationTargetException.class, () -> {
            PostConstructUtil.invokeMethod(DefaultSkillsService.class, defaultSkillsService);
        });

        var targetException = exception.getTargetException();
        assertEquals(IllegalStateException.class, targetException.getClass());
        assertEquals("Error loading skills YAML file", targetException.getMessage());
    }

    @Test
    void getSkills_Correctly_Sets_Class_Skills_And_Bonus_Skills() {
        var defaultSkillsService = getConfiguredDefaultSkillsService(true,
                SpeciesSkillsStatus.VALID);
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
        var actualClassSkillNameList = getSkillNameList(skillsResponse.getClassSkills());
        var actualSpeciesSkillNameList = getSkillNameList(skillsResponse.getSpeciesSkills());
        var actualBonusSkillNameList = getSkillNameList(skillsResponse.getBonusSkills());

        assertEquals(DEFAULT_CLASS_SKILL_NAME_LIST, actualClassSkillNameList);
        assertEquals(expectedSpeciesSkillNameList, actualSpeciesSkillNameList);
        assertEquals(expectedBonusSkillNameList, actualBonusSkillNameList);
    }

    @Test
    void getSkills_Returns_Empty_Response_If_Class_Skill_Is_Missing_From_Master_Skill_List() {
        var defaultSkillsService = getConfiguredDefaultSkillsService(false,
                SpeciesSkillsStatus.VALID);
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
    void getSkills_Returns_Empty_Response_If_Species_Skill_Is_Missing_From_Master_Skill_List() {
        var defaultSkillsService = getConfiguredDefaultSkillsService(true,
                SpeciesSkillsStatus.INVALID);
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

    @Test
    void getSkills_Returns_Expected_Results_For_Human_Species() {
        var defaultSkillsService = getConfiguredDefaultSkillsService(true,
                SpeciesSkillsStatus.NULL);
        if (defaultSkillsService == null) {
            fail();
        }

        // All non-class skills should appear as bonus skills for a human
        var expectedBonusSkillNameList = new ArrayList<>(List.of("Animal Expertise",
                "Appraisal",
                "Athletics",
                "Culture",
                "Deceit"));

        var skillsResponse = defaultSkillsService.getSkills(CharType.SKALD, SpeciesType.HUMAN);

        assertNotNull(skillsResponse);
        assertNotNull(skillsResponse.getClassSkills());
        assertNotNull(skillsResponse.getBonusSkills());
        assertTrue(skillsResponse.getSpeciesSkills().isEmpty());

        assertEquals(expectedBonusSkillNameList, getSkillNameList(skillsResponse.getBonusSkills()));
    }

    private DefaultSkillsService getConfiguredDefaultSkillsService(boolean hasValidClassSkills,
                                                                   SpeciesSkillsStatus speciesSkillsStatus) {
        var classSkillList = hasValidClassSkills ?
                DEFAULT_CLASS_SKILL_NAME_LIST :
                INVALID_SKILL_NAME_LIST;
        var charClass = new CharClass("test", classSkillList, null);
        var species = new Species("test", speciesSkillsStatus.getSkillsList());

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

    private List<String> getSkillNameList(List<Skill> skillList) {
        if (skillList == null) {
            return new ArrayList<>();
        }

        return skillList.stream().map(Skill::name).toList();
    }
}
