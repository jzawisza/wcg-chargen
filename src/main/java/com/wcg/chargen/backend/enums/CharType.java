package com.wcg.chargen.backend.enums;

public enum CharType {
    BERZERKER,
    MAGE,
    MYSTIC,
    RANGER,
    ROGUE,
    SHAMAN,
    SKALD,
    WARRIOR;

    /**
     *
     * @return Normalized representation of enum in lowercase
     */
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
