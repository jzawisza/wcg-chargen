package com.wcg.chargen.backend.model;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CharacterCreateRequest(@NotEmpty String characterName,
                                     CharType characterClass,
                                     @NotNull SpeciesType species,
                                     String profession,
                                     @NotNull Integer level) {

}
