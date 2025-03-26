import { CreateCharacterRequest } from "./CreateCharacterRequest";

export class CreateCharacterRequestBuilder {
    _charName = "";
    _charClass = "";
    _species = "";

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

    build() {
        const createCharacterRequest: CreateCharacterRequest = {
            "characterName": this._charName,
            "characterClass": this._charClass,
            "species": this._species
        };

        return createCharacterRequest;
    }
}