package com.wcg.chargen.backend.model;

import java.util.List;

public record Feature(String description, List<FeatureAttribute> attributes) {
}
