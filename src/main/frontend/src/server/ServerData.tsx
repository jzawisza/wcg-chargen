import { preload } from "swr";
import useSWRImmutable from 'swr/immutable';
import { ProfessionsType } from "./ProfessionsType";
import { SkillsType } from "./SkillsType";
import { FeaturesType } from "./FeaturesType";
import { CreateCharacterRequest } from "./CreateCharacterRequest";

// Taken from https://github.com/vercel/swr/discussions/939
async function fetcher<JSON = any>(
    input: RequestInfo,
    init?: RequestInit
  ): Promise<JSON> {
    const res = await fetch(input, init);
    return res.json()
  }

const PROFESSION_ENDPOINT = 'api/v1/professions/generate';
const SKILLS_ENDPOINT = 'api/v1/skills';
const FEATURES_ENDPOINT = 'api/v1/features';
const GOOGLE_SHEETS_ENDPOINT = 'api/v1/createcharacter/googlesheets';

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

export function useFeaturesData(charClass: string, level: number) {
  const featuresUri = `${FEATURES_ENDPOINT}?charClass=${charClass}&level=${level}`;

  const { data, error, isLoading } = useSWRImmutable<FeaturesType>(featuresUri, fetcher);

  return { data, error, isLoading };
}

export async function invokeGoogleSheetsApi(tokenType : string, token : string, payload: CreateCharacterRequest) {
  try {
    const response = await fetch(GOOGLE_SHEETS_ENDPOINT, {
      method: 'POST',
      headers: {
        Authorization: `${tokenType} ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });
    if (!response.ok) {
      throw new Error(`Response status: ${response.status}`);
    }
  }
  catch (error) {
    console.error(error);
    return false;
  }

  return true;
}