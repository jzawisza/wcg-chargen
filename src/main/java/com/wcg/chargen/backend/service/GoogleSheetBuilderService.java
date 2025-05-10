package com.wcg.chargen.backend.service;

import com.google.api.services.sheets.v4.model.Sheet;
import com.wcg.chargen.backend.model.CharacterCreateRequest;

public interface GoogleSheetBuilderService {
    Sheet buildStatsSheet(CharacterCreateRequest characterCreateRequest);

    Sheet buildSpellsSheet();

    Sheet buildFeaturesSheet(CharacterCreateRequest characterCreateRequest);

    Sheet buildGearSheet(CharacterCreateRequest characterCreateRequest);
}
