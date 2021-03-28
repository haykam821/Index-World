package io.github.haykam821.indexworld;

import io.github.haykam821.indexworld.world.gen.IndexWorldChunkGenerator;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Main implements ModInitializer {
	private static final String MOD_ID = "indexworld";
	private static final Identifier INDEX_WORLD_ID = new Identifier(MOD_ID, "index_world");

	@Override
	public void onInitialize() {
		Registry.register(Registry.CHUNK_GENERATOR, INDEX_WORLD_ID, IndexWorldChunkGenerator.CODEC);
	}
}
