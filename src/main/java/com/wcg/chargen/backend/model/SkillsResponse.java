package com.wcg.chargen.backend.model;

import java.util.ArrayList;
import java.util.List;

public class SkillsResponse {
    private final List<Skill> classSkills;

    private final List<Skill> bonusSkills;

    public SkillsResponse() {
        classSkills = new ArrayList<>();
        bonusSkills = new ArrayList<>();
    }

    public void addClassSkill(Skill classSkill) {
        classSkills.add(classSkill);
    }

    public void addBonusSkill(Skill bonusSkill) {
        bonusSkills.add(bonusSkill);
    }

    // Getter methods are required for object serialization
    public List<Skill> getClassSkills() {
        return classSkills;
    }

    public List<Skill> getBonusSkills() {
        return bonusSkills;
    }
}
