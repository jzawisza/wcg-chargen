import { render, screen } from "@testing-library/react";
import CharacterClass from "../CharacterClass";
import { CharacterContext } from "../../../Context";

test('all eight classes represented on component', () => {
    render(<CharacterClass />);

    const berzerkerText = screen.getByText('Berzerker');
    const mageText = screen.getByText('Mage');
    const mysticText = screen.getByText('Mystic');
    const rangerText = screen.getByText('Ranger');
    const rogueText = screen.getByText('Rogue');
    const shamanText = screen.getByText('Shaman');
    const skaldText = screen.getByText('Skald');
    const warriorText = screen.getByText('Warrior');

    expect(berzerkerText).toBeTruthy();
    expect(mageText).toBeTruthy();
    expect(mysticText).toBeTruthy();
    expect(rangerText).toBeTruthy();
    expect(rogueText).toBeTruthy();
    expect(shamanText).toBeTruthy();
    expect(skaldText).toBeTruthy();
    expect(warriorText).toBeTruthy();
});

test('use quick gear checkbox displays', () => {
    render(<CharacterClass />);

    const quickGearElement = screen.getByText('Use Quick Gear');

    expect(quickGearElement).toBeTruthy();
});

test.each([
    ["Berzerker"],
    ["Mage"],
    ["Mystic"],
    ["Ranger"],
    ["Rogue"],
    ["Shaman"],
    ["Skald"],
    ["Warrior"]
])('selecting character class %s highlights correct card', async (charClassStr) => {
    const charInfoContext = {
        charClass: charClassStr.toLowerCase(),
        setCharClass: (newCharClass: string) => {}
    };

    render(
        <CharacterContext.Provider value={charInfoContext}>
            <CharacterClass />
        </CharacterContext.Provider>
    );

    const charClassCardTitleElt = screen.getByText(charClassStr);
    const charClassCardElt = charClassCardTitleElt.parentElement?.parentElement?.parentElement;
    expect(charClassCardElt).toHaveClass('selectableCard-selected');
});