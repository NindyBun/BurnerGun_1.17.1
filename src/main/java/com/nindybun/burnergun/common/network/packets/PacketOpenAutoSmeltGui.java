package com.nindybun.burnergun.common.network.packets;

import com.nindybun.burnergun.common.containers.AutoSmeltContainer;
import com.nindybun.burnergun.common.items.BurnerGunNBT;
import com.nindybun.burnergun.common.items.burnergunmk1.BurnerGunMK1;
import com.nindybun.burnergun.common.items.burnergunmk2.BurnerGunMK2;
import com.nindybun.burnergun.common.items.upgrades.Auto_Smelt.AutoSmelt;
import com.nindybun.burnergun.common.items.upgrades.Auto_Smelt.AutoSmeltHandler;
import com.nindybun.burnergun.common.items.upgrades.Upgrade;
import com.nindybun.burnergun.util.UpgradeUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import java.util.function.Supplier;

import java.util.List;

public class PacketOpenAutoSmeltGui {
    public PacketOpenAutoSmeltGui(){

    }

    public static void encode(PacketOpenAutoSmeltGui msg, FriendlyByteBuf buffer) {
    }

    public static PacketOpenAutoSmeltGui decode(FriendlyByteBuf buffer) {
        return new PacketOpenAutoSmeltGui();
    }

    public static class Handler {
        public static void handle(PacketOpenAutoSmeltGui msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();

                if (player == null)
                    return;

                ItemStack gun = !BurnerGunMK2.getGun(player).isEmpty() ? BurnerGunMK2.getGun(player) : BurnerGunMK1.getGun(player);
                ItemStack smelt = player.getMainHandItem();
                if (!gun.isEmpty()){
                    List<Upgrade> upgradeList = BurnerGunNBT.getUpgrades(gun);
                    if (upgradeList.contains(Upgrade.AUTO_SMELT))
                        smelt = UpgradeUtil.getStackByUpgrade(gun, Upgrade.AUTO_SMELT);
                }

                if (!(smelt.getItem() instanceof AutoSmelt))
                    return;

                IItemHandler handler = smelt.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
                player.openMenu(new SimpleMenuProvider(
                        (windowId, playerInv, playerEntity) -> new AutoSmeltContainer(windowId, playerInv, (AutoSmeltHandler) handler),
                        new TextComponent("")
                ));

            });

            ctx.get().setPacketHandled(true);
        }
    }




}
