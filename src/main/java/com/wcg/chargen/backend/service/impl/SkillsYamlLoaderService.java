package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.model.Skills;
import com.wcg.chargen.backend.service.YamlLoaderService;
import org.springframework.stereotype.Component;

@Component
public class SkillsYamlLoaderService implements YamlLoaderService<Skills> {
    @Override
    public String getYamlFile() {
        return "skills.yml";
    }

    @Override
    public Class<Skills> getObjClass() {
        return Skills.class;
    }
}
