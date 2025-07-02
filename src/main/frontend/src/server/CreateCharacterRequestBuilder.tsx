import { AttributeScoreObject, EMPTY_ATTRIBUTE_SCORE_OBJ } from "../constants/AttributeScoreObject";
import { CreateCharacterRequest } from "./CreateCharacterRequest";

let emptyStringArray: string[] = [];

let emptyFeaturesType = {
    tier1: emptyStringArray,
    tier2: emptyStringArray}

export class CreateCharacterRequestBuilder {
    _charName = "";
    _charClass = "";
    _species = "";
    _profession = "";
    _level = 1;
    _attributes = EMPTY_ATTRIBUTE_SCORE_OBJ;
    _speciesStrength = "";
    _speciesWeakness = "";
    _speciesSkill = "";
    _bonusSkills = emptyStringArray;
    _useQuickGear = false;
    _features = emptyFeaturesType;

    withCharacterName(charName: string) {
        this._charName = charName;

        return this;
    }

    withCharacterClass(charClass: string) {
        this._charClass = charClass.toUpperCase();

        return this;
    }

    withSpecies(species: string) {
        this._species = species.toUpperCase();

        return this;
    }

    withProfession(profession: string) {
        this._profession = profession;
    }

    withLevel(level: number) {
        this._level = level;

        return this;
    }

    withAttributes(attributes: AttributeScoreObject) {
        this._attributes = attributes;

        return this;
    }

    withSpeciesStrength(speciesStrength: string) {
        this._speciesStrength = speciesStrength;

        return this;
    }

    withSpeciesWeakness(speciesWeakness: string) {
        this._speciesWeakness = speciesWeakness;

        return this;
    }

    withSpeciesSkill(speciesSkill: string) {
        this._speciesSkill = speciesSkill;

        return this;
    }

    withBonusSkills(bonusSkills: string[]) {
        this._bonusSkills = bonusSkills;

        return this;
    }

    withUseQuickGear(useQuickGear: boolean) {
        this._useQuickGear = useQuickGear;

        return this;
    }

    withFeatures(features: { tier1: string[], tier2: string[] }) {
        this._features = features;

        return this;
    }

    build() {
        const createCharacterRequest: CreateCharacterRequest = {
            "characterName": this._charName,
            "species": this._species,
            "level": this._level,
            "attributes": this._attributes,
            "speciesStrength": this._speciesStrength,
            "speciesWeakness": this._speciesWeakness
        };

        if (this._level > 0) {
            createCharacterRequest.characterClass = this._charClass;
            createCharacterRequest.bonusSkills = Array.isArray(this._bonusSkills)
            ? this._bonusSkills
            : [this._bonusSkills];
            createCharacterRequest.useQuickGear = this._useQuickGear;
        }
        else {
            createCharacterRequest.profession = this._profession;
        }

        if (this._speciesSkill !== "") {
            createCharacterRequest.speciesSkill = this._speciesSkill;
        }

        if (this._level > 1) {
            createCharacterRequest.features = this._features;
        }

        return createCharacterRequest;
    }
}