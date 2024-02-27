export interface ProfessionType {
    name: string,
    rangeStart: number,
    rangeEnd: number
}

export interface ProfessionsType {
    professions: ProfessionType[]
}