package com.wcg.chargen.backend.service;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.model.Skills;

public interface SkillsService {
    Skills getAllSkills();

    Skills getSkills(CharType charType);
}
