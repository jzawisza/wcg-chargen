package com.wcg.chargen.backend.testUtil;

import com.wcg.chargen.backend.service.impl.yaml.CharClassYamlLoaderService;
import com.wcg.chargen.backend.service.impl.yaml.SpeciesYamlLoaderService;

import java.util.*;

public class TestYamlLoaderServices {
    public static class TestBerzerkerYamlLoaderService extends CharClassYamlLoaderService {
        public TestBerzerkerYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "berzerker.yml";
        }
    }

    public static class TestMageYamlLoaderService extends CharClassYamlLoaderService {
        public TestMageYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "mage.yml";
        }
    }

    public static class TestMysticYamlLoaderService extends CharClassYamlLoaderService {
        public TestMysticYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "mystic.yml";
        }
    }

    public static class TestRangerYamlLoaderService extends CharClassYamlLoaderService {
        public TestRangerYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "ranger.yml";
        }
    }

    public static class TestRogueYamlLoaderService extends CharClassYamlLoaderService {
        public TestRogueYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "rogue.yml";
        }
    }

    public static class TestShamanYamlLoaderService extends CharClassYamlLoaderService {
        public TestShamanYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "shaman.yml";
        }
    }

    public static class TestSkaldYamlLoaderService extends CharClassYamlLoaderService {
        public TestSkaldYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "skald.yml";
        }
    }

    public static class TestWarriorYamlLoaderService extends CharClassYamlLoaderService {
        public TestWarriorYamlLoaderService() {}

        @Override
        public String getYamlFile() {
            return "warrior.yml";
        }
    }

    public static class TestDwarfYamlLoaderService extends SpeciesYamlLoaderService {
        public TestDwarfYamlLoaderService() {
        }

        @Override
        public String getYamlFile() { return "dwarf.yml"; }
    }

    public static class TestElfYamlLoaderService extends SpeciesYamlLoaderService {
        public TestElfYamlLoaderService() {
        }

        @Override
        public String getYamlFile() { return "elf.yml"; }
    }

    public static class TestHalflingYamlLoaderService extends SpeciesYamlLoaderService {
        public TestHalflingYamlLoaderService() {
        }

        @Override
        public String getYamlFile() { return "halfling.yml"; }
    }

    public static class TestHumanYamlLoaderService extends SpeciesYamlLoaderService {
        public TestHumanYamlLoaderService() {

        }

        @Override
        public String getYamlFile() { return "human.yml"; }
    }

    public static List<CharClassYamlLoaderService> getAllTestCharClassesList() {
        return new ArrayList<>(List.of(
           new TestBerzerkerYamlLoaderService(),
           new TestMageYamlLoaderService(),
           new TestMysticYamlLoaderService(),
           new TestRangerYamlLoaderService(),
           new TestRogueYamlLoaderService(),
           new TestShamanYamlLoaderService(),
           new TestSkaldYamlLoaderService(),
           new TestWarriorYamlLoaderService()
        ));
    }

    public static List<SpeciesYamlLoaderService> getAllTestSpeciesList() {
        return new ArrayList<>(List.of(
                new TestDwarfYamlLoaderService(),
                new TestElfYamlLoaderService(),
                new TestHalflingYamlLoaderService(),
                new TestHumanYamlLoaderService()
        ));
    }
}
