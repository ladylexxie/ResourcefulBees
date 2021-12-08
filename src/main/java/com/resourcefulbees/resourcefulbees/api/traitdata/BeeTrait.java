package com.resourcefulbees.resourcefulbees.api.traitdata;

import com.resourcefulbees.resourcefulbees.ResourcefulBees;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.potion.Effect;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class BeeTrait {
    private final Item beepediaItem;
    private final List<Pair<Effect, Integer>> potionDamageEffects;
    private final List<String> damageImmunities;
    private final List<Effect> potionImmunities;
    private final List<Pair<String, Integer>> damageTypes;
    private final List<String> specialAbilities;
    private final List<BeeAura> beeAuras;
    private final BasicParticleType particleEffect;
    private final String name;

    private BeeTrait(String name, Item beepediaItem, List<Pair<Effect, Integer>> potionDamageEffects, List<String> damageImmunities, List<Effect> potionImmunities, List<Pair<String, Integer>> damageTypes, List<String> specialAbilities, BasicParticleType particleEffect, List<BeeAura> beeAuras) {
        this.name = name;
        this.beepediaItem = beepediaItem;
        this.potionDamageEffects = potionDamageEffects;
        this.damageImmunities = damageImmunities;
        this.potionImmunities = potionImmunities;
        this.damageTypes = damageTypes;
        this.specialAbilities = specialAbilities;
        this.particleEffect = particleEffect;
        this.beeAuras = beeAuras;
    }

    public boolean hasDamagePotionEffects() {
        return this.potionDamageEffects != null && !this.potionDamageEffects.isEmpty();
    }

    public boolean hasDamageImmunities() {
        return this.damageImmunities != null && !this.damageImmunities.isEmpty();
    }

    public boolean hasPotionImmunities() {
        return this.potionImmunities != null && !this.potionImmunities.isEmpty();
    }

    public boolean hasDamageTypes() {
        return this.damageTypes != null && !this.damageTypes.isEmpty();
    }

    public boolean hasSpecialAbilities() {
        return this.specialAbilities != null && !this.specialAbilities.isEmpty();
    }

    public boolean hasBeeAuras() {
        return this.beeAuras != null && !this.beeAuras.isEmpty();
    }

    public boolean hasParticleEffect() {
        return this.particleEffect != null;
    }

    public List<Pair<Effect, Integer>> getPotionDamageEffects() {
        return this.potionDamageEffects;
    }

    public List<String> getDamageImmunities() {
        return this.damageImmunities;
    }

    public List<Effect> getPotionImmunities() {
        return this.potionImmunities;
    }

    public List<Pair<String, Integer>> getDamageTypes() {
        return this.damageTypes;
    }

    public List<String> getSpecialAbilities() {
        return this.specialAbilities;
    }

    public BasicParticleType getParticleEffect() {
        return this.particleEffect;
    }

    public Item getBeepediaItem() {
        return beepediaItem == null ? Items.BLAZE_POWDER : beepediaItem;
    }

    public String getTranslationKey() {
        return String.format("trait.%s.%s", ResourcefulBees.MOD_ID, name);
    }

    public List<BeeAura> getAuras() {
        return beeAuras;
    }


    @SuppressWarnings("unused")
    public static class Builder {
        String name;
        Item beepediaItem;
        List<Pair<Effect, Integer>> potionDamageEffects = new ArrayList<>();
        List<String> damageImmunities = new ArrayList<>();
        List<Effect> potionImmunities = new ArrayList<>();
        List<Pair<String, Integer>> damageTypes = new ArrayList<>();
        List<String> specialAbilities = new ArrayList<>();
        BasicParticleType particleEffect;
        List<BeeAura> beeAuras = new ArrayList<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder setBeepediaItem(Item beepediaItem) {
            this.beepediaItem = beepediaItem;
            return this;
        }

        public Builder addDamagePotionEffects(List<Pair<Effect, Integer>> potionDamageEffects) {
            this.potionDamageEffects.addAll(potionDamageEffects);
            return this;
        }

        public Builder addDamagePotionEffect(Pair<Effect, Integer> potionDamageEffect) {
            this.potionDamageEffects.add(potionDamageEffect);
            return this;
        }

        public Builder addDamageImmunities(List<String> damageImmunities) {
            this.damageImmunities.addAll(damageImmunities);
            return this;
        }

        public Builder addDamageImmunity(String damageImmunity) {
            this.damageImmunities.add(damageImmunity);
            return this;
        }

        public Builder addPotionImmunities(List<Effect> potionImmunities) {
            this.potionImmunities.addAll(potionImmunities);
            return this;
        }

        public Builder addPotionImmunity(Effect potionImmunity) {
            this.potionImmunities.add(potionImmunity);
            return this;
        }

        public Builder addDamageTypes(List<Pair<String, Integer>> damageTypes) {
            this.damageTypes.addAll(damageTypes);
            return this;
        }

        public Builder addDamageType(Pair<String, Integer> damageType) {
            this.damageTypes.add(damageType);
            return this;
        }

        public Builder addSpecialAbilities(List<String> specialAbilities) {
            this.specialAbilities.addAll(specialAbilities);
            return this;
        }

        public Builder addSpecialAbility(String specialAbility) {
            this.specialAbilities.add(specialAbility);
            return this;
        }

        public Builder addAura(BeeAura beeAura) {
            this.beeAuras.add(beeAura);
            return this;
        }

        public Builder setParticleEffect(BasicParticleType particleEffect) {
            this.particleEffect = particleEffect;
            return this;
        }

        public BeeTrait build() {
            return new BeeTrait(name, beepediaItem, potionDamageEffects, damageImmunities, potionImmunities, damageTypes, specialAbilities, particleEffect, beeAuras);
        }
    }
}
