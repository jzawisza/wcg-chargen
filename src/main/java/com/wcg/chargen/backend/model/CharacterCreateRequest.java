package com.wcg.chargen.backend.model;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;

public record CharacterCreateRequest(String characterName,
                                     CharType characterClass,
                                     SpeciesType species) {

}
