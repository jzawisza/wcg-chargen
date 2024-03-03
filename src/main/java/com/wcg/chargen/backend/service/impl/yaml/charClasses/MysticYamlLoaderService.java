package com.wcg.chargen.backend.service.impl.yaml.charClasses;

import com.wcg.chargen.backend.service.impl.yaml.CharClassYamlLoaderService;
import org.springframework.stereotype.Component;

@Component
public class MysticYamlLoaderService extends CharClassYamlLoaderService {
    @Override
    public String getYamlFile() {
        return "mystic.yml";
    }
}
