package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;

/**
 * Port of {@code danger.orespawn.CrystalPickaxe} (CrystalPickaxe.java:8-20): an
 * {@code ItemPickaxe} on the material passed in, stack 1, tab Tools, material durability and
 * harvest level (2 iron, 3 diamond, 4 netherite - R6). Four registrations (OreSpawnMain.java:1342,
 * :1347, :1352, :1357): {@code crystalwoodpickaxe}, {@code crystalpinkpickaxe},
 * {@code crystalstonepickaxe}, {@code tigerseye_pickaxe}.
 */
public class CrystalPickaxe extends PickaxeItem {

    public CrystalPickaxe(Material material, Item.Properties props) {
        super(material, props.attributes(OreSpawnTiers.pickaxe(material)));
    }
}
