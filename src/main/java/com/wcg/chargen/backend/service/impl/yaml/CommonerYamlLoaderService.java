package com.wcg.chargen.backend.service.impl.yaml;

import com.wcg.chargen.backend.model.Commoner;
import com.wcg.chargen.backend.service.YamlLoaderService;
import org.springframework.stereotype.Component;

@Component
public class CommonerYamlLoaderService implements YamlLoaderService<Commoner> {
    @Override
    public String getYamlFile() {
        return "commoner.yml";
    }

    @Override
    public Class<Commoner> getObjClass() {
        return Commoner.class;
    }
}
