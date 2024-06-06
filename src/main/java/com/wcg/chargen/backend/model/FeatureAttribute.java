package com.wcg.chargen.backend.model;

import com.wcg.chargen.backend.enums.FeatureAttributeType;

public record FeatureAttribute(FeatureAttributeType type, String modifier) {
}
