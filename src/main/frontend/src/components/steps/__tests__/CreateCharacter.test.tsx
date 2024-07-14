import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import CreateCharacter from "../CreateCharacter";

beforeAll(() => {
    Object.defineProperty(window, 'location', {
      configurable: true,
      value: { reload: jest.fn() },
    });
});

const PDF_BUTTON_TEXT = 'PDF';
const GOOGLE_SHEET_BUTTON_TEXT = 'Google Sheet';
const CREATE_CHARACTER_BUTTON_TEXT = 'Create Character';

test('initial load displays buttons for choosing character sheet type', () => {
    render(<CreateCharacter />);

    const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
    const googleSheetButton = screen.getByText(GOOGLE_SHEET_BUTTON_TEXT);

    expect(pdfButton).toBeTruthy();
    expect(googleSheetButton).toBeTruthy();
});

test('initial load displays Create Character button as disabled', () => {
    render(<CreateCharacter />);

    const createCharText = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
    const createCharButton = createCharText.parentElement;

    expect(createCharButton).toBeTruthy();
    expect(createCharButton).toHaveProperty('disabled', true);
})

test('clicking on PDF button displays appropriate text and enables Create Character button', () => {
    render(<CreateCharacter />);

    const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
    fireEvent.click(pdfButton);

    const pdfText = screen.getByText('Official OSF character sheet');
    const createCharText = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
    const createCharButton = createCharText.parentElement;

    expect(pdfText).toBeTruthy();
    expect(createCharButton).toBeTruthy();
    expect(createCharButton).toHaveProperty('disabled', false);
});

test('clicking on Google Sheet button displays appropriate text and enables Create Character button', () => {
    render(<CreateCharacter />);

    const googleSheetButton = screen.getByText(GOOGLE_SHEET_BUTTON_TEXT);
    fireEvent.click(googleSheetButton);

    const googleSheetText = screen.getByText('Homebrew character sheet with dynamic calculation of modifiers');
    const createCharText = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
    const createCharButton = createCharText.parentElement;

    expect(googleSheetText).toBeTruthy();
    expect(createCharButton).toBeTruthy();
    expect(createCharButton).toHaveProperty('disabled', false);
});

test('clicking Create Character button displays appropriate screen', async () => {
    render(<CreateCharacter />);

    const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
    fireEvent.click(pdfButton);

    const createCharButton = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
    fireEvent.click(createCharButton);

    await waitFor(() => {
        const charCreationText = screen.getByText('Character Created Successfully!');
        const createAnotherButton = screen.getByText('Create Another');

        expect(charCreationText).toBeTruthy();
        expect(createAnotherButton).toBeTruthy();
    });
})

test('clicking Create Another button reloads window', async () => {
    render(<CreateCharacter />);

    const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
    fireEvent.click(pdfButton);

    const createCharButton = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
    fireEvent.click(createCharButton);

    await waitFor(() => {
        const createAnotherButton = screen.getByText('Create Another');
        fireEvent.click(createAnotherButton);

        expect(window.location.reload).toHaveBeenCalled();
    });
})