const CHAR_CLASSES: CharClass[] = [];

export class CharClass {
    public readonly charClass: string;
    public readonly capitalizedClassName: string;

    constructor(charClass: string, className: string) {
        this.charClass = charClass;
        this.capitalizedClassName = className;
        CHAR_CLASSES.push(this);
    }

    pluralCapitalizedClassName() {
        return this.capitalizedClassName + 's';
    }
}

export const BERZERKER_CHARCLASS = new CharClass('berzerker', 'Berzerker');
export const MAGE_CHARCLASS = new CharClass('mage', 'Mage');
export const MYSTIC_CHARCLASS = new CharClass('mystic', 'Mystic');
export const RANGER_CHARCLASS =  new CharClass('ranger', 'Ranger');
export const ROGUE_CHARCLASS = new CharClass('rogue', 'Rogue');
export const SHAMAN_CHARCLASS = new CharClass('shaman', 'Shaman');
export const SKALD_CHARCLASS = new CharClass('skald', 'Skald');
export const WARRIOR_CHARCLASS = new CharClass('warrior', 'Warrior');

export function getCharClassByName(name: string) {
    return CHAR_CLASSES.find(x => (x.charClass === name));
}
