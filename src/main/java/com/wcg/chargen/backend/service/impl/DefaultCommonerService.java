package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.model.Commoner;
import com.wcg.chargen.backend.service.CommonerService;
import com.wcg.chargen.backend.service.YamlLoaderService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultCommonerService implements CommonerService  {
    private final YamlLoaderService<Commoner> yamlLoaderService;
    private Commoner commonerInfo;

    @Autowired
    public DefaultCommonerService(YamlLoaderService<Commoner> yamlLoaderService) {
        this.yamlLoaderService = yamlLoaderService;
    }

    @PostConstruct
    private void postConstruct() {
        // Since the YAML loader service is autowired, we need to do this
        // after the bean has been constructed
        commonerInfo = yamlLoaderService.loadFromYaml();
        if (commonerInfo == null) {
            throw new IllegalStateException("Error loading commoner YAML file");
        }

        if (commonerInfo.attack() == null) {
            throw new IllegalStateException("Missing commoner attack info");
        }

        if (commonerInfo.evasion() == null) {
            throw new IllegalStateException("Missing commoner evasion info");
        }

        if (commonerInfo.maxCopper() == null) {
            throw new IllegalStateException("Missing commoner max copper info");
        }

        if (commonerInfo.maxSilver() == null) {
            throw new IllegalStateException("Missing commoner max silver info");
        }

        if (commonerInfo.items() == null || commonerInfo.items().isEmpty()) {
            throw new IllegalStateException("Missing commoner item info");
        }
    }

    @Override
    public Commoner getInfo() {
        return commonerInfo;
    }
}
