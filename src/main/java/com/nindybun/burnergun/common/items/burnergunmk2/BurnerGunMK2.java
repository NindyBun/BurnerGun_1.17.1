package com.nindybun.burnergun.common.items.burnergunmk2;

import com.nindybun.burnergun.client.Keybinds;
import com.nindybun.burnergun.common.BurnerGun;
import com.nindybun.burnergun.common.blocks.Light;
import com.nindybun.burnergun.common.blocks.ModBlocks;
import com.nindybun.burnergun.common.items.upgrades.Upgrade;
import com.nindybun.burnergun.util.UpgradeUtil;
import com.nindybun.burnergun.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class BurnerGunMK2 extends Item {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final RecipeType<? extends AbstractCookingRecipe> RECIPE_TYPE = RecipeType.SMELTING;


    public BurnerGunMK2() {
        super(new Item.Properties().stacksTo(1).setNoRepair().fireResistant().tab(BurnerGun.itemGroup));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(new TextComponent("Was is Worth it? What did it cost?").withStyle(ChatFormatting.GOLD));
        tooltip.add(new TextComponent("Press " + GLFW.glfwGetKeyName(Keybinds.burnergun_gui_key.getKey().getValue(), GLFW.glfwGetKeyScancode(Keybinds.burnergun_gui_key.getKey().getValue())).toUpperCase() + " to open GUI").withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent("Press " + GLFW.glfwGetKeyName(Keybinds.burnergun_light_key.getKey().getValue(), GLFW.glfwGetKeyScancode(Keybinds.burnergun_light_key.getKey().getValue())).toUpperCase() + " to shoot light!").withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent("Press " + GLFW.glfwGetKeyName(Keybinds.burnergun_lightPlayer_key.getKey().getValue(), GLFW.glfwGetKeyScancode(Keybinds.burnergun_lightPlayer_key.getKey().getValue())).toUpperCase() + " to place light at your head!").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Nonnull
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag oldCapNbt) {
        return new BurnerGunMK2Provider();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack p_77661_1_) {
        return UseAnim.NONE;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean canMine(Level world, BlockPos pos, BlockState state, Player player){
        if (    state.getDestroySpeed(world, pos) <= 0
                || state.getBlock() instanceof Light
                || !world.mayInteract(player, pos)
                || !player.mayBuild()
                || MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, state, player))
                || state.getBlock().equals(Blocks.AIR.defaultBlockState())
                || state.getBlock().equals(Blocks.CAVE_AIR.defaultBlockState()))
            return false;
        return true;
    }

    public ItemStack trashItem(List<Item> trashList, ItemStack drop, Boolean trashWhitelist){
        if (trashList.contains(drop.getItem()) && !trashWhitelist)
            return drop;
        else if (!trashList.contains(drop.getItem()) && trashWhitelist)
            return drop;
        return ItemStack.EMPTY;
    }

    public ItemStack smeltItem(Level world, List<Item> smeltList, ItemStack drop, Boolean smeltWhitelist){
        SimpleContainer inv = new SimpleContainer(1);
        inv.setItem(0, drop);
        Optional<? extends AbstractCookingRecipe> recipe = world.getRecipeManager().getRecipeFor(RECIPE_TYPE, inv, world);
        if (recipe.isPresent()){
            ItemStack smelted = recipe.get().getResultItem().copy();
            if (smeltList.contains(smelted.getItem()) && smeltWhitelist)
                return smelted;
            else if (!smeltList.contains(smelted.getItem()) && !smeltWhitelist)
                return smelted;
        }
        return drop;
    }

    public void spawnLight(Level world, BlockHitResult ray){
        if (world.getBrightness(LightLayer.BLOCK, ray.getBlockPos().relative(ray.getDirection())) <= 8 && ray.getType() == BlockHitResult.Type.BLOCK)
            world.setBlockAndUpdate(ray.getBlockPos(), ModBlocks.LIGHT.get().defaultBlockState());
    }


    public void mineBlock(Level world, BlockHitResult ray, ItemStack gun, List<Upgrade> activeUpgrades, List<Item> smeltFilter, List<Item> trashFilter, BlockPos blockPos, BlockState blockState, Player player){
        if (canMine(world, blockPos, blockState, player)){
            List<ItemStack> blockDrops = blockState.getDrops(new LootContext.Builder((ServerLevel) world)
                .withParameter(LootContextParams.TOOL, gun)
                .withParameter(LootContextParams.ORIGIN, new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()))
                .withParameter(LootContextParams.BLOCK_STATE, blockState)
            );
            world.destroyBlock(blockPos, false);
            int blockXP = blockState.getExpDrop(world, blockPos, UpgradeUtil.containsUpgradeFromList(activeUpgrades, Upgrade.FORTUNE_1) ? UpgradeUtil.getUpgradeFromListByUpgrade(activeUpgrades, Upgrade.FORTUNE_1).getTier() : 0, UpgradeUtil.containsUpgradeFromList(activeUpgrades, Upgrade.SILK_TOUCH) ? 1 : 0);
            if (!blockDrops.isEmpty()){
                blockDrops.forEach(drop -> {
                    if (UpgradeUtil.containsUpgradeFromList(activeUpgrades, Upgrade.AUTO_SMELT))
                        drop = smeltItem(world, smeltFilter, drop.copy(), info.getSmeltIsWhitelist());
                    if (UpgradeUtil.containsUpgradeFromList(activeUpgrades, Upgrade.TRASH))
                        drop = trashItem(trashFilter, drop.copy(), info.getTrashIsWhitelist());
                    if (UpgradeUtil.containsUpgradeFromList(activeUpgrades, Upgrade.MAGNET)){
                        if (!player.inventory.add(drop.copy()))
                            player.drop(drop.copy(), true);
                    }else{
                        world.addFreshEntity(new ItemEntity(world, blockPos.getX()+0.5, blockPos.getY()+0.5, blockPos.getZ()+0.5, drop.copy()));
                    }
                });
            }
            if (UpgradeUtil.containsUpgradeFromList(activeUpgrades, Upgrade.MAGNET))
                player.giveExperiencePoints(blockXP);
            else
                blockState.getBlock().popExperience((ServerWorld) world, blockPos, blockXP);
            if (UpgradeUtil.containsUpgradeFromList(activeUpgrades, Upgrade.LIGHT))
                spawnLight(world, ray);
        }
    }

    public void mineArea(World world, BlockRayTraceResult ray, ItemStack gun, BurnerGunMK2Info info, List<Upgrade> activeUpgrades, List<Item> smeltFilter, List<Item> trashFilter, BlockPos blockPos, BlockState blockState, PlayerEntity player){
        int xRad = info.getHorizontal();
        int yRad = info.getVertical();
        Vector3d size = WorldUtil.getDim(ray, xRad, yRad, player);
        for (int xPos = blockPos.getX() - (int)size.x(); xPos <= blockPos.getX() + (int)size.x(); ++xPos){
            for (int yPos = blockPos.getY() - (int)size.y(); yPos <= blockPos.getY() + (int)size.y(); ++yPos){
                for (int zPos = blockPos.getZ() - (int)size.z(); zPos <= blockPos.getZ() + (int)size.z(); ++zPos){
                    BlockPos thePos = new BlockPos(xPos, yPos, zPos);
                    BlockState theState = world.getBlockState(thePos);
                    mineBlock(world, ray, gun, info, activeUpgrades, smeltFilter, trashFilter, thePos, theState, player);
                }
            }
        }
    }


    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack gun = player.getItemInHand(hand);
        BurnerGunMK2Info info = getInfo(gun);
        List<Upgrade> activeUpgrades = UpgradeUtil.getActiveUpgrades(gun);
        BlockRayTraceResult blockRayTraceResult = WorldUtil.getLookingAt(world, player, RayTraceContext.FluidMode.NONE, info.getRaycastRange());
        BlockPos blockPos = blockRayTraceResult.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        List<Item> smeltFilter = UpgradeUtil.getItemsFromList(info.getSmeltNBTFilter());
        List<Item> trashFilter = UpgradeUtil.getItemsFromList(info.getTrashNBTFilter());
        if (world.isClientSide)
            player.playSound(SoundEvents.FIRECHARGE_USE, info.getVolume()*0.5f, 1.0f);
        if (!world.isClientSide){
           if (canMine(world, blockPos, blockState, player)){
               gun.enchant(Enchantments.BLOCK_FORTUNE, UpgradeUtil.containsUpgradeFromList(activeUpgrades, Upgrade.FORTUNE_1) ? UpgradeUtil.getUpgradeFromListByUpgrade(activeUpgrades, Upgrade.FORTUNE_1).getTier() : 0);
               gun.enchant(Enchantments.SILK_TOUCH, UpgradeUtil.containsUpgradeFromList(activeUpgrades, Upgrade.SILK_TOUCH) ? 1 : 0);
                if (player.isCrouching())
                    mineBlock(world, blockRayTraceResult, gun, info, activeUpgrades, smeltFilter, trashFilter, blockPos, blockState, player);
                else
                    mineArea(world, blockRayTraceResult, gun, info, activeUpgrades, smeltFilter, trashFilter, blockPos, blockState, player);
            }
        }
        UpgradeUtil.removeEnchantment(gun, Enchantments.BLOCK_FORTUNE);
        UpgradeUtil.removeEnchantment(gun, Enchantments.SILK_TOUCH);
        return ActionResult.consume(gun);
    }

    public static ItemStack getGun(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof BurnerGunMK2)) {
            heldItem = player.getOffhandItem();
            if (!(heldItem.getItem() instanceof BurnerGunMK2)) {
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }


}
