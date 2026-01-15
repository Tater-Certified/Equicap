package com.github.tatercertified.equicap.spawn_area;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public class DynamicSpawnArea {
    private final Random random;

    private int minRadius = 24;
    private int maxRadius = 128;
    private int minHeight = 24;
    private int maxHeight = 128;

    private BlockPos offset = BlockPos.ORIGIN;
    private SpawnShape shape = SpawnShape.SPHERE;

    /**
     * Creates the framework for getting positions within a DynamicSpawnArea
     * @param random Random instance from the World
     */
    public DynamicSpawnArea(Random random) {
        this.random = random;
    }

    /**
     * Sets the radius where mobs can't spawn near the player
     * @param minRadius Radius value; Default 24
     */
    public void setMinRadius(int minRadius) {
        this.minRadius = minRadius;
    }

    /**
     * Sets the radius where mobs despawn instantly
     * @param maxRadius Radius value; Default 128
     */
    public void setMaxRadius(int maxRadius) {
        this.maxRadius = maxRadius;
    }

    /**
     * Sets the minimum height where mobs can't spawn vertically above or below the player.
     * Not applicable to spheres
     * @param minHeight Height value; Default 24
     */
    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    /**
     * Sets the max height where mobs will instantly despawn above or below the player.
     * Not applicable to spheres
     * @param maxHeight Height value; Default 128
     */
    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    /**
     * The offset from the player's position. Useful if you only want mobs spawning in a specific relative location.
     * @param offset Block offset; Default is (0,0,0)
     */
    public void setOffset(BlockPos offset) {
        this.offset = offset;
    }

    /**
     * Sets the shape of the spawn area
     * @param shape {@link SpawnShape}
     */
    public void setShape(SpawnShape shape) {
        this.shape = shape;
    }

    /**
     * Gets the position after being translated by the offset
     * @param playerPos The player's position
     * @return A mutable instance of the center position of the spawn area
     */
    public BlockPos.Mutable getCenterPosition(BlockPos playerPos) {
        return new BlockPos.Mutable(playerPos.getX() + offset.getX(),
                playerPos.getY() + offset.getY(),
                playerPos.getZ() + offset.getZ());
    }

    /**
     * Gets a random chunk location in the shape, respecting the min and max values
     * @param playerPos The player's position
     * @param world ServerWorld instance
     */
    public WorldChunk getRandomChunk(BlockPos playerPos, ServerWorld world) {
        BlockPos center = this.getCenterPosition(playerPos);
        return switch (shape) {
            case CYLINDER -> sampleCylinder(center, world);
            case CUBE -> sampleCube(center, world);
        };
    }

    private WorldChunk posToChunk(ServerWorld world, int x, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        return world.getChunk(chunkX, chunkZ);
    }

    private WorldChunk sampleCylinder(BlockPos center, ServerWorld world) {
        float angle = random.nextFloat() * MathHelper.TAU;
        int distance = random.nextBetween(minRadius, maxRadius + 1) * (random.nextBoolean() ? -1 : 1);
        //int height = random.nextBetween(minHeight, maxHeight + 1) * (random.nextBoolean() ? -1 : 1);
        return posToChunk(world, (int) (MathHelper.cos(angle) * distance) + center.getX(), (int) (MathHelper.sin(angle) * distance) + center.getZ());
    }

    private WorldChunk sampleCube(BlockPos center, ServerWorld world) {
        return posToChunk(world,
                center.getX() + random.nextBetween(minRadius, maxRadius + 1) * (random.nextBoolean() ? -1 : 1),
                center.getZ() + random.nextBetween(minRadius, maxRadius + 1) * (random.nextBoolean() ? -1 : 1)
        );
    }

    public BlockPos getRandomPosInChunkSection(World world, WorldChunk chunk, int playerY) {
        // TODO This is an absolute mess
        return switch (this.shape) {
            case CUBE -> {
                ChunkPos chunkPos = chunk.getPos();
                int x = chunkPos.getStartX() + world.random.nextInt(16);
                int z = chunkPos.getStartZ() + world.random.nextInt(16);
                int max = MathHelper.clamp(chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z) + 1, playerY - maxHeight, playerY + maxHeight);
                int l = MathHelper.nextBetween(world.random, world.getBottomY(), k);
                yield new BlockPos(x, l, z);
            }
            case CYLINDER -> {
            }
        };
    }
}
