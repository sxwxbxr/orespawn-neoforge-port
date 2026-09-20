package com.swbr.orespawn.combat;

import com.swbr.orespawn.OreSpawn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Decides for whom the 1.7.10 armor formula applies and which armor value it sees
 * (docs/DECISIONS.md, R5). The arithmetic itself is {@link LegacyCombatMath#damageAfterArmor};
 * the hook that wires it into NeoForge's damage sequence is {@link CombatEvents}.
 *
 * <p>R5 in full: the formula applies when the target is an OreSpawn entity or a player wearing at
 * least one OreSpawn armor piece. For players the armor sum is computed from the item values, not
 * from the {@code ARMOR} attribute, which 1.21.1 clamps to 30 - the Royal Guardian set alone is 42.
 */
public final class LegacyArmorFormula {

    /**
     * Item tag that marks OreSpawn armor. W03 fills it with every {@code ItemOreSpawnArmor} port.
     * {@link LegacyArmorItem} is the code-side alternative; either one is enough.
     */
    public static final TagKey<Item> LEGACY_ARMOR =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "legacy_armor"));

    private LegacyArmorFormula() {}

    /** {@return whether the entity's type is registered under the {@code orespawn} namespace} */
    public static boolean isOreSpawnEntity(Entity entity) {
        return EntityType.getKey(entity.getType()).getNamespace().equals(OreSpawn.MOD_ID);
    }

    /** {@return whether the stack is OreSpawn armor, by tag or by marker interface} */
    public static boolean isLegacyArmor(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof LegacyArmorItem || stack.is(LEGACY_ARMOR));
    }

    /** {@return whether the entity wears at least one piece of OreSpawn armor} */
    public static boolean wearsLegacyArmor(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorSlots()) {
            if (isLegacyArmor(stack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * R5's condition: an OreSpawn entity, or a player wearing at least one OreSpawn armor piece.
     * Other mobs wearing OreSpawn armor keep the vanilla formula - the ruling names the player.
     */
    public static boolean applies(LivingEntity entity) {
        if (isOreSpawnEntity(entity)) {
            return true;
        }
        return entity instanceof Player && wearsLegacyArmor(entity);
    }

    /**
     * The armor points the 1.7.10 formula uses for this entity, in the order the original resolved
     * them: an explicit {@code getTotalArmorValue()} override first, then the player's item sum
     * ({@code InventoryPlayer.getTotalArmorValue}), then the attribute for a mob without override.
     */
    public static int armorValue(LivingEntity entity) {
        if (entity instanceof LegacyArmor legacy) {
            return legacy.getLegacyArmorValue();
        }
        if (entity instanceof Player) {
            return armorFromEquipment(entity);
        }
        return entity.getArmorValue();
    }

    /**
     * The sum of the armor the entity's worn items grant, unclamped: every {@code ADD_VALUE}
     * modifier on {@code Attributes.ARMOR} in each armor slot. {@code ArmorItem} contributes its
     * defense this way ({@code ArmorItem.createAttributes}), so vanilla, OreSpawn and third-party
     * armor all count - the 1.7.10 equivalent of summing {@code ItemArmor.damageReduceAmount}.
     *
     * <p>Girlfriend and Boyfriend do not use this helper: their {@code getTotalArmorValue} summed the
     * {@code ItemArmor} of all five 1.7.10 equipment slots, the held item included, clamped to 8..23
     * ({@code entity.companion.CompanionSupport.totalArmorValue}).
     */
    public static int armorFromEquipment(LivingEntity entity) {
        double[] sum = {0.0};
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            stack.forEachModifier(slot, (attribute, modifier) -> {
                // Holder.is(Holder) is deprecated in 1.21.1; both sides are Holder<Attribute>, so
                // the held instances compare directly.
                if (attribute.value() == Attributes.ARMOR.value()
                        && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                    sum[0] += modifier.amount();
                }
            });
        }
        // 1.7.10 summed ints; ArmorItem's defense is an int too, so the floor is exact for them.
        return (int) Math.floor(sum[0]);
    }
}
