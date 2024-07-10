package com.wcg.chargen.backend.controller;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharClass;
import com.wcg.chargen.backend.model.FeatureResponse;
import com.wcg.chargen.backend.model.Features;
import com.wcg.chargen.backend.model.SkillsResponse;
import com.wcg.chargen.backend.service.impl.DefaultCharClassesService;
import com.wcg.chargen.backend.service.impl.DefaultFeaturesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeaturesController.class)
public class FeaturesControllerTests {
    @MockBean
    private DefaultFeaturesService featuresService;

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
    @ValueSource(ints = {0, 8})
    void Invalid_Level_Query_Parameters_Return_Bad_Request(int level) {
        try {
            mockMvc.perform(get("/api/v1/features?charClass=mage&level=" + level))
                    .andExpect(status().isBadRequest());
        }
        catch (Exception e) {
            fail();
        }
    }

    @ParameterizedTest
    @MethodSource("generateCharTypeLevelCrossProduct")
    void Valid_Query_Parameters_And_Data_Return_Success(CharType charType, int level) {
        var features = new Features(Collections.emptyList(), Collections.emptyList());
        var featureResponse = new FeatureResponse(1, 0, features);
        when(featuresService.getFeatures(any(CharType.class), anyInt()))
                .thenReturn(featureResponse);

        try {
            var endpointUrl = String.format("/api/v1/features?charClass=%s&level=%d",
                    charType.name().toLowerCase(),
                    level);
            mockMvc.perform(get(endpointUrl))
                    .andExpect(status().isOk());
        }
        catch (Exception e) {
            fail();
        }
    }

    private static Stream<Arguments> generateCharTypeLevelCrossProduct() {
        var paramsList = new ArrayList<Arguments>();

        for (var charType: CharType.values()) {
            for (int i = 1; i < 8; i++) {
                paramsList.add(Arguments.arguments(charType, i));
            }
        }

        return paramsList.stream();
    }
}
