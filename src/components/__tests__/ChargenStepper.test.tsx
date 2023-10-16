import ChargenStepper from "../ChargenStepper";
import { NextButtonEnabledContext } from "../../Context";
import { render, screen, act, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useContext } from "react";

const CustomComponent = () => {
    const { setNextEnabled } = useContext(NextButtonEnabledContext);

    return (
        <button onClick={() => setNextEnabled(true)} aria-label="enableNext">
            Enable Next
        </button>
    );
};

const testSteps = [
    { title: 'First Step', content: <CustomComponent /> },
    { title: 'Second Step', content: <CustomComponent /> },
    { title: 'Third Step', content: <CustomComponent /> }
];

test('only next button and not previous button displayed on initial load', () => {
    render(<ChargenStepper steps={testSteps} />);

    const prevButton = screen.queryByText("Previous");
    const nextButton = screen.getByText("Next");

    expect(prevButton).toBeNull();
    expect(nextButton).toBeTruthy();
});

test('after clicking next button, both previous and next buttons display', async () => {
    render(<ChargenStepper steps={testSteps} />);

    // Trigger custom component to set context so that next button is enabled
    const enableNextButton = screen.getByRole("button", { name: "enableNext" });
    fireEvent.click(enableNextButton);

    const nextButton = screen.getByText("Next");
    act(() => {
        userEvent.click(nextButton);
    });

    await waitFor(() => {
        const prevButton = screen.getByText("Previous");

        expect(prevButton).toBeTruthy();
        expect(nextButton).toBeTruthy();
    });
});

test('only previous button and not next button displayed on final step', async () => {
    render(<ChargenStepper steps={testSteps} />);

    const enableNextButton = screen.getByRole("button", { name: "enableNext" });
    const nextButton = screen.getByText("Next");

    // Before triggering a click on Next, trigger the custom component so the
    // context is updated to enable the Next button
    fireEvent.click(enableNextButton);
    fireEvent.click(nextButton);
    fireEvent.click(enableNextButton);
    fireEvent.click(nextButton);
    
    await waitFor(() => {
        const prevButton = screen.getByText("Previous");
        const missingNextButton = screen.queryByText("Next");

        expect(prevButton).toBeTruthy();
        expect(missingNextButton).toBeNull();
    });
});