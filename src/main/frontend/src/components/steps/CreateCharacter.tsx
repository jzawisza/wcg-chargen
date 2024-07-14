import React, { MouseEvent, useState } from "react";
import { Radio, RadioChangeEvent, Button, Result } from "antd";

const PDF_SHEET_TYPE = 'pdf';
const GOOGLE_SHEETS_SHEET_TYPE='googlesheets';

const CreateCharacter: React.FC = () => {
    const [charSheetType, setCharSheetType] = useState<string | null>(null);
    const [charGenerated, setCharGenerated] = useState(false);

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
            <div className="charSheetSelectorArea">
            <p>Choose the type of character sheet you want to create, and then click on the Create Character button to generate your character.</p>
            <p>Since this is just a mockup, <b>character creation is not working yet.</b>  Stay tuned...</p>
                <div className="charSheetButtonCenter">
                    <Radio.Group buttonStyle="solid" onChange={onRadioGroupChange} value={charSheetType}>
                        <Radio.Button value={PDF_SHEET_TYPE}>PDF</Radio.Button>
                        <Radio.Button value={GOOGLE_SHEETS_SHEET_TYPE}>Google Sheet</Radio.Button>
                    </Radio.Group>
                    {charSheetType === PDF_SHEET_TYPE && (
                        <p className="charSheetDescription">Official OSF character sheet</p>
                    )}
                    {charSheetType === GOOGLE_SHEETS_SHEET_TYPE && (
                        <p className="charSheetDescription">Homebrew character sheet with dynamic calculation of modifiers</p>
                    )}
                    <div className="createCharButton">
                        <Button
                            type="primary"
                            disabled={charSheetType == null}
                            onClick={onCreateCharacter}>
                            Create Character
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CreateCharacter;