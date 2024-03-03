package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.model.CharClass;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.impl.yaml.CharClassYamlLoaderService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class DefaultCharClassesService implements CharClassesService {
    private final List<CharClassYamlLoaderService> charClassYamlLoaderServiceList;

    private final HashMap<String, CharClass> charClassTypeMap = new HashMap<String, CharClass>();

    @Autowired
    public DefaultCharClassesService(List<CharClassYamlLoaderService> charClassYamlLoaderServiceList) {
        this.charClassYamlLoaderServiceList = charClassYamlLoaderServiceList;
    }

    @PostConstruct
    private void postConstruct() {
        for (var yamlLoaderService : charClassYamlLoaderServiceList) {
            var charClass = yamlLoaderService.loadFromYaml();
            if (charClass == null) {
                throw new IllegalStateException("Error loading character class YAML file " + yamlLoaderService.getYamlFile());
            }

            charClassTypeMap.put(charClass.type(), charClass);
        }

        // Ensure that we have one character class for each character type
        for (var charType : CharType.values()) {
            if (charClassTypeMap.get(charType.toString()) == null) {
                throw new IllegalStateException("No entry for character type " + charType.toString() + " in character class type map");
            }
        }
    }

    @Override
    public CharClass getCharClassByType(String type) {
        return charClassTypeMap.get(type);
    }
}
