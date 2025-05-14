package com.wcg.chargen.backend.enums;

import com.wcg.chargen.backend.model.CharacterCreateRequest;
import com.wcg.chargen.backend.testUtil.CharacterCreateRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CharacterCreateRequestTests {
    private Map<String, Integer> attributesMap;

    @BeforeEach
    public void setup() {
        attributesMap = CharacterCreateRequestBuilder.getAttributesMap(
                -3, -2, -1, 0, 1, 2, 3);
    }

    @ParameterizedTest
    @MethodSource("attributesTypesAndValues")
    public void getAttributeValue_ReturnsExpectedValuesIfNoSpeciesStrengthOrWeaknessSpecified
            (AttributeType attrType, int expectedAttributeValue) {
        // arrange
        var characterCreateRequest = CharacterCreateRequestBuilder.getBuilder()
                .withAttributes(attributesMap)
                .build();

        // act
        var attributeValue = characterCreateRequest.getAttributeValue(attrType);

        // assert
        assertEquals(expectedAttributeValue, attributeValue);
    }

    @ParameterizedTest
    @MethodSource("attributesTypesAndValues")
    public void getAttributeValue_ReturnsExpectedValuesIfSpeciesStrengthIsSpecified
            (AttributeType attrType, int baseAttributeValue) {
        // arrange
        var expectedAttributeValue = baseAttributeValue + 1;
        var characterCreateRequest = CharacterCreateRequestBuilder.getBuilder()
                .withAttributes(attributesMap)
                .withSpeciesStrength(attrType.name())
                .build();

        // act
        var attributeValue = characterCreateRequest.getAttributeValue(attrType);

        // assert
        assertEquals(expectedAttributeValue, attributeValue);
    }

    @ParameterizedTest
    @MethodSource("attributesTypesAndValues")
    public void getAttributeValue_ReturnsExpectedValuesIfSpeciesWeaknessIsSpecified
            (AttributeType attrType, int baseAttributeValue) {
        // arrange
        var expectedAttributeValue = baseAttributeValue -   1;
        var characterCreateRequest = CharacterCreateRequestBuilder.getBuilder()
                .withAttributes(attributesMap)
                .withSpeciesWeakness(attrType.name())
                .build();

        // act
        var attributeValue = characterCreateRequest.getAttributeValue(attrType);

        // assert
        assertEquals(expectedAttributeValue, attributeValue);
    }

    static Stream<Arguments> attributesTypesAndValues() {
        return Stream.of(
          Arguments.arguments(AttributeType.STR, -3),
          Arguments.arguments(AttributeType.COR, -2),
          Arguments.arguments(AttributeType.STA, -1),
          Arguments.arguments(AttributeType.PER, 0),
          Arguments.arguments(AttributeType.INT, 1),
          Arguments.arguments(AttributeType.PRS, 2),
          Arguments.arguments(AttributeType.LUC, 3)
        );
    }
}
