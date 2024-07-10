package com.wcg.chargen.backend.model;

public class FeatureResponse {
    private final int numAllowedTier1Features;

    private final int numAllowedTier2Features;

    private final Features features;

    public FeatureResponse(int numAllowedTier1Features, int numAllowedTier2Features, Features features) {
        this.numAllowedTier1Features = numAllowedTier1Features;
        this.numAllowedTier2Features = numAllowedTier2Features;
        this.features = features;
    }

    // Getter methods are required for object serialization
    public int getNumAllowedTier1Features() {
        return numAllowedTier1Features;
    }

    public int getNumAllowedTier2Features() {
        return numAllowedTier2Features;
    }

    public Features getFeatures() {
        return features;
    }
}
