package com.wcg.chargen.backend.service.impl.yaml.species;

import com.wcg.chargen.backend.service.impl.yaml.SpeciesYamlLoaderService;
import org.springframework.stereotype.Component;

@Component
public class HalflingYamlLoaderService extends SpeciesYamlLoaderService {
    @Override
    public String getYamlFile() {
        return "halfling.yml";
    }
}