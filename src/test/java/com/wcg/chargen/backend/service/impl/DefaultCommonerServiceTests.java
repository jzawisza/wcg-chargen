package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.model.Commoner;
import com.wcg.chargen.backend.service.YamlLoaderService;
import com.wcg.chargen.backend.testUtil.PostConstructUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultCommonerServiceTests {
    static class MissingAttackDataYamlLoaderService implements YamlLoaderService<Commoner> {
        @Override
        public String getYamlFile() { return "commoner-missing-attack.yml"; }

        @Override
        public Class<Commoner> getObjClass() { return Commoner.class; }
    }

    static class EmptyAttackDataYamlLoaderService implements YamlLoaderService<Commoner> {
        @Override
        public String getYamlFile() { return "commoner-empty-attack.yml"; }

        @Override
        public Class<Commoner> getObjClass() { return Commoner.class; }
    }

    static class MissingEvasionDataYamlLoaderService implements YamlLoaderService<Commoner> {
        @Override
        public String getYamlFile() { return "commoner-missing-evasion.yml"; }

        @Override
        public Class<Commoner> getObjClass() { return Commoner.class; }
    }

    static class EmptyEvasionDataYamlLoaderService implements YamlLoaderService<Commoner> {
        @Override
        public String getYamlFile() { return "commoner-empty-evasion.yml"; }

        @Override
        public Class<Commoner> getObjClass() { return Commoner.class; }
    }

    static class ValidDataYamlLoaderService implements YamlLoaderService<Commoner> {
        @Override
        public String getYamlFile() { return "commoner.yml"; }

        @Override
        public Class<Commoner> getObjClass() { return Commoner.class; }
    }

    @Test
    public void getInfo_ReturnsCorrectDataIfYamlFileIsValid() {
        var commonerService = new DefaultCommonerService(new ValidDataYamlLoaderService());

        // Invoke PostConstruct method to populate data
        try {
            PostConstructUtil.invokeMethod(DefaultCommonerService.class, commonerService);
        }
        catch (Exception e) {
            fail();
        }

        var commonerInfo = commonerService.getInfo();
        assertEquals(0, commonerInfo.attack());
        assertEquals(10, commonerInfo.evasion());
    }

    @ParameterizedTest
    @MethodSource("yamlServicesWithBadDataProvider")
    public void YamlFileWithInvalidCommonerDataThrowsException(YamlLoaderService<Commoner> yamlLoaderService,
                                                               String expectedMsg) {
        var commonerService = new DefaultCommonerService(yamlLoaderService);
        // When reflection is used, the top-level exception is InvocationTargetException
        var exception = assertThrows(InvocationTargetException.class, () -> {
            PostConstructUtil.invokeMethod(DefaultCommonerService.class, commonerService);
        });

        var targetException = exception.getTargetException();
        assertEquals(IllegalStateException.class, targetException.getClass());
        assertEquals(expectedMsg, targetException.getMessage());
    }

    static Stream<Arguments> yamlServicesWithBadDataProvider() {
        return Stream.of(
                Arguments.arguments(new MissingAttackDataYamlLoaderService(),
                        "Missing commoner attack info"),
                Arguments.arguments(new EmptyAttackDataYamlLoaderService(),
                        "Missing commoner attack info"),
                Arguments.arguments(new MissingEvasionDataYamlLoaderService(),
                        "Missing commoner evasion info"),
                Arguments.arguments(new EmptyEvasionDataYamlLoaderService(),
                        "Missing commoner evasion info")
        );
    }
}
