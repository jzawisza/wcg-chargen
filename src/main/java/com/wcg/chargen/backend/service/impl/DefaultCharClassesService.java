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

    private final HashMap<CharType, CharClass> charClassTypeMap = new HashMap<CharType, CharClass>();

    @Autowired
    public DefaultCharClassesService(List<CharClassYamlLoaderService> charClassYamlLoaderServiceList) {
        this.charClassYamlLoaderServiceList = charClassYamlLoaderServiceList;
    }

    @PostConstruct
    private void postConstruct() {
        for (var yamlLoaderService : charClassYamlLoaderServiceList) {
            var charClass = yamlLoaderService.loadFromYaml();
            var yamlFile = yamlLoaderService.getYamlFile();

            if (charClass == null) {
                throw new IllegalStateException("Error loading character class YAML file " + yamlFile);
            }

            try {
                var charType = CharType.valueOf(charClass.type().toUpperCase());
                charClassTypeMap.put(charType, charClass);
            }
            catch (IllegalArgumentException e) {
                throw new IllegalStateException("Character class type " + charClass.type() + " found in YAML file " + yamlFile + " is not valid");
            }
        }

        // Ensure that we have one character class for each character type
        for (var charType : CharType.values()) {
            if (charClassTypeMap.get(charType) == null) {
                throw new IllegalStateException("No entry for character type " + charType.toString() + " in character class type map");
            }
        }
    }

    @Override
    public CharClass getCharClassByType(CharType charType) {
        return charClassTypeMap.get(charType);
    }
}
