package com.wcg.chargen.backend.service;

import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.PdfCharacterCreateStatus;

public interface PdfCharacterCreateService {
    PdfCharacterCreateStatus createCharacter(CharacterCreateRequest request);
}
