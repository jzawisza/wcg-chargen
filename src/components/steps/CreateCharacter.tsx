import React, { MouseEvent, useState } from "react";
import { Radio, RadioChangeEvent, Button, Result, Input, Space } from "antd";

const PDF_SHEET_TYPE = 'pdf';
const GOOGLE_SHEETS_SHEET_TYPE='googlesheets';

const CreateCharacter: React.FC = () => {
    const [charName, setCharName] = useState('');
    const [charSheetType, setCharSheetType] = useState<string | null>(null);
    const [charGenerated, setCharGenerated] = useState(false);

    const onInputCharacterName = (e: React.ChangeEvent<HTMLInputElement>) => {
        setCharName(e.target.value);
    };

    const onRadioGroupChange = (e: RadioChangeEvent) => {
        setCharSheetType(e.target.value);
    };

    const onCreateCharacter = (e: MouseEvent<HTMLElement>) => {
        setCharGenerated(true);
    };

    // Display Result control showing status of operation if the operation has been performed
    if (charGenerated) {
        return (
            <Result
                status="success"
                title="Character Created Successfully!"
                subTitle="Click on the Create Another button if you would like to create another character sheet.  For Wicked Hard Mode, you must create three characters."
                extra={[
                    // TODO: replace window.location.reload with a smarter state update
                    <Button type="primary" key="newCharacter" onClick={() => window.location.reload()}>
                        Create Another
                    </Button>
                ]}
            />
        );
    }

    // Otherwise, show the base screen
    return (
        <div>
            <p>Now that you have designed your character, the last step is to give them a name.  Enter a name for your character.  Once you have done this, the buttons below will be activated.</p>
            <Space>
                <b>Character name:</b>
                <Input onChange={onInputCharacterName}/>
            </Space>
            <div className="charSheetSelectorArea">
            <p>Once these buttons are activated, choose the type of character sheet you want to create.  This will reveal the Create Character button.  Press this button to create your character.</p>
            <p>Since this is just a mockup, <b>character creation is not working yet.</b>  Stay tuned...</p>
                <div className="charSheetButtonCenter">
                    <Radio.Group buttonStyle="solid" onChange={onRadioGroupChange} value={charSheetType} disabled={charName === ''}>
                        <Radio.Button value={PDF_SHEET_TYPE}>PDF</Radio.Button>
                        <Radio.Button value={GOOGLE_SHEETS_SHEET_TYPE}>Google Sheet</Radio.Button>
                    </Radio.Group>
                    {charSheetType === PDF_SHEET_TYPE && (
                        <p className="charSheetDescription">Official OSF character sheet</p>
                    )}
                    {charSheetType === GOOGLE_SHEETS_SHEET_TYPE && (
                        <p className="charSheetDescription">Homebrew character sheet with dynamic calculation of modifiers</p>
                    )}
                    {charSheetType !== null && (
                        <div className="createCharButton">
                            <Button type="primary" onClick={onCreateCharacter}>Create Character</Button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default CreateCharacter;