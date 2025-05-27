package com.wcg.chargen.backend.model;

import java.util.List;

public record Commoner(Integer attack, Integer evasion, Integer maxCopper, Integer maxSilver,
                       List<String> items) {
}
