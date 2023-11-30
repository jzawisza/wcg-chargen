import React, { useContext, useEffect, useState } from "react";
import { Row, Col, Modal, Radio, RadioChangeEvent } from "antd";
import ClickableCard from "../util/cards/ClickableCard";
import AttributeSelector from "../util/AttributeSelector";
import { CharacterContext, NextButtonEnabledContext } from "../../Context";

const Attributes: React.FC = () => {
    const { setNextEnabled } = useContext(NextButtonEnabledContext);
    const { attributeMethod, setAttributeMethod } = useContext(CharacterContext);
    const [ showNotImplementedModal, setShowNotImplementedModal ] = useState(false);

    useEffect(() => {
        setNextEnabled(false);
    }, [setNextEnabled]);

    const hideModal = () => {
        setShowNotImplementedModal(false);
    }

    const onMethodASelect = (e: RadioChangeEvent) => {
        setAttributeMethod(e.target.value);
    };

    if (attributeMethod !== "") {
        return (<AttributeSelector method={attributeMethod} />);
    }

    return (
        <div>
            <p>Choose the appropriate method for attribute selection, based on the input of your GM.</p>

            <Modal title="Not Implemented" open={showNotImplementedModal} onOk={hideModal} onCancel={hideModal} >
                <p>This option has not been implemented yet.  Please choose a different one.</p>
            </Modal>

            <Row justify="center">
                <Col span={8}>
                    <ClickableCard title="Method A"
                        description="Use a standard attribute array for scores.  Choose Challenging or Heroic."
                        className="attributeMethodCard"
                    >
                        <Radio.Group buttonStyle="solid" onChange={onMethodASelect}>
                            <Radio.Button value="a_challenging">Challenging</Radio.Button>
                            <Radio.Button value="a_heroic">Heroic</Radio.Button>
                        </Radio.Group>
                    </ClickableCard>
                </Col>
                <Col span={8}>
                    <div onClick={() => setShowNotImplementedModal(true)}>
                        <ClickableCard title="Method B"
                            description="Scores are generated randomly with no opportunity to make any changes.  Suggested for Wicked Hard Mode."
                            className="attributeMethodCard"
                        />
                    </div>
                </Col>
            </Row>
            <Row justify="center">
                <Col span={8}>
                    <div onClick={() => setShowNotImplementedModal(true)}>
                        <ClickableCard
                            title="Method C"
                            description="Like Method B, but players may re-roll one attribute and swap two scores."
                            className="attributeMethodCard"
                        />
                    </div>
                </Col>
                <Col span={8}>
                    <div onClick={() => setShowNotImplementedModal(true)}>
                        <ClickableCard
                            title="Method D"
                            description="Use attribute array provided by GM."
                            className="attributeMethodCard"
                        />
                    </div>
                </Col>
            </Row>
        </div>);
};

export default Attributes;