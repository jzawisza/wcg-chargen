// Interface for an object tracking attribute scores
export interface AttributeScoreObject {
    STR: number | null;
    COR: number | null;
    STA: number | null;
    PER: number | null;
    INT: number | null;
    PRS: number | null;
    LUC: number | null;
};

export const emptyAtributeScoreObj: AttributeScoreObject = {
    STR: null,
    COR: null,
    STA: null,
    PER: null,
    INT: null,
    PRS: null,
    LUC: null
};