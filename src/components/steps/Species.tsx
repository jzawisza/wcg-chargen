import React, { useContext, useEffect } from "react";
import { Col, Row } from "antd";
import SelectableCard from "../util/cards/SelectableCard";
import { CharacterContext, NextButtonEnabledContext } from "../../Context";
import { DWARF_SPECIES_INFO, ELF_SPECIES_INFO, HALFLING_SPECIES_INFO, HUMAN_SPECIES_INFO } from "../../constants/SpeciesInfo";

const dwarfTraits = (
    <ul>
        <li>STR or STA +1</li>
        <li>PRS or LUC -1</li>
        <li><b>Lowlight Vision</b> to Close Range; Blind in Darkness</li>
        <li><b>Magic Spite</b>: Spell Luck saves at Double ADV</li>
        <li><b>One free</b>: Appraisal, Athletics, or Intimidation skill</li>
        <li>Languages: Dwarven and Common</li>
    </ul>
);

const elfTraits = (
    <ul>
        <li>COR or PER +1</li>
        <li>STR or STA -1</li>
        <li><b>Lowlight Vision</b> to Close Range; Blind in Darkness</li>
        <li><b>Aura Sense</b> in Close Zone; Arcana check for detail</li>
        <li><b>One free</b>: Alchemy, Arcana, or Nature skill</li>
        <li>Languages: Elven and Common</li>
    </ul>
);

const halflingTraits = (
    <ul>
        <li>LUC or COR +1</li>
        <li>PER or STR -1</li>
        <li>One <b>extra Fortune Point</b> (minimum start of 1)</li>
        <li><b>One free</b>: Culture, Precise Tasks, or Stealth skill</li>
        <li>Languages: Halfling and Common</li>
    </ul>
);

const humanTraits = (
    <ul>
        <li>+1 to one attribute which is not their highest value</li>
        <li><b>One extra trained skill</b> for free</li>
        <li>Languages: Human, Common, and one other</li>
    </ul>
);

const Species: React.FC = () => {
    const { setNextEnabled } = useContext(NextButtonEnabledContext);
    const { species, setSpecies } = useContext(CharacterContext);

    const dwarfInternalName = DWARF_SPECIES_INFO.internalName;
    const dwarfSpeciesName = DWARF_SPECIES_INFO.speciesName;
    const elfInternalName = ELF_SPECIES_INFO.internalName;
    const elfSpeciesName = ELF_SPECIES_INFO.speciesName;
    const halflingInternalName = HALFLING_SPECIES_INFO.internalName;
    const halflingSpeciesName = HALFLING_SPECIES_INFO.speciesName;
    const humanInternalName = HUMAN_SPECIES_INFO.internalName;
    const humanSpeciesName = HUMAN_SPECIES_INFO.speciesName;

    useEffect(() => {
        setNextEnabled(species !== '');
    }, []);

    const onSpeciesCardClick = (speciesName: string) => {
        setSpecies(speciesName);
        setNextEnabled(true);
    };

    return (
        <div>
            <p>Select the species you want to play as, and then click Next to proceed.</p>
            <p>Hover over or click the information icons for more details on the features of each species.</p>
            <Row justify="center">
                <div className="selectableCardWrapper" onClick={() => onSpeciesCardClick(dwarfInternalName)}>
                    <Col span={8}>
                        <SelectableCard title={dwarfSpeciesName}
                            className="speciesCard"
                            description="Dwarves are tough, stocky, bearded, gruff, naturally magic resistant creatures just under five feet tall."
                            features={dwarfTraits}
                            selected={species === dwarfInternalName}
                        />
                    </Col>
                </div>
                <div className="selectableCardWrapper" onClick={() => onSpeciesCardClick(elfInternalName)}>
                    <Col span={8}>
                        <SelectableCard title={elfSpeciesName}
                            className="speciesCard"
                            description="Slender, patient, graceful, sylvan-dwelling artisans with an affinity for magic, elves sometimes live for two centuries."
                            features={elfTraits}
                            selected={species === elfInternalName}
                        />
                    </Col>
                </div>
            </Row>
            <Row justify="center">
                <div className="selectableCardWrapper" onClick={() => onSpeciesCardClick(halflingInternalName)}>
                    <Col span={8}>
                        <SelectableCard title={halflingSpeciesName}
                            className="speciesCard"
                            description="Halflings are short, lighthearted, stealthy, comfort loving, nimble folk.   Sporting a slight build, they rarely reach four feet."
                            features={halflingTraits}
                            selected={species === halflingInternalName}
                        />
                    </Col>
                </div>
                <div className="selectableCardWrapper" onClick={() => onSpeciesCardClick(humanInternalName)}>
                    <Col span={8}>
                        <SelectableCard title={humanSpeciesName}
                            className="speciesCard"
                            description="The most common character species, humans are adaptable, ambitious, resilient folk."
                            features={humanTraits}
                            selected={species === humanInternalName}
                        />
                    </Col>
                </div>
            </Row>
        </div>
    );
};

export default Species;