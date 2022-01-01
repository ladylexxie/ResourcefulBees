package com.resourcefulbees.resourcefulbees.compat.jei;

import com.resourcefulbees.resourcefulbees.ResourcefulBees;
import com.resourcefulbees.resourcefulbees.api.beedata.CustomBeeData;
import com.resourcefulbees.resourcefulbees.client.gui.screen.CentrifugeScreen;
import com.resourcefulbees.resourcefulbees.client.gui.screen.MechanicalCentrifugeScreen;
import com.resourcefulbees.resourcefulbees.compat.jei.ingredients.EntityIngredient;
import com.resourcefulbees.resourcefulbees.compat.jei.ingredients.EntityIngredientFactory;
import com.resourcefulbees.resourcefulbees.compat.jei.ingredients.EntityIngredientHelper;
import com.resourcefulbees.resourcefulbees.compat.jei.ingredients.EntityRenderer;
import com.resourcefulbees.resourcefulbees.item.Beepedia;
import com.resourcefulbees.resourcefulbees.registry.BeeRegistry;
import com.resourcefulbees.resourcefulbees.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.resourcefulbees.resourcefulbees.recipe.CentrifugeRecipe.CENTRIFUGE_RECIPE_TYPE;

@JeiPlugin
public class JEICompat implements IModPlugin {

