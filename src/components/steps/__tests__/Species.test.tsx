import { render, screen } from "@testing-library/react";
import Species from "../Species";
import { CharacterContext } from "../../../Context";

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
        setSpecies: (newSpecies: string) => {},
        profession: '',
        setProfession: (newProfession: string) => {}
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