package com.wcg.chargen.backend.service;

import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;

public interface CharacterCreateService {
    CharacterCreateStatus createCharacter(CharacterCreateRequest characterCreateRequest, String bearerToken);
}
