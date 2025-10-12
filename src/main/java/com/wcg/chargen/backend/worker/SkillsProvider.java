package com.wcg.chargen.backend.worker;

import com.wcg.chargen.backend.model.Skill;

import java.util.Set;

public interface SkillsProvider {
    Skill getByName(String name);
    Set<String> getSkillNameSet();
}
