import { CreateCharacterRequest } from "./CreateCharacterRequest";

export class CreateCharacterRequestBuilder {
    _charName = "";
    _charClass = "";
    _species = "";
    _profession = "";
    _level = 1;

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

    build() {
        const createCharacterRequest: CreateCharacterRequest = {
            "characterName": this._charName,
            "species": this._species,
            "level": this._level
        };

        if (this._level > 0) {
            createCharacterRequest.characterClass = this._charClass;
        }
        else {
            createCharacterRequest.profession = this._profession;
        }

        return createCharacterRequest;
    }
}