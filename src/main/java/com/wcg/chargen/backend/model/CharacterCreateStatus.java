package com.wcg.chargen.backend.model;

public record CharacterCreateStatus(boolean isSuccess, String message) {
    public static CharacterCreateStatus SUCCESS = new CharacterCreateStatus(true, "");
}
