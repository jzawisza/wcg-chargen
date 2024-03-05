package com.wcg.chargen.backend.service;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.model.CharClass;

public interface CharClassesService {
    public CharClass getCharClassByType(CharType charType);
}
