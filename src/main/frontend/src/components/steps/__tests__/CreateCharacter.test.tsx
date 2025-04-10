import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import CreateCharacter from "../CreateCharacter";
import { GoogleOAuthProvider, useGoogleLogin } from "@react-oauth/google";
import { ReactNode } from "react";

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
    render(<GoogleOAuthProvider clientId="">
            <CreateCharacter />
        </GoogleOAuthProvider>
    );

    const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
    const googleSheetButton = screen.getByText(GOOGLE_SHEET_BUTTON_TEXT);

    expect(pdfButton).toBeTruthy();
    expect(googleSheetButton).toBeTruthy();
});

test('initial load displays Create Character button as disabled', () => {
    render(<GoogleOAuthProvider clientId="">
            <CreateCharacter />
        </GoogleOAuthProvider>
)   ;

    const createCharText = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
    const createCharButton = createCharText.parentElement;

    expect(createCharButton).toBeTruthy();
    expect(createCharButton).toHaveProperty('disabled', true);
});

test('PDF button is disabled for the time being', () => {
    render(<GoogleOAuthProvider clientId="">
            <CreateCharacter />
        </GoogleOAuthProvider>
    );

    const pdfButton = screen.getByText(PDF_BUTTON_TEXT);

    expect(pdfButton).toBeTruthy();
    expect(pdfButton.previousElementSibling?.classList.contains('ant-radio-button-disabled')).toBeTruthy();
});

test('clicking on Google Sheet button displays appropriate text and enables Create Character button', () => {
    render(<GoogleOAuthProvider clientId="">
            <CreateCharacter />
        </GoogleOAuthProvider>
    );

    const googleSheetButton = screen.getByText(GOOGLE_SHEET_BUTTON_TEXT);
    fireEvent.click(googleSheetButton);

    const googleSheetText = screen.getByText('Homebrew character sheet with dynamic calculation of modifiers');
    const createCharText = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
    const createCharButton = createCharText.parentElement;

    expect(googleSheetText).toBeTruthy();
    expect(createCharButton).toBeTruthy();
    expect(createCharButton).toHaveProperty('disabled', false);
});

test.skip('clicking Create Character button displays appropriate screen', async () => {
    render(<GoogleOAuthProvider clientId="">
            <CreateCharacter />
        </GoogleOAuthProvider>
    );

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

test.skip('clicking Create Another button reloads window', async () => {
    render(<GoogleOAuthProvider clientId="">
            <CreateCharacter />
        </GoogleOAuthProvider>
    );

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