package com.wcg.chargen.backend.worker;

import com.wcg.chargen.backend.enums.FeatureAttributeType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;

public interface CharacterSheetWorker {
    String generateName(CharacterCreateRequest request);

    int getFortunePoints(CharacterCreateRequest request);

    int getBaseEvasion(CharacterCreateRequest request);

    int getEvasionBonus(CharacterCreateRequest request);

    FeatureAttributeType getAdvOrDadvByModifier(CharacterCreateRequest request, String modifierStr);

    int getHitPoints(CharacterCreateRequest characterCreateRequest);
}
