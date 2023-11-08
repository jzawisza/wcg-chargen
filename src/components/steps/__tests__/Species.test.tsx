import { render, screen, fireEvent } from "@testing-library/react";
import Species from "../Species";
import { CharacterContext, NextButtonEnabledContext } from "../../../Context";

test('all four species represented on component', () => {
    render(<Species />);

    const dwarfText = screen.getByText('Dwarf');
    const elfText = screen.getByText('Elf');
    const halflingText = screen.getByText('Halfling');
    const humanText = screen.getByText('Human');

    expect(dwarfText).toBeTruthy();
    expect(elfText).toBeTruthy();
    expect(halflingText).toBeTruthy();
    expect(humanText).toBeTruthy();
});

test.each([
    ["Dwarf"],
    ["Elf"],
    ["Halfling"],
    ["Human"]
])('selecting species %s highlights correct card', async (speciesStr) => {
    const charInfoContext = {
        species: speciesStr.toLowerCase(),
        setSpecies: (newSpecies: string) => {}
    };

    render(
        <CharacterContext.Provider value={charInfoContext}>
            <Species />
        </CharacterContext.Provider>
    );

    const speciesCardTitleElt = screen.getByText(speciesStr);
    const speciesCardElt = speciesCardTitleElt.parentElement?.parentElement?.parentElement;
    expect(speciesCardElt).toHaveClass('selectableCard-selected');
});

test.each([
    ["Dwarf"],
    ["Elf"],
    ["Halfling"],
    ["Human"]
])('selecting species %s enables Next button', (speciesStr) => {
    const mockSetNextEnabled = jest.fn();
    const nextButtonEnabledContext = {
        setNextEnabled: mockSetNextEnabled
    };

    render(
        <NextButtonEnabledContext.Provider value={nextButtonEnabledContext}>
            <Species />
        </NextButtonEnabledContext.Provider>);

    const speciesCardTitleElt = screen.getByText(speciesStr);
    const speciesCardElt = speciesCardTitleElt.parentElement?.parentElement?.parentElement;
    
    if (speciesCardElt) {
        fireEvent.click(speciesCardElt);
    }

    // The next button should be disabled on initial render, and enabled on clicking the species card
    expect(mockSetNextEnabled).toHaveBeenCalledTimes(2);
    expect(mockSetNextEnabled).toHaveBeenNthCalledWith(1, false);
    expect(mockSetNextEnabled).toHaveBeenLastCalledWith(true);
});

test.each([
    ["Dwarf"],
    ["Elf"],
    ["Halfling"],
    ["Human"]
])('selecting species %s sets context correctly', (speciesStr) => {
    const mockSetSpecies = jest.fn();
    const charInfoContext = {
        setSpecies: mockSetSpecies
    }

    render(
        <CharacterContext.Provider value={charInfoContext}>
            <Species />
        </CharacterContext.Provider>
    );

    const speciesCardTitleElt = screen.getByText(speciesStr);
    const speciesCardElt = speciesCardTitleElt.parentElement?.parentElement?.parentElement;
    
    if (speciesCardElt) {
        fireEvent.click(speciesCardElt);
    }

    expect(mockSetSpecies).toHaveBeenCalled();
});