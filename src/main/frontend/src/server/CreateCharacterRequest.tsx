import { AttributeScoreObject } from "../constants/AttributeScoreObject";

export interface CreateCharacterRequest {
    characterName: string,
    characterClass?: string,
    species: string,
    profession?: string,
    level: number,
    attributes: AttributeScoreObject,
    speciesStrength: string,
    speciesWeakness?: string,
    speciesSkill?: string,
    bonusSkills?: string[]
}