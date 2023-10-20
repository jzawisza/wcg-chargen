import React, { useContext, useEffect } from "react";
import { NextButtonEnabledContext, CharacterContext } from "../../Context";
import { Row, Col, Checkbox, Popover } from "antd";
import { InfoCircleOutlined } from '@ant-design/icons';
import SelectableCard from "../cards/SelectableCard";

const berzerkerFeatures = (
    <ul>
        <li><b>Rage</b> once per day for one entire combat for the following effects:</li>
        <ul>
            <li>Advantage on Damage, STR, & STA checks</li>
            <li>Immune to mind controlling magic</li>
            <li>When Rage ends, take a quick rest or suffer Fatigue (-2 to checks until rest is achieved)</li>
        </ul>
    <li><b>Fight On:</b> If reduced to 0 HP or less, can fight for 1d4 rds, then go unconscious at round’s end for Bleed Out.</li>
    </ul>
);

const mageFeatures = (
    <ul>
        <li><b>May cast spells of character level or less</b>. GM gives two cantrips & two Level 1 spells.</li>
        <li><b>Aura Sense:</b> See most magical auras at Close range. Arcana roll for more detail.</li>
        <li>Can read & use magic spell scrolls.</li>
    </ul>
);

const mysticFeatures = (
    <ul>
        <li>When attacking unarmed, receive the following effects:</li>
        <ul>
            <li><b>Precise Strike:</b> 1d6 damage ignores first 3 DA</li>
            <li><b>Natural 19-20 to hit stuns</b> foes made of flesh until the end of next round.</li>
        </ul>
        <li>Advantage to any checks to <b>resist poisons & diseases.</b></li>
        <li>Immune to stun from other mystics.</li>
    </ul>
);

const rangerFeatures = (
    <ul>
        <li>When in nature, outside of humanoid settlements, receive the following effects:</li>
        <ul>
            <li>Ignore terrain movement penalties</li>
            <li>Gain Advantage on weather related resistance rolls</li>
            <li>When traveling alone in wilderness, double speed</li>
        </ul>
        <li><b>May use second light weapon off-hand to parry</b> adding +1 to Evasion or as a thrown weapon bonus attack</li>
    </ul>
);

const rogueFeatures = (
    <ul>
        <li>May <b>Backstab</b> when attacking undetected (melee or ranged) for the following effects:</li>
        <ul>
            <li>Advantage to attack</li>
            <li>Roll an extra damage die, add to normal die, & then add other damage modifiers.  Back stab damage dice may explode.</li>
            <li>May <b>not</b> Backstab if any situation grants Disadvantage</li>
        </ul>
        <li>Proficient in the secret communication of burglars, <b>Thieves’ Cant</b>, using code words & hand gestures.</li>
    </ul>
);

const shamanFeatures = (
    <ul>
        <li><b>May cast spells of persona level or less</b>. GM gives two Level 1 spells. Spirits teach spells as PC gains a level.</li>
        <li><b>Animal Affinity:</b> Advantage on reaction/social animal encounters unless animal is provoked or spellbound.</li>
        <li><b>Spirit Sense:</b> See into the spirit realm in Close zone. Religion roll for detail.</li>
    </ul>
);

const skaldFeatures = (
    <ul>
        <li><b>Perform magic song</b> during travel or rest (not combat):</li>
        <ul>
            <li><b>War Song/Chant:</b> +1 to hit and +1 to Initiative for allies during the first combat within the hour</li>
            <li><b>Enthralling Performance:</b> On success, hold listener’s attention giving Disadvantage to PER or add +2 to PRS social checks for 1 hour</li>
        </ul>
        <li><b>Forgotten Lore:</b> Skald has knowledge of unique/obscure persons, places, and items.</li>
        <li><b>Jack of All Trades:</b> +2 to untrained skill rolls.  Rolls are still at Disadvantage.</li>
    </ul>
);

const warriorFeatures = (
    <ul>
        <li><b>Second Wind:</b> During a quick rest once/day, recover 2 HP or AP + STA (min. 2) instead of 1.</li>
        <li><b>Hit Wicked Hard:</b> On a successful melee weapon hit, may break weapon to roll damage at Double Advantage.</li>
    </ul>
);

const quickGearExplanation = (
    <div>
        <p>Each character class has <b>quick gear</b> associated with it, i.e. a basic set of equipment suitable for an adventurer.  This includes weapons, armor, supplies, and money.</p>
        <p>If you check this checkbox, the quick gear for the character class you select will be added to your character sheet.</p>
    </div>
);

