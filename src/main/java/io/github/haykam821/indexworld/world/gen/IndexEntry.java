package io.github.haykam821.indexworld.world.gen;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;

public class IndexEntry implements Comparable<IndexEntry> {
	private static final BlockState SIGN = Blocks.BIRCH_WALL_SIGN.getDefaultState()
		.with(WallSignBlock.FACING, Direction.WEST);

	private Identifier id;
	private BlockState state;
	private ItemStack stack;

	private IndexEntry(Identifier id, BlockState state, ItemStack stack) {
		this.id = id;
		this.state = state;
		this.stack = stack;
	}

	public void placeBlocks(ChunkRegion region, Chunk chunk, BlockPos pos, int y) {
		if (y == 0 || y == 1) {
			BlockPos signPos = pos.offset(Direction.WEST);
			region.setBlockState(signPos, SIGN, 2);

			SignBlockEntity sign = (SignBlockEntity) region.getBlockEntity(signPos);
			if (y == 0) {
				sign.setTextOnRow(1, new LiteralText(this.id.getNamespace()));
				sign.setTextOnRow(2, new LiteralText(this.id.getPath()));
			} else {
				sign.setTextOnRow(1, this.stack.getName());
			}
			chunk.setBlockEntity(signPos, sign);
		} else if (y == 2 && this.state != null) {
			chunk.setBlockState(pos, this.state, false);
		}
	}

	@Override
	public int compareTo(IndexEntry other) {
		return this.id.compareTo(other.id);
	}

	public static IndexEntry fromItem(Identifier id, Item item) {
		ItemStack stack = new ItemStack(item);

		if (item instanceof BlockItem) {
			Block block = ((BlockItem) item).getBlock();
			return new IndexEntry(id, block.getDefaultState(), stack);
		}
		return new IndexEntry(id, null, stack);
	}

	public static boolean hasBlockEntity(Item item) {
		if (!(item instanceof BlockItem)) return false;

		Block block = ((BlockItem) item).getBlock();
		return block.hasBlockEntity();
	}
}
