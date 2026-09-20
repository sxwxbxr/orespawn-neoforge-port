package com.swbr.orespawn.block.ore;

import com.swbr.orespawn.block.misc.Legacy;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code OreGenericEgg} (OreGenericEgg.java:10-40): the 119 Ancient Dried Spawn Egg blocks
 * (OreSpawnMain.java:5908-6028) plus {@code blockenderpearl} and {@code blockeyeofender}
 * (OreSpawnMain.java:1634-1635) - the two "storage blocks" are plain instances of this class in the
 * source; only their recipes (9 pearls/eyes, :2970-2973, W12) make them storage blocks.
 *
 * <p>The block does nothing on its own: {@code Material.ground}, hardness 0.5, resistance 1, gravel sound
 * (:13-17), drops itself, and with a 50 % roll gives {@code 5 + nextInt(3) + nextInt(3)} experience
 * (:20-26). Turning an egg block into a spawn egg is a recipe (W12).
 *
 * <p>Ground material meant no tool requirement in 1.7.10 and no shovel bonus either ({@code ItemSpade}
 * listed vanilla blocks by identity, not by material), so there is no {@code mineable/shovel} tag.
 */
public class OreGenericEgg extends Block {

    /** {@code MyEnderPearlBlock} (OreSpawnMain.java:1634), legacy id +111. */
    public static final String ENDER_PEARL_ID = "blockenderpearl";
    /** {@code MyEyeOfEnderBlock} (OreSpawnMain.java:1635), legacy id +112. */
    public static final String EYE_OF_ENDER_ID = "blockeyeofender";

    /**
     * The 119 dried-egg ids (manifest {@code blocks}, {@code class == OreGenericEgg}, without the two
     * ender blocks), in legacy block-id order ({@code BaseBlockID + n}: 0-96, 119, 122, 125, 250-261,
     * 300-306) - the order the 1.7.10 creative tab showed them in.
     */
    public static final List<String> DRIED_EGG_IDS = List.of(
            "orespider", "orebat", "orecow", "orepig", "oresquid", "orechicken", "orecreeper", "oreskeleton",
            "orezombie", "oreslime", "oreghast", "orezombiepigman", "oreenderman", "orecavespider",
            "oresilverfish", "oremagmacube", "orewitch", "oresheep", "orewolf", "oremooshroom", "oreocelot",
            "oreblaze", "orewitherskeleton", "oreenderdragon", "oresnowgolem", "oreirongolem", "orewitherboss",
            "oregirlfriend", "oreredcow", "oregoldcow", "oreenchantedcow", "oremothra", "orealosaurus",
            "orecryolophosaurus", "orecamarasaurus", "orevelocityraptor", "orehydrolisc", "orebasilisc",
            "oredragonfly", "oreemperorscorpion", "orescorpion", "orecavefisher", "orespyro", "orebaryonyx",
            "oregammametroid", "orecockateil", "orekyuubi", "orealien", "oreattacksquid", "orewaterdragon",
            "orekraken", "orelizard", "orecephadrome", "oredragon", "orebee", "orehorse", "oretrooper",
            "orespit", "orestink", "oreostrich", "oregazelle", "orechipmunk", "orecreepinghorror",
            "oreterribleterror", "orecliffracer", "oretriffid", "orenightmare", "orelurkingterror",
            "oregodzillapart", "oresmallworm", "oremediumworm", "orelargeworm", "orecassowary",
            "orecloudshark", "oregoldfish", "oreleafmonster", "oretshirt", "oreenderknight", "oreenderreaper",
            "orebeaver", "oretrex", "orehercules", "oremantis", "orestinky", "oreboyfriend", "orethekingpart",
            "oreeasterbunny", "orecaterkiller", "oremolenoid", "oreseamonster", "oreseaviper", "oreleon",
            "orehammerhead", "orerubberducky", "orevillager", "orecriminal", "orethequeenpart",
            "oregodzilla", "oretheking", "orethequeen",
            "oreurchin", "oreflounder", "oreskate", "orerotator", "orepeacock", "orefairy", "oredungeonbeast",
            "orevortex", "orerat", "orewhale", "oreirukandji", "orecrystalcow",
            "orebrutalfly", "orenastysaurus", "orepointysaurus", "orecricket", "orefrog", "orespiderdriver",
            "orecrab");

    public OreGenericEgg(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** {@code Material.ground}, hardness 0.5, resistance 1, gravel sound (:13-16). */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(0.5f, Legacy.resistance(1.0f))
                .sound(SoundType.GRAVEL);
    }

    /**
     * {@code dropBlockAsItemWithChance} (:20-26): the amount is rolled before the 50 % roll, in that order,
     * on every drop path including explosions. Silk Touch skipped the method in 1.7.10
     * ({@code canSilkHarvest} true for a normal cube); {@code stack} carries that here.
     *
     * <p>PORT: {@code dropExperience} is ignored - see {@link Legacy#dropXpOnBlockBreak}.
     */
    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        int j1 = 5 + level.random.nextInt(3) + level.random.nextInt(3);
        if (level.random.nextInt(2) == 1) {
            Legacy.dropXpOnBlockBreak(this, level, pos, stack, j1);
        }
    }
}
