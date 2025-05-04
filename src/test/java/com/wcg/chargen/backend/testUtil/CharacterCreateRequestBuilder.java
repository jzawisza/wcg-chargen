package com.wcg.chargen.backend.testUtil;

import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;

public class CharacterCreateRequestBuilder {
    private String characterName = null;
    private CharType characterType = null;
    private SpeciesType speciesType = null;
    private String profession = null;
    private Integer level = null;

    // Private constructor to disallow direct instantiation
    private CharacterCreateRequestBuilder() {}

    public static CharacterCreateRequestBuilder getBuilder() {
        return new CharacterCreateRequestBuilder();
    }

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

    public CharacterCreateRequest build() {
        return new CharacterCreateRequest(characterName, characterType, speciesType, profession, level);
    }
}
