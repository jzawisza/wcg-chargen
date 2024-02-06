package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.model.Professions;
import com.wcg.chargen.backend.service.ProfessionService;
import com.wcg.chargen.backend.service.YamlLoaderService;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultProfessionService implements ProfessionService {
    @Autowired
    YamlLoaderService<Professions> yamlLoaderService;

    private static Professions professions;

    @PostConstruct
    private void postConstruct() {
        // Since the YAML loader service is autowired, we need to do this
        // after the bean has been constructed
        professions = yamlLoaderService.loadFromYaml();
        if (professions == null) {
            throw new IllegalStateException("Error loading professions YAML file");
        }
    }

    @Override
    public Professions getAllProfessions() {
        return professions;
    }

    @Override
    public Professions generateRandomProfessions() {
        return null;
    }
}
