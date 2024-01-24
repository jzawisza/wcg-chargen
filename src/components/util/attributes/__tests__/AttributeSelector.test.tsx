import { render, screen, fireEvent, act } from "@testing-library/react";
import AttributeSelector from "../AttributeSelector";
import { CharacterContext, NextButtonEnabledContext } from "../../../../Context";
import { AttributeScoreObject, EMPTY_ATTRIBUTE_SCORE_OBJ } from "../../../../constants/AttributeScoreObject";
import { getArrayByName } from "../../../../constants/AttributeArrayType";
import { DefaultOptionType } from "antd/es/select";
import userEvent from "@testing-library/user-event";

// Mock for Select component
type SelectMockProps = {
    disabled?: boolean
    onChange: (value: string[]) => void
    options: (DefaultOptionType[] | undefined)
};

const SELECT_TEST_ID = 'select-test-id';

jest.mock('antd', () => {
    const antd = jest.requireActual('antd');
  
    const Select = (props: SelectMockProps) => {
      let options = undefined;

      if (props.options) {
        options = props.options.map(o => {
            return (<option key={o.value} value={o.value ? o.value : ''}>{o.value}</option>);
        });
      }
  
      return (
        <select
          data-testid={SELECT_TEST_ID}
          disabled={props.disabled}
          onChange={(e) => props.onChange(Array.of(e.target.value))
          }
        >
        {options}
        </select>
      );
    };
  
    return { ...antd, Select };
});

const COMPLETE_ATTRIBUTE_SCORE_OBJ: AttributeScoreObject = {
    STR: 1,
    COR: 2,
    STA: 1,
    PER: 0,
    INT: -1,
    PRS: -2,
    LUC: 0
}

function getNumDisabledSpanElements(spanEltList: HTMLCollectionOf<HTMLSpanElement> | undefined) {
    if (spanEltList) {
        const spanEltArray = Array.from(spanEltList);
        const disabledSpanEltList = spanEltArray.filter(x => x.classList.contains('ant-radio-button-disabled'));

        return disabledSpanEltList.length;
    }

    // Return null if we pass in an undefined value
    return null;
}

function getNumSpanElementsWithText(spanEltList: HTMLCollectionOf<HTMLSpanElement> | undefined, content: string) {
    if (spanEltList) {
        const spanEltArray = Array.from(spanEltList);
        const spanEltWithContentList = spanEltArray.filter(x => x.textContent === content);

        return spanEltWithContentList.length;
    }

    // Return null if we pass in an undefined value
    return null;
}


