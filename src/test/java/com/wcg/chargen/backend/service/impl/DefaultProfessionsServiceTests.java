package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.model.Professions;
import com.wcg.chargen.backend.service.RandomNumberService;
import com.wcg.chargen.backend.service.YamlLoaderService;

import com.wcg.chargen.backend.testUtil.PostConstructUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultProfessionsServiceTests {
    @Mock
    RandomNumberService randomNumberServiceMock;

    /**
     * Class to load YAML file with invalid profession data
     */
    static class InvalidProfessionDataYamlLoaderService implements YamlLoaderService<Professions> {
        public InvalidProfessionDataYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "invalid-data.yml";
        }

        @Override
        public Class<Professions> getObjClass() {
            return Professions.class;
        }
    }

    /**
     * Class to load YAML file with incomplete range data, i.e. not all integers 1-99
     * have a profession associated with them
     */
    static class IncompleteRangeDataYamlLoaderService implements YamlLoaderService<Professions> {

        @Override
        public String getYamlFile() {
            return "professions-incomplete-range.yml";
        }

        @Override
        public Class<Professions> getObjClass() {
            return Professions.class;
        }
    }

    /**
     * Class to load YAML file with valid test data
     */
    static class ValidDataYamlLoaderService implements YamlLoaderService<Professions> {

        @Override
        public String getYamlFile() {
            return "professions-test.yml";
        }

        @Override
        public Class<Professions> getObjClass() {
            return Professions.class;
        }
    }

    @ParameterizedTest
    @MethodSource("yamlServicesWithBadDataProvider")
    void test_yamlFile_Without_Valid_Profession_Data_Throws_Exception(YamlLoaderService<Professions> yamlLoaderService,
                                                           String expectedMsg) {
        var defaultProfessionService = new DefaultProfessionsService(yamlLoaderService, randomNumberServiceMock);
        // When reflection is used, the top-level exception is InvocationTargetException
        var exception = assertThrows(InvocationTargetException.class, () -> {
            PostConstructUtil.invokeMethod(DefaultProfessionsService.class, defaultProfessionService);
        });

        var targetException = exception.getTargetException();
        assertEquals(IllegalStateException.class, targetException.getClass());
        assertEquals(expectedMsg, targetException.getMessage());
    }

    @Test
    void test_generateRandomProfessions_PalindromeNumber_Returns_Three_Processions() {
        var defaultProfessionService = getValidDefaultProfessionService();

        when(randomNumberServiceMock.getIntFromRange(1, 99)).thenReturn(77);

        var professions = defaultProfessionService.generateRandomProfessions();
        var professionList = professions.professions();

        assertEquals(3, professionList.size());
        assertEquals("Test5", professionList.get(0).name());
        assertEquals("Test6", professionList.get(1).name());
        assertEquals("Test7", professionList.get(2).name());
    }

    @Test
    void test_generateRandomProfessions_Number_Less_Than_10_Returns_Correct_Inverse() {
        var defaultProfessionService = getValidDefaultProfessionService();

        when(randomNumberServiceMock.getIntFromRange(1, 99)).thenReturn(9);

        var professions = defaultProfessionService.generateRandomProfessions();
        var professionList = professions.professions();

        assertEquals(2, professionList.size());
        assertEquals("Test2", professionList.get(0).name());
        assertEquals("Test9", professionList.get(1).name());
    }

    @Test
    void test_generateRandomProfessions_Number_Divisible_By_10_Returns_Correct_Inverse() {
        var defaultProfessionService = getValidDefaultProfessionService();

        when(randomNumberServiceMock.getIntFromRange(1, 99)).thenReturn(40);

        var professions = defaultProfessionService.generateRandomProfessions();
        var professionList = professions.professions();

        assertEquals(2, professionList.size());
        assertEquals("Test4", professionList.get(0).name());
        assertEquals("Test1", professionList.get(1).name());
    }

    @Test
    void test_generateRandomProfessions_Number_Greater_Than_10_Not_Divisible_By_10_Returns_Correct_Inverse() {
        var defaultProfessionService = getValidDefaultProfessionService();

        when(randomNumberServiceMock.getIntFromRange(1, 99)).thenReturn(28);

        var professions = defaultProfessionService.generateRandomProfessions();
        var professionList = professions.professions();

        assertEquals(2, professionList.size());
        assertEquals("Test3", professionList.get(0).name());
        assertEquals("Test8", professionList.get(1).name());
    }

    private DefaultProfessionsService getValidDefaultProfessionService() {
        var defaultProfessionService = new DefaultProfessionsService(new ValidDataYamlLoaderService(),
                randomNumberServiceMock);

        // Invoke PostConstruct method to populate professions data
        try {
            PostConstructUtil.invokeMethod(DefaultProfessionsService.class, defaultProfessionService);
        }
        catch (Exception e)
        {
            fail();
        }

        return defaultProfessionService;
    }

    static Stream<Arguments> yamlServicesWithBadDataProvider() {
        return Stream.of(
                Arguments.arguments(new InvalidProfessionDataYamlLoaderService(),
                        "Error loading professions YAML file"),
                Arguments.arguments(new IncompleteRangeDataYamlLoaderService(),
                        "Professions table has missing elements")
        );
    }
}
