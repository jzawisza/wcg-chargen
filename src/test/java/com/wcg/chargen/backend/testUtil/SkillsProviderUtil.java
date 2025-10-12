package com.wcg.chargen.backend.testUtil;

import com.wcg.chargen.backend.model.Skills;
import com.wcg.chargen.backend.worker.SkillsProvider;
import com.wcg.chargen.backend.service.YamlLoaderService;
import com.wcg.chargen.backend.worker.impl.DefaultSkillsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkillsProviderUtil {
    private static SkillsProvider skillsProvider = null;

    private static final Logger logger = LoggerFactory.getLogger(SkillsProviderUtil.class);

    static class ValidSkillsDataYamlLoaderService implements YamlLoaderService<Skills> {
        public ValidSkillsDataYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "skills.yml";
        }

        @Override
        public Class<Skills> getObjClass() {
            return Skills.class;
        }
    }

    // Private constructor for singleton pattern
    private SkillsProviderUtil() {
    }

    public static SkillsProvider getObject() {
        if (skillsProvider == null) {
            try {
                skillsProvider = new DefaultSkillsProvider(new ValidSkillsDataYamlLoaderService());
                PostConstructUtil.invokeMethod(DefaultSkillsProvider.class, skillsProvider);
            }
            catch (Exception e) {
                logger.error("Error initializing skills provider", e);
                return null;
            }
        }

        return skillsProvider;
    }
}
