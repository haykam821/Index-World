package io.github.haykam821.indexworld.world.gen;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;

public class IndexWorldChunkGenerator extends ChunkGenerator {
	public static final Codec<IndexWorldChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.populationSource),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("wall_provider", new SimpleBlockStateProvider(Blocks.BARRIER.getDefaultState())).forGetter(generator -> generator.wallProvider),
			Codec.INT.optionalFieldOf("start_y", 64).forGetter(generator -> generator.startY),
			Codec.INT.optionalFieldOf("top_padding", 1).forGetter(generator -> generator.topPadding),
			Codec.INT.optionalFieldOf("bottom_padding", 8).forGetter(generator -> generator.bottomPadding),
			Codec.BOOL.optionalFieldOf("filter_block_entities", true).forGetter(generator -> generator.filterBlockEntities)
		).apply(instance, IndexWorldChunkGenerator::new);
	});

	private final BlockStateProvider wallProvider;
	private final int startY;
	private final int topPadding;
	private final int bottomPadding;
	private final boolean filterBlockEntities;

	private final List<IndexEntry> entries;

	public IndexWorldChunkGenerator(BiomeSource biomeSource, BlockStateProvider wallProvider, int startY, int topPadding, int bottomPadding, boolean filterBlockEntities) {
		super(biomeSource, new StructuresConfig(Optional.empty(), Collections.emptyMap()));

		this.wallProvider = wallProvider;
		this.startY = startY;
		this.topPadding = topPadding;
		this.bottomPadding = bottomPadding;
		this.filterBlockEntities = filterBlockEntities;

		this.entries = Registry.ITEM.getEntries().stream().map(entry -> {
			if (filterBlockEntities && IndexEntry.hasBlockEntity(entry.getValue())) return null;

			Identifier id = entry.getKey().getValue();
			return IndexEntry.fromItem(id, entry.getValue());
		}).filter(Predicates.notNull()).sorted().collect(Collectors.toList());
	}

	@Override
	public void generateFeatures(ChunkRegion region, StructureAccessor accessor) {
		if (region.getCenterChunkX() != 0) return;

		BlockPos.Mutable pos = new BlockPos.Mutable();
		int startZ = region.getCenterChunkZ() << 4;
		
		Chunk chunk = region.getChunk(region.getCenterChunkX(), region.getCenterChunkZ());

		for (int z = 0; z < 16; z++) {
			int index = startZ + z;

			if (index < 0) continue;
			if (index >= this.entries.size()) continue;

			IndexEntry entry = this.entries.get(index);

			for (int y = this.startY - this.bottomPadding; y < this.startY + 3 + this.topPadding; y++) {
				pos.set(0, y, index);
				region.setBlockState(pos, this.wallProvider.getBlockState(region.getRandom(), pos), 2);

				entry.placeBlocks(region, chunk, pos, y - this.startY);
			}
		}
	}

	@Override
	public void populateEntities(ChunkRegion region) {
		return;
	}

	@Override
	public void populateNoise(WorldAccess world, StructureAccessor accessor, Chunk chunk) {
		return;
	}

	@Override
	public void buildSurface(ChunkRegion region, Chunk chunk) {
		return;
	}

	@Override
	public BlockView getColumnSample(int x, int z) {
		return new VerticalBlockSample(new BlockState[0]);
	}

	@Override
	public int getHeight(int x, int z, Heightmap.Type heightmapType) {
		return 0;
	}

	@Override
	public int getSpawnHeight() {
		return this.startY;
	}

	@Override
	public Codec<IndexWorldChunkGenerator> getCodec() {
		return IndexWorldChunkGenerator.CODEC;
	}

	@Override
	public ChunkGenerator withSeed(long seed) {
		return this;
	}
}
