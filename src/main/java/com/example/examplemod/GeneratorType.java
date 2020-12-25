package com.example.examplemod;

import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeneratorType {
    public static ForgeWorldType firstItem;
    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<ForgeWorldType> event) {
        event.getRegistry().registerAll(
                firstItem = new ForgeWorldType(new ForgeWorldType.IChunkGeneratorFactory() {
                    @Override
                    public ChunkGenerator createChunkGenerator(Registry<Biome> biomeRegistry, Registry<DimensionSettings> dimensionSettingsRegistry, long seed, String generatorSettings) {
                        return new NoiseChunkGenerator(new A(seed, false, false, biomeRegistry), seed, () -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.field_242734_c));
                    }
                }).setRegistryName("reg:test"));
    }

   public static class A extends OverworldBiomeProvider{
       private final Registry<Biome> lookupRegistry;
       private final boolean legacyBiomes;
       private final boolean largeBiomes;
       public A(long seed, boolean legacyBiomes, boolean largeBiomes, Registry<Biome> lookupRegistry) {
           super(seed, legacyBiomes, largeBiomes, lookupRegistry);
            this.lookupRegistry = lookupRegistry;
            this.legacyBiomes = legacyBiomes;
            this.largeBiomes = largeBiomes;
       }
       @Override
       public Biome getNoiseBiome(int x, int y, int z) {
           System.out.println(121);
           if (x*x+z*z<=500*500){
               return super.getNoiseBiome(x, y, z);
           }
           if (x*x+z*z<=512*512){
               return lookupRegistry.getOrThrow(Biomes.BEACH);
           }
           if (x*x+z*z<=512*512*2){
               return lookupRegistry.getOrThrow(Biomes.OCEAN);
           }
           return lookupRegistry.getOrThrow(Biomes.DEEP_OCEAN);
       }

       @OnlyIn(Dist.CLIENT)
       public BiomeProvider getBiomeProvider(long seed) {
           return new A(seed, this.legacyBiomes, this.largeBiomes, this.lookupRegistry);
       }
   }
}
