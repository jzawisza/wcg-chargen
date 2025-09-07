type IncompleteFeaturesProps = {
    tier1Features: string[],
    tier2Features: string[]
}

const IncompleteFeatures = (props: IncompleteFeaturesProps) => {
    return (
        <div className="incompleteFeatures">
            <p>The following features will not be automatically populated on your character sheet, and must be applied by hand after the character sheet is generated.</p>
            {(props.tier1Features.length > 0) &&
                <div>
                    <h3>Tier I Features:</h3>
                    <ul>
                        {props.tier1Features.map(feature => (
                            <li key={feature}>{feature}</li>
                        ))}
                    </ul>
                </div>
            }

            {(props.tier2Features.length > 0) &&
                <div>
                    <h3>Tier II Features:</h3>
                    <ul>
                        {props.tier2Features.map(feature => (
                            <li key={feature}>{feature}</li>
                        ))}
                    </ul>
                </div>
            }
        </div>
    );
};

export default IncompleteFeatures;