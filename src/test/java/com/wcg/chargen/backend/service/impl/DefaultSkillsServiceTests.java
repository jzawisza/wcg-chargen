package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharClass;
import com.wcg.chargen.backend.model.Skill;
import com.wcg.chargen.backend.model.Species;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.SpeciesService;

import com.wcg.chargen.backend.testUtil.SkillsProviderUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DefaultSkillsServiceTests {
    @Mock
    CharClassesService charClassesServiceMock;

    @Mock
    SpeciesService speciesServiceMock;

    private static final List<String> ROGUE_SKILL_NAME_LIST = new ArrayList<>(List.of(
            "Appraisal",
            "Athletics",
            "Deceit",
            "Gather Information",
            "Precise Tasks",
            "Stealth"));

    private static final List<String> DWARF_SKILL_NAME_LIST =  new ArrayList<>(List.of(
            "Appraisal",
            "Athletics",
            "Intimidation"));

    private static final List<String> INVALID_SKILL_NAME_LIST = Collections.singletonList("Invalid Skill");

    private enum SpeciesSkillsStatus {
        VALID(DWARF_SKILL_NAME_LIST),
        INVALID(INVALID_SKILL_NAME_LIST),
        NULL(null);

        private final List<String> skillsList;

        SpeciesSkillsStatus(List<String> skillsList) {
            this.skillsList = skillsList;
        }

        public List<String> getSkillsList() { return skillsList; }
    }

    @Test
    void getSkills_Correctly_Sets_Class_Skills_And_Bonus_Skills() {
        var defaultSkillsService = getConfiguredDefaultSkillsService(true,
                SpeciesSkillsStatus.VALID);

        // Appraisal and Athletics are excluded from the species skill list because they're class skills
        var expectedSpeciesSkillNameList = new ArrayList<>(List.of("Intimidation"));

        // Any non-class skills, including species skills, should be in the bonus skill list
        var expectedBonusSkillNameList = new ArrayList<>(List.of(
                "Alchemy",
                "Animal Expertise",
                "Arcana",
                "Culture",
                "Healing",
                "History",
                "Intimidation",
                "Languages",
                "Nature",
                "Negotiation",
                "Perform",
                "Religion",
                "Survival"));

        // Parameter values don't matter: mock controls logic
        // Mock is set up for character class Rogue and species Dwarf
        var skillsResponse = defaultSkillsService.getSkills(CharType.ROGUE, SpeciesType.DWARF);

        // Get names of skills returned
        var actualClassSkillNameList = getSkillNameList(skillsResponse.getClassSkills());
        var actualSpeciesSkillNameList = getSkillNameList(skillsResponse.getSpeciesSkills());
        var actualBonusSkillNameList = getSkillNameList(skillsResponse.getBonusSkills());

        assertEquals(ROGUE_SKILL_NAME_LIST, actualClassSkillNameList);
        assertEquals(expectedSpeciesSkillNameList, actualSpeciesSkillNameList);
        assertEquals(expectedBonusSkillNameList, actualBonusSkillNameList);
    }

    @Test
    void getSkills_Returns_Empty_Response_If_Class_Skill_Is_Missing_From_Master_Skill_List() {
        var defaultSkillsService = getConfiguredDefaultSkillsService(false,
                SpeciesSkillsStatus.VALID);

        // Parameter values don't matter: mock controls logic
        // Mock is set up for character class Rogue and species Dwarf
        var skillsResponse = defaultSkillsService.getSkills(CharType.ROGUE, SpeciesType.DWARF);

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

        // Parameter values don't matter: mock controls logic
        // Mock is set up for character class Rogue and species Dwarf
        var skillsResponse = defaultSkillsService.getSkills(CharType.ROGUE, SpeciesType.DWARF);

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

        // All non-class skills should appear as bonus skills for a human
        var expectedBonusSkillNameList = new ArrayList<>(List.of(
                "Alchemy",
                "Animal Expertise",
                "Arcana",
                "Culture",
                "Healing",
                "History",
                "Intimidation",
                "Languages",
                "Nature",
                "Negotiation",
                "Perform",
                "Religion",
                "Survival"));

        // Parameter values don't matter: mock controls logic
        // Mock is set up for character class Rogue
        var skillsResponse = defaultSkillsService.getSkills(CharType.ROGUE, SpeciesType.HUMAN);

        assertNotNull(skillsResponse);
        assertNotNull(skillsResponse.getClassSkills());
        assertNotNull(skillsResponse.getBonusSkills());
        assertTrue(skillsResponse.getSpeciesSkills().isEmpty());

        assertEquals(expectedBonusSkillNameList, getSkillNameList(skillsResponse.getBonusSkills()));
    }

    private DefaultSkillsService getConfiguredDefaultSkillsService(boolean hasValidClassSkills,
                                                                   SpeciesSkillsStatus speciesSkillsStatus) {
        var classSkillList = hasValidClassSkills ?
                ROGUE_SKILL_NAME_LIST :
                INVALID_SKILL_NAME_LIST;
        var charClass = new CharClass(CharType.SKALD.toString(), null, null,
                null, null, classSkillList, null);
        var species = new Species("test", null, null, speciesSkillsStatus.getSkillsList());

        when(charClassesServiceMock.getCharClassByType(any(CharType.class))).thenReturn(charClass);
        when(speciesServiceMock.getSpeciesByType(any(SpeciesType.class))).thenReturn(species);

        return new DefaultSkillsService(SkillsProviderUtil.getObject(),
                charClassesServiceMock,
                speciesServiceMock);
    }

    private List<String> getSkillNameList(List<Skill> skillList) {
        if (skillList == null) {
            return new ArrayList<>();
        }

        return skillList.stream().map(Skill::name).toList();
    }
}
