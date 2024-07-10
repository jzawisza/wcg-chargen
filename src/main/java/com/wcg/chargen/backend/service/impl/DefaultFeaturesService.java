package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.constants.LevelConstants;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.model.FeatureResponse;
import com.wcg.chargen.backend.service.CharClassesService;
import com.wcg.chargen.backend.service.FeaturesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultFeaturesService implements FeaturesService {
    private static final int[] CUMULATIVE_TIER_1_FEATURES_PER_LEVEL = {0, 1, 3, 3, 3, 4, 5};

    private static final int[] CUMULATIVE_TIER_2_FEATURES_PER_LEVEL = {0, 0, 0, 1, 2, 3, 4};

    Logger logger = LoggerFactory.getLogger(DefaultFeaturesService.class);

    private final CharClassesService charClassesService;

    @Autowired
    public DefaultFeaturesService(CharClassesService charClassesService) {
        this.charClassesService = charClassesService;
    }

    @Override
    public FeatureResponse getFeatures(CharType charType, int level) {
        try {
            if (level < LevelConstants.MIN_LEVEL || level > LevelConstants.MAX_LEVEL) {
                logger.error("Level must be between {} and {}, but was {}",
                        LevelConstants.MIN_LEVEL,
                        LevelConstants.MAX_LEVEL,
                        level);
                return null;
            }

            // Levels are 1-based and arrays are 0-based, so correct for that here
            var tier1FeaturesAllowed = CUMULATIVE_TIER_1_FEATURES_PER_LEVEL[level - 1];
            var tier2FeaturesAllowed = CUMULATIVE_TIER_2_FEATURES_PER_LEVEL[level - 1];

            var charClass = charClassesService.getCharClassByType(charType);

            return new FeatureResponse(tier1FeaturesAllowed, tier2FeaturesAllowed, charClass.features());
        }
        catch (Exception e) {
            logger.error("Error retrieving features for character class {} and level {}",
                    charType,
                    level,
                    e);
        }

        return null;
    }
}
