package com.wcg.chargen.backend.worker;

import com.wcg.chargen.backend.enums.FeatureAttributeType;
import com.wcg.chargen.backend.model.CharacterCreateRequest;

import java.util.List;

public interface CharacterSheetWorker {
    String generateName(CharacterCreateRequest request);

    int getFortunePoints(CharacterCreateRequest request);

    int getBaseEvasion(CharacterCreateRequest request);

    int getEvasionBonus(CharacterCreateRequest request);

    FeatureAttributeType getAdvOrDadvByModifier(CharacterCreateRequest request, String modifierStr);

    int getHitPoints(CharacterCreateRequest request);

    String getWeaponName(CharacterCreateRequest request, int index);

    String getWeaponType(CharacterCreateRequest request, int index);

    String getWeaponDamage(CharacterCreateRequest request, int index);

    String getArmorName(CharacterCreateRequest request, int index);

    String getArmorType(CharacterCreateRequest request, int index);

    String getArmorDa(CharacterCreateRequest request, int index);

    List<String> getEquipmentList(CharacterCreateRequest request);

    int getCopper(CharacterCreateRequest request);

    int getSilver(CharacterCreateRequest request);

    boolean hasMagic(CharacterCreateRequest request);
}