    public static final IIngredientType<EntityIngredient> ENTITY_INGREDIENT = () -> EntityIngredient.class;

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new BeeHiveCategory(helper));
        registration.addRecipeCategories(new BeeBreedingCategory(helper));
        registration.addRecipeCategories(new FlowersCategory(helper));
        registration.addRecipeCategories(new EntityFlowerCategory(helper));
        registration.addRecipeCategories(new CentrifugeRecipeCategory(helper));
        registration.addRecipeCategories(new BlockMutation(helper));
        registration.addRecipeCategories(new EntityToEntity(helper));
        registration.addRecipeCategories(new BlockToItem(helper));
        registration.addRecipeCategories(new ApiaryCategory(helper));
    }

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ResourcefulBees.MOD_ID, "jei");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.T1_BEEHIVE_ITEM.get()), BeeHiveCategory.ID);
        registration.addRecipeCatalyst(new ItemStack(ModItems.T2_BEEHIVE_ITEM.get()), BeeHiveCategory.ID);
        registration.addRecipeCatalyst(new ItemStack(ModItems.T3_BEEHIVE_ITEM.get()), BeeHiveCategory.ID);
        registration.addRecipeCatalyst(new ItemStack(ModItems.T4_BEEHIVE_ITEM.get()), BeeHiveCategory.ID);
        registration.addRecipeCatalyst(new ItemStack(ModItems.T1_APIARY_ITEM.get()), ApiaryCategory.ID);
        registration.addRecipeCatalyst(new ItemStack(ModItems.T2_APIARY_ITEM.get()), ApiaryCategory.ID);
        registration.addRecipeCatalyst(new ItemStack(ModItems.T3_APIARY_ITEM.get()), ApiaryCategory.ID);
        registration.addRecipeCatalyst(new ItemStack(ModItems.T4_APIARY_ITEM.get()), ApiaryCategory.ID);
        registration.addRecipeCatalyst(new ItemStack(ModItems.CENTRIFUGE_ITEM.get()), CentrifugeRecipeCategory.ID);
        registration.addRecipeCatalyst(new ItemStack(ModItems.MECHANICAL_CENTRIFUGE_ITEM.get()), CentrifugeRecipeCategory.ID);
        registration.addRecipeCatalyst(new ItemStack(ModItems.CENTRIFUGE_CONTROLLER_ITEM.get()), CentrifugeRecipeCategory.ID);
        registration.addRecipeCatalyst(new ItemStack(ModItems.ELITE_CENTRIFUGE_CONTROLLER_ITEM.get()), CentrifugeRecipeCategory.ID);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        World clientWorld = Minecraft.getInstance().level;
        if (clientWorld != null) {
            RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
            registration.addRecipes(BeeHiveCategory.getHoneycombRecipes(), BeeHiveCategory.ID);
            registration.addRecipes(recipeManager.byType(CENTRIFUGE_RECIPE_TYPE).values(), CentrifugeRecipeCategory.ID);
            registration.addRecipes(BeeBreedingCategory.getBreedingRecipes(), BeeBreedingCategory.ID);
            registration.addRecipes(BlockMutation.getMutationRecipes(), BlockMutation.ID);
            registration.addRecipes(BlockToItem.getMutationRecipes(), BlockToItem.ID);
            registration.addRecipes(EntityToEntity.getMutationRecipes(), EntityToEntity.ID);
            registration.addRecipes(ApiaryCategory.getHoneycombRecipes(), ApiaryCategory.ID);
            registration.addRecipes(FlowersCategory.getFlowersRecipes(), FlowersCategory.ID);
            registration.addRecipes(EntityFlowerCategory.getFlowersRecipes(), EntityFlowerCategory.ID);
            registerInfoDesc(registration);
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(MechanicalCentrifugeScreen.class, 80, 30, 18, 18, CentrifugeRecipeCategory.ID);

        registration.addGuiContainerHandler(CentrifugeScreen.class, new IGuiContainerHandler<CentrifugeScreen>() {

            @Override
            public @NotNull Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull CentrifugeScreen screen, double mouseX, double mouseY) {
                IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(screen.getXSize() - 25, 50, 18, 18, CentrifugeRecipeCategory.ID);
                return Collections.singleton(clickableArea);
            }
        });
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        List<EntityIngredient> entityIngredients = EntityIngredientFactory.create();
        registration.register(ENTITY_INGREDIENT, entityIngredients, new EntityIngredientHelper(), new EntityRenderer());
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.BEEPEDIA.get(), itemStack -> itemStack.hasTag() && itemStack.getTag().contains(Beepedia.CREATIVE_TAG) ? "creative.beepedia" : "");
    }

    public void registerInfoDesc(IRecipeRegistration registration) {
        for (EntityIngredient bee : EntityIngredientFactory.create()) {
            CustomBeeData beeData = BeeRegistry.getRegistry().getBeeData(bee.getBeeType());

            StringBuilder stats = new StringBuilder();
            String aqua = TextFormatting.DARK_AQUA.toString();
            String purple = TextFormatting.DARK_PURPLE.toString();


            stats.append(aqua).append(" Base Health: ").append(purple).append(beeData.getCombatData().getBaseHealth()).append("\n");
            stats.append(aqua).append(" Attack Damage: ").append(purple).append(beeData.getCombatData().getAttackDamage()).append("\n");
            stats.append(aqua).append(" Has Honeycomb: ").append(purple).append(StringUtils.capitalize(String.valueOf(beeData.hasHoneycomb()))).append("\n");
            stats.append(aqua).append(" Max Time in Hive: ").append(purple).append(beeData.getMaxTimeInHive()).append(" ticks\n");

            stats.append(aqua).append(" Has Mutation: ").append(purple).append(StringUtils.capitalize(String.valueOf(beeData.getMutationData().hasMutation()))).append("\n");
            if (beeData.getMutationData().hasMutation()) {
                stats.append(aqua).append(" Mutation Count: ").append(purple).append(StringUtils.capitalize(String.valueOf(beeData.getMutationData().getMutationCount()))).append("\n");
            }

            stats.append(aqua).append(" Is Breedable: ").append(purple).append(StringUtils.capitalize(String.valueOf(beeData.getBreedData().isBreedable()))).append("\n");
            if (beeData.getBreedData().isBreedable() && beeData.getBreedData().hasParents()) {
                stats.append(aqua).append(" Parents: ").append(purple).append(StringUtils.capitalize(beeData.getBreedData().getParent1())).append(" Bee, ")
                        .append(StringUtils.capitalize(beeData.getBreedData().getParent2())).append(" Bee\n");
            }

            if (beeData.hasTraitNames()) {
                StringJoiner traits = new StringJoiner(", ");
                //noinspection deprecation
                Arrays.stream(beeData.getTraitNames()).forEach(trait -> traits.add(WordUtils.capitalize(trait.replace("_", " "))));
                stats.append(aqua).append(" Traits: ").append(purple).append(traits.toString()).append("\n");
            }

            stats.append(aqua).append(" Spawns in World: ").append(purple).append(StringUtils.capitalize(String.valueOf(beeData.getSpawnData().canSpawnInWorld()))).append("\n");
            if (beeData.getSpawnData().canSpawnInWorld()) {
                stats.append(aqua).append(" Light Level: ").append(purple).append(beeData.getSpawnData().getLightLevel()).append("\n");
                stats.append(aqua).append(" Min Y Level: ").append(purple).append(beeData.getSpawnData().getMinYLevel()).append("\n");
                stats.append(aqua).append(" Max Y Level: ").append(purple).append(beeData.getSpawnData().getMaxYLevel()).append("\n");
                stats.append(aqua).append(" Min Group Size: ").append(purple).append(beeData.getSpawnData().getMinGroupSize()).append("\n");
                stats.append(aqua).append(" Max Group Size: ").append(purple).append(beeData.getSpawnData().getMaxGroupSize()).append("\n");
                stats.append(aqua).append(" Biomes: ").append(purple).append(BiomeParser.parseBiomes(beeData));
            }

            registration.addIngredientInfo(bee, ENTITY_INGREDIENT, stats.toString());
        }
    }
}
