package com.wcg.chargen.backend.service;

import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.Species;

public interface SpeciesService {
    Species getSpeciesByType(SpeciesType speciesType);
}
