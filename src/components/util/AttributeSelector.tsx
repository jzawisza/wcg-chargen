import { useContext, useState } from "react";
import { Radio, RadioChangeEvent, Row, Col, Modal } from "antd";
import { DefaultOptionType } from "antd/es/select";
import { CharacterContext } from "../../Context";
import { DWARF_SPECIES_INFO, ELF_SPECIES_INFO, HALFLING_SPECIES_INFO, getIsHuman, getPluralSpeciesNameFromVariable } from "../../constants/SpeciesInfo";
import SelectMultiple from "./SelectMultiple";
import AttributeScoreSelector from "./AttributeScoreSelector";
import { ATTRIBUTE_ARRAY_SIZE, getArrayByName } from "../../constants/AttributeArrayType";

// Return a two-element array containing the strengths for a given non-human species
function getStrengths(species: string) {
    switch (species) {
        case DWARF_SPECIES_INFO.internalName:
            return ["STR", "STA"];
        case ELF_SPECIES_INFO.internalName:
            return ["COR", "PER"];
        case HALFLING_SPECIES_INFO.internalName:
            return ["LUC", "COR"];
        default:
            return [];
    }
}

// Return a two-element array containing the weaknesses for a given non-human species
function getWeaknesses(species: string) {
    switch (species) {
        case DWARF_SPECIES_INFO.internalName:
            return ["PRS", "LUC"];
        case ELF_SPECIES_INFO.internalName:
            return ["STR", "STA"];
        case HALFLING_SPECIES_INFO.internalName:
            return ["PER", "STR"];
        default:
            return [];
    }
}

const attributeNames: DefaultOptionType[] = [
    { value: 'STR', label: 'STR' },
    { value: 'COR', label: 'COR' },
    { value: 'STA', label: 'STA' },
    { value: 'PER', label: 'PER' },
    { value: 'INT', label: 'INT' },
    { value: 'PRS', label: 'PRS' },
    { value: 'LUC', label: 'LUC' },
];

type AttributeSelectorProps = {
    arrayType?: string
};

const AttributeSelector = (props: AttributeSelectorProps) => {
    const [ humanStrength, setHumanStrength ] = useState<string[]>([]);
    const [ showHelpModal, setShowHelpModal ] = useState(false);
    const {species, level} = useContext(CharacterContext);

    const isHuman = getIsHuman(species);
    const isTraditionalMode = (level > 0);

    const hideHelpModal = () => {
        setShowHelpModal(false);
    };

    const onNonHumanStrengthChange = (e: RadioChangeEvent) => {

    };

    const onNonHumanWeaknessChange = (e: RadioChangeEvent) => {

    };

    const onHumanStrengthChange = (value: string[]) => {
        setHumanStrength(value);
    };

    let nonHumanStrengthDesc = "";
    let nonHumanWeaknessDesc = "";
    if (!isHuman) {
        let strengths = getStrengths(species);
        let weaknesses = getWeaknesses(species);

        nonHumanStrengthDesc = `${strengths[0]} or ${strengths[1]}`;
        nonHumanWeaknessDesc = `${weaknesses[0]} or ${weaknesses[1]}`;
    }

    let attributeValues: number[] = [];
    // Look up standard attribute arrays for Traditional Mode
    if (isTraditionalMode && props.arrayType) {
        let attributeArray = getArrayByName(props.arrayType)?.array;
        attributeValues = (attributeArray ? attributeArray : []);
    }
    // Generate random values for Wicked Hard Mode
    else {
        for (let i = 0; i < ATTRIBUTE_ARRAY_SIZE; i++) {
            // Generate random numbers between -3 and 3 by generating two random numbers between 1 and 4,
            // and subtracting the first from the second.
            // This makes the distribution into a bell curve.
            attributeValues.push(Math.ceil(Math.random() * 3) - Math.ceil(Math.random() * 3));
        }
    }

    return (
        <div>
            <Modal title="Selecting Attribute Values" open={showHelpModal} onOk={hideHelpModal} onCancel={hideHelpModal}>
                TODO: text goes here
            </Modal>

            {isTraditionalMode ?
                <>
                <p>Drag and drop the values below into the Scores column to assign scores for the seven attributes used in this system.</p>
                <p>Once you've finished that, you'll be able to choose species-specific strengths and weaknesses related to your attributes.</p>
                <p><a href="#" onClick={() => setShowHelpModal(true)}>What attribute values should I choose?</a></p>
                </>
                : <>
                <p>In Wicked Hard mode, attribute scores are randomly generated over a normal distribution, i.e a bell curve.</p>
                <p>The scores below have been generated for you.  To continue, select your species-specfic strength {!isHuman && "and weakness"} below.</p>
                </>
            }
            <Row>
                <Col span={12}>
                    <AttributeScoreSelector values={attributeValues} canSelectValues={isTraditionalMode} />
                </Col>
                <Col span={12}>
                    {!isHuman &&
                        <div>
                            <p>Select which species-specific strength and weakness you want to choose for your character.</p>
                            <p>{getPluralSpeciesNameFromVariable(species)} get a +1 to either {nonHumanStrengthDesc}, and a -1 to either {nonHumanWeaknessDesc}.</p>
                            <Row>
                                <Col>
                                    <h3>Strength (+1)</h3>
                                    <Radio.Group buttonStyle="solid" onChange={onNonHumanStrengthChange}>
                                        {getStrengths(species).map(x => (<Radio.Button value={x}>{x}</Radio.Button>))}
                                    </Radio.Group>
                                </Col>
                                <Col>
                                    <h3>Weakness (-1)</h3>
                                    <Radio.Group buttonStyle="solid" onChange={onNonHumanWeaknessChange}>
                                        {getWeaknesses(species).map(x => (<Radio.Button value={x}>{x}</Radio.Button>))}
                                    </Radio.Group>
                                </Col>
                            </Row>
                        </div>
                    }
                    {isHuman &&
                        <div>
                            <p>Select which attribute you want to boost.</p>
                            <p>Humans get a +1 to any attribute which is not the character's highest.</p>
                            <SelectMultiple
                                defaultValue={humanStrength}
                                numElementsAllowed={1}
                                onChange={onHumanStrengthChange}
                                options={attributeNames}
                                placeholder="Select attribute to boost" 
                            />
                        </div>
                    }
                </Col>
            </Row>
        </div>
    );
};

export default AttributeSelector;