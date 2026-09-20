package com.swbr.orespawn.block.entity;

import com.swbr.orespawn.menu.ContainerCrystalFurnace;
import com.swbr.orespawn.registry.ModBlockEntities;
import com.swbr.orespawn.registry.ModBlocks;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Port of {@code danger.orespawn.TileEntityCrystalFurnace} (TileEntityCrystalFurnace.java:15-315):
 * the block entity of the Crystal Furnace. Registered in the original as
 * {@code "TileEntityCrystalFurnace"} (OreSpawnMain.java:5063); the port registers it under the
 * block's id {@code crystalfurnace} (the manifest has no id for the tile entity itself).
 *
 * <p>What is deliberately <em>not</em> vanilla 1.21.1 furnace behaviour, because the original
 * had its own copy of the 1.7.10 furnace and the copy is what is ported:
 * <ul>
 *   <li>the cook time is a fixed <b>150</b> ticks (:165), not the recipe's {@code cookingTime}
 *       (200 for vanilla smelting);</li>
 *   <li>the fuel list is the original table in {@link #getItemBurnTime} (:221-272) with no data-map
 *       fallback (DECISIONS R18);</li>
 *   <li>cooking progress resets to 0 the moment the furnace cannot smelt (:171-173) - 1.21.1's
 *       slow cool-down by 2 per tick does not exist here;</li>
 *   <li>a change of the input stack does not reset the progress (no {@code setItem} override,
 *       1.7.10 {@code setInventorySlotContents} :63-68 only clamped the size);</li>
 *   <li>smelting experience is paid on take from the output slot, per taken item, looked up by the
 *       taken item's smelting recipe (1.7.10 {@code SlotFurnace}) - see
 *       {@link ContainerCrystalFurnace.SlotFurnace}; there is no {@code recipesUsed} bookkeeping
 *       and breaking the furnace pays nothing (1:1).</li>
 * </ul>
 *
 * <p>The three progress values keep their original names and are synced to the menu through a
 * {@link ContainerData} in the original {@code sendProgressBarUpdate} order
 * (ContainerCrystalFurnace.java:33-35): 0 = cook time, 1 = burn time, 2 = current item burn time.
 */
public class TileEntityCrystalFurnace extends BaseContainerBlockEntity implements WorldlyContainer {

    /** The original {@code furnaceCookTime == 150} (:165) and the GUI scale divisor (:126). */
    public static final int COOK_TIME = 150;

    // ContainerData indices, in the order the original sent them (ContainerCrystalFurnace.java:33-35).
    public static final int DATA_COOK_TIME = 0;
    public static final int DATA_BURN_TIME = 1;
    public static final int DATA_CURRENT_ITEM_BURN_TIME = 2;
    public static final int DATA_COUNT = 3;

    /** Slot indices (:26-28, :186-217): 0 input, 1 fuel, 2 output. */
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;

    private static final int[] SLOTS_TOP = {0};        // :311
    private static final int[] SLOTS_BOTTOM = {2, 1};  // :312
    private static final int[] SLOTS_SIDES = {1};      // :313

    private NonNullList<ItemStack> furnaceItemStacks = NonNullList.withSize(3, ItemStack.EMPTY); // :27
    public int furnaceBurnTime;
    public int currentItemBurnTime;
    public int furnaceCookTime;

    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> quickCheck =
            RecipeManager.createCheck(RecipeType.SMELTING);

    /**
     * The three progress bars of {@code ContainerCrystalFurnace} (:31-68). 1.21.1's data slots are
     * sent on open and then on change, which is exactly what {@code addCraftingToCrafters} and
     * {@code detectAndSendChanges} did; values travel as shorts there too (max fuel 20000 fits).
     */
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_COOK_TIME -> TileEntityCrystalFurnace.this.furnaceCookTime;
                case DATA_BURN_TIME -> TileEntityCrystalFurnace.this.furnaceBurnTime;
                case DATA_CURRENT_ITEM_BURN_TIME -> TileEntityCrystalFurnace.this.currentItemBurnTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_COOK_TIME -> TileEntityCrystalFurnace.this.furnaceCookTime = value;
                case DATA_BURN_TIME -> TileEntityCrystalFurnace.this.furnaceBurnTime = value;
                case DATA_CURRENT_ITEM_BURN_TIME -> TileEntityCrystalFurnace.this.currentItemBurnTime = value;
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public TileEntityCrystalFurnace(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_FURNACE.get(), pos, state);
    }

    // ---------------------------------------------------------------------------------------------
    // Inventory (:30-80, :120-122)
    // ---------------------------------------------------------------------------------------------

    @Override
    public int getContainerSize() {
        return this.furnaceItemStacks.size(); // :30-32
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.furnaceItemStacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.furnaceItemStacks = items;
    }

    /** {@code getInventoryStackLimit} (:120-122). 1.21.1's default would be 99. */
    @Override
    public int getMaxStackSize() {
        return 64;
    }

    /** {@code getInventoryName} (:70-72): the custom name, else {@code container.furnace}. */
    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ContainerCrystalFurnace(containerId, inventory, this, this.dataAccess);
    }

    /** {@code isUseableByPlayer} (:278-280): still this tile entity, and within 8 blocks of the centre. */
    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    // ---------------------------------------------------------------------------------------------
    // NBT (:82-118) - the original keys, BurnTime and CookTime as shorts, currentItemBurnTime
    // recomputed from the fuel slot. CustomName is read and written by BaseContainerBlockEntity
    // (PORT: as a JSON text component, the 1.21.1 format, not the raw string of 1.7.10).
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.furnaceItemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY); // :85
        ContainerHelper.loadAllItems(tag, this.furnaceItemStacks, registries);                  // :84-92 "Items"/"Slot"
        this.furnaceBurnTime = tag.getShort("BurnTime");                                        // :93
        this.furnaceCookTime = tag.getShort("CookTime");                                        // :94
        this.currentItemBurnTime = getItemBurnTime(this.furnaceItemStacks.get(SLOT_FUEL));     // :95
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("BurnTime", (short) this.furnaceBurnTime); // :103
        tag.putShort("CookTime", (short) this.furnaceCookTime); // :104
        ContainerHelper.saveAllItems(tag, this.furnaceItemStacks, registries); // :105-114
    }

    // ---------------------------------------------------------------------------------------------
    // Ticking (:137-219)
    // ---------------------------------------------------------------------------------------------

    /** {@code isBurning} (:137-139). */
    public boolean isBurning() {
        return this.furnaceBurnTime > 0;
    }

    /**
     * {@code updateEntity} (:141-183), line by line. PORT: server only. The original also ran on
     * the client, where the only effect was counting {@code furnaceBurnTime} down between the
     * container's progress-bar packets; the data slots of the menu deliver every change now.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, TileEntityCrystalFurnace furnace) {
        boolean flag = furnace.furnaceBurnTime > 0; // :142
        boolean flag2 = false;                      // :143
        if (furnace.furnaceBurnTime > 0) {          // :144-146
            --furnace.furnaceBurnTime;
        }
        // :147 - !isRemote: the ticker is server-only (see above)
        if (furnace.furnaceBurnTime == 0 && furnace.canSmelt(level)) { // :148
            int itemBurnTime = getItemBurnTime(furnace.furnaceItemStacks.get(SLOT_FUEL)); // :149
            furnace.furnaceBurnTime = itemBurnTime;      // :150
            furnace.currentItemBurnTime = itemBurnTime;  // :151
            if (furnace.furnaceBurnTime > 0) {           // :152
                flag2 = true;
                ItemStack fuel = furnace.furnaceItemStacks.get(SLOT_FUEL);
                if (!fuel.isEmpty()) {                   // :154
                    Item fuelItem = fuel.getItem();
                    fuel.shrink(1);                      // :156
                    if (fuel.isEmpty()) {                // :157-159 getContainerItem, e.g. the bucket of a lava bucket
                        furnace.furnaceItemStacks.set(SLOT_FUEL, fuelItem.getCraftingRemainingItem(fuel));
                    }
                }
            }
        }
        if (furnace.isBurning() && furnace.canSmelt(level)) { // :163
            ++furnace.furnaceCookTime;                        // :164
            if (furnace.furnaceCookTime == COOK_TIME) {       // :165
                furnace.furnaceCookTime = 0;                  // :166
                furnace.smeltItem(level);                     // :167
                flag2 = true;                                 // :168
            }
        } else {
            furnace.furnaceCookTime = 0;                      // :172
        }
        if (flag != furnace.furnaceBurnTime > 0) {            // :174
            flag2 = true;
            // :177 CrystalFurnace.updateFurnaceBlockState: swapped the off/on block with flag 3 and
            // restored the metadata. One block with LIT now (DECISIONS R2); flag 3 as in the original.
            state = state.setValue(BlockStateProperties.LIT, furnace.furnaceBurnTime > 0);
            level.setBlock(pos, state, 3);
        }
        if (flag2) {                                          // :180-182 markDirty
            setChanged(level, pos, state);
        }
    }

    private ItemStack smeltingResult(Level level) {
        return this.quickCheck.getRecipeFor(new SingleRecipeInput(this.furnaceItemStacks.get(SLOT_INPUT)), level)
                .map(holder -> holder.value().assemble(new SingleRecipeInput(this.furnaceItemStacks.get(SLOT_INPUT)),
                        level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }

    /** {@code canSmelt} (:185-201) against the vanilla smelting recipes ({@code FurnaceRecipes.smelting()}). */
    private boolean canSmelt(Level level) {
        if (this.furnaceItemStacks.get(SLOT_INPUT).isEmpty()) { // :186-188
            return false;
        }
        ItemStack itemstack = this.smeltingResult(level);        // :189
        if (itemstack.isEmpty()) {                               // :190-192
            return false;
        }
        ItemStack output = this.furnaceItemStacks.get(SLOT_OUTPUT);
        if (output.isEmpty()) {                                  // :193-195
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(output, itemstack)) { // :196-198 isItemEqual
            return false;
        }
        int result = output.getCount() + itemstack.getCount();   // :199
        return result <= this.getMaxStackSize() && result <= itemstack.getMaxStackSize(); // :200
    }

    /** {@code smeltItem} (:203-219). */
    public void smeltItem(Level level) {
        if (this.canSmelt(level)) {
            ItemStack itemstack = this.smeltingResult(level);     // :205
            ItemStack output = this.furnaceItemStacks.get(SLOT_OUTPUT);
            if (output.isEmpty()) {                               // :206-208
                this.furnaceItemStacks.set(SLOT_OUTPUT, itemstack.copy());
            } else if (ItemStack.isSameItemSameComponents(output, itemstack)) { // :209-212
                output.grow(itemstack.getCount());
            }
            this.furnaceItemStacks.get(SLOT_INPUT).shrink(1);    // :213-217 (shrink leaves EMPTY at 0)
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Fuel (:221-276)
    // ---------------------------------------------------------------------------------------------

    /**
     * The 1.7.10 {@code Material.wood} census for the {@code block.getMaterial() == Material.wood}
     * branch (:231-233). 1.21.1 has no materials, so the set is written out: the vanilla 1.7.10
     * blocks of that material that had an {@code ItemBlock} (planks, logs, stairs, fences, fence
     * gates, trapdoors, pressure plates, note block, chests, crafting table, bookshelf, jukebox,
     * daylight detector, huge mushrooms) as the tags and blocks that hold the same things today,
     * plus the mod's own wood blocks ({@code Material.wood} in CrystalWood.java:13,
     * BlockCrystalTreeLog.java:21, BlockSkyTreeLog.java:16, BlockDuplicatorLog.java:15 and the
     * {@code BlockWorkbench} base of CrystalWorkbench.java:11). Not in the set, because they were
     * not {@code ItemBlock}s or not {@code Material.wood}: signs, doors, ladders, buttons.
     * Nether wood is excluded through {@code #minecraft:non_flammable_wood} - it did not exist and
     * is its own material.
     *
     * <p>PORT: R22 / BUGHUNT2 2.4 - the category, not the 2014 list. The blocks that 1.21.1 added to
     * it were taken from {@code Blocks.java}: Mojang replaced {@code Material.WOOD} by
     * {@code instrument(BASS)} plus {@code ignitedByLava()}, and every block item carrying both is in
     * the set now, through a tag or by name - bamboo blocks ({@code log(...)} helper, not in
     * {@code #minecraft:logs}), bamboo mosaic and its stairs (not in {@code #minecraft:wooden_stairs}),
     * mangrove roots, barrel, lectern, composter, loom, cartography/fletching/smithing table, chiseled
     * bookshelf, bee nest, beehive, campfire, soul campfire and the banners (1.8 {@code BlockBanner}:
     * {@code Material.wood}, {@code ItemBanner extends ItemBlock}). Still left out by the 1.7.10 rule
     * above: signs and hanging signs, doors ({@code ItemSign}/{@code ItemDoor} were no
     * {@code ItemBlock}s) and buttons ({@code Material.circuits}; no BASS in 1.21.1 either). The bamboo
     * mosaic slab is a wooden slab and goes to the 150 branch with the other wooden slabs.
     */
    private static final List<TagKey<Item>> MATERIAL_WOOD_TAGS = List.of(
            ItemTags.LOGS, ItemTags.PLANKS, ItemTags.WOODEN_STAIRS, ItemTags.WOODEN_FENCES,
            ItemTags.FENCE_GATES, ItemTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_PRESSURE_PLATES,
            ItemTags.BAMBOO_BLOCKS, ItemTags.BANNERS);
    private static final Set<Block> MATERIAL_WOOD_BLOCKS = Set.of(
            Blocks.NOTE_BLOCK, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.BOOKSHELF,
            Blocks.JUKEBOX, Blocks.DAYLIGHT_DETECTOR, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.RED_MUSHROOM_BLOCK,
            Blocks.MUSHROOM_STEM,
            // 1.21.1 members of the category (BASS + ignitedByLava in Blocks.java), see above.
            Blocks.BAMBOO_MOSAIC, Blocks.BAMBOO_MOSAIC_STAIRS, Blocks.MANGROVE_ROOTS, Blocks.BARREL,
            Blocks.LECTERN, Blocks.COMPOSTER, Blocks.LOOM, Blocks.CARTOGRAPHY_TABLE, Blocks.FLETCHING_TABLE,
            Blocks.SMITHING_TABLE, Blocks.CHISELED_BOOKSHELF, Blocks.BEE_NEST, Blocks.BEEHIVE,
            Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);

    private static boolean isMaterialWood(ItemStack stack, Block block) {
        if (MATERIAL_WOOD_BLOCKS.contains(block)
                || block == ModBlocks.CRYSTAL_PLANKS.get() || block == ModBlocks.CRYSTAL_TREE_LOG.get()
                || block == ModBlocks.SKY_TREE_LOG.get() || block == ModBlocks.DUPLICATOR_TREE_LOG.get()
                || block == ModBlocks.CRYSTALWORKBENCH.get()) {
            return true;
        }
        if (stack.is(ItemTags.NON_FLAMMABLE_WOOD)) {
            return false;
        }
        for (TagKey<Item> tag : MATERIAL_WOOD_TAGS) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@code getItemBurnTime} (:221-272) in the original order - the order decides: every wooden
     * block item hits the 300 branch before the 800/400 lines for the mod's own log and planks,
     * which are therefore unreachable (verhalten/itemblock-03.md) and stay unreachable here.
     *
     * <p>PORT: the final {@code GameRegistry.getFuelValue} (:271) asked Forge's {@code IFuelHandler}s;
     * the mod registered none, so it only ever saw other mods' fuels. DECISIONS R18 rules out the
     * 1.21.1 data-map fallback: anything not in this table is not fuel for the Crystal Furnace.
     */
    public static int getItemBurnTime(ItemStack stack) {
        if (stack.isEmpty()) { // :222-224
            return 0;
        }
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem && blockItem.getBlock() != Blocks.AIR) { // :226
            Block block = blockItem.getBlock();
            // PORT: R22 - the bamboo mosaic slab is a wooden slab outside #minecraft:wooden_slabs.
            if ((stack.is(ItemTags.WOODEN_SLABS) || block == Blocks.BAMBOO_MOSAIC_SLAB)
                    && !stack.is(ItemTags.NON_FLAMMABLE_WOOD)) { // :228 Blocks.wooden_slab
                return 150;
            }
            if (isMaterialWood(stack, block)) { // :231
                return 300;
            }
            if (block == Blocks.COAL_BLOCK) { // :234
                return 16000;
            }
        }
        // :238-246 ItemTool / ItemSword / ItemHoe with tool material "WOOD" - every wooden tool is a
        // TieredItem of Tiers.WOOD in 1.21.1.
        if (item instanceof TieredItem tiered && tiered.getTier() == Tiers.WOOD) {
            return 200;
        }
        if (item == Items.STICK) { // :247
            return 100;
        }
        if (item == Items.COAL || item == Items.CHARCOAL) { // :250 Items.coal covered both metadata values
            return 1600;
        }
        if (item == Items.LAVA_BUCKET) { // :253
            return 20000;
        }
        if (stack.is(ItemTags.SAPLINGS)) { // :256 Blocks.sapling, one block with six metadata values
            return 100;
        }
        if (item == Items.BLAZE_ROD) { // :259
            return 2400;
        }
        if (item == ModBlocks.CRYSTALCOAL.get().asItem()) { // :262
            return 20000;
        }
        if (item == ModBlocks.CRYSTAL_TREE_LOG.get().asItem()) { // :265 - unreachable, see above
            return 800;
        }
        if (item == ModBlocks.CRYSTAL_PLANKS.get().asItem()) { // :268 - unreachable, see above
            return 400;
        }
        return 0; // PORT: :271 GameRegistry.getFuelValue - no fallback (R18)
    }

    /** {@code isItemFuel} (:274-276). */
    public static boolean isItemFuel(ItemStack stack) {
        return getItemBurnTime(stack) > 0;
    }

    /**
     * The smelting experience of a taken output stack, the way 1.7.10
     * {@code FurnaceRecipes.smelting().func_151398_b(stack)} answered it: the experience of the
     * first smelting recipe whose <em>result</em> is the same item. PORT: 1.7.10 compared item and
     * damage value; the port compares the item only ({@link ItemStack#isSameItem}), and "first"
     * follows the recipe manager's order where 1.7.10 iterated a hash map.
     */
    public static float getSmeltingExperience(Level level, ItemStack result) {
        for (RecipeHolder<SmeltingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
            AbstractCookingRecipe recipe = holder.value();
            if (ItemStack.isSameItem(result, recipe.getResultItem(level.registryAccess()))) {
                return recipe.getExperience();
            }
        }
        return 0.0f;
    }

    // ---------------------------------------------------------------------------------------------
    // Sided access (:288-314)
    // ---------------------------------------------------------------------------------------------

    /** {@code isItemValidForSlot} (:288-290): never the output, fuel slot only for fuel. */
    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index != SLOT_OUTPUT && (index != SLOT_FUEL || isItemFuel(stack));
    }

    /** {@code getAccessibleSlotsFromSide} (:292-294): 1.7.10 side 0 = down, 1 = up. */
    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return SLOTS_BOTTOM;
        }
        return side == Direction.UP ? SLOTS_TOP : SLOTS_SIDES;
    }

    /** {@code canInsertItem} (:296-298). */
    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    /** {@code canExtractItem} (:300-302): from below, the fuel slot gives up only buckets. */
    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction != Direction.DOWN || index != SLOT_FUEL || stack.is(Items.BUCKET);
    }
}
