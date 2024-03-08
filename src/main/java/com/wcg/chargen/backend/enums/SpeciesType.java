package com.wcg.chargen.backend.enums;

public enum SpeciesType {
    DWARF,
    ELF,
    HALFLING,
    HUMAN;

    /**
     *
     * @return Normalized representation of enum in lowercase
     */
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
