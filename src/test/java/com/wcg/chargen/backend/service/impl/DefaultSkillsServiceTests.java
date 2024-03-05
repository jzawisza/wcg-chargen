package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.model.Skills;
import com.wcg.chargen.backend.service.YamlLoaderService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultSkillsServiceTests {
    static class InvalidSkillsDataYamlLoaderService implements YamlLoaderService<Skills> {
        public InvalidSkillsDataYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "invalid-data.yml";
        }

        @Override
        public Class<Skills> getObjClass() {
            return Skills.class;
        }
    }

    @Test
    void test_yamlFile_Without_Valid_Skills_Data_Throws_Exception() {
        var defaultSkillsService = new DefaultSkillsService(new InvalidSkillsDataYamlLoaderService());
        var exception = assertThrows(InvocationTargetException.class, () -> {
            // Use reflection to invoke @PostConstruct annotated method directly rather than via Spring framework
            var postConstructMethod = DefaultSkillsService.class.getDeclaredMethod("postConstruct");
            postConstructMethod.setAccessible(true);
            postConstructMethod.invoke(defaultSkillsService);
        });

        var targetException = exception.getTargetException();
        assertEquals(IllegalStateException.class, targetException.getClass());
        assertEquals("Error loading skills YAML file", targetException.getMessage());
    }
}
