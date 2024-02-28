package com.wcg.chargen.backend.service.impl.yaml;

import com.wcg.chargen.backend.model.Professions;
import com.wcg.chargen.backend.service.YamlLoaderService;
import org.springframework.stereotype.Component;

@Component
public class ProfessionsYamlLoaderService implements YamlLoaderService<Professions> {
    @Override
    public String getYamlFile() {
        return "professions.yml";
    }

    @Override
    public Class<Professions> getObjClass() {
        return Professions.class;
    }
}
