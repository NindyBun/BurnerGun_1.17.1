package com.nindybun.burnergun.common.network.packets;

import com.nindybun.burnergun.common.containers.BurnerGunMK2Container;
import com.nindybun.burnergun.common.items.burnergunmk1.BurnerGunMK1;
import com.nindybun.burnergun.common.containers.BurnerGunMK1Container;
import com.nindybun.burnergun.common.items.burnergunmk1.BurnerGunMK1Handler;
import com.nindybun.burnergun.common.items.burnergunmk2.BurnerGunMK2;
import com.nindybun.burnergun.common.items.burnergunmk2.BurnerGunMK2Handler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Supplier;

public class PacketOpenBurnerGunGui {
    public PacketOpenBurnerGunGui(){

    }

    public static void encode(PacketOpenBurnerGunGui msg, FriendlyByteBuf buffer) {
    }

    public static PacketOpenBurnerGunGui decode(FriendlyByteBuf buffer) {
        return new PacketOpenBurnerGunGui();
    }

    public static class Handler {
        public static void handle(PacketOpenBurnerGunGui msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();

                if (player == null)
                    return;

                ItemStack stack = !BurnerGunMK2.getGun(player).isEmpty() ? BurnerGunMK2.getGun(player) : BurnerGunMK1.getGun(player);
                if (stack.isEmpty())
                    return;

                IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
                if (stack.getItem() instanceof BurnerGunMK1)
                    player.openMenu(new SimpleMenuProvider(
                        (windowId, playerInv, playerEntity) ->
                            new BurnerGunMK1Container(windowId, playerInv, (BurnerGunMK1Handler) handler),
                        new TextComponent("")
                    ));
                else if (stack.getItem() instanceof BurnerGunMK2)
                    player.openMenu(new SimpleMenuProvider(
                            (windowId, playerInv, playerEntity) -> new BurnerGunMK2Container(windowId, playerInv, (BurnerGunMK2Handler) handler),
                            new TextComponent("")
                    ));

            });

            ctx.get().setPacketHandled(true);
        }
    }




}
