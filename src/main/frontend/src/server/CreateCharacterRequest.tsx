export interface CreateCharacterRequest {
    characterName: string,
    characterClass?: string,
    species: string,
    profession?: string,
    level: number
}