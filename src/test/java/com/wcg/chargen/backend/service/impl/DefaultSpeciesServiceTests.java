package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.service.impl.yaml.SpeciesYamlLoaderService;
import com.wcg.chargen.backend.testUtil.PostConstructUtil;
import com.wcg.chargen.backend.testUtil.TestYamlLoaderServices;

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

public class DefaultSpeciesServiceTests {
    static class InvalidSpeciesDataYamlLoaderService extends SpeciesYamlLoaderService {
        public InvalidSpeciesDataYamlLoaderService() {

        }

        @Override
        public String getYamlFile() {
            return "invalid-data.yml";
        }
    }

    static class InvalidSpeciesTypeYamlLoaderService extends SpeciesYamlLoaderService {
        public InvalidSpeciesTypeYamlLoaderService() {

        }

        @Override
        public String getYamlFile() {
            return "invalid-species.yml";
        }
    }

    @ParameterizedTest
    @MethodSource("yamlServicesWithBadDataProvider")
    void test_yamlFiles_With_Invalid_Data_Throw_Exception(List<SpeciesYamlLoaderService> yamlLoaderServiceList, String expectedMsg) {
        var defaultSpeciesService = new DefaultSpeciesService(yamlLoaderServiceList);

        var exception = assertThrows(InvocationTargetException.class, () -> {
            PostConstructUtil.invokeMethod(DefaultSpeciesService.class, defaultSpeciesService);
        });

        var targetException = exception.getTargetException();
        assertEquals(IllegalStateException.class, targetException.getClass());
        assertEquals(expectedMsg, targetException.getMessage());
    }

    @ParameterizedTest
    @EnumSource(SpeciesType.class)
    void test_yamlFiles_Representing_All_Species_Loads_Successfully(SpeciesType speciesType) {
        var defaultSpeciesService = new DefaultSpeciesService(TestYamlLoaderServices.getAllTestSpeciesList());

        try {
            PostConstructUtil.invokeMethod(DefaultSpeciesService.class, defaultSpeciesService);
        }
        catch (Exception e) {
            fail();
        }

        var species = defaultSpeciesService.getSpeciesByType(speciesType);
        assertNotNull(species);
    }

    static Stream<Arguments> yamlServicesWithBadDataProvider() {
        return Stream.of(
                Arguments.arguments(Collections.singletonList(new InvalidSpeciesDataYamlLoaderService()),
                        "Error loading species YAML file invalid-data.yml"),
                Arguments.arguments(Collections.singletonList(new InvalidSpeciesTypeYamlLoaderService()),
                        "Species type invalid found in YAML file invalid-species.yml is not valid"),
                Arguments.arguments(new ArrayList<>(List.of(
                  // Randomly chosen selection of species
                  new TestYamlLoaderServices.TestElfYamlLoaderService(),
                  new TestYamlLoaderServices.TestDwarfYamlLoaderService()
                )),
                        "No entry for species halfling in species type map")
        );
    }
}
