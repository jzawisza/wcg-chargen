package com.wcg.chargen.backend.controller;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharClass;
import com.wcg.chargen.backend.model.Features;
import com.wcg.chargen.backend.model.SkillsResponse;
import com.wcg.chargen.backend.service.impl.DefaultCharClassesService;
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

@WebMvcTest(FeaturesController.class)
public class FeaturesControllerTests {
    @MockBean
    private DefaultCharClassesService charClassesService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void Missing_Query_Parameters_Returns_Bad_Request() {
        try {
            mockMvc.perform(get("/api/v1/features"))
                    .andExpect(status().isBadRequest());
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    void Invalid_Character_Class_Query_Parameter_Returns_Bad_Request() {
        try {
            mockMvc.perform(get("/api/v1/features?charClass=invalid"))
                    .andExpect(status().isBadRequest());
        }
        catch (Exception e) {
            fail();
        }
    }

    @ParameterizedTest
    @EnumSource(CharType.class)
    void Valid_Character_Class_Query_Parameter_And_Valid_Data_Returns_Success(CharType charType) {
        var features = new Features(Collections.emptyList(), Collections.emptyList());
        var charClass = new CharClass("test", Collections.emptyList(), features);
        when(charClassesService.getCharClassByType(any(CharType.class)))
                .thenReturn(charClass);

        try {
            mockMvc.perform(get(
                            "/api/v1/features?charClass=" + charType.name().toLowerCase()))
                    .andExpect(status().isOk());
        }
        catch (Exception e) {
            fail();
        }
    }
}
