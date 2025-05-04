package com.wcg.chargen.backend.enums;

import org.apache.commons.lang3.StringUtils;

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

    public String toCharSheetString() { return StringUtils.capitalize(toString() );
    }

    public boolean isMagicUser() {
        return this == CharType.MAGE ||
                this == CharType.SHAMAN;
    }
}
