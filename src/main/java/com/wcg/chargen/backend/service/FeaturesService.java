package com.wcg.chargen.backend.service;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.model.FeatureResponse;

public interface FeaturesService {
    FeatureResponse getFeatures(CharType charType, int level);
}
