package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.CrystalAxe} (CrystalAxe.java:8-20): an {@code ItemAxe} on the
 * material passed in, stack 1, tab Tools, no {@code setMaxDamage} and no {@code setHarvestLevel} -
 * the material's values apply. Four registrations (OreSpawnMain.java:1345, :1350, :1355, :1360):
 * {@code crystalwoodaxe} (CRYSTALWOOD), {@code crystalpinkaxe} (CRYSTALPINK),
 * {@code crystalstoneaxe} (CRYSTALSTONE), {@code tigerseye_axe} (TIGERSEYE).
 */
public class CrystalAxe extends AxeItem {

    public CrystalAxe(Material material, Item.Properties props) {
        super(material, props.attributes(OreSpawnTiers.axe(material)));
    }
}
