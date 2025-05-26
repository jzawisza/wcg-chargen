package com.wcg.chargen.backend.model;

import java.util.List;

public record Gear(List<Armor> armor, List<Weapon> weapons, Integer maxCopper, Integer maxSilver,
                   List<String> items) {
}
