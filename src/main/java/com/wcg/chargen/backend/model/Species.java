package com.wcg.chargen.backend.model;

import java.util.List;

public record Species(String type, List<String> strengths, List<String> weaknesses, List<String> skills) {
}
