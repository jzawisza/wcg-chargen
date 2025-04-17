package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;
import com.wcg.chargen.backend.service.CharacterCreateRequestValidatorService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class DefaultCharacterCreateRequestValidatorService implements CharacterCreateRequestValidatorService {
    public CharacterCreateStatus validate(CharacterCreateRequest characterCreateRequest) {
        if (characterCreateRequest == null) {
            return new CharacterCreateStatus(false, "Invalid object");
        }
        if (StringUtils.isEmpty(characterCreateRequest.characterName())) {
            return new CharacterCreateStatus(false, "Missing character name");
        }
        if (characterCreateRequest.characterClass() == null) {
            return new CharacterCreateStatus(false, "Missing character class");
        }
        if (characterCreateRequest.species() == null) {
            return new CharacterCreateStatus(false, "Missing species");
        }

        return CharacterCreateStatus.SUCCESS;
    }
}
