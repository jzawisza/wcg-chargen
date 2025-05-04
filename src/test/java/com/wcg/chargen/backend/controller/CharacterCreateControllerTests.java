package com.wcg.chargen.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;
import com.wcg.chargen.backend.service.impl.charCreate.GoogleSheetsCharacterCreateService;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

import java.util.stream.Stream;

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

    private static final CharacterCreateRequest VALID_CHARACTER_CREATE_REQUEST_WITH_CLASS =
            CharacterCreateRequestBuilder.getBuilder()
                    .withCharacterName("Test")
                    .withCharacterType(CharType.MAGE)
                    .withSpeciesType(SpeciesType.DWARF)
                    .withProfession(null)
                    .withLevel(1)
                    .build();
    private static final CharacterCreateRequest VALID_CHARACTER_CREATE_REQUEST_WITH_PROFESSION =
            CharacterCreateRequestBuilder.getBuilder()
                    .withCharacterName("Test")
                    .withCharacterType(null)
                    .withSpeciesType(SpeciesType.DWARF)
                    .withProfession("TestProfession")
                    .withLevel(0)
                    .build();
    private static final String DUMMY_BEARER_TOKEN = "some token";
    private static final String GOOGLE_SHEETS_URL = "/api/v1/createcharacter/googlesheets";

    @Test
    public void createCharacterGoogle_Returns400IfJsonIsInvalid() {
        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
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
                            .content(objectMapper.writeValueAsString(VALID_CHARACTER_CREATE_REQUEST_WITH_CLASS))
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
    public void createCharacterGoogle_Returns400IfCharacterNameIsNullOrEmpty(String characterName) {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(characterName)
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.HUMAN)
                .withProfession("Test")
                .withLevel(1)
                .build();

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                            .content(objectMapper.writeValueAsString(request))
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
    public void createCharacterGoogle_Returns400IfLevelIsNull() {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName("Test")
                .withCharacterType(CharType.MAGE)
                .withSpeciesType(SpeciesType.HUMAN)
                .withProfession("Test")
                .withLevel(null)
                .build();

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                            .content(objectMapper.writeValueAsString(request))
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
    public void createCharacterGoogle_Returns400IfSpeciesIsNull() {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName("Test")
                .withCharacterType(CharType.MAGE)
                .withSpeciesType(null)
                .withProfession("Test")
                .withLevel(1)
                .build();

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                            .content(objectMapper.writeValueAsString(request))
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
    @ValueSource(strings = {"", "rogue", "Rogue", "roGUE"})
    public void createCharacterGoogle_Returns400IfRequestCharacterClassStringCannotBeConvertedToEnumValue(String characterClass) {
        var requestJson = "{\"characterName\": \"Name\", \"species\": \"ELF\", \"characterClass\": \""
                + characterClass
                + "\", \"level\": 1}";

        try {
            System.out.println(requestJson);

            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
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
    @ValueSource(strings = {"", "elf", "Elf", "eLF"})
    public void createCharacterGoogle_Returns400IfRequestSpeciesStringCannotBeConvertedToEnumValue(String species) {
        var requestJson = "{\"characterName\": \"Name\", \"characterClass\": \"SKALD\", \"species\": \""
                + species
                + "\", \"level\": 1}";

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
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
            googleSheetsCharacterCreateService.createCharacter(VALID_CHARACTER_CREATE_REQUEST_WITH_CLASS,
                    DUMMY_BEARER_TOKEN))
            .thenReturn(status);

        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .post(GOOGLE_SHEETS_URL)
                    .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                    .content(objectMapper.writeValueAsString(VALID_CHARACTER_CREATE_REQUEST_WITH_CLASS))
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
            googleSheetsCharacterCreateService.createCharacter(VALID_CHARACTER_CREATE_REQUEST_WITH_CLASS,
                    DUMMY_BEARER_TOKEN))
            .thenThrow(new RuntimeException());

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                            .content(objectMapper.writeValueAsString(VALID_CHARACTER_CREATE_REQUEST_WITH_CLASS))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @ParameterizedTest
    @MethodSource("validCharacterCreateRequests")
    public void createCharacterGoogle_Returns200OnSuccessIfRequestIsValid(CharacterCreateRequest validRequest) {
        Mockito.when(
            googleSheetsCharacterCreateService.createCharacter(validRequest,
                    DUMMY_BEARER_TOKEN))
            .thenReturn(CharacterCreateStatus.SUCCESS);

        try {
            System.out.println(objectMapper.writeValueAsString(validRequest));
            mockMvc.perform(MockMvcRequestBuilders
                            .post(GOOGLE_SHEETS_URL)
                            .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                            .content(objectMapper.writeValueAsString(validRequest))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    static Stream<Arguments> validCharacterCreateRequests() {
        return Stream.of(
                Arguments.arguments(VALID_CHARACTER_CREATE_REQUEST_WITH_CLASS),
                Arguments.arguments(VALID_CHARACTER_CREATE_REQUEST_WITH_PROFESSION)
        );
    }
}
