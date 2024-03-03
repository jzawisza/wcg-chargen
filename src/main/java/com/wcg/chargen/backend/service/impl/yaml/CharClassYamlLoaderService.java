package com.wcg.chargen.backend.service.impl.yaml;

import com.wcg.chargen.backend.model.CharClass;
import com.wcg.chargen.backend.service.YamlLoaderService;

public abstract class CharClassYamlLoaderService implements YamlLoaderService<CharClass> {
    @Override
    public String getYamlPath() {
        return "/yaml/charClasses/";
    }

    @Override
    public Class<CharClass> getObjClass() {
        return CharClass.class;
    }
}
