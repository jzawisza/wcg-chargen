package com.wcg.chargen.backend.model;

import java.util.List;

/**
 * Object to represent the original request received via the client to create a character,
 * plus all additional data needed for character creation.
 *
 * @param characterCreateRequest    Character create request received from the client
 * @param professions               List of all valid professions
 */
public record CharacterCreateInfo(CharacterCreateRequest characterCreateRequest,
                                  List<String> professions) {
}
