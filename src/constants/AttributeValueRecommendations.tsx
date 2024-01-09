import { CharClass, getCharClassByName, BERZERKER_CHARCLASS, MAGE_CHARCLASS, MYSTIC_CHARCLASS, RANGER_CHARCLASS,
    ROGUE_CHARCLASS, SHAMAN_CHARCLASS, SKALD_CHARCLASS, WARRIOR_CHARCLASS } from "./CharClassInfo";

export function getAttributeValueRecommendations(charClass: string) {
    const charClassType = getCharClassByName(charClass);
    const charClassRecommendationHeading = (charClassType ?
            charClassType.pluralCapitalizedClassName() :
            "Unknown Character Class");

    return (
        <>
        <h3>General</h3>
        <p>All character types benefit from decent <b>COR</b> and <b>STA</b> scores.  <b>COR</b> boosts Evasion, which helps you avoid getting hit, and Initiative, which allows you to attack more quickly.  <b>STA</b> gives you more hit points at Level 1 and can increase the number of hit points recovered during a quick rest.  Both of these will help you survive.</p>

        <p><b>PER</b> and <b>LUC</b> tend to be rolled fairly frequently, so you might want to consider higher values for those attributes.</p>

        <h3>{charClassRecommendationHeading}</h3>
        {getCharClassAttributeValueRecommendations(charClassType)}
        </>
    );
}

function getCharClassAttributeValueRecommendations(charClassType: CharClass | undefined) {
    switch (charClassType) {
        case BERZERKER_CHARCLASS:
            return (<>
                <p>A berzerker should have high <b>STR</b> and <b>STA</b> scores.  Checks to both those attributes are rolled at advantage while a berzerker is raging, and <b>STR</b> improves the attack and damage rolls for the melee weapons they typically specialize in.</p>
                <p>They also need to take quick rests after raging, and a high <b>STA</b> score will improve healing during those rests.</p>
                </>);
        case MAGE_CHARCLASS:
            return (<p>A high <b>INT</b> score is a must for a mage.  Mages use <b>INT</b> when casting spells, and all of their core class skills are <b>INT</b>-based.</p>);
        case MYSTIC_CHARCLASS:
            return (<>
                <p><b>STR</b> is the most useful attribute for a mystic, as it improves the attack and damage scores for the unarmed attacks they specialize in.</p>
                <p>A decent <b>COR</b> score is also useful, as it contributes to the Athletics and Stealth skills that are core for a mystic.  <b>INT</b> may also be useful for their Religion skill if no one else in the party has that skill.</p>
                </>);
        case RANGER_CHARCLASS:
            return (<>
                <p>Most of the skills a ranger specializes in are <b>INT</b>-based, so a high <b>INT</b> score is recommended.  A high <b>COR</b> score is also useful for rangers who plan to specialize in bows and/or horseback riding.</p>
                <p>Rangers who want to be adept at taming animals should have a decent <b>PRS</b> score.</p>
            </>);
        case ROGUE_CHARCLASS:
            return (<>
                <p>A rogue will benefit from a high <b>COR</b> score, as <b>COR</b> is used for the Precise Tasks and Stealth skills that rogues specialize in.</p>
                <p>Depending on what else they want to focus on, rogues may also benefit from decent scores in <b>PRS</b> (Deceit and Gather Information) and <b>INT</b> (Appraisal).</p>
                </>);
        case SHAMAN_CHARCLASS:
            return (<p>A shaman needs to have a high <b>PRS</b> score, since they use <b>PRS</b> when casting spells.  Their other core skills are <b>INT</b>-based, so a high <b>INT</b> is also recommended.</p>);
        case SKALD_CHARCLASS:
            return (<>
                <p>A skald should have high <b>PRS</b> and <b>INT</b> scores, as all core skald skills use these attributes.</p>
                <p>Those attributes contribute to the skald's class features as well.  Magic songs require Perform checks, a <b>PRS</b>-based skill, and Forgotten Lore requires an <b>INT</b>-based check.</p>
            </>);
        case WARRIOR_CHARCLASS:
            return (<>
                <p>A warrior should have high <b>STR</b>, <b>COR</b>, and <b>STA</b> scores.  High <b>STA</b> combined with their Second Wind class feature leads to increased HP recovery.</p>
                <p>While both <b>STR</b> and <b>COR</b> should be high, the choice of weapon specialization should determine which one should be highest.  A melee weapon specialization calls for a high <b>STR</b>, while a ranged weapon specialization requires a high <b>COR</b>.</p>
            </>);
        default:
            break;
    }

    return (<p>Unable to find information for character class</p>);
}