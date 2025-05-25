package com.wcg.chargen.backend.testUtil;

import com.wcg.chargen.backend.enums.AttributeType;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterCreateRequestBuilder {
    private String characterName = null;
    private CharType characterType = null;
    private SpeciesType speciesType = null;
    private String profession = null;
    private Integer level = null;
    private Map<String, Integer> attributes = null;
    private String speciesStrength = null;
    private String speciesWeakness = null;
    private String speciesSkill = null;
    private List<String> bonusSkills = new ArrayList<>();
    private boolean useNullAttributes = false;


    // Private constructor to disallow direct instantiation
    private CharacterCreateRequestBuilder() {}

    public static CharacterCreateRequestBuilder getBuilder() {
        return new CharacterCreateRequestBuilder();
    }

    public static final Map<String, Integer> VALID_ATTRIBUTES_MAP =
            getAttributesMap(2, 1, 1, 0, 0, -1, -2);

    public CharacterCreateRequestBuilder withCharacterName(String characterName) {
        this.characterName = characterName;

        return this;
    }

    public CharacterCreateRequestBuilder withCharacterType(CharType characterType) {
        this.characterType = characterType;

        return this;
    }

    public CharacterCreateRequestBuilder withSpeciesType(SpeciesType speciesType) {
        this.speciesType = speciesType;

        return this;
    }

    public CharacterCreateRequestBuilder withProfession(String profession) {
        this.profession = profession;

        return this;
    }

    public CharacterCreateRequestBuilder withLevel(Integer level) {
        this.level = level;

        return this;
    }

    public CharacterCreateRequestBuilder withAttributes(Map<String, Integer> attributes) {
        this.attributes = attributes;

        return this;
    }

    public CharacterCreateRequestBuilder withNullAttributes() {
        this.useNullAttributes = true;

        return this;
    }

    public CharacterCreateRequestBuilder withSpeciesStrength(String speciesStrength) {
        this.speciesStrength = speciesStrength;

        return this;
    }

    public CharacterCreateRequestBuilder withSpeciesWeakness(String speciesWeakness) {
        this.speciesWeakness = speciesWeakness;

        return this;
    }

    public CharacterCreateRequestBuilder withSpeciesSkill(String speciesSkill) {
        this.speciesSkill = speciesSkill;

        return this;
    }

    public CharacterCreateRequestBuilder withBonusSkills(List<String> bonusSkills) {
        this.bonusSkills = bonusSkills;

        return this;
    }

    public CharacterCreateRequest build() {
        // If the attributes map is null, provide something valid by default so the unit tests pass,
        // but allow for the attributes to be deliberately null as well
        if (attributes == null && !useNullAttributes) {
            attributes = CharacterCreateRequestBuilder.getAttributesMap(0, 0, 0, 0, 0,0, 0);
        }
        return new CharacterCreateRequest(characterName, characterType, speciesType, profession, level,
                attributes, speciesStrength, speciesWeakness, speciesSkill, bonusSkills);
    }

    public static Map<String, Integer> getAttributesMap(int strVal, int corVal, int staVal, int perVal,
                                                        int intVal, int prsVal, int lucVal) {
        var attributesMap = new HashMap<String, Integer>();

        attributesMap.put(AttributeType.STR.name(), strVal);
        attributesMap.put(AttributeType.COR.name(), corVal);
        attributesMap.put(AttributeType.STA.name(), staVal);
        attributesMap.put(AttributeType.PER.name(), perVal);
        attributesMap.put(AttributeType.INT.name(), intVal);
        attributesMap.put(AttributeType.PRS.name(), prsVal);
        attributesMap.put(AttributeType.LUC.name(), lucVal);

        return attributesMap;
    }
}
