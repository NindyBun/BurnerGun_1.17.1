package com.nindybun.burnergun.common.containers;

import com.nindybun.burnergun.common.items.upgrades.Upgrade_Bag.UpgradeBag;
import com.nindybun.burnergun.common.items.upgrades.Upgrade_Bag.UpgradeBagHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.SlotItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public class UpgradeBagContainer extends Container {

    UpgradeBagContainer(int windowId, PlayerInventory playerInv,
                        PacketBuffer buf){
        this(windowId, playerInv, new UpgradeBagHandler(MAX_EXPECTED_HANDLER_SLOT_COUNT));
    }

    public UpgradeBagContainer(int windowId, PlayerInventory playerInventory, UpgradeBagHandler handler){
        super(ModContainers.UPGRADE_BAG_CONTAINER.get(), windowId);
        this.handler = handler;
        this.setup(playerInventory);
        }

    private final UpgradeBagHandler handler;

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;

    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int HANDLER_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    public static final int MAX_EXPECTED_HANDLER_SLOT_COUNT = 27;

    private final int HANDLER_SLOTS_PER_ROW = 9;

    private final int HANDLER_INVENTORY_XPOS = 8;
    private static final int HANDLER_INVENTORY_YPOS = 8;

    private final int PLAYER_INVENTORY_XPOS = 8;
    private static final int PLAYER_INVENTORY_YPOS = 84;

    private final int SLOT_X_SPACING = 18;
    private final int SLOT_Y_SPACING = 18;
    private final int HOTBAR_XPOS = 8;
    private final int HOTBAR_YPOS = 142;

    private void setup(PlayerInventory playerInv){
        // Add the players hotbar to the gui - the [xpos, ypos] location of each item
        for (int x = 0; x < HOTBAR_SLOT_COUNT; x++) {
            int slotNumber = x;
            addSlot(new Slot(playerInv, slotNumber, HOTBAR_XPOS + SLOT_X_SPACING * x, HOTBAR_YPOS));
        }

        // Add the rest of the player's inventory to the gui
        for (int y = 0; y < PLAYER_INVENTORY_ROW_COUNT; y++) {
            for (int x = 0; x < PLAYER_INVENTORY_COLUMN_COUNT; x++) {
                int slotNumber = HOTBAR_SLOT_COUNT + y * PLAYER_INVENTORY_COLUMN_COUNT + x;
                int xpos = PLAYER_INVENTORY_XPOS + x * SLOT_X_SPACING;
                int ypos = PLAYER_INVENTORY_YPOS + y * SLOT_Y_SPACING;
                addSlot(new Slot(playerInv, slotNumber, xpos, ypos));
            }
        }

        int bagSlotCount = handler.getSlots();
        if (bagSlotCount < 1 || bagSlotCount > MAX_EXPECTED_HANDLER_SLOT_COUNT) {
            LOGGER.warn("Unexpected invalid slot count in UpgradeBagHandler(" + bagSlotCount + ")");
            bagSlotCount = MathHelper.clamp(bagSlotCount, 1, MAX_EXPECTED_HANDLER_SLOT_COUNT);
        }

        // Add the tile inventory container to the gui
        for (int bagSlot = 0; bagSlot < bagSlotCount; ++bagSlot) {
            int slotNumber = bagSlot;
            int bagRow = bagSlot / HANDLER_SLOTS_PER_ROW;
            int bagCol = bagSlot % HANDLER_SLOTS_PER_ROW;
            int xpos = HANDLER_INVENTORY_XPOS + SLOT_X_SPACING * bagCol;
            int ypos = HANDLER_INVENTORY_YPOS + SLOT_Y_SPACING * bagRow;
            addSlot(new SlotItemHandler(handler, slotNumber, xpos, ypos));
        }
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        ItemStack main = playerIn.getMainHandItem();
        ItemStack off = playerIn.getOffhandItem();
        return (!main.isEmpty() && main.getItem() instanceof UpgradeBag) ||
                (!off.isEmpty() && off.getItem() instanceof UpgradeBag);
    }


    @Nonnull
    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int sourceSlotIndex) {
        Slot sourceSlot = slots.get(sourceSlotIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();
        final int BAG_SLOT_COUNT = handler.getSlots();

        // Check if the slot clicked is one of the vanilla container slots
        if (sourceSlotIndex >= VANILLA_FIRST_SLOT_INDEX && sourceSlotIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the bag inventory
            if (!moveItemStackTo(sourceStack, HANDLER_FIRST_SLOT_INDEX, HANDLER_FIRST_SLOT_INDEX + BAG_SLOT_COUNT, false)){
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (sourceSlotIndex >= HANDLER_FIRST_SLOT_INDEX && sourceSlotIndex < HANDLER_FIRST_SLOT_INDEX + BAG_SLOT_COUNT) {
            // This is a bag slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            LOGGER.warn("Invalid slotIndex:" + sourceSlotIndex);
            return ItemStack.EMPTY;
        }

        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(player, sourceStack);
        return copyOfSourceStack;
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
