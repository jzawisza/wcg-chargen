type AttributeScoreSelectorProps = {
    values: number[];
    canSelectValues: boolean;
};

const AttributeScoreSelector = (props: AttributeScoreSelectorProps) => {

    // Use a HTML table to represent the attributes, the draggable score values,
    // and the places where those scores can be dropped
    return (
        <table className="attributeTable">
            <tr>
                <th className="attributeTableHeaderCell">Attributes</th>
                <th className="attributeTableHeaderCell">Scores</th>
                <th className="attributeTableHeaderCell" />
                <th className="attributeTableHeaderCell">Values</th>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Strength (STR)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {props.values[0]}
                    </div>
                </td>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Coordination (COR)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {props.values[1]}
                    </div>
                </td>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Stamina (STA)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {props.values[2]}
                    </div>
                </td>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Perception (PER)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {props.values[3]}
                    </div>
                </td>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Intellect (INT)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {props.values[4]}
                    </div>
                </td>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Presence (PRS)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {props.values[5]}
                    </div>
                </td>
            </tr>
            <tr>
                <td className="attributeTableAttributeCell">Luck (LUC)</td>
                <td className="attributeTableDroppableCell"></td>
                <td className="attributeTableEmptyCell"></td>
                <td className="attributeTableScoreCell">
                    <div className="attributeScore">
                        {props.values[6]}
                    </div>
                </td>
            </tr>
        </table>
    );
};

export default AttributeScoreSelector;