test('link to attribute value recommendation modal not displayed for Wicked Hard Mode', () => {
    const charInfoContext = {
        level: 0,
        attributeScoreObj: EMPTY_ATTRIBUTE_SCORE_OBJ,
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
        attributeScoreObj: EMPTY_ATTRIBUTE_SCORE_OBJ,
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
        attributeScoreObj: EMPTY_ATTRIBUTE_SCORE_OBJ,
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

test.each([
    ["dwarf", "STR", "STA", "PRS", "LUC"],
    ["elf", "COR", "PER", "STR", "STA"],
    ["halfling", "LUC", "COR", "PER", "STR"]
])('expected strength and weakness buttons for non-human species %s are displayed and initially disabled',
    (speciesStr: string, strength1: string, strength2: string, weakness1: string, weakness2: string) => {
        const charInfoContext = {
            level: 0,
            attributeScoreObj: EMPTY_ATTRIBUTE_SCORE_OBJ,
            attributeValues: [0, 0, 0, 0, 0, 0, 0],
            setAttributeScoreObj: jest.fn(),
            species: speciesStr
        };

        render(
            <CharacterContext.Provider value={charInfoContext}>
                <AttributeSelector />
            </CharacterContext.Provider>
        );

        const strengthHeaderElt = screen.getByText('Strength (+1)');
        const weaknessHeaderElt = screen.getByText('Weakness (-1)');

        expect(strengthHeaderElt).toBeTruthy();
        expect(weaknessHeaderElt).toBeTruthy();

        const strengthSpanEltList = strengthHeaderElt.nextElementSibling?.getElementsByTagName('span');
        expect(getNumDisabledSpanElements(strengthSpanEltList)).toBe(2);
        expect(getNumSpanElementsWithText(strengthSpanEltList, strength1)).toBe(1);
        expect(getNumSpanElementsWithText(strengthSpanEltList, strength2)).toBe(1);

        const weaknessSpanEltList = weaknessHeaderElt.nextElementSibling?.getElementsByTagName('span');
        expect(getNumDisabledSpanElements(weaknessSpanEltList)).toBe(2);
        expect(getNumSpanElementsWithText(weaknessSpanEltList, weakness1)).toBe(1);
        expect(getNumSpanElementsWithText(weaknessSpanEltList, weakness2)).toBe(1);
});

test.each([
    ["dwarf"],
    ["elf"],
    ["halfling"]
])('setting all attribute scores for non-human species %s enables strength and weakness buttons', (speciesStr: string) => {
    const charInfoContext = {
        level: 0,
        attributeScoreObj: COMPLETE_ATTRIBUTE_SCORE_OBJ,
        attributeValues: [0, 0, 0, 0, 0, 0, 0],
        setAttributeScoreObj: jest.fn(),
        species: speciesStr
    };

    render(
        <CharacterContext.Provider value={charInfoContext}>
            <AttributeSelector />
        </CharacterContext.Provider>
    );

    const strengthHeaderElt = screen.getByText('Strength (+1)');
    const weaknessHeaderElt = screen.getByText('Weakness (-1)');

    expect(strengthHeaderElt).toBeTruthy();
    expect(weaknessHeaderElt).toBeTruthy();

    const strengthSpanEltList = strengthHeaderElt.nextElementSibling?.getElementsByTagName('span');
    const weaknessSpanEltList = weaknessHeaderElt.nextElementSibling?.getElementsByTagName('span');

    expect(getNumDisabledSpanElements(strengthSpanEltList)).toBe(0);
    expect(getNumDisabledSpanElements(weaknessSpanEltList)).toBe(0);
});

test.each([
    [EMPTY_ATTRIBUTE_SCORE_OBJ, true],
    [COMPLETE_ATTRIBUTE_SCORE_OBJ, false]
])('select strength dropdown for humans with attribute score object %s has disabled = %s',
    (initialAttributeScoreObj: AttributeScoreObject, isDisabled: boolean) => {
        const expectedAttributeValue = isDisabled ? '' : null;

        const charInfoContext = {
            level: 0,
            attributeScoreObj: initialAttributeScoreObj,
            attributeValues: [0, 0, 0, 0, 0, 0, 0],
            setAttributeScoreObj: jest.fn(),
            species: "human"
        };
    
        render(
            <CharacterContext.Provider value={charInfoContext}>
                <AttributeSelector />
            </CharacterContext.Provider>
        );
    
        const selectInput = screen.getByRole('combobox');
        expect(selectInput).toBeTruthy();
    
        const disabledProperty = selectInput.getAttribute('disabled');
        expect(disabledProperty).toBe(expectedAttributeValue);
});

test.each([
    ["dwarf", EMPTY_ATTRIBUTE_SCORE_OBJ],
    ["dwarf", COMPLETE_ATTRIBUTE_SCORE_OBJ],
    ["elf", EMPTY_ATTRIBUTE_SCORE_OBJ],
    ["elf", COMPLETE_ATTRIBUTE_SCORE_OBJ],
    ["halfling", EMPTY_ATTRIBUTE_SCORE_OBJ],
    ["halfling", COMPLETE_ATTRIBUTE_SCORE_OBJ],
    ["human", EMPTY_ATTRIBUTE_SCORE_OBJ],
    ["human", COMPLETE_ATTRIBUTE_SCORE_OBJ],
])('next button is disabled for species %s with attribute score object %s',
    (speciesStr: string, initialAttributeScoreObj: AttributeScoreObject) => {
    const charInfoContext = {
        level: 0,
        attributeScoreObj: initialAttributeScoreObj,
        attributeValues: [0, 0, 0, 0, 0, 0, 0],
        setAttributeScoreObj: jest.fn(),
        species: speciesStr,
        speciesStrengthAttribute: "",
        speciesWeaknessAttribute: ""
    };

    const mockSetNextEnabled = jest.fn();
    const nextButtonEnabledContext = {
        setNextEnabled: mockSetNextEnabled
    };

    render(
        <CharacterContext.Provider value={charInfoContext}>
        <NextButtonEnabledContext.Provider value={nextButtonEnabledContext}>
            <AttributeSelector />
        </NextButtonEnabledContext.Provider>
        </CharacterContext.Provider>
    );

    expect(mockSetNextEnabled).toHaveBeenLastCalledWith(false);
});

test.each([
    ["dwarf"],
    ["elf"],
    ["halfling"],
    ["human"]
])('next button is enabled for species %s if attribute score object is fully populated and strength and/or weakness are set',
    (speciesStr: string) => {
        const charInfoContext = {
            level: 0,
            attributeScoreObj: COMPLETE_ATTRIBUTE_SCORE_OBJ,
            attributeValues: [0, 0, 0, 0, 0, 0, 0],
            setAttributeScoreObj: jest.fn(),
            species: speciesStr,
            speciesStrengthAttribute: "foo",
            speciesWeaknessAttribute: "bar"
        };
    
        const mockSetNextEnabled = jest.fn();
        const nextButtonEnabledContext = {
            setNextEnabled: mockSetNextEnabled
        };
    
        render(
            <CharacterContext.Provider value={charInfoContext}>
            <NextButtonEnabledContext.Provider value={nextButtonEnabledContext}>
                <AttributeSelector />
            </NextButtonEnabledContext.Provider>
            </CharacterContext.Provider>
        );
    
        expect(mockSetNextEnabled).toHaveBeenLastCalledWith(true);
});

test.each([
    ["dwarf", "STR", "PRS"],
    ["dwarf", "STA", "PRS"],
    ["dwarf", "STR", "LUC"],
    ["dwarf", "STA", "LUC"],
    ["elf", "COR", "STR"],
    ["elf", "PER", "STR"],
    ["elf", "COR", "STA"],
    ["elf", "PER", "STA"],
    ["halfling", "LUC", "PER"],
    ["halfling", "LUC", "STR"],
    ["halfling", "COR", "PER"],
    ["halfling", "COR", "STR"]
])('for non-human species %s, selecting strength %s and weakness %s sets context correctly',
    (speciesStr: string, strength: string, weakness: string) => {
        const mockSetSpeciesStrengthAttribute = jest.fn();
        const mocksetSpeciesWeaknessAttribute = jest.fn();

        const charInfoContext = {
            level: 0,
            attributeScoreObj: COMPLETE_ATTRIBUTE_SCORE_OBJ,
            attributeValues: [0, 0, 0, 0, 0, 0, 0],
            setAttributeScoreObj: jest.fn(),
            species: speciesStr,
            setSpeciesStrengthAttribute: mockSetSpeciesStrengthAttribute,
            setSpeciesWeaknessAttribute: mocksetSpeciesWeaknessAttribute
        };

        render(
            <CharacterContext.Provider value={charInfoContext}>
                <AttributeSelector />
            </CharacterContext.Provider>
        );

        const strengthButtonElt = screen.getByRole('radio', {name: strength});
        const weaknessButtonElt = screen.getByRole('radio', {name: weakness});
        fireEvent.click(strengthButtonElt);
        fireEvent.click(weaknessButtonElt);

        expect(mockSetSpeciesStrengthAttribute).toHaveBeenLastCalledWith(strength);
        expect(mocksetSpeciesWeaknessAttribute).toHaveBeenLastCalledWith(weakness);
});


test.each([
    ["STR"],
    ["COR"],
    ["STA"],
    ["PER"],
    ["INT"],
    ["PRS"],
    ["LUC"],
])('for humans, selecting strength %s sets context correctly', (attributeType: string) => {
    const mockSetSpeciesStrengthAttribute = jest.fn();

    // Create a new attribute score object where the attribute type has the lowest possible score,
    // to guarantee it'll be in the dropdown list
    let newAttributeScoreObj = Object.create(COMPLETE_ATTRIBUTE_SCORE_OBJ);
    const attributeKey = attributeType as keyof AttributeScoreObject;
    newAttributeScoreObj[attributeKey] = -3;

    const charInfoContext = {
        level: 0,
        attributeScoreObj: newAttributeScoreObj,
        attributeValues: [0, 0, 0, 0, 0, 0, 0],
        setAttributeScoreObj: jest.fn(),
        species: "human",
        setSpeciesStrengthAttribute: mockSetSpeciesStrengthAttribute
    };

    render(
        <CharacterContext.Provider value={charInfoContext}>
            <AttributeSelector />
        </CharacterContext.Provider>
    );

    const selectElt = screen.getByTestId(SELECT_TEST_ID);
    act(() => {
        userEvent.selectOptions(selectElt, [attributeType]);
    });

    expect(mockSetSpeciesStrengthAttribute).toHaveBeenCalledWith(attributeType);
});

// if select dropdown is enabled for humans, selecting one item sets context correctly