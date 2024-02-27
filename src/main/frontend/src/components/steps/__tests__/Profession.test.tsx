import { fireEvent, render, screen } from "@testing-library/react";
import Profession from "../Profession";
import { CharacterContext, NextButtonEnabledContext } from "../../../Context";
import { ProfessionsType } from "../../../server/ProfessionsType";
import * as professions from "../../../server/ServerData";

const MOCK_PROFESSIONS_DATA: ProfessionsType = {
    "professions": [
        {
            "name": "Farmer",
            "rangeStart": 30,
            "rangeEnd": 36
        },
        {
            "name": "Hunter",
            "rangeStart": 52,
            "rangeEnd": 55
        }
    ]
};

const MOCK_EMPTY_PROFESSIONS_DATA: ProfessionsType = {
    "professions": []
};


test('clicking on profession radio button enables Next button', () => {
    jest.spyOn(professions, 'preloadProfessionsData').mockImplementation(jest.fn());
    jest.spyOn(professions, 'useProfessionsData').mockReturnValue({
        data: MOCK_PROFESSIONS_DATA,
        error: false,
        isLoading: false
    });

    const mockSetNextEnabled = jest.fn();
    const nextButtonEnabledContext = {
        setNextEnabled: mockSetNextEnabled
    };

    render(
        <NextButtonEnabledContext.Provider value={nextButtonEnabledContext}>
            <Profession />
        </NextButtonEnabledContext.Provider>);

    // Get first button in radio group and click on it
    const inputNode = screen.getByRole('radio', {name: 'Farmer'});
    fireEvent.click(inputNode);

    // The next button should be disabled on initial render, and enabled on clicking the radio button
    expect(mockSetNextEnabled).toHaveBeenCalledTimes(2);
    expect(mockSetNextEnabled).toHaveBeenNthCalledWith(1, false);
    expect(mockSetNextEnabled).toHaveBeenLastCalledWith(true);
});

test('clicking on profession radio button sets context correctly', () => {
    jest.spyOn(professions, 'preloadProfessionsData').mockImplementation(jest.fn());
    jest.spyOn(professions, 'useProfessionsData').mockReturnValue({
        data: MOCK_PROFESSIONS_DATA,
        error: false,
        isLoading: false
    });

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
    const inputNode = screen.getByRole('radio', {name: 'Farmer'});
    fireEvent.click(inputNode);

    expect(mockSetProfession).toHaveBeenCalled();
});

test('error from server displays expected output', () => {
    jest.spyOn(professions, 'preloadProfessionsData').mockImplementation(jest.fn());
    jest.spyOn(professions, 'useProfessionsData').mockReturnValue({
        data: MOCK_EMPTY_PROFESSIONS_DATA,
        error: true,
        isLoading: false
    });

    render(<Profession />);

    const errorText = screen.getByText('Error loading professions data from server.');

    expect(errorText).toBeTruthy();
});

test('data loading from server displays expected output', () => {
    jest.spyOn(professions, 'preloadProfessionsData').mockImplementation(jest.fn());
    jest.spyOn(professions, 'useProfessionsData').mockReturnValue({
        data: MOCK_EMPTY_PROFESSIONS_DATA,
        error: false,
        isLoading: true
    });

    const { container } = render(<Profession />);

    const spinner = container.querySelector('.ant-spin');

    expect(spinner).toBeTruthy();
});