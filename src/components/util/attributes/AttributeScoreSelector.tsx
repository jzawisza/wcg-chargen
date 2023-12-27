import { useContext, useEffect } from "react";
import { Button } from "antd";
import { ReloadOutlined } from '@ant-design/icons';
import { DndContext, DragEndEvent } from "@dnd-kit/core";
import AttributeDndScoreCell from "./AttributeDndScoreCell";
import AttributeDndValueCell from "./AttributeDndValueCell";
import { ScoreStyle } from "../../../constants/AttributeScoreStyle";
import { AttributeScoreObject, emptyAttributeScoreObj } from "../../../constants/AttributeScoreObject";
import { CharacterContext } from "../../../Context";

type AttributeScoreSelectorProps = {
    initialValues: number[];
    canSelectValues: boolean;
};

// Generate a table cell containing an attribute score that is not changeable
function generateStaticTableRow(score: number | null) {
        return (
                <td className="attributeTableGeneralCell">
                    <div style={ScoreStyle}>
                        {score}
                    </div>
                </td>
        );
}

// Generate a table row with all data for a given attribute.
// For Traditional Mode, show four columns:
//   1) Attribute name
//   2) Attribute scores: empty initially, populated by dragging from #4
//   3) Empty column for spacing
//   4) Attribute score values which can be dragged to #2
function generateDndTableRow(attributeScores: AttributeScoreObject,
    attributeValueArray: (number | null)[],
    attributeShortName: keyof AttributeScoreObject,
    index: number) {
        const attributeScore = attributeScores[attributeShortName];
        const valueScore = attributeValueArray[index];

        return (<>
                <AttributeDndScoreCell attributeShortName={attributeShortName} score={attributeScore} />
                <td className="attributeTableGeneralCell" />
                <AttributeDndValueCell index={index} score={valueScore} />
            </>);
}

const AttributeScoreSelector = (props: AttributeScoreSelectorProps) => {
    const { attributeScoreObj, setAttributeScoreObj, attributeValues, setAttributeValues } = useContext(CharacterContext);
    
    useEffect(() => {
        // Initialize attributeValues in character context iff it's not already initialized
        if (attributeValues.length === 0) {
            setAttributeValues(props.initialValues);
        }
        // Construct AttributeScoreObj if we're using Wicked Hard mode and scores are static,
        // and if we haven't already initialized it
        if (!props.canSelectValues && Object.values(attributeScoreObj).every(x => x === null)) {
            const wickedHardAttributeScoreObj = {
                STR: props.initialValues[0],
                COR: props.initialValues[1],
                STA: props.initialValues[2],
                PER: props.initialValues[3],
                INT: props.initialValues[4],
                PRS: props.initialValues[5],
                LUC: props.initialValues[6]
            };
            setAttributeScoreObj(wickedHardAttributeScoreObj);
        }
    }, [attributeValues, setAttributeValues, attributeScoreObj, setAttributeScoreObj,
        props.initialValues, props.canSelectValues]);

    const handleDragEnd = (e: DragEndEvent) => {
        const {active, over} = e;

        // Don't do anything unless we actually land on a droppable element
        if (over) {
            // The following code assumes draggable IDs of the format "draggable<VALUE>"
            // and droppable IDs of the format "droppable<VALUE>"
            const valuePos = active.id.toString().slice(9);
            const attribute = over.id.toString().slice(9) as keyof AttributeScoreObject;

            // Only allow drag and drop if attribute score is not yet set
            if (attributeScoreObj[attribute] === null) {
                // Copy the old attribute score object, and update it based on what was dropped on it
                // Use the spread operator to do the copy in order to trigger a state update and re-render
                const attributeScoreObjModified = {...attributeScoreObj};
                attributeScoreObjModified[attribute] = attributeValues[+valuePos];

                // Copy the old attributeValues, and update it based on what was dragged to the Score column
                // Use the spread operator to do the copy in order to trigger a state update and re-render
                const attributeValuesModified = [...attributeValues];
                attributeValuesModified[+valuePos] = null;

                // Update with the new object references
                setAttributeScoreObj(attributeScoreObjModified);
                setAttributeValues(attributeValuesModified);
            }
        }
    };

    const resetValues = () => {
        setAttributeScoreObj(emptyAttributeScoreObj);
        setAttributeValues(props.initialValues);
    }

    // Use a HTML table to represent the attributes, the draggable score values,
    // and the places where those scores can be dropped
    return (
        <DndContext onDragEnd={handleDragEnd}>
            <table className="attributeTable">
                <thead>
                    <tr>
                        <th className="attributeTableHeaderCell">Attribute</th>
                        <th className="attributeTableHeaderCell">Score</th>
                        {props.canSelectValues &&
                            <>
                            <th className="attributeTableHeaderCell" />
                            <th className="attributeTableHeaderCell">Value</th>
                            </>
                        }
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td className="attributeTableAttributeCell">Strength (STR)</td>
                        {props.canSelectValues ?
                            generateDndTableRow(attributeScoreObj, attributeValues, "STR", 0) :
                            generateStaticTableRow(attributeScoreObj.STR)
                        }
                    </tr>
                    <tr>
                        <td className="attributeTableAttributeCell">Coordination (COR)</td>
                        {props.canSelectValues ?
                            generateDndTableRow(attributeScoreObj, attributeValues, "COR", 1) :
                            generateStaticTableRow(attributeScoreObj.COR)
                        }
                    </tr>
                    <tr>
                        <td className="attributeTableAttributeCell">Stamina (STA)</td>
                        {props.canSelectValues ?
                            generateDndTableRow(attributeScoreObj, attributeValues, "STA", 2) :
                            generateStaticTableRow(attributeScoreObj.STA)
                        }
                    </tr>
                    <tr>
                        <td className="attributeTableAttributeCell">Perception (PER)</td>
                        {props.canSelectValues ?
                            generateDndTableRow(attributeScoreObj, attributeValues, "PER", 3) :
                            generateStaticTableRow(attributeScoreObj.PER)
                        }
                    </tr>
                    <tr>
                        <td className="attributeTableAttributeCell">Intellect (INT)</td>
                        {props.canSelectValues ?
                            generateDndTableRow(attributeScoreObj, attributeValues, "INT", 4) :
                            generateStaticTableRow(attributeScoreObj.INT)
                        }
                    </tr>
                    <tr>
                        <td className="attributeTableAttributeCell">Presence (PRS)</td>
                        {props.canSelectValues ?
                            generateDndTableRow(attributeScoreObj, attributeValues, "PRS", 5) :
                            generateStaticTableRow(attributeScoreObj.PRS)
                        }
                    </tr>
                    <tr>
                        <td className="attributeTableAttributeCell">Luck (LUC)</td>
                        {props.canSelectValues ?
                            generateDndTableRow(attributeScoreObj, attributeValues, "LUC", 6) :
                            generateStaticTableRow(attributeScoreObj.LUC)
                        }
                    </tr>
                </tbody>
            </table>
            {props.canSelectValues &&
                <div className="resetValuesButtonCenter">
                    <Button onClick={() => resetValues()} icon={<ReloadOutlined />}>Reset values</Button>
                </div>
            }

        </DndContext>
    );
};

export default AttributeScoreSelector;