package com.wcg.chargen.backend.controller;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.model.Skill;
import com.wcg.chargen.backend.model.Skills;
import com.wcg.chargen.backend.service.impl.DefaultSkillsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SkillsController.class)
public class SkillsControllerTests {
    @MockBean
    private DefaultSkillsService skillsService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void test_Missing_Character_Class_Query_Parameter_Returns_Bad_Request() {
        try {
            mockMvc.perform(get("/api/v1/skills"))
                    .andExpect(status().isBadRequest());
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    void test_Invalid_Character_Class_Query_Parameter_Returns_Bad_Request() {
        try {
            mockMvc.perform(get("/api/v1/skills?charClass=foo"))
                    .andExpect(status().isBadRequest());
        }
        catch (Exception e) {
            fail();
        }
    }

    @ParameterizedTest
    @EnumSource(CharType.class)
    void test_Valid_Character_Class_Query_Parameter_And_Valid_Data_Returns_Success(CharType charType) {
        var testSkill = new Skill("Test", new String[] { "TST" });
        var skills = new Skills(Collections.singletonList(testSkill));

        when(skillsService.getSkills(any(CharType.class))).thenReturn(skills);

        try {
            mockMvc.perform(get("/api/v1/skills?charClass=" + charType.name().toLowerCase()))
                    .andExpect(status().isOk());
        }
        catch (Exception e) {
            fail();
        }
    }
}
