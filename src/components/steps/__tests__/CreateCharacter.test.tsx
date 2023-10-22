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

test('initial load displays buttons for choosing character sheet type, but in disabled state', () => {
    render(<CreateCharacter />);

    const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
    const googleSheetButton = screen.getByText(GOOGLE_SHEET_BUTTON_TEXT);

    expect(pdfButton).toBeTruthy();
    expect(googleSheetButton).toBeTruthy();

    const pdfButtonParent = pdfButton.parentElement;
    const googleSheetButtonParent = googleSheetButton.parentElement;

    expect(pdfButtonParent).toHaveClass('ant-radio-button-wrapper-disabled');
    expect(googleSheetButtonParent).toHaveClass('ant-radio-button-wrapper-disabled');
});

test('initial load displays character name input box and input box descriptive text', () => {
    render(<CreateCharacter />);

    const charNameText = screen.getByText('Character name:');
    expect(charNameText).toBeTruthy();

    const inputNode = screen.getByRole('textbox', {name: ''});
    expect(inputNode).toBeTruthy();
});

test('initial load does not display Create Character button', () => {
    render(<CreateCharacter />);

    expect(() => screen.getByText('CreateCharacter')).toThrow();
})

test('entering text in character name input box enables character sheet type buttons', () => {
    render(<CreateCharacter />);

    const inputNode = screen.getByRole('textbox', {name: ''});
    fireEvent.change(inputNode, {target: {value: 'MyCharacter'}});

    const pdfButtonParent = screen.getByText(PDF_BUTTON_TEXT).parentElement;
    const googleSheetButtonParent = screen.getByText(GOOGLE_SHEET_BUTTON_TEXT).parentElement;

    expect(pdfButtonParent).not.toHaveClass('ant-radio-button-wrapper-disabled');
    expect(googleSheetButtonParent).not.toHaveClass('ant-radio-button-wrapper-disabled');    
});

test('entering and then deleting text from character name input box disables character sheet type buttons', async () => {
    render(<CreateCharacter />);

    const inputNode = screen.getByRole('textbox', {name: ''});
    fireEvent.change(inputNode, {target: {value: 'MyCharacter'}});
    fireEvent.change(inputNode, {target: {value: ''}});

    await waitFor(() => {
        const pdfButtonParent = screen.getByText(PDF_BUTTON_TEXT).parentElement;
        const googleSheetButtonParent = screen.getByText(GOOGLE_SHEET_BUTTON_TEXT).parentElement;

        expect(pdfButtonParent).toHaveClass('ant-radio-button-wrapper-disabled');
        expect(googleSheetButtonParent).toHaveClass('ant-radio-button-wrapper-disabled');
    });
});

test('clicking on PDF button displays appropriate text and Create Character button', () => {
    render(<CreateCharacter />);

    // Enable PDF button
    const inputNode = screen.getByRole('textbox', {name: ''});
    fireEvent.change(inputNode, {target: {value: 'MyCharacter'}});

    const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
    fireEvent.click(pdfButton);

    const pdfText = screen.getByText('Official OSF character sheet');
    const createCharButton = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);

    expect(pdfText).toBeTruthy();
    expect(createCharButton).toBeTruthy();
});

test('clicking on Google Sheet button displays appropriate text and Create Character button', () => {
    render(<CreateCharacter />);

    // Enable Google Sheet button
    const inputNode = screen.getByRole('textbox', {name: ''});
    fireEvent.change(inputNode, {target: {value: 'MyCharacter'}});

    const googleSheetButton = screen.getByText(GOOGLE_SHEET_BUTTON_TEXT);
    fireEvent.click(googleSheetButton);

    const googleSheetText = screen.getByText('Homebrew character sheet with dynamic calculation of modifiers');
    const createCharButton = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);

    expect(googleSheetText).toBeTruthy();
    expect(createCharButton).toBeTruthy();
});

test('clicking Create Character button displays appropriate screen', async () => {
    render(<CreateCharacter />);

    // Enable PDF button
    const inputNode = screen.getByRole('textbox', {name: ''});
    fireEvent.change(inputNode, {target: {value: 'MyCharacter'}});

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

    // Enable PDF button
    const inputNode = screen.getByRole('textbox', {name: ''});
    fireEvent.change(inputNode, {target: {value: 'MyCharacter'}});

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