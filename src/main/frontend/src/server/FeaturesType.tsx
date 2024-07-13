export interface FeatureAttribute {
    type: string,
    modifier: string
}

export interface Feature {
    description: string,
    attributes: FeatureAttribute[]
}

export interface Features {
    tier1: Feature[],
    tier2: Feature[]
}

export interface FeaturesType {
    numAllowedTier1Features: number,
    numAllowedTier2Features: number,
    features: Features
}