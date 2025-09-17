package com.wcg.chargen.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcg.chargen.backend.enums.CharType;
import com.wcg.chargen.backend.enums.SpeciesType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.model.CharacterCreateStatus;
import com.wcg.chargen.backend.model.PdfCharacterCreateStatus;
import com.wcg.chargen.backend.service.PdfCharacterCreateService;
import com.wcg.chargen.backend.service.impl.charCreate.GoogleSheetsCharacterCreateService;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CharacterCreateController.class)
public class CharacterCreateControllerTests {
    @MockBean
    private GoogleSheetsCharacterCreateService googleSheetsCharacterCreateService;
    @MockBean
    private PdfCharacterCreateService pdfCharacterCreateService;
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, Integer> ATTRIBUTES = Map.of(
        "STR", 1,
            "COR", 1,
            "STA", 2,
            "PER", 2,
            "INT", -1,
            "PRS", 0,
            "LUC", 0
    );

    private static final String SPECIES_STRENGTH_ATTRIBUTE = "STR";

    private static final CharacterCreateRequest VALID_CHARACTER_CREATE_REQUEST_WITH_CLASS =
            CharacterCreateRequestBuilder.getBuilder()
                    .withCharacterName("Test")
                    .withCharacterType(CharType.MAGE)
                    .withSpeciesType(SpeciesType.DWARF)
                    .withProfession(null)
                    .withLevel(1)
                    .withAttributes(ATTRIBUTES)
                    .withSpeciesStrength(SPECIES_STRENGTH_ATTRIBUTE)
                    .build();
    private static final CharacterCreateRequest VALID_CHARACTER_CREATE_REQUEST_WITH_PROFESSION =
            CharacterCreateRequestBuilder.getBuilder()
                    .withCharacterName("Test")
                    .withCharacterType(null)
                    .withSpeciesType(SpeciesType.DWARF)
                    .withProfession("TestProfession")
                    .withLevel(0)
                    .withAttributes(ATTRIBUTES)
                    .withSpeciesStrength(SPECIES_STRENGTH_ATTRIBUTE)
                    .build();
    private static final String DUMMY_BEARER_TOKEN = "some token";
    private static final String GOOGLE_SHEETS_URL = "/api/v1/createcharacter/googlesheets";
    private static final String PDF_URL = "/api/v1/createcharacter/pdf";

