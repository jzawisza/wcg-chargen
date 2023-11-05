import { createContext } from "react";

// Whether the Next button in ChargenStepper is enabled
export const NextButtonEnabledContext = createContext({
    nextEnabled: false,
    setNextEnabled: (nextEnabled: boolean) => {}
});


// Information about the character being created
let defaultBonusSkills: string[] = [];

export const CharacterContext = createContext({
    species: '',
    setSpecies: (newSpecies: string) => {},
    profession: '',
    setProfession: (newProfession: string) => {},
    charClass: '',
    setCharClass: (newCharClass: string) => {},
    speciesSkill: '',
    setSpeciesSkill: (newSpeciesSkill: string) => {},
    bonusSkills: defaultBonusSkills,
    setBonusSkills: (newBonusSkills: string[]) => {}
});