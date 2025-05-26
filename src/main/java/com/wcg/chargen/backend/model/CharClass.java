package com.wcg.chargen.backend.model;

import java.util.List;

public record CharClass(String type, List<Integer> attackModifiers, List<Integer> evasionModifiers,
                        Integer level1Hp, Integer maxHpAtLevelUp, List<String> skills, Gear gear,
                        Features features) {
}
