package com.portingdeadmods.cable_facades;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = CFMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CFConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCK_STRINGS = BUILDER.comment("List of blocks that are allowed to be covered. Supports '*' as a wildcard.")
            .defineListAllowEmpty("blocks", List.of("pipez:*_pipe","mekanism:*_cable","mekanism:*_conductor","mekanism:*_pipe","mekanism:*_tube","mekanism:*_transporter","mekanism_extras:*_cable","mekanism_extras:*_conductor","mekanism_extras:*_pipe","mekanism_extras:*_tube","mekanism_extras:*_transporter","thermal:*_duct","thermal:*_duct_windowed","computercraft:cable","powah:energy_cable_*","create:fluid_pipe","pneumaticcraft:*_tube","ppfluids:fluid_pipe","prettypipes:pipe","laserio:laser_*","cyclic:*_pipe","embers:*_pipe","embers:item_extractor","elementalcraft:elementpipe*","gtceu:*wire","gtceu:*pipe","enderio:*_conduit"), CFConfig::validateBlockName);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> NOT_ALLOWED_BLOCK_STRINGS = BUILDER.comment("List of blocks that are explicitly not allowed to be used as a cover. Supports '*' as a wildcard.")
            .defineListAllowEmpty("not_allowed_blocks", List.of(), CFConfig::validateBlockName);
    private static final ForgeConfigSpec.BooleanValue CONSUME_FACADE = BUILDER.comment("Whether the facade should be consumed when placed.")
            .define("consumeFacade", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    /**
     * Cache results for blocks in this map to avoid repeatedly matching strings
     */
    private static final Map<Block, Boolean> allowedBlocks = new HashMap<>();
    private static final Map<Block, Boolean> disallowedBlocks = new HashMap<>();
    private static final List<Pattern> blockPatterns = new ArrayList<>();
    private static final List<Pattern> notAllowedBlockPatterns = new ArrayList<>();
    public static boolean consumeFacade;

    private static boolean validateBlockName(final Object obj) {
        if (obj instanceof String blockName) {
            if (blockName.contains("*")) {
                return true;
            }
            return ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(blockName));
        }
        return false;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        consumeFacade = CONSUME_FACADE.get();

        allowedBlocks.clear();
        disallowedBlocks.clear();
        blockPatterns.clear();
        notAllowedBlockPatterns.clear();

        for (String blockName : BLOCK_STRINGS.get()) {
            if (blockName.contains("*")) {
                String regex = blockName.replace("*", ".*");
                blockPatterns.add(Pattern.compile(regex));
            } else {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
                if (block != null) {
                    allowedBlocks.put(block, true);
                }
            }
        }

        for (String blockName : NOT_ALLOWED_BLOCK_STRINGS.get()) {
            if (blockName.contains("*")) {
                String regex = blockName.replace("*", ".*");
                notAllowedBlockPatterns.add(Pattern.compile(regex));
            } else {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
                if (block != null) {
                    disallowedBlocks.put(block, true);
                }
            }
        }
    }

    public static boolean isBlockAllowed(Block targetBlock) {

        // Check if the block is already in the cache
        Boolean cached = allowedBlocks.get(targetBlock);
        if (cached != null) {
            return cached;
        }

        // If the block is not in the cache, check if it matches any of the patterns
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(targetBlock);
        if (blockId != null) {
            String blockIdString = blockId.toString();
            for (Pattern pattern : blockPatterns) {
                if (pattern.matcher(blockIdString).matches()) {
                    allowedBlocks.put(targetBlock, true);
                    return true;
                }
            }
        }
        allowedBlocks.put(targetBlock, false);
        return false;
    }

    public static boolean isBlockDisallowed(Block targetBlock) {
        // Check if the block is already in the cache
        Boolean cached = disallowedBlocks.get(targetBlock);
        if (cached != null) {
            return cached;
        }

        // If the block is not in the cache, check if it matches any of the patterns
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(targetBlock);
        if (blockId != null) {
            String blockIdString = blockId.toString();
            for (Pattern pattern : notAllowedBlockPatterns) {
                if (pattern.matcher(blockIdString).matches()) {
                    disallowedBlocks.put(targetBlock, true);
                    return true;
                }
            }
        }
        disallowedBlocks.put(targetBlock, false);
        return false;
    }
}
