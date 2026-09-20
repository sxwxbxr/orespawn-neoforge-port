package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.CrystalSword} (CrystalSword.java:8-27): an {@code ItemSword} on
 * the material passed in, stack 1, tab Combat, material durability. Four registrations
 * (OreSpawnMain.java:1341, :1346, :1351, :1356): {@code crystalwoodsword}, {@code crystalpinksword},
 * {@code crystalstonesword}, {@code tigerseye_sword}. Attack {@code 4 + material} in 1.7.10 (R6).
 *
 * <p>Blocks for 300 ticks: {@code getMaxItemUseDuration} (:19-21) is live in the jar as
 * {@code func_77626_a} (checked in CrystalSword.class), DECISIONS R19.
 */
public class CrystalSword extends BlockingSword {

    public CrystalSword(Material material, Item.Properties props) {
        super(material, props.attributes(OreSpawnTiers.sword(material)), 300);
    }
}
