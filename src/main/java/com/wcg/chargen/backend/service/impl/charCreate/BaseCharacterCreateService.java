package com.wcg.chargen.backend.service.impl.charCreate;

import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;
import com.wcg.chargen.backend.service.CharacterCreateService;
import org.apache.commons.lang3.StringUtils;

public abstract class BaseCharacterCreateService implements CharacterCreateService {
    public CharacterCreateStatus createCharacter(CharacterCreateRequest characterCreateRequest, String bearerToken) {
        var status = validateRequest(characterCreateRequest);
        if (!status.isSuccess()) {
            // If the request isn't valid, abort here
            return status;
        }

        return doCreateCharacter(characterCreateRequest, bearerToken);
    }

    public abstract CharacterCreateStatus doCreateCharacter(CharacterCreateRequest characterCreateRequest, String bearerToken);

    private CharacterCreateStatus validateRequest(CharacterCreateRequest characterCreateRequest) {
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
