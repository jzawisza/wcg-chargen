import { render, screen, fireEvent } from "@testing-library/react";
import AttributeSelector from "../AttributeSelector";
import { CharacterContext } from "../../../../Context";
import { emptyAttributeScoreObj } from "../../../../constants/AttributeScoreObject";
import { getArrayByName } from "../../../../constants/AttributeArrayType";

test('link to attribute value recommendation modal not displayed for Wicked Hard Mode', () => {
    const charInfoContext = {
        level: 0,
        attributeScoreObj: emptyAttributeScoreObj,
        attributeValues: [0, 0, 0, 0, 0, 0, 0],
        setAttributeScoreObj: jest.fn()
    };

    render(<CharacterContext.Provider value={charInfoContext}>
        <AttributeSelector />
    </CharacterContext.Provider>);

    const modalDisplayLink = screen.queryByText('What attribute values should I choose?');
    expect(modalDisplayLink).toBeNull(); 
});

test.each([
    ["challenging"],
    ["heroic"]
])('link to attribute value recommendation modal displayed for Traditional Mode with attribute array type %s', (arrayTypeStr: string) => {
    const charInfoContext = {
        level: 1,
        attributeScoreObj: emptyAttributeScoreObj,
        attributeValues: getArrayByName(arrayTypeStr)?.array
    };

    render(<CharacterContext.Provider value={charInfoContext}>
        <AttributeSelector arrayType={arrayTypeStr} />
    </CharacterContext.Provider>);

    const modalDisplayLink = screen.getByText('What attribute values should I choose?');  
    
    expect(modalDisplayLink).toBeTruthy();
    expect(modalDisplayLink.tagName).toBe('A');
});

test.each([
    ["berzerker", "Berzerkers"],
    ["mage", "Mages"],
    ["mystic", "Mystics"],
    ["ranger", "Rangers"],
    ["rogue", "Rogues"],
    ["shaman", "Shamans"],
    ["skald", "Skalds"],
    ["warrior", "Warriors"]
])('correct recommendations display in modal for character class %s', (charClass: string, pluralClassName: string) => {
    const arrayTypeStr="heroic";

    const charInfoContext = {
        level: 1,
        attributeScoreObj: emptyAttributeScoreObj,
        attributeValues: getArrayByName(arrayTypeStr)?.array,
        charClass: charClass
    };

    render(<CharacterContext.Provider value={charInfoContext}>
        <AttributeSelector arrayType={arrayTypeStr} />
    </CharacterContext.Provider>);

    const modalDisplayLink = screen.getByText('What attribute values should I choose?');
    fireEvent.click(modalDisplayLink);

    const modalTitle = screen.getByText('Selecting Attribute Values');
    const generalHeader = screen.getByText('General');
    const charClassHeader = screen.getByText(pluralClassName);

    expect(modalTitle).toBeTruthy();
    expect(generalHeader).toBeTruthy();
    expect(generalHeader.tagName).toBe('H3');
    expect(charClassHeader).toBeTruthy();
    expect(charClassHeader.tagName).toBe('H3');
});