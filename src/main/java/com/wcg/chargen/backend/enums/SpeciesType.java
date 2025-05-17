package com.wcg.chargen.backend.enums;

import org.apache.commons.lang3.StringUtils;

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

    public String toCharSheetString() { return StringUtils.capitalize(toString() );
    }

    public boolean isHuman() { return this == SpeciesType.HUMAN; }

}