const CharacterClass: React.FC = () => {
    const { setNextEnabled } = useContext(NextButtonEnabledContext);
    const { charClass, setCharClass } = useContext(CharacterContext);

    useEffect(() => {
        setNextEnabled(charClass !== '');
    }, []);

    const onCharClassCardClick = (charClassName: string) => {
        setCharClass(charClassName);
        setNextEnabled(true);
    };

    return (
        <div>
            <p>Select the character class you want to play as, and then click Next to proceed.</p>
            <p>Each class has a particular set of skills which they use during an adventure.  It is advisable for a party to have a diverse set of classes, but any and all classes can be invaluable.</p>
            <p>Hover over or click the information icons for more details on the features of each class.</p>
            <div  className="quickGearCheckboxCenter">
                <Checkbox>Use Quick Gear</Checkbox>
                <Popover content={quickGearExplanation} title="About Quick Gear">
                    <InfoCircleOutlined />
                </Popover>
            </div>
            <Row justify="center">
                <div className="selectableCardWrapper" onClick={() => onCharClassCardClick('berzerker')}>
                    <Col span={8}>
                        <SelectableCard title="Berzerker"
                            className="charClassCard"
                            description="Fearless fighters longing to taste the blood rapture of battle fury."
                            features={berzerkerFeatures}
                            selected={charClass === 'berzerker'}
                        />
                    </Col>
                </div>
                <div className="selectableCardWrapper" onClick={() => onCharClassCardClick('mage')}>
                    <Col span={8}>
                        <SelectableCard title="Mage"
                            className="charClassCard"
                            description="Studious, sorcerous spell-casters risking essence to probe ancient secrets and acquire hidden lore."
                            features={mageFeatures}
                            selected={charClass === 'mage'}
                        />
                    </Col>
                </div>
            </Row>
            <Row justify="center">
                <div className="selectableCardWrapper" onClick={() => onCharClassCardClick('mystic')}>
                    <Col span={8}>
                        <SelectableCard title="Mystic"
                            className="charClassCard"
                            description="Disciplined, spiritual martial artists excelling in unarmed combat & preternatural athletic prowess."
                            features={mysticFeatures}
                            selected={charClass === 'mystic'}
                        />
                    </Col>
                </div>
                <div className="selectableCardWrapper" onClick={() => onCharClassCardClick('ranger')}>
                    <Col span={8}>
                        <SelectableCard title="Ranger"
                            className="charClassCard"
                            description="Woodland fighters sworn to protect forest and field."
                            features={rangerFeatures}
                            selected={charClass === 'ranger'}
                        />
                    </Col>
                </div>
            </Row>
            <Row justify="center">
                <div className="selectableCardWrapper" onClick={() => onCharClassCardClick('rogue')}>
                    <Col span={8}>
                        <SelectableCard title="Rogue"
                            className="charClassCard"
                            description="Stealthy criminals and rascals living by their wits, often fighting dirty when cornered."
                            features={rogueFeatures}
                            selected={charClass === 'rogue'}
                        />
                    </Col>
                </div>
                <div className="selectableCardWrapper" onClick={() => onCharClassCardClick('shaman')}>
                    <Col span={8}>
                        <SelectableCard title="Shaman"
                            className="charClassCard"
                            description="Spell users, healers, hexers, and religious leaders who interact with nature and the animal spirit world."
                            features={shamanFeatures}
                            selected={charClass === 'shaman'}
                        />
                    </Col>
                </div>
            </Row>
            <Row justify="center">
                <div className="selectableCardWrapper" onClick={() => onCharClassCardClick('skald')}>
                    <Col span={8}>
                        <SelectableCard title="Skald"
                            className="charClassCard"
                            description="Traveling storytellers, poets, and performers dabbling in all manner of knowledge."
                            features={skaldFeatures}
                            selected={charClass === 'skald'}
                        />
                    </Col>
                </div>
                <div className="selectableCardWrapper" onClick={() => onCharClassCardClick('warrior')}>
                    <Col span={8}>
                        <SelectableCard title="Warrior"
                            className="charClassCard"
                            description="Highly skilled combatants on the ground and on horseback."
                            features={warriorFeatures}
                            selected={charClass === 'warrior'}
                        />
                    </Col>
                </div>
            </Row>
        </div>
    );
};

export default CharacterClass;