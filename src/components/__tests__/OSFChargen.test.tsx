import OSFChargen from "../OSFChargen";
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DropDownProps } from "antd";
import { ReactNode, ReactInstance, createRef } from "react";

const MOCK_ID_STR = "dropdownMockId";

// Define this here rather than in the mock below to avoid
// out-of-scope variable errors from Jest when using createRef
var mockReactInstance = createRef<ReactInstance>().current!;

// TODO: figure out why this mock reports syntax errors if put in setupTests.ts
jest.mock("antd", () => {
    const antd = jest.requireActual("antd");

    // The Dropdown component doesn't directly update the DOM, so it's impossible to directly test.
    // Instead, represent it as a set of radio buttons for purposes of the tests here.
    const Dropdown: React.FC<DropDownProps> = ({menu}) => {
        const radioButtons: Array<ReactNode> = [];

        if (!menu) {
            return ("");
        }

        menu.items?.forEach( (item) => {
            let itemNum = item?.key?.toString()!;
            let idStr = MOCK_ID_STR + itemNum;
            let onClickFn = menu.onClick;
            radioButtons.push(<input type="radio"
                name="mockDropDown"
                key={idStr}
                data-testid={idStr}
                onClick={event => {
                    if (onClickFn) {
                        let info = {
                            'item': mockReactInstance,
                            'key': itemNum,
                            'keyPath': [],
                            domEvent: event
                        };
                        onClickFn(info);
                    }
                }} />);
        });

        return radioButtons;
    };

    return {
        ...antd,
        Dropdown,
    };
});

/**
 * Helper method to find all step titles in the rendered page.
 * 
 * @param container Object representing the DOM
 * @returns List of step titles as strings
 */
function getStepTitles(container: HTMLElement) {
    const stepTitleElements = container.querySelectorAll(".ant-steps-item-title");

    let stepTextValues: Array<string | null> = new Array<string | null>();
    stepTitleElements.forEach(function(currentValue) {
        stepTextValues.push(currentValue.textContent);
    });

    return stepTextValues;
}

test('has Traditional and Wicked Hard Mode cards', () => {
    render(<OSFChargen />);

    const traditionalCard = screen.getByText("Traditional Start");
    const wickedHardCard = screen.getByText("Wicked Hard Mode");

    expect(traditionalCard).toBeTruthy();
    expect(wickedHardCard).toBeTruthy();
});

test('level selector has values 1-7', () => {
    render(<OSFChargen />);

    for(var i: number = 1; i <= 7; i++) {
        let elementIdToFind = MOCK_ID_STR + i.toString();
        let element = screen.getByTestId(elementIdToFind);
        expect(element).toBeTruthy();
    }
});

test('selecting Wicked Hard Mode displays appropriate steps', async () => {
    const expectedStepTitles = ["Species", "Profession", "Attributes", "Create Character"];

    const { container } = render(<OSFChargen />);

    const wickedHardCard = screen.getByText("Wicked Hard Mode");
    act(() => {
        userEvent.click(wickedHardCard);
    })

    await waitFor(() =>{
        let actualStepTitles = getStepTitles(container);

        expect(actualStepTitles.length).toEqual(4);
        expect(actualStepTitles).toEqual(expectedStepTitles);
    });
});

test('selecting Traditional Mode Level 1 displays appropriate steps', async () => {
    const expectedStepTitles = ["Species", "Class", "Skills", "Attributes", "Create Character"];

    const { container } = render(<OSFChargen />);

    let level1TestId = MOCK_ID_STR + "1";
    const level1Selector = screen.getByTestId(level1TestId);
    act(() => {
        userEvent.click(level1Selector);
    })

    await waitFor(() => {
        let actualStepTitles = getStepTitles(container);

        expect(actualStepTitles.length).toEqual(5);
        expect(actualStepTitles).toEqual(expectedStepTitles);
    })
});

test.each([
    ["2"],
    ["3"],
    ["4"],
    ["5"],
    ["6"],
    ["7"]
])('selecting Traditional Mode Level %s displays appropriate steps', async (levelStr) => {
    const expectedStepTitles = ["Species", "Class", "Skills", "Attributes", "Features", "Create Character"];

    const { container } = render(<OSFChargen />);

    let levelTestId = MOCK_ID_STR + levelStr;
    const levelSelector = screen.getByTestId(levelTestId);
    act(() => {
        userEvent.click(levelSelector);
    })

    await waitFor(() => {
        let actualStepTitles = getStepTitles(container);

        expect(actualStepTitles.length).toEqual(6);
        expect(actualStepTitles).toEqual(expectedStepTitles);
    })
});