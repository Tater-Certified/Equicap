package com.github.tatercertified.equicap.spawn_area;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class DynamicSpawnArea {
    private final RandomSource random;

    private int minRadius = 24;
    private int maxRadius = 128;
    private int minHeight = 24;
    private int maxHeight = 128;

    private BlockPos offset = BlockPos.ZERO;
    private SpawnShape shape = SpawnShape.SPHERE;

    /**
     * Creates the framework for getting positions within a DynamicSpawnArea
     * @param random Random instance from the World
     */
    public DynamicSpawnArea(RandomSource random) {
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
    public BlockPos.MutableBlockPos getCenterPosition(BlockPos playerPos) {
        return new BlockPos.MutableBlockPos(playerPos.getX() + offset.getX(),
                playerPos.getY() + offset.getY(),
                playerPos.getZ() + offset.getZ());
    }

    /**
     * Gets a random chunk location in the shape, respecting the min and max values
     * @param playerPos The player's position
     * @param world ServerWorld instance
     */
    public LevelChunk getRandomChunk(BlockPos playerPos, ServerLevel world) {
        BlockPos center = this.getCenterPosition(playerPos);
        return switch (shape) {
            case SPHERE -> sampleSphere(center, world);
            case CYLINDER -> sampleCylinder(center, world);
            case CUBE -> sampleCube(center, world);
        };
    }

    private LevelChunk posToChunk(ServerLevel world, int x, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        return world.getChunk(chunkX, chunkZ);
    }

    private LevelChunk sampleCylinder(BlockPos center, ServerLevel world) {
        float angle = random.nextFloat() * Mth.TWO_PI;
        int distance = random.nextIntBetweenInclusive(minRadius, maxRadius + 1) * (random.nextBoolean() ? -1 : 1);
        //int height = random.nextBetween(minHeight, maxHeight + 1) * (random.nextBoolean() ? -1 : 1);
        return posToChunk(world, (int) (Mth.cos(angle) * distance) + center.getX(), (int) (Mth.sin(angle) * distance) + center.getZ());
    }

    private LevelChunk sampleSphere(BlockPos center, ServerLevel world) {
        float angle = random.nextFloat() * Mth.TWO_PI;
        int distance = random.nextIntBetweenInclusive(minRadius, maxRadius + 1) * (random.nextBoolean() ? -1 : 1);
        return posToChunk(world, (int) (Mth.cos(angle) * distance) + center.getX(), (int) (Mth.sin(angle) * distance) + center.getZ());
    }

    private LevelChunk sampleCube(BlockPos center, ServerLevel world) {
        return posToChunk(world,
                center.getX() + random.nextIntBetweenInclusive(minRadius, maxRadius + 1) * (random.nextBoolean() ? -1 : 1),
                center.getZ() + random.nextIntBetweenInclusive(minRadius, maxRadius + 1) * (random.nextBoolean() ? -1 : 1)
        );
    }

    public BlockPos getRandomPosInChunkSection(Level world, LevelChunk chunk, int playerY) {
        // TODO This is an absolute mess
        return switch (this.shape) {
            case CUBE, CYLINDER -> {
                ChunkPos chunkPos = chunk.getPos();
                int x = chunkPos.getMinBlockX() + world.getRandom().nextInt(16);
                int z = chunkPos.getMinBlockZ() + world.getRandom().nextInt(16);
                int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
                int minAllowedY = Math.max(world.getMinY(), playerY - maxHeight);
                int maxAllowedY = Math.min(surfaceY, playerY + maxHeight);
                if (maxAllowedY < minAllowedY) {
                    maxAllowedY = minAllowedY;
                }

                int lowerMin = minAllowedY;
                int lowerMax = Math.min(maxAllowedY, playerY - minHeight);
                int upperMin = Math.max(minAllowedY, playerY + minHeight);
                int upperMax = maxAllowedY;

                int l;
                boolean hasLower = lowerMax >= lowerMin;
                boolean hasUpper = upperMax >= upperMin;
                if (hasLower && hasUpper) {
                    int lowerSize = lowerMax - lowerMin + 1;
                    int upperSize = upperMax - upperMin + 1;
                    if (world.getRandom().nextInt(lowerSize + upperSize) < lowerSize) {
                        l = Mth.randomBetweenInclusive(world.getRandom(), lowerMin, lowerMax);
                    } else {
                        l = Mth.randomBetweenInclusive(world.getRandom(), upperMin, upperMax);
                    }
                } else if (hasLower) {
                    l = Mth.randomBetweenInclusive(world.getRandom(), lowerMin, lowerMax);
                } else if (hasUpper) {
                    l = Mth.randomBetweenInclusive(world.getRandom(), upperMin, upperMax);
                } else {
                    l = Mth.randomBetweenInclusive(world.getRandom(), minAllowedY, maxAllowedY);
                }
                yield new BlockPos(x, l, z);
            }
            case SPHERE -> {
                ChunkPos chunkPos = chunk.getPos();
                int x = chunkPos.getMinBlockX() + world.getRandom().nextInt(16);
                int z = chunkPos.getMinBlockZ() + world.getRandom().nextInt(16);
                int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
                int minAllowedY = Math.max(world.getMinY(), playerY - maxRadius);
                int maxAllowedY = Math.min(surfaceY, playerY + maxRadius);
                if (maxAllowedY < minAllowedY) {
                    maxAllowedY = minAllowedY;
                }
                int l = Mth.randomBetweenInclusive(world.getRandom(), minAllowedY, maxAllowedY);
                yield new BlockPos(x, l, z);
            }
        };
    }
}
