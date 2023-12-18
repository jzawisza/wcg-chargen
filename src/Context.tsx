import { createContext } from "react";
import { AttributeScoreObject, emptyAtributeScoreObj } from "./constants/AttributeScoreObject";

// Whether the Next button in ChargenStepper is enabled
export const NextButtonEnabledContext = createContext({
    nextEnabled: false,
    setNextEnabled: (nextEnabled: boolean) => {}
});


// Information about the character being created
let emptyStringArray: string[] = [];
let emptyNumberOrNullArray: (number | null)[] = [];

export const CharacterContext = createContext({
    level: 0,
    species: '',
    setSpecies: (newSpecies: string) => {},
    profession: '',
    setProfession: (newProfession: string) => {},
    charClass: '',
    setCharClass: (newCharClass: string) => {},
    speciesSkill: '',
    setSpeciesSkill: (newSpeciesSkill: string) => {},
    bonusSkills: emptyStringArray,
    setBonusSkills: (newBonusSkills: string[]) => {},
    tier1Features: emptyStringArray,
    setTier1Features: (newTier1Features: string[]) => {},
    tier2Features: emptyStringArray,
    setTier2Features: (newTier2Features: string[]) => {},
    charName: '',
    setCharName: (newCharName: string) => {},
    attributeArrayType: '',
    setAttributeArrayType: (newAttributeArrayType: string) => {},
    attributeScoreObj: emptyAtributeScoreObj,
    setAttributeScoreObj: (newAttributeScoreObj: AttributeScoreObject) => {},
    attributeValues: emptyNumberOrNullArray,
    setAttributeValues: (newAttributeValues: (number | null)[]) => {}
});