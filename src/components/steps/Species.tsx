import React, { useContext } from "react";
import { Col, Row } from "antd";
import SpeciesCard from "./SpeciesCard";
import { CharacterContext, NextButtonEnabledContext } from "../../Context";

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

    const onSpeciesCardClick = (speciesName: string) => {
        setSpecies(speciesName);
        setNextEnabled(true);
    };

    return (
        <div>
            <p>Select the species you want to play as, and then click Next to proceed.</p>
            <p>Hover over or click the information icons for more details on the traits of each species.</p>
            <Row justify="center">
                <div className="speciesCardWrapper" onClick={() => onSpeciesCardClick('dwarf')}>
                    <Col span={8}>
                        <SpeciesCard species="Dwarf"
                            description="Dwarves are tough, stocky, bearded, gruff, naturally magic resistant creatures just under five feet tall."
                            traits={dwarfTraits}
                            selected={species === 'dwarf'}
                        />
                    </Col>
                </div>
                <div className="speciesCardWrapper" onClick={() => onSpeciesCardClick('elf')}>
                    <Col span={8}>
                        <SpeciesCard species="Elf"
                            description="Slender, patient, graceful, sylvan-dwelling artisans with an affinity for magic, elves sometimes live for two centuries."
                            traits={elfTraits}
                            selected={species === 'elf'}
                        />
                    </Col>
                </div>
            </Row>
            <Row justify="center">
                <div className="speciesCardWrapper" onClick={() => onSpeciesCardClick('halfling')}>
                    <Col span={8}>
                        <SpeciesCard species="Halfling"
                            description="Halflings are short, lighthearted, stealthy, comfort loving, nimble folk.   Sporting a slight build, they rarely reach four feet."
                            traits={halflingTraits}
                            selected={species === 'halfling'}
                        />
                    </Col>
                </div>
                <div className="speciesCardWrapper" onClick={() => onSpeciesCardClick('human')}>
                    <Col span={8}>
                        <SpeciesCard species="Human"
                            description="The most common character species, humans are adaptable, ambitious, resilient folk."
                            traits={humanTraits}
                            selected={species === 'human'}
                        />
                    </Col>
                </div>
            </Row>
        </div>
    );
};

export default Species;