import ChargenStepper from "../ChargenStepper";
import { render, screen, act, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

const testSteps = [
    { title: 'First Step'},
    { title: 'Second Step'},
    { title: 'Third Step' }
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

    const nextButton = screen.getByText("Next");

    fireEvent.click(nextButton);
    fireEvent.click(nextButton);
    
    await waitFor(() => {
        const prevButton = screen.getByText("Previous");
        const missingNextButton = screen.queryByText("Next");

        expect(prevButton).toBeTruthy();
        expect(missingNextButton).toBeNull();
    });
});