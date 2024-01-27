import { fireEvent, render, screen } from "@testing-library/react";
import { CharacterContext } from "../../../../Context";
import AttributeScoreSelector from "../AttributeScoreSelector";
import { EMPTY_ATTRIBUTE_SCORE_OBJ } from "../../../../constants/AttributeScoreObject";

test('reset values button works as expected', () => {
    const attributeValueArray = [3, 2, 1, 0, -1, -2, -3];

    const mockSetAttributeScoreObj = jest.fn();
    const mockSetAttributeValues = jest.fn();
    const mockSetSpeciesStrengthAttribute = jest.fn();
    const mockSetSpeciesWeaknessAttribute = jest.fn();
    const charContext = {
        attributeScoreObj: EMPTY_ATTRIBUTE_SCORE_OBJ,
        attributeValues: attributeValueArray,
        setAttributeScoreObj: mockSetAttributeScoreObj,
        setAttributeValues: mockSetAttributeValues,
        setSpeciesStrengthAttribute: mockSetSpeciesStrengthAttribute,
        setSpeciesWeaknessAttribute: mockSetSpeciesWeaknessAttribute
    };

    render(
        <CharacterContext.Provider value={charContext}>
            <AttributeScoreSelector canSelectValues={true} initialValues={attributeValueArray}/>
        </CharacterContext.Provider>
    );

    const resetValuesButtonSpan = screen.getByText('Reset values');
    expect (resetValuesButtonSpan).toBeTruthy();

    const resetValuesButton = resetValuesButtonSpan?.parentElement;
    if (resetValuesButton) {
        fireEvent.click(resetValuesButton);
    }

    expect(mockSetAttributeScoreObj).toHaveBeenCalledWith(EMPTY_ATTRIBUTE_SCORE_OBJ);
    expect(mockSetAttributeValues).toHaveBeenCalledWith(attributeValueArray);
    expect(mockSetSpeciesStrengthAttribute).toHaveBeenCalledWith("");
    expect(mockSetSpeciesWeaknessAttribute).toHaveBeenCalledWith("");
});

test.each([
    ["STR", 0],
    ["COR", 1],
    ["STA", 2],
    ["PER", 3],
    ["INT", 4],
    ["PRS", 5],
    ["LUC", 6],
])('setting species strength to %s displays "+ 1 = 1" in third column of table', (strength: string, pos: number) => {
    // Set attribute scores and values to all 0 so the text to expect will always be "+ 1 = 1"
    const attributeValueArray = [0, 0, 0, 0, 0, 0, 0];
    const attributeScoreObj = {
        STR: 0,
        COR: 0,
        STA: 0,
        PER: 0,
        INT: 0,
        PRS: 0,
        LUC: 0
    }
    const expectedText = " + 1 = 1";

    const charContext = {
        attributeScoreObj: attributeScoreObj,
        attributeValues: attributeValueArray,
        speciesStrengthAttribute: strength
    };

    const { container } = render(
        <CharacterContext.Provider value={charContext}>
            <AttributeScoreSelector canSelectValues={true} initialValues={attributeValueArray}/>
        </CharacterContext.Provider>
    );

    const tdStrengthWeaknessEltList = container.querySelectorAll('.attributeTableStrengthWeaknessCell');
    expect(tdStrengthWeaknessEltList).toBeTruthy();
    expect(tdStrengthWeaknessEltList.length).toBe(7);

    const textAtSpecifiedPos = tdStrengthWeaknessEltList[pos].textContent;
    expect(textAtSpecifiedPos).toBe(expectedText);
});

test.each([
    ["STR", 0],
    ["COR", 1],
    ["STA", 2],
    ["PER", 3],
    ["INT", 4],
    ["PRS", 5],
    ["LUC", 6],
])('setting species weakness to %s displays "- 1 = -1" in third column of table', (weakness: string, pos: number) => {
    // Set attribute scores and values to all 0 so the text to expect will always be "- 1 = -1"
    const attributeValueArray = [0, 0, 0, 0, 0, 0, 0];
    const attributeScoreObj = {
        STR: 0,
        COR: 0,
        STA: 0,
        PER: 0,
        INT: 0,
        PRS: 0,
        LUC: 0
    }
    const expectedText = " - 1 = -1";

    const charContext = {
        attributeScoreObj: attributeScoreObj,
        attributeValues: attributeValueArray,
        speciesWeaknessAttribute: weakness
    };

    const { container } = render(
        <CharacterContext.Provider value={charContext}>
            <AttributeScoreSelector canSelectValues={true} initialValues={attributeValueArray}/>
        </CharacterContext.Provider>
    );

    const tdStrengthWeaknessEltList = container.querySelectorAll('.attributeTableStrengthWeaknessCell');
    expect(tdStrengthWeaknessEltList).toBeTruthy();
    expect(tdStrengthWeaknessEltList.length).toBe(7);

    const textAtSpecifiedPos = tdStrengthWeaknessEltList[pos].textContent;
    expect(textAtSpecifiedPos).toBe(expectedText);
});
