package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.Species;
import com.wcg.chargen.backend.service.SpeciesService;
import com.wcg.chargen.backend.service.impl.yaml.SpeciesYamlLoaderService;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class DefaultSpeciesService implements SpeciesService {
    private final List<SpeciesYamlLoaderService> speciesYamlLoaderServiceList;

    private final HashMap<SpeciesType, Species> speciesTypeMap = new HashMap<>();

    @Autowired
    public DefaultSpeciesService(List<SpeciesYamlLoaderService> speciesYamlLoaderServiceList) {
        this.speciesYamlLoaderServiceList = speciesYamlLoaderServiceList;
    }

    @PostConstruct
    private void postConstruct() {
        for (var yamlLoaderService : speciesYamlLoaderServiceList) {
            var species = yamlLoaderService.loadFromYaml();
            var yamlFile = yamlLoaderService.getYamlFile();

            if (species == null) {
                throw new IllegalStateException("Error loading species YAML file " + yamlFile);
            }

            try {
                var speciesType = SpeciesType.valueOf(species.type().toUpperCase());
                speciesTypeMap.put(speciesType, species);
            }
            catch (IllegalArgumentException e) {
                throw new IllegalStateException("Species type " + species.type() + " found in YAML file " + yamlFile + " is not valid");
            }
        }

        // Ensure we have a species object for each species type
        for (var speciesType : SpeciesType.values()) {
            if (speciesTypeMap.get(speciesType) == null) {
                throw new IllegalStateException("No entry for species " + speciesType.toString() + " in species type map");
            }
        }
    }

    @Override
    public Species getSpeciesByType(SpeciesType speciesType) {
        return speciesTypeMap.get(speciesType);
    }
}
