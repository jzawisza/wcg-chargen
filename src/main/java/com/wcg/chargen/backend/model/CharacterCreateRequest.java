package com.wcg.chargen.backend.model;

import com.wcg.chargen.backend.enums.AttributeType;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CharacterCreateRequest(@NotEmpty String characterName,
                                     CharType characterClass,
                                     @NotNull SpeciesType species,
                                     String profession,
                                     @NotNull Integer level,
                                     @NotNull Map<String, Integer> attributes,
                                     @NotEmpty String speciesStrength,
                                     String speciesWeakness) {
    public Integer getAttributeValue(AttributeType attributeType) {
        // The request validation will guarantee that this value exists
        var attributeValue = attributes().get(attributeType.toString());

        if (attributeType.toString().equals(speciesStrength())) {
            return attributeValue + 1;
        }
        else if (attributeType.toString().equals(speciesWeakness())) {
            return attributeValue - 1;
        }
        else {
            return attributeValue;
        }
    }
}
