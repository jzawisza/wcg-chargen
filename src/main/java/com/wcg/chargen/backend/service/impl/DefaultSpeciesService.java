package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.AttributeType;
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

        // Ensure we have a species object for each species type, and that non-human species have
        // 2 valid strengths and weaknesses specified
        for (var speciesType : SpeciesType.values()) {
            var species = speciesTypeMap.get(speciesType);
            if (species == null) {
                throw new IllegalStateException("No entry for species " + speciesType.toString() + " in species type map");
            }

            if (!speciesType.isHuman()) {
                var strengths = species.strengths();
                var weaknesses = species.weaknesses();
                if (strengths == null || strengths.size() != 2) {
                    throw new IllegalStateException("Expected 2 species strengths for species " + speciesType.toString());
                }
                if (weaknesses == null || weaknesses.size() != 2) {
                    throw new IllegalStateException("Expected 2 species weaknesses for species " + speciesType.toString());
                }

                for (var strength : species.strengths()) {
                    try {
                        AttributeType.valueOf(strength);
                    }
                    catch (IllegalArgumentException e) {
                        throw new IllegalStateException(
                                String.format("Species strength value %s for species %s is not valid",
                                        strength, speciesType.toString()));
                    }
                }

                for (var weakness : species.weaknesses()) {
                    try {
                        AttributeType.valueOf(weakness);
                    }
                    catch (IllegalArgumentException e) {
                        throw new IllegalStateException(
                                String.format("Species weakness value %s for species %s is not valid",
                                        weakness, speciesType.toString()));
                    }
                }
            }
        }
    }

    @Override
    public Species getSpeciesByType(SpeciesType speciesType) {
        return speciesTypeMap.get(speciesType);
    }
}
