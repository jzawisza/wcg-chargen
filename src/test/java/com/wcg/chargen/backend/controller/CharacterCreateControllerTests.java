package com.wcg.chargen.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;
import com.wcg.chargen.backend.service.impl.charCreate.GoogleSheetsCharacterCreateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CharacterCreateController.class)
public class CharacterCreateControllerTests {
    @MockBean
    private GoogleSheetsCharacterCreateService googleSheetsCharacterCreateService;
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final CharacterCreateRequest VALID_CHARACTER_CREATE_REQUEST =
            new CharacterCreateRequest("Test", CharType.MAGE, SpeciesType.DWARF);
    private static final String DUMMY_BEARER_TOKEN = "some token";
    private static final String GOOGLE_SHEETS_URL = "/api/v1/createcharacter/googlesheets";

    @Test
    public void createCharacterGoogle_Returns400IfJsonIsInvalid() {
        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .content("not valid JSON")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void createCharacterGoogle_Returns400IfAuthorizationHeaderIsMissing() {
        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .content(objectMapper.writeValueAsString(VALID_CHARACTER_CREATE_REQUEST))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"rogue", "Rogue", "roGUE"})
    public void createCharacterGoogle_Returns400IfRequestCharacterClassIsNullEmptyOrInvalid(String characterClass) {
        var requestJson = "{\"characterName\": \"Name\", \"species\": \"ELF\", \"characterClass\": \""
                + characterClass
                + "\"}";

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .content(requestJson)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"elf", "Elf", "eLF"})
    public void createCharacterGoogle_Returns400IfSpeciesIsNullEmptyOrInvalid(String species) {
        var requestJson = "{\"characterName\": \"Name\", \"characterClass\": \"SKALD\", \"species\": \""
                + species
                + "\"}";

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .content(requestJson)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    @Test
    public void createCharacterGoogle_Returns500WithErrorMessageIfGoogleServiceReturnsFailureStatus() {
        var expectedErrMsg = "Some error message";
        var status = new CharacterCreateStatus(false, expectedErrMsg);

        Mockito.when(
            googleSheetsCharacterCreateService.createCharacter(VALID_CHARACTER_CREATE_REQUEST,
                    DUMMY_BEARER_TOKEN))
            .thenReturn(status);

        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .post(GOOGLE_SHEETS_URL)
                    .header(HttpHeaders.AUTHORIZATION, "some token")
                    .content(objectMapper.writeValueAsString(VALID_CHARACTER_CREATE_REQUEST))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(expectedErrMsg));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void createCharacterGoogle_Returns500IfGoogleServiceThrowsException() {
        Mockito.when(
            googleSheetsCharacterCreateService.createCharacter(VALID_CHARACTER_CREATE_REQUEST,
                    DUMMY_BEARER_TOKEN))
            .thenThrow(new RuntimeException());

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                            .content(objectMapper.writeValueAsString(VALID_CHARACTER_CREATE_REQUEST))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void createCharacterGoogle_Returns200OnSuccess() {
        Mockito.when(
            googleSheetsCharacterCreateService.createCharacter(VALID_CHARACTER_CREATE_REQUEST,
                    DUMMY_BEARER_TOKEN))
            .thenReturn(CharacterCreateStatus.SUCCESS);

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                            .content(objectMapper.writeValueAsString(VALID_CHARACTER_CREATE_REQUEST))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
