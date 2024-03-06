package com.wcg.chargen.backend.service;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.model.Skills;
import com.wcg.chargen.backend.model.SkillsResponse;

public interface SkillsService {
    SkillsResponse getSkills(CharType charType);
}
