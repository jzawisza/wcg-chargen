package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.model.Skill;
import com.wcg.chargen.backend.model.Skills;
import com.wcg.chargen.backend.service.SkillsProvider;
import com.wcg.chargen.backend.service.YamlLoaderService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

@Component
public class DefaultSkillsProvider implements SkillsProvider {
    private final YamlLoaderService<Skills> yamlLoaderService;

    private final HashMap<String, Skill> skillsMap = new HashMap<>();

    @Autowired
    public DefaultSkillsProvider(YamlLoaderService<Skills> yamlLoaderService) {
        this.yamlLoaderService = yamlLoaderService;
    }

    @PostConstruct
    private void postConstruct() {
        var skills = yamlLoaderService.loadFromYaml();
        if (skills == null) {
            throw new IllegalStateException("Error loading skills YAML file");
        }

        // Initialize map from skill name to skill object
        for (var skill : skills.skills()) {
            skillsMap.put(skill.name(), skill);
        }
    }

    @Override
    public Skill getByName(String name) {
        return skillsMap.get(name);
    }

    @Override
    public Set<String> getSkillNameSet() {
        return new TreeSet<>(skillsMap.keySet());
    }
}
