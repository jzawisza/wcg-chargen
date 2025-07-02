package com.wcg.chargen.backend.model;

import java.util.List;

public record FeaturesRequest(List<String> tier1, List<String> tier2) {
}
