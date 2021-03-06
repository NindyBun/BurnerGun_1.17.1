package com.nindybun.burnergun.common.network.packets;

import com.nindybun.burnergun.common.items.BurnerGunNBT;
import com.nindybun.burnergun.common.items.burnergunmk1.BurnerGunMK1;
import com.nindybun.burnergun.common.items.upgrades.Upgrade;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class PacketRefuel {
    private static final Logger LOGGER = LogManager.getLogger();

    public PacketRefuel(){

    }

    public static void encode(PacketRefuel msg, FriendlyByteBuf buffer){

    }

    public static PacketRefuel decode(FriendlyByteBuf buffer){
        return new PacketRefuel();
    }

    public static class Handler {
        public static void handle(PacketRefuel msg, Supplier<NetworkEvent.Context> ctx){
            ctx.get().enqueueWork( ()-> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;
                ItemStack gun = BurnerGunMK1.getGun(player);
                if (gun.isEmpty())
                    return;
                IItemHandler handler = BurnerGunMK1.getHandler(gun);
                ItemStack stack = handler.getStackInSlot(0);
                if (stack == ItemStack.EMPTY
                        || stack.getItem().equals(Upgrade.AMBIENCE_1.getCard().asItem())
                        || stack.getItem().equals(Upgrade.AMBIENCE_2.getCard().asItem())
                        || stack.getItem().equals(Upgrade.AMBIENCE_3.getCard().asItem())
                        || stack.getItem().equals(Upgrade.AMBIENCE_4.getCard().asItem())
                        || stack.getItem().equals(Upgrade.AMBIENCE_5.getCard().asItem()))
                    return;
                while (handler.getStackInSlot(0).getCount() > 0){
                    if (BurnerGunNBT.getFuelValue(gun) + handler.getStackInSlot(0).getBurnTime(RecipeType.SMELTING) > BurnerGunMK1.base_use_buffer)
                        break;
                    BurnerGunNBT.setFuelValue(gun, BurnerGunNBT.getFuelValue(gun) + handler.getStackInSlot(0).getBurnTime(RecipeType.SMELTING));
                    ItemStack containerItem = handler.getStackInSlot(0).getContainerItem();
                    handler.getStackInSlot(0).shrink(1);
                    if (!containerItem.isEmpty())
                        handler.insertItem(0, containerItem, false);
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
