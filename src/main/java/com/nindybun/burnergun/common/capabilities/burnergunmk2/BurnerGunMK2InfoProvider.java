package com.nindybun.burnergun.common.capabilities.burnergunmk2;

import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BurnerGunMK2InfoProvider implements BurnerGunMK2Info, ICapabilitySerializable<Tag> {
    @CapabilityInject(BurnerGunMK2Info.class)
    public static Capability<BurnerGunMK2Info> burnerGunInfoMK2Capability = null;

    private LazyOptional<BurnerGunMK2Info> instance = LazyOptional.of(burnerGunInfoMK2Capability::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == burnerGunInfoMK2Capability ? instance.cast() : LazyOptional.empty();
    }

    @Override
    public Tag serializeNBT() {
        return burnerGunInfoMK2Capability.getStorage().writeNBT(burnerGunInfoMK2Capability, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        burnerGunInfoMK2Capability.getStorage().readNBT(burnerGunInfoMK2Capability, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null, nbt);

    }

    public float volume = 1.0f;
    public int vertical, maxVertical;
    public int horizontal, maxHorizontal;
    public int raycast = 5, maxRaycast = 5;
    public boolean trash = true;
    public boolean smelt = true;
    public ListTag upgrades = new ListTag();
    public ListTag trashFilter = new ListTag();
    public ListTag smeltFilter = new ListTag();
    public ListTag color = new ListTag();

    @Override
    public void setVolume(float value) {
        volume = value;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public void setVertical(int value) {
        vertical = value;
    }

    @Override
    public int getVertical() {
        return vertical;
    }

    @Override
    public void setMaxVertical(int value) {
        maxVertical = value;
    }

    @Override
    public int getMaxVertical() {
        return maxVertical;
    }

    @Override
    public void setHorizontal(int value) {
        horizontal = value;
    }

    @Override
    public int getHorizontal() {
        return horizontal;
    }

    @Override
    public void setMaxHorizontal(int value) {
        maxHorizontal = value;
    }

    @Override
    public int getMaxHorizontal() {
        return maxHorizontal;
    }

    @Override
    public void setTrashIsWhitelist(boolean value) {
        trash = value;
    }

    @Override
    public boolean getTrashIsWhitelist() {
        return trash;
    }

    @Override
    public void setSmeltIsWhitelist(boolean value) {
        smelt = value;
    }

    @Override
    public boolean getSmeltIsWhitelist() {
        return smelt;
    }

    @Override
    public void setRaycastRange(int value) {
        raycast = value;
    }

    @Override
    public int getRaycastRange() {
        return raycast;
    }

    @Override
    public void setMaxRaycastRange(int value) {
        maxRaycast = value;
    }

    @Override
    public int getMaxRaycastRange() {
        return maxRaycast;
    }

    @Override
    public void setUpgradeNBTList(ListTag upgrades) {
        this.upgrades = upgrades;
    }

    @Override
    public ListTag getUpgradeNBTList() {
        return upgrades;
    }

    @Override
    public void setTrashNBTFilter(ListTag items) {
        trashFilter = items;
    }

    @Override
    public ListTag getTrashNBTFilter() {
        return trashFilter;
    }

    @Override
    public void setSmeltNBTFilter(ListTag items) {
        smeltFilter = items;
    }

    @Override
    public ListTag getSmeltNBTFilter() {
        return smeltFilter;
    }

    @Override
    public void setColor(ListTag color) {
        this.color = color;
    }

    @Override
    public ListTag getColor() {
        return color;
    }

}
