import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import CharacterName from "../CharacterName";
import { NextButtonEnabledContext } from "../../../Context";

test('character name input box and input box descriptive text displayed', () => {
    render(<CharacterName />);

    const charNameText = screen.getByText('Character name:');
    expect(charNameText).toBeTruthy();

    const inputNode = screen.getByRole('textbox', {name: ''});
    expect(inputNode).toBeTruthy();
});

test('entering text in character name input box enables Next button', () => {
    const mockSetNextEnabled = jest.fn();
    const nextButtonEnabledContext = {
        setNextEnabled: mockSetNextEnabled
    };

    render (
        <NextButtonEnabledContext.Provider value={nextButtonEnabledContext}>
            <CharacterName />
        </NextButtonEnabledContext.Provider>
    );

    const inputNode = screen.getByRole('textbox', {name: ''});
    fireEvent.change(inputNode, {target: {value: 'MyCharacterName'}});

    // The next button should be disabled on initial render, and only enabled
    // after text is entered
    expect(mockSetNextEnabled).toHaveBeenNthCalledWith(1, false);
    expect(mockSetNextEnabled).toHaveBeenLastCalledWith(true);
});

test('entering and then deleting text from character name input box disables Next button', async () => {
    const mockSetNextEnabled = jest.fn();
    const nextButtonEnabledContext = {
        setNextEnabled: mockSetNextEnabled
    };

    render (
        <NextButtonEnabledContext.Provider value={nextButtonEnabledContext}>
            <CharacterName />
        </NextButtonEnabledContext.Provider>
    );

    const inputNode = screen.getByRole('textbox', {name: ''});
    fireEvent.change(inputNode, {target: {value: 'MyCharacterName'}});
    fireEvent.change(inputNode, {target: {value: ''}});

    // The next button should be disabled on initial render, and then disabled again
    // after an empty string is entered into the textbox
    expect(mockSetNextEnabled).toHaveBeenNthCalledWith(1, false);
    expect(mockSetNextEnabled).toHaveBeenLastCalledWith(false);
});
