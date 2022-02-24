package com.nindybun.burnergun.common.capabilities.burnergunmk2;

import net.minecraft.nbt.ListTag;

public interface BurnerGunMK2Info {
    void setVolume(float value);
    float getVolume();

    void setVertical(int value);
    int getVertical();

    void setMaxVertical(int value);
    int getMaxVertical();

    void setHorizontal(int value);
    int getHorizontal();

    void setMaxHorizontal(int value);
    int getMaxHorizontal();

    void setTrashIsWhitelist(boolean value);
    boolean getTrashIsWhitelist();

    void setSmeltIsWhitelist(boolean value);
    boolean getSmeltIsWhitelist();

    void setRaycastRange(int value);
    int getRaycastRange();

    void setMaxRaycastRange(int value);
    int getMaxRaycastRange();

    void setUpgradeNBTList(ListTag upgrades);
    ListTag getUpgradeNBTList();

    void setTrashNBTFilter(ListTag items);
    ListTag getTrashNBTFilter();

    void setSmeltNBTFilter(ListTag items);
    ListTag getSmeltNBTFilter();

    void setColor(ListTag color);
    ListTag getColor();
}