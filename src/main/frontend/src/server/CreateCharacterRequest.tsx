import { AttributeScoreObject } from "../constants/AttributeScoreObject";

export type featuresType = {
    tier1: string[],
    tier2: string[]
}

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
    bonusSkills?: string[],
    useQuickGear?: boolean,
    features?: featuresType
}