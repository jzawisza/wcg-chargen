package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.model.Skills;
import com.wcg.chargen.backend.service.SkillsService;
import com.wcg.chargen.backend.service.YamlLoaderService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class DefaultSkillsService implements SkillsService {
    private final YamlLoaderService<Skills> yamlLoaderService;

    private Skills skills;

    public DefaultSkillsService(YamlLoaderService<Skills> yamlLoaderService) {
        this.yamlLoaderService = yamlLoaderService;
    }

    @PostConstruct
    private void postConstruct() {
        skills = yamlLoaderService.loadFromYaml();
        if (skills == null) {
            throw new IllegalStateException("Error loading skills YAML file");
        }
    }

    @Override
    public Skills getAllSkills() {
        return skills;
    }
}
