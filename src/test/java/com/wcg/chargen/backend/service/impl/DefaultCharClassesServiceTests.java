package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.service.impl.yaml.CharClassYamlLoaderService;
import com.wcg.chargen.backend.testUtil.TestCharClassYamlLoaderServices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultCharClassesServiceTests {
    static class InvalidCharClassDataYamlLoaderService extends CharClassYamlLoaderService {
        public InvalidCharClassDataYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "invalid-data.yml";
        }
    }

    static class InvalidCharTypeYamlLoaderService extends CharClassYamlLoaderService {
        public InvalidCharTypeYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "invalid-class.yml";
        }
    }

    @ParameterizedTest
    @MethodSource("yamlServicesWithBadDataProvider")
    void test_yamlFiles_With_Invalid_Data_Throw_Exception(List<CharClassYamlLoaderService> yamlLoaderServiceList, String expectedMsg) {
        var defaultCharClassesService = new DefaultCharClassesService(yamlLoaderServiceList);

        // When reflection is used, the top-level exception is InvocationTargetException
        var exception = assertThrows(InvocationTargetException.class, () -> {
            // Use reflection to invoke @PostConstruct annotated method directly rather than via Spring framework
            var postConstructMethod = DefaultCharClassesService.class.getDeclaredMethod("postConstruct");
            postConstructMethod.setAccessible(true);
            postConstructMethod.invoke(defaultCharClassesService);
        });

        var targetException = exception.getTargetException();
        assertEquals(IllegalStateException.class, targetException.getClass());
        assertEquals(expectedMsg, targetException.getMessage());
    }

    @ParameterizedTest
    @EnumSource(CharType.class)
    void test_yamlFiles_Representing_All_Classes_Loads_Successfully(CharType charType) {
        var defaultCharClassesService = new DefaultCharClassesService(TestCharClassYamlLoaderServices.getAllTestClassesList());

        try {
            var postConstructMethod = DefaultCharClassesService.class.getDeclaredMethod("postConstruct");
            postConstructMethod.setAccessible(true);
            postConstructMethod.invoke(defaultCharClassesService);
        }
        catch (Exception e) {
            fail();
        }
        
        var charClass = defaultCharClassesService.getCharClassByType(charType);
        assertNotNull(charClass);
    }

    static Stream<Arguments> yamlServicesWithBadDataProvider() {
        return Stream.of(
          Arguments.arguments(
                  Collections.singletonList(new InvalidCharClassDataYamlLoaderService()),
                  "Error loading character class YAML file invalid-data.yml"),
            Arguments.arguments(
                    Collections.singletonList(new InvalidCharTypeYamlLoaderService()),
                    "Character class type invalid found in YAML file invalid-class.yml is not valid"),
            Arguments.arguments(
                    new ArrayList<CharClassYamlLoaderService>(
                            // Randomly chosen selection of character classes
                            List.of(new TestCharClassYamlLoaderServices.TestBerzerkerYamlLoaderService(),
                                    new TestCharClassYamlLoaderServices.TestRangerYamlLoaderService(),
                                    new TestCharClassYamlLoaderServices.TestSkaldYamlLoaderService())),
                    "No entry for character type mage in character class type map"
            )
        );
    }
}
