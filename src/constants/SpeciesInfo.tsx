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

export const SPECIES_INFO_LIST = [ DWARF_SPECIES_INFO, ELF_SPECIES_INFO, HALFLING_SPECIES_INFO, HUMAN_SPECIES_INFO ];