    @ParameterizedTest
    @ValueSource(strings = {GOOGLE_SHEETS_URL, PDF_URL})
    public void createCharacterMethods_Return400IfJsonIsInvalid(String url) {
        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(url)
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
    @MethodSource("nullEmptyCharacterNamesAndUrls")
    public void createCharacterMethods_Return400IfCharacterNameIsNullOrEmpty(String characterName,
                                                                             String url) {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName(characterName)
                .withCharacterType(CharType.BERZERKER)
                .withSpeciesType(SpeciesType.HUMAN)
                .withProfession("Test")
                .withLevel(1)
                .build();

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(url)
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

    static Stream<Arguments> nullEmptyCharacterNamesAndUrls() {
        return Stream.of(
                Arguments.arguments(null, GOOGLE_SHEETS_URL),
                Arguments.arguments("", GOOGLE_SHEETS_URL),
                Arguments.arguments(null, PDF_URL),
                Arguments.arguments("", PDF_URL)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {GOOGLE_SHEETS_URL, PDF_URL})
    public void createCharacterMethods_Return400IfLevelIsNull(String url) {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName("Test")
                .withCharacterType(CharType.MAGE)
                .withSpeciesType(SpeciesType.HUMAN)
                .withProfession("Test")
                .withLevel(null)
                .build();

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(url)
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
    @ValueSource(strings = {GOOGLE_SHEETS_URL, PDF_URL})
    public void createCharacterMethods_Return400IfSpeciesIsNull(String url) {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName("Test")
                .withCharacterType(CharType.MAGE)
                .withSpeciesType(null)
                .withProfession("Test")
                .withLevel(1)
                .build();

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(url)
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
    @MethodSource("invalidCharacterClassStringsAndUrls")
    public void createCharacterMethods_Return400IfRequestCharacterClassStringCannotBeConvertedToEnumValue(
            String characterClass, String url) {
        var requestJson = "{\"characterName\": \"Name\", \"species\": \"ELF\", \"characterClass\": \""
                + characterClass
                + "\", \"level\": 1}";

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(url)
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

    static Stream<Arguments> invalidCharacterClassStringsAndUrls() {
        return Stream.of(
                Arguments.arguments("", GOOGLE_SHEETS_URL),
                Arguments.arguments("rogue", GOOGLE_SHEETS_URL),
                Arguments.arguments("Rogue", GOOGLE_SHEETS_URL),
                Arguments.arguments("roGUE", GOOGLE_SHEETS_URL),
                Arguments.arguments("", PDF_URL),
                Arguments.arguments("rogue", PDF_URL),
                Arguments.arguments("Rogue", PDF_URL),
                Arguments.arguments("roGUE", PDF_URL)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidSpeciesStringsAndUrls")
    public void createCharacterMethods_Return400IfRequestSpeciesStringCannotBeConvertedToEnumValue(
            String species, String url) {
        var requestJson = "{\"characterName\": \"Name\", \"characterClass\": \"SKALD\", \"species\": \""
                + species
                + "\", \"level\": 1}";

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(url)
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

    static Stream<Arguments> invalidSpeciesStringsAndUrls() {
        return Stream.of(
                Arguments.arguments("", GOOGLE_SHEETS_URL),
                Arguments.arguments("elf", GOOGLE_SHEETS_URL),
                Arguments.arguments("Elf", GOOGLE_SHEETS_URL),
                Arguments.arguments("eLF", GOOGLE_SHEETS_URL),
                Arguments.arguments("", PDF_URL),
                Arguments.arguments("elf", PDF_URL),
                Arguments.arguments("Elf", PDF_URL),
                Arguments.arguments("eLF", PDF_URL)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {GOOGLE_SHEETS_URL, PDF_URL})
    public void createCharacterMethods_Return400IfAttributesIsNull(String url) {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName("Test")
                .withCharacterType(CharType.MAGE)
                .withSpeciesType(SpeciesType.DWARF)
                .withProfession("Test")
                .withLevel(1)
                .withNullAttributes()
                .withSpeciesStrength("COR")
                .build();

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(url)
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
    @ValueSource(strings = {GOOGLE_SHEETS_URL, PDF_URL})
    public void createCharacterMethods_Return400IfSpeciesStrengthIsNull(String url) {
        var request = CharacterCreateRequestBuilder.getBuilder()
                .withCharacterName("Test")
                .withCharacterType(CharType.MAGE)
                .withSpeciesType(SpeciesType.DWARF)
                .withProfession("Test")
                .withLevel(1)
                .withAttributes(new HashMap<>())
                .build();

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(url)
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
    public void createCharacterPdf_Returns500WithErrorMessageIfPdfServiceReturnsFailureStatus() {
        var expectedErrMsg = "Some error message";
        var status = PdfCharacterCreateStatus.error(expectedErrMsg);

        Mockito.when(
                pdfCharacterCreateService.createCharacter(VALID_CHARACTER_CREATE_REQUEST_WITH_CLASS))
                .thenReturn(status);

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(PDF_URL)
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

    @Test
    public void createCharacterPdf_Returns500IfPdfServiceThrowsException() {
        Mockito.when(
                pdfCharacterCreateService.createCharacter(VALID_CHARACTER_CREATE_REQUEST_WITH_CLASS))
                .thenThrow(new RuntimeException());

        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .post(PDF_URL)
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

    @ParameterizedTest
    @MethodSource("validCharacterCreateRequests")
    public void createCharacterPdf_Returns200OnSuccessIfRequestIsValid(CharacterCreateRequest validRequest) {
        var testInputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
        var pdfSuccessStatus = new PdfCharacterCreateStatus(testInputStream, "test.pdf", null);
        Mockito.when(
                pdfCharacterCreateService.createCharacter(validRequest))
                .thenReturn(pdfSuccessStatus);

        try {
            System.out.println(objectMapper.writeValueAsString(validRequest));
            mockMvc.perform(MockMvcRequestBuilders
                            .post(PDF_URL)
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
