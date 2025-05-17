package com.wcg.chargen.backend.testUtil;

import com.wcg.chargen.backend.service.impl.yaml.SpeciesYamlLoaderService;

public class TestInvalidSpeciesYamlLoaderServices {
    public static class InvalidSpeciesDataYamlLoaderService extends SpeciesYamlLoaderService {
        public InvalidSpeciesDataYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "invalid-data.yml";
        }
    }

    public static class InvalidSpeciesTypeYamlLoaderService extends SpeciesYamlLoaderService {
        public InvalidSpeciesTypeYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "invalid-species.yml";
        }
    }

    public static class NoStrengthsSpeciesTypeYamlLoaderService extends SpeciesYamlLoaderService {
        public NoStrengthsSpeciesTypeYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "no-strengths.yml";
        }
    }

    public static class EmptyStrengthsSpeciesTypeYamlLoaderService extends SpeciesYamlLoaderService {
        public EmptyStrengthsSpeciesTypeYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "empty-strengths.yml";
        }
    }

    public static class TooFewStrengthsSpeciesTypeYamlLoaderService extends SpeciesYamlLoaderService {
        public TooFewStrengthsSpeciesTypeYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "too-few-strengths.yml";
        }
    }

    public static class TooManyStrengthsSpeciesTypeYamlLoaderService extends SpeciesYamlLoaderService {
        public TooManyStrengthsSpeciesTypeYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "too-many-strengths.yml";
        }
    }

    public static class NoWeaknessesSpeciesTypeYamlLoaderService extends SpeciesYamlLoaderService {
        public NoWeaknessesSpeciesTypeYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "no-weaknesses.yml";
        }
    }

    public static class EmptyWeaknessesSpeciesTypeYamlLoaderService extends SpeciesYamlLoaderService {
        public EmptyWeaknessesSpeciesTypeYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "empty-weaknesses.yml";
        }
    }

    public static class TooFewWeaknessesSpeciesTypeYamlLoaderService extends SpeciesYamlLoaderService {
        public TooFewWeaknessesSpeciesTypeYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "too-few-weaknesses.yml";
        }
    }

    public static class TooManyWeaknessesSpeciesTypeYamlLoaderService extends SpeciesYamlLoaderService {
        public TooManyWeaknessesSpeciesTypeYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "too-many-weaknesses.yml";
        }
    }

    public static class InvalidStrengthSpeciesTypeYamlLoaderService extends SpeciesYamlLoaderService {
        public InvalidStrengthSpeciesTypeYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "invalid-strength.yml";
        }
    }

    public static class InvalidWeaknessSpeciesTypeYamlLoaderService extends SpeciesYamlLoaderService {
        public InvalidWeaknessSpeciesTypeYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "invalid-weakness.yml";
        }
    }
}
