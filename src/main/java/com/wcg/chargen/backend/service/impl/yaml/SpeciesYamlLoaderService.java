package com.wcg.chargen.backend.service.impl.yaml;

import com.wcg.chargen.backend.model.Species;
import com.wcg.chargen.backend.service.YamlLoaderService;

public abstract class SpeciesYamlLoaderService implements YamlLoaderService<Species> {
    @Override
    public String getYamlPath() {
        return "/yaml/species/";
    }

    @Override
    public Class<Species> getObjClass() {
        return Species.class;
    }
}
