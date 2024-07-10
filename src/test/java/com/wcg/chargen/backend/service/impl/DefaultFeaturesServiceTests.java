package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.model.CharClass;
import com.wcg.chargen.backend.model.Feature;
import com.wcg.chargen.backend.model.Features;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.FeaturesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DefaultFeaturesServiceTests {
    @Mock
    CharClassesService charClassesServiceMock;

    @ParameterizedTest
    @ValueSource(ints = {0, 8})
    public void GetFeatures_Invalid_Level_Values_Return_Null(int level) {
        var featuresService = configureFeaturesService(false);

        // CharType doesn't matter here
        var featureResponse = featuresService.getFeatures(CharType.BERZERKER, level);

        assertNull(featureResponse);
    }

    @Test
    public void GetFeatures_CharClassService_Exception_Returns_Null() {
        var featuresService = configureFeaturesService(true);

        // All that matters for parameters is that level is valid
        var featureResponse = featuresService.getFeatures(CharType.BERZERKER, 1);

        assertNull(featureResponse);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 0, 0",
            "2, 1, 0",
            "3, 3, 0",
            "4, 3, 1",
            "5, 3, 2",
            "6, 4, 3",
            "7, 5, 4"
    })
    public void GetFeatures_Valid_Data_Returns_Valid_Features_And_Correct_Tier_Counts(
            int level, int expectedTotalTier1Features, int expectedTotalTier2Features) {
        var featuresService = configureFeaturesService(false);

        // All that matters for parameters is that level is valid
        var featureResponse = featuresService.getFeatures(CharType.BERZERKER, level);

        assertNotNull(featureResponse);
        assertNotNull(featureResponse.getFeatures());
        assertEquals(expectedTotalTier1Features, featureResponse.getNumAllowedTier1Features(), "Tier I feature count incorrect");
        assertEquals(expectedTotalTier2Features, featureResponse.getNumAllowedTier2Features(), "Tier II feature count incorrect");
    }

    private FeaturesService configureFeaturesService(boolean throwsException) {
        if (throwsException) {
            // Exception needs to be unchecked for Mockito to work
            var exception = new RuntimeException("Char class service error");
            when(charClassesServiceMock.getCharClassByType(any(CharType.class))).thenThrow(exception);
        }
        else {
            var skillsList = new ArrayList<String>();
            var featureList = new ArrayList<Feature>();
            var features = new Features(featureList, featureList);
            var charClass = new CharClass("test", skillsList, features);

            when(charClassesServiceMock.getCharClassByType(any(CharType.class))).thenReturn(charClass);
        }

        return new DefaultFeaturesService(charClassesServiceMock);
    }
}
