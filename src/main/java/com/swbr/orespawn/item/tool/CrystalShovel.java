package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import com.swbr.orespawn.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Port of {@code danger.orespawn.CrystalShovel} (CrystalShovel.java:12-34). The class extends
 * {@code ItemTool} <em>directly</em>, not {@code ItemSpade} (:12; manifest superclass
 * {@code ItemTool}), with base damage {@code 1.0f} (:17), stack 1, tab Tools, its own block set
 * (:31-33) and {@code canHarvestBlock} true only for the snow block (:22-24). Four registrations
 * (OreSpawnMain.java:1343, :1348, :1353, :1358): {@code crystalwoodshovel},
 * {@code crystalpinkshovel}, {@code crystalstoneshovel}, {@code tigerseye_shovel}.
 *
 * <p>What that meant in 1.7.10, and what the port keeps 1:1 through an explicit {@link Tool}
 * component instead of {@code ShovelItem}:
 * <ul>
 *   <li>Material efficiency on exactly the listed blocks, speed 1.0 everywhere else
 *       ({@code ItemTool.func_150893_a}).</li>
 *   <li>No Forge tool class "shovel" (only {@code ItemSpade} registered one), so the only block that
 *       counted the shovel as its harvest tool was {@code Blocks.snow} - snow layers dropped
 *       nothing, unlike with a vanilla spade (catalogue itemblock-01, CrystalShovel).</li>
 *   <li>No right-click action: paths did not exist, and a {@code TieredItem} has no
 *       {@code ItemAbilities}.</li>
 *   <li>{@code ItemTool.hitEntity}: two durability per hit.</li>
 * </ul>
 * PORT: R22 - {@code grass}, {@code dirt}, {@code sand} and {@code gravel} were the soil, sand and
 * gravel categories of 1.7.10. The speed rule takes {@code #minecraft:dirt} (grass block, dirt,
 * coarse dirt, podzol, mycelium, rooted dirt, moss, mud, muddy mangrove roots) and
 * {@code #minecraft:sand} (sand, red sand, suspicious sand - soul sand is not in it and was not in
 * the original either), gravel with suspicious gravel, and the dirt path as the 1.21.1 form of a
 * dirt block. Snow, snow layer, clay, farmland and crystal grass stay single, as named.
 */
public class CrystalShovel extends TieredItem {

    public CrystalShovel(Material material, Item.Properties props) {
        super(material, props
                .component(DataComponents.TOOL, tool(material))
                .attributes(OreSpawnTiers.shovel(material)));
    }

    /** {@code blocksEffectiveAgainst} (:32) plus the snow-only {@code canHarvestBlock} (:22-24). */
    private static Tool tool(Tier tier) {
        List<Block> effective = List.of(
                Blocks.GRAVEL, Blocks.SUSPICIOUS_GRAVEL,
                Blocks.DIRT_PATH,
                Blocks.SNOW_BLOCK,
                Blocks.SNOW,
                Blocks.CLAY,
                Blocks.FARMLAND,
                ModBlocks.CRYSTAL_GRASS.get());
        return new Tool(List.of(
                Tool.Rule.minesAndDrops(List.of(Blocks.SNOW_BLOCK), tier.getSpeed()),
                Tool.Rule.overrideSpeed(BlockTags.DIRT, tier.getSpeed()),
                Tool.Rule.overrideSpeed(BlockTags.SAND, tier.getSpeed()),
                Tool.Rule.overrideSpeed(effective, tier.getSpeed())),
                1.0f, 1);
    }

    /** {@code ItemTool.hitEntity}: {@code damageItem(2)} - mirrors {@code DiggerItem}. */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND);
    }
}
