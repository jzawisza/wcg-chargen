import { useState } from "react";
import { DndContext, DragEndEvent } from "@dnd-kit/core";
import AttributeDndScoreCell from "./AttributeDndScoreCell";
import AttributeDndValueCell from "./AttributeDndValueCell";

type AttributeScoreSelectorProps = {
    values: (number | null)[];
    canSelectValues: boolean;
};

// Interface for an object tracking attribute scores
interface AttributeScoreObject {
    STR: number | null;
    COR: number | null;
    STA: number | null;
    PER: number | null;
    INT: number | null;
    PRS: number | null;
    LUC: number | null;
};

// Generate a table cell containing an attribute score that is not changeable
function generateStaticTableRow(score: number | null) {
    return (
            <td className="attributeTableValueCell">
                <div className="attributeScore">
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
                <td className="attributeTableEmptyCell" />
                <AttributeDndValueCell index={index} score={valueScore} />
            </>);
}

const AttributeScoreSelector = (props: AttributeScoreSelectorProps) => {
    const emptyAtributeScoreObj: AttributeScoreObject = {
        STR: null,
        COR: null,
        STA: null,
        PER: null,
        INT: null,
        PRS: null,
        LUC: null
    };

    const [attributeScoreObj, setAttributeScoreObj] = useState(emptyAtributeScoreObj);
    const [valueArray, setValueArray] = useState(props.values);

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
                attributeScoreObjModified[attribute] = valueArray[+valuePos];

                // Copy the old valueArray, and update it based on what was dragged to the Score column
                // Use the spread operator to do the copy in order to trigger a state update and re-render
                const valueArrayModified = [...valueArray];
                valueArrayModified[+valuePos] = null;

                // Update with the new object references
                setAttributeScoreObj(attributeScoreObjModified);
                setValueArray(valueArrayModified);
            }
        }
    };

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
                            generateDndTableRow(attributeScoreObj, valueArray, "STR", 0) :
                            generateStaticTableRow(valueArray[0])
                        }
                    </tr>
                    <tr>
                        <td className="attributeTableAttributeCell">Coordination (COR)</td>
                        {props.canSelectValues ?
                            generateDndTableRow(attributeScoreObj, valueArray, "COR", 1) :
                            generateStaticTableRow(valueArray[1])
                        }
                    </tr>
                    <tr>
                        <td className="attributeTableAttributeCell">Stamina (STA)</td>
                        {props.canSelectValues ?
                            generateDndTableRow(attributeScoreObj, valueArray, "STA", 2) :
                            generateStaticTableRow(valueArray[2])
                        }
                    </tr>
                    <tr>
                        <td className="attributeTableAttributeCell">Perception (PER)</td>
                        {props.canSelectValues ?
                            generateDndTableRow(attributeScoreObj, valueArray, "PER", 3) :
                            generateStaticTableRow(valueArray[3])
                        }
                    </tr>
                    <tr>
                        <td className="attributeTableAttributeCell">Intellect (INT)</td>
                        {props.canSelectValues ?
                            generateDndTableRow(attributeScoreObj, valueArray, "INT", 4) :
                            generateStaticTableRow(valueArray[4])
                        }
                    </tr>
                    <tr>
                        <td className="attributeTableAttributeCell">Presence (PRS)</td>
                        {props.canSelectValues ?
                            generateDndTableRow(attributeScoreObj, valueArray, "PRS", 5) :
                            generateStaticTableRow(valueArray[5])
                        }
                    </tr>
                    <tr>
                        <td className="attributeTableAttributeCell">Luck (LUC)</td>
                        {props.canSelectValues ?
                            generateDndTableRow(attributeScoreObj, valueArray, "LUC", 6) :
                            generateStaticTableRow(valueArray[6])
                        }
                    </tr>
                </tbody>
            </table>
        </DndContext>
    );
};

export default AttributeScoreSelector;