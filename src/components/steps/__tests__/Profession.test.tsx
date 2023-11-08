import { fireEvent, render, screen } from "@testing-library/react";
import Profession from "../Profession";
import { CharacterContext, NextButtonEnabledContext } from "../../../Context";

test('clicking on profession radio button enables Next button', () => {
    const mockSetNextEnabled = jest.fn();
    const nextButtonEnabledContext = {
        setNextEnabled: mockSetNextEnabled
    };

    render(
        <NextButtonEnabledContext.Provider value={nextButtonEnabledContext}>
            <Profession />
        </NextButtonEnabledContext.Provider>);

    // Get first button in radio group and click on it
    const inputNode = screen.getByRole('radio', {name: 'Scribe'});
    fireEvent.click(inputNode);

    // The next button should be disabled on initial render, and enabled on clicking the radio button
    expect(mockSetNextEnabled).toHaveBeenCalledTimes(2);
    expect(mockSetNextEnabled).toHaveBeenNthCalledWith(1, false);
    expect(mockSetNextEnabled).toHaveBeenLastCalledWith(true);
});

test('clicking on profession radio button sets context correctly', () => {
    const mockSetProfession = jest.fn();
    const charInfoContext = {
        setProfession: mockSetProfession
    };

    render(
        <CharacterContext.Provider value={charInfoContext}>
            <Profession />
        </CharacterContext.Provider>
    );

    // Get first button in radio group and click on it
    const inputNode = screen.getByRole('radio', {name: 'Scribe'});
    fireEvent.click(inputNode);

    expect(mockSetProfession).toHaveBeenCalled();
});