package com.example.examplemod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.*;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;
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
                firstItem = new ForgeWorldType(GeneratorType::createChunkGen).setRegistryName("reg:test"));
    }
    private static ChunkGenerator createChunkGen(Registry<Biome> biomeReg, Registry<DimensionSettings> dimSettingsReg, long seed) {
        return new NoiseChunkGenerator(new TestBiomeProvider(seed, false, false, biomeReg), seed, () -> dimSettingsReg.getOrThrow(DimensionSettings.field_242734_c));
    }
   public static class TestBiomeProvider extends OverworldBiomeProvider{
       private final Registry<Biome> lookupRegistry;
       private final boolean legacyBiomes;
       private final boolean largeBiomes;
       private final long seed;
       public static final Codec<TestBiomeProvider> CODEC = RecordCodecBuilder.create((builder) -> {
           return builder.group(Codec.LONG.fieldOf("seed").stable().forGetter((overworldProvider) -> {
               return overworldProvider.seed;
           }), Codec.BOOL.optionalFieldOf("legacy_biome_init_layer", Boolean.valueOf(false), Lifecycle.stable()).forGetter((overworldProvider) -> {
               return overworldProvider.legacyBiomes;
           }), Codec.BOOL.fieldOf("large_biomes").orElse(false).stable().forGetter((overworldProvider) -> {
               return overworldProvider.largeBiomes;
           }), RegistryLookupCodec.getLookUpCodec(Registry.BIOME_KEY).forGetter((overworldProvider) -> {
               return overworldProvider.lookupRegistry;
           })).apply(builder, builder.stable(TestBiomeProvider::new));
       });
       public TestBiomeProvider(long seed, boolean legacyBiomes, boolean largeBiomes, Registry<Biome> lookupRegistry) {
           super(seed, legacyBiomes, largeBiomes, lookupRegistry);
            this.lookupRegistry = lookupRegistry;
            this.legacyBiomes = legacyBiomes;
            this.largeBiomes = largeBiomes;
            this.seed = seed;
       }
       @Override
       public Biome getNoiseBiome(int x, int y, int z) {
           if (x*x+z*z<=500*500){
               return super.getNoiseBiome(x, y, z);
           }
           if (x*x+z*z<=512*512*2){
               return lookupRegistry.getOrThrow(Biomes.OCEAN);
           }
           return lookupRegistry.getOrThrow(Biomes.DEEP_OCEAN);
       }
       protected Codec<? extends BiomeProvider> getBiomeProviderCodec() {
           return CODEC;
       }

       @OnlyIn(Dist.CLIENT)
       public BiomeProvider getBiomeProvider(long seed) {
           return new TestBiomeProvider(seed, this.legacyBiomes, this.largeBiomes, this.lookupRegistry);
       }
   }
    static {
        Registry.register(Registry.BIOME_PROVIDER_CODEC, "test", TestBiomeProvider.CODEC);
    }
}
