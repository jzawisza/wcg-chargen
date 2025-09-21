import { render, screen, fireEvent, waitFor, act } from "@testing-library/react";
import CreateCharacter from "../CreateCharacter";
import { GoogleOAuthProvider } from "@react-oauth/google";
import * as pdfApi from "../../../server/ServerData";
import { CreateCharacterRequest } from "../../../server/CreateCharacterRequest";
import { HUMAN_SPECIES_INFO } from "../../../constants/SpeciesInfo";
import { CharacterContext } from "../../../Context";
import { EMPTY_ATTRIBUTE_SCORE_OBJ } from "../../../constants/AttributeScoreObject";

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

test('clicking on PDF button displays appropriate text and enables Create Character button', () => {
    render(<GoogleOAuthProvider clientId="">
            <CreateCharacter />
        </GoogleOAuthProvider>
    );

    const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
    fireEvent.click(pdfButton);

    const pdfText = screen.getByText('Official OSF character sheet');
    const createCharText = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
    const createCharButton = createCharText.parentElement;

    expect(pdfText).toBeTruthy();
    expect(createCharButton).toBeTruthy();
    expect(createCharButton).toHaveProperty('disabled', false);
});

test('clicking Create Character button displays appropriate screen', async () => {
    jest.spyOn(pdfApi, 'invokePdfApi').mockReturnValue(Promise.resolve(true));

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

test('clicking Create Another button reloads window', async () => {
    jest.spyOn(pdfApi, 'invokePdfApi').mockReturnValue(Promise.resolve(true));

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

test('clicking Create Character button and getting failure from API displays modal', async () => {
    jest.spyOn(pdfApi, 'invokePdfApi').mockReturnValue(Promise.resolve(false));

    render(<GoogleOAuthProvider clientId="">
            <CreateCharacter />
        </GoogleOAuthProvider>
    );

    const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
    fireEvent.click(pdfButton);

    const createCharButton = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
    fireEvent.click(createCharButton);

    await waitFor(() => {
        const errorModalTitle = screen.getByText('Error creating character sheet');

        expect(errorModalTitle).toBeTruthy();
    });
});

test('character create request sent to server contains fields that should be populated for all characters', async () => {
    expect.assertions(5);

    var expectedCharacterName = 'Test Character';
    var expectedSpeciesName = 'HUMAN';
    var expectedLevel = 1;
    var expectedAttributes = {
        STR: 2,
        COR: 1,
        STA: 2,
        PER: 0,
        INT: 0,
        PRS: 0,
        LUC: -1
    }
    var expectedSpeciesStrength = 'STR';

    var createCharacterContext = {
        charName: expectedCharacterName,
        species: expectedSpeciesName,
        level: expectedLevel,
        attributeScoreObj: expectedAttributes,
        speciesStrengthAttribute: expectedSpeciesStrength,
        charClass: ''
    };

    jest.spyOn(pdfApi, 'invokePdfApi').mockImplementation((request: CreateCharacterRequest, document: Document) => {
        expect(request.characterName).toBe(expectedCharacterName);
        expect(request.species).toBe(expectedSpeciesName);
        expect(request.level).toBe(expectedLevel);
        expect(request.attributes).toEqual(expectedAttributes);
        expect(request.speciesStrength).toBe(expectedSpeciesStrength);

        return Promise.resolve(true);
    });

    render(<GoogleOAuthProvider clientId="">
            <CharacterContext.Provider value={createCharacterContext}>
                <CreateCharacter />
            </CharacterContext.Provider>
        </GoogleOAuthProvider>
    );

    act(() => {
        const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
        fireEvent.click(pdfButton);

        const createCharButton = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
        fireEvent.click(createCharButton);
    });
});

test.each([
    ['human', false],
    ['elf', true]
])('character create request sent to server contains species weakness only for non-human characters', async (species: string, hasWeakness: boolean) => {
    expect.assertions(1);

    var expectedSpeciesWeakness = hasWeakness ? 'STR' : '';

    var createCharacterContext = {
        charName: '',
        species: species,
        level: 1,
        attributeScoreObj: EMPTY_ATTRIBUTE_SCORE_OBJ,
        speciesStrengthAttribute: 'INT',
        speciesWeaknessAttribute: expectedSpeciesWeakness,
        charClass: ''
    };

    jest.spyOn(pdfApi, 'invokePdfApi').mockImplementation((request: CreateCharacterRequest, document: Document) => {
        expect(request.speciesWeakness).toBe(expectedSpeciesWeakness);

        return Promise.resolve(true);
    });

    render(<GoogleOAuthProvider clientId="">
            <CharacterContext.Provider value={createCharacterContext}>
                <CreateCharacter />
            </CharacterContext.Provider>
        </GoogleOAuthProvider>
    );

    act(() => {
        const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
        fireEvent.click(pdfButton);

        const createCharButton = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
        fireEvent.click(createCharButton);
    });
});

test.each([
    ['human', false],
    ['elf', true]
])('character create request sent to server contains expected fields for class characters', async (species: string, hasSkill: boolean) => {
    expect.assertions(4);

    var expectedCharacterClass = 'RANGER';
    var expectedSpeciesSkill = hasSkill ? 'Arcana' : undefined;
    var expectedBonusSkills = ['Languages'];
    var expectedUseQuickGear = true;

    var createCharacterContext = {
        charName: '',
        species: species,
        level: 1,
        attributeScoreObj: EMPTY_ATTRIBUTE_SCORE_OBJ,
        speciesStrengthAttribute: 'INT',
        speciesWeaknessAttribute: '',
        charClass: expectedCharacterClass,
        speciesSkill: expectedSpeciesSkill,
        bonusSkills: expectedBonusSkills,
        useQuickGear: expectedUseQuickGear
    };

    jest.spyOn(pdfApi, 'invokePdfApi').mockImplementation((request: CreateCharacterRequest, document: Document) => {
        expect(request.characterClass).toBe(expectedCharacterClass);
        expect(request.speciesSkill).toBe(expectedSpeciesSkill);
        expect(request.bonusSkills).toEqual(expectedBonusSkills);
        expect(request.useQuickGear).toBe(expectedUseQuickGear);

        return Promise.resolve(true);
    });

    render(<GoogleOAuthProvider clientId="">
            <CharacterContext.Provider value={createCharacterContext}>
                <CreateCharacter />
            </CharacterContext.Provider>
        </GoogleOAuthProvider>
    );

    act(() => {
        const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
        fireEvent.click(pdfButton);

        const createCharButton = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
        fireEvent.click(createCharButton);
    });
});

test('character create request sent to server contains expected fields for commoner characters', async () => {
    expect.assertions(1);

    var createCharacterContext = {
        charName: '',
        species: 'human',
        level: 0,
        attributeScoreObj: EMPTY_ATTRIBUTE_SCORE_OBJ,
        speciesStrengthAttribute: 'INT',
        speciesWeaknessAttribute: '',
        profession: 'Cartwright'
    };

    jest.spyOn(pdfApi, 'invokePdfApi').mockImplementation((request: CreateCharacterRequest, document: Document) => {
        expect(request.profession).toBe(createCharacterContext.profession);

        return Promise.resolve(true);
    });

    render(<GoogleOAuthProvider clientId="">
            <CharacterContext.Provider value={createCharacterContext}>
                <CreateCharacter />
            </CharacterContext.Provider>
        </GoogleOAuthProvider>
    );

    act(() => {
        const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
        fireEvent.click(pdfButton);

        const createCharButton = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
        fireEvent.click(createCharButton);
    });
})

test.each([
    [['Feature 1'], []],
    [[], ['Feature 2']],
    [['Feature 1'], ['Feature 2']]
])('character create request sent to server contains expected features', async (expectedTier1Features: string[], expectedTier2Features: string[]) => {
 expect.assertions(2);

    var createCharacterContext = {
        charName: '',
        species: 'human',
        level: 5,
        attributeScoreObj: EMPTY_ATTRIBUTE_SCORE_OBJ,
        speciesStrengthAttribute: 'INT',
        speciesWeaknessAttribute: '',
        charClass: 'RANGER',
        tier1Features: expectedTier1Features,
        tier2Features: expectedTier2Features
    };

    jest.spyOn(pdfApi, 'invokePdfApi').mockImplementation((request: CreateCharacterRequest, document: Document) => {
        expect(request.features?.tier1).toEqual(expectedTier1Features);
        expect(request.features?.tier2).toEqual(expectedTier2Features);

        return Promise.resolve(true);
    });

    render(<GoogleOAuthProvider clientId="">
            <CharacterContext.Provider value={createCharacterContext}>
                <CreateCharacter />
            </CharacterContext.Provider>
        </GoogleOAuthProvider>
    );

    act(() => {
        const pdfButton = screen.getByText(PDF_BUTTON_TEXT);
        fireEvent.click(pdfButton);

        const createCharButton = screen.getByText(CREATE_CHARACTER_BUTTON_TEXT);
        fireEvent.click(createCharButton);
    });
});
