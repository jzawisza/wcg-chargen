import { preload } from "swr";
import useSWRImmutable from 'swr/immutable';
import { ProfessionsType } from "./ProfessionsType";
import { SkillsType } from "./SkillsType";

// Taken from https://github.com/vercel/swr/discussions/939
async function fetcher<JSON = any>(
    input: RequestInfo,
    init?: RequestInit
  ): Promise<JSON> {
    const res = await fetch(input, init);
    return res.json()
  }

const PROFESSION_ENDPOINT = 'api/v1/professions/generate';
const SKILLS_ENDPOINT = 'api/v1/skills'

export function preloadProfessionsData() {
    preload(PROFESSION_ENDPOINT, fetcher);
}

export function useProfessionsData() {
    const { data, error, isLoading } = useSWRImmutable<ProfessionsType>(PROFESSION_ENDPOINT, fetcher);

    return { data, error, isLoading };
}

export function useSkillsData(charClass: string, species: string) {
  const skillsUri = `${SKILLS_ENDPOINT}?charClass=${charClass}&species=${species}`;
  const { data, error, isLoading } = useSWRImmutable<SkillsType>(skillsUri, fetcher);

  return { data, error, isLoading };
}