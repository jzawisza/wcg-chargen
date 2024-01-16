import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { CharacterContext } from "../../../Context";
import Attributes from "../Attributes";
import { emptyAttributeScoreObj } from "../../../constants/AttributeScoreObject";

test('initial render for Wicked Hard mode does not display clickable cards', () => {
    const attributeProvider = {
        level: 0,
        attributeArrayType: "",
        attributeScoreObj: emptyAttributeScoreObj,
        attributeValues: [0, 0, 0, 0, 0, 0, 0],
        setAttributeScoreObj: jest.fn()
    };

    const { container } = render(
        <CharacterContext.Provider value={attributeProvider}>
            <Attributes />
        </CharacterContext.Provider>
    );

    const clickableCards = container.querySelector('.ant-card');

    expect(clickableCards).toBeFalsy();
});

test('initial render for Traditional mode displays clickable cards', () => {
    const attributeProvider = {
        level: 1,
        attributeArrayType: "",
        attributeScoreObj: emptyAttributeScoreObj,
        attributeValues: [0, 0, 0, 0, 0, 0, 0],
        setAttributeScoreObj: jest.fn()
    };

    const { container } = render(
        <CharacterContext.Provider value={attributeProvider}>
            <Attributes />
        </CharacterContext.Provider>
    );

    const clickableCards = container.querySelectorAll('.ant-card');
    const challengingCard = screen.getByText('Challenging');
    const heroicCard = screen.getByText('Heroic');

    expect(clickableCards.length).toBe(2);
    expect(challengingCard).toBeTruthy();
    expect(heroicCard).toBeTruthy();
});

test.each([
        ["Challenging"],
        ["Heroic",]
    ]
)('clicking %s card sets context correctly', (attributeArrayType: string) => {
    const mockSetAttributeArrayType = jest.fn();
    const attributeProvider = {
        level: 1,
        attributeArrayType: "",
        attributeScoreObj: emptyAttributeScoreObj,
        attributeValues: [],
        setAttributeScoreObj: jest.fn(),
        setAttributeArrayType: mockSetAttributeArrayType
    };

    const { container } = render(
        <CharacterContext.Provider value={attributeProvider}>
            <Attributes />
        </CharacterContext.Provider>
    );

    const attributeArrayTypeCard = screen.getByText(attributeArrayType);
    const attributeArrayTypeCardElt = attributeArrayTypeCard.parentElement?.parentElement?.parentElement;

    if (attributeArrayTypeCardElt) {
        fireEvent.click(attributeArrayTypeCardElt);
    }

    expect(mockSetAttributeArrayType).toHaveBeenCalled();
    expect(mockSetAttributeArrayType).toHaveBeenCalledWith(attributeArrayType.toLowerCase());
});