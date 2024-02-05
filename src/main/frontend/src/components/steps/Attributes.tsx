import React, { useContext, useEffect } from "react";
import { Row, Col } from "antd";
import ClickableCard from "../util/cards/ClickableCard";
import AttributeSelector from "../util/attributes/AttributeSelector";
import { CharacterContext, NextButtonEnabledContext } from "../../Context";
import AttributeArrayType from "../../constants/AttributeArrayType";

const Attributes: React.FC = () => {
    const { setNextEnabled } = useContext(NextButtonEnabledContext);
    const { level, attributeArrayType, setAttributeArrayType } = useContext(CharacterContext);

    useEffect(() => {
        // This should NOT be optimized to setNextEnabled(level > 0 && attributeArrayType === "")
        // The desired behavior is to not call setNextEnabled at all if the condition is false
        if (level > 0 && attributeArrayType === "") {
            setNextEnabled(false);
        }
    }, [level, attributeArrayType, setNextEnabled]);

    if (level === 0) {
        return (<AttributeSelector />);
    }
    else if (level > 0 && attributeArrayType !== "") {
        return (<AttributeSelector arrayType={attributeArrayType} />); 
    }

    return (
        <div>
            <p>Attribute score selection is done via standard attribute arrays.  Based on the input of your GM, select which type of attribute array to use.</p>
            <Row justify="center">
                <Col span={8}>
                    <div onClick={() => setAttributeArrayType(AttributeArrayType.CHALLENGING.toString())}>
                        <ClickableCard title="Challenging"
                            description="Values: +2, +1, +1, 0, 0, -1, -2"
                            className="attributeTypeCard"
                        />
                    </div>
                </Col>
                <Col span={8}>
                    <div onClick={() => setAttributeArrayType(AttributeArrayType.HEROIC.toString())}>
                        <ClickableCard title="Heroic"
                            description="Values: +2, +2, +1, 0, 0, 0, -1"
                            className="attributeTypeCard"
                        />
                    </div>
                </Col>
            </Row>
        </div>);
};

export default Attributes;