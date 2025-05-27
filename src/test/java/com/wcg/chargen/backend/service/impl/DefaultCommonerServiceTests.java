package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.model.Commoner;
import com.wcg.chargen.backend.service.YamlLoaderService;
import com.wcg.chargen.backend.service.impl.yaml.CommonerYamlLoaderService;
import com.wcg.chargen.backend.testUtil.PostConstructUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultCommonerServiceTests {
    static class MissingAttackDataYamlLoaderService extends CommonerYamlLoaderService {
        @Override
        public String getYamlFile() { return "commoner-missing-attack.yml"; }
    }

    static class EmptyAttackDataYamlLoaderService extends CommonerYamlLoaderService{
        @Override
        public String getYamlFile() { return "commoner-empty-attack.yml"; }
    }

    static class MissingEvasionDataYamlLoaderService extends CommonerYamlLoaderService {
        @Override
        public String getYamlFile() { return "commoner-missing-evasion.yml"; }
    }

    static class EmptyEvasionDataYamlLoaderService extends CommonerYamlLoaderService {
        @Override
        public String getYamlFile() { return "commoner-empty-evasion.yml"; }
    }

    static class MissingMaxCopperDataYamlLoaderService extends CommonerYamlLoaderService {
        @Override
        public String getYamlFile() { return "commoner-missing-max-copper.yml"; }
    }

    static class EmptyMaxCopperDataYamlLoaderService extends CommonerYamlLoaderService {
        @Override
        public String getYamlFile() { return "commoner-empty-max-copper.yml"; }
    }

    static class MissingMaxSilverDataYamlLoaderService extends CommonerYamlLoaderService {
        @Override
        public String getYamlFile() { return "commoner-missing-max-silver.yml"; }
    }

    static class EmptyMaxSilverDataYamlLoaderService extends CommonerYamlLoaderService {
        @Override
        public String getYamlFile() { return "commoner-empty-max-silver.yml"; }
    }

    static class MissingItemsDataYamlLoaderService extends CommonerYamlLoaderService {
        @Override
        public String getYamlFile() { return "commoner-missing-items.yml"; }
    }

    static class EmptyItemsDataYamlLoaderService extends CommonerYamlLoaderService {
        @Override
        public String getYamlFile() { return "commoner-empty-items.yml"; }
    }

    static class ValidDataYamlLoaderService extends CommonerYamlLoaderService{
        @Override
        public String getYamlFile() { return "commoner.yml"; }
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
                        "Missing commoner evasion info"),
                Arguments.arguments(new MissingMaxCopperDataYamlLoaderService(),
                        "Missing commoner max copper info"),
                Arguments.arguments(new EmptyMaxCopperDataYamlLoaderService(),
                        "Missing commoner max copper info"),
                Arguments.arguments(new MissingMaxSilverDataYamlLoaderService(),
                        "Missing commoner max silver info"),
                Arguments.arguments(new EmptyMaxSilverDataYamlLoaderService(),
                        "Missing commoner max silver info"),
                Arguments.arguments(new MissingItemsDataYamlLoaderService(),
                        "Missing commoner item info"),
                Arguments.arguments(new EmptyItemsDataYamlLoaderService(),
                        "Missing commoner item info")
        );
    }
}
