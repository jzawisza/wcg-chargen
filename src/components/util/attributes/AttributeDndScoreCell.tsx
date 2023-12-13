import { useDroppable } from "@dnd-kit/core";

type AttributeDndScoreCellProps = {
    attributeShortName: string;
    score: number | null;
};

// Table cell containing an attribute score.
// If the attribute score currently has no value, this will be droppable
// so that it can be populated with a value.
// If it does has a value, it will be draggable AND droppable, so that
// the value can be dragged back to the Values column or two scores can be swapped.
const AttributeDndScoreCell = (props: AttributeDndScoreCellProps) => {
    const {isOver, setNodeRef} = useDroppable({
        id: `droppable${props.attributeShortName}`
    });
    const style = {
        backgroundColor: isOver ? 'silver' : undefined,
    };

    let CustomStyle = {};
    if (props.score !== null) {
        CustomStyle = {
            "fontSize": "125%",
            "fontWeight": "bold",
            "textAlign": "center" as "center",
            "height": "2em",
            "padding": "0.3em 0.3em 0.3em 0.3em",
            "backgroundColor": "chocolate",
            "border": "1px solid"
        }
    }

    return (
        <td className="attributeTableScoreCell" ref={setNodeRef} style={{...style, ...CustomStyle}}>
            {props.score}
        </td>
    );
};

export default AttributeDndScoreCell;