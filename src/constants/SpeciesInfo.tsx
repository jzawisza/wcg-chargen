class SpeciesName
 {
    public readonly internalName: string;
    public readonly speciesName: string;
    public readonly speciesNamePlural: string;

    constructor(internalName: string, speciesName: string, speciesNamePlural: string) {
        this.internalName = internalName;
        this.speciesName = speciesName;
        this.speciesNamePlural = speciesNamePlural;
    }
};

export const DWARF_SPECIES_INFO = new SpeciesName('dwarf', 'Dwarf', 'Dwarves');
export const ELF_SPECIES_INFO = new SpeciesName('elf', 'Elf', 'Elves');
export const HALFLING_SPECIES_INFO = new SpeciesName('halfling', 'Halfling', 'Halflings');
export const HUMAN_SPECIES_INFO = new SpeciesName('human', 'Human', 'Humans');

const SPECIES_INFO_LIST = [ DWARF_SPECIES_INFO, ELF_SPECIES_INFO, HALFLING_SPECIES_INFO, HUMAN_SPECIES_INFO ];

/**
 * 
 * @param speciesInternalName Internal name for the species selected
 * @returns The appropriate plural name for a given species, e.g. Dwarves, Humans, etc.
 */
export function getPluralSpeciesNameFromVariable(speciesInternalName: string) {
    const speciesInfo = SPECIES_INFO_LIST.filter((si) => {
        return si.internalName === speciesInternalName;
    });

    if (speciesInfo.length === 1) {
        return speciesInfo[0].speciesNamePlural;
    }

    // Fall through; should never happen
    return '';
}

/**
 * 
 * @param speciesInternalName Internal name for the species selected
 * @returns True if the species selected is human, false otherwise
 */
export function getIsHuman(speciesInternalName: string) {
    return (speciesInternalName === HUMAN_SPECIES_INFO.internalName);
}