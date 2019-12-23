package org.valkyrienskies.mod.common.physmanagement.chunk;

import com.google.common.collect.Streams;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import javax.annotation.concurrent.Immutable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

/**
 * This stores the chunk claims for a PhysicsObject; not the chunks themselves
 *
 * @author thebest108
 */
@Immutable
@Value
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE) // For Kryo
public class VSChunkClaim {

    private final int centerX;
    private final int centerZ;
    private final int radius;

    public VSChunkClaim(NBTTagCompound readFrom) {
        this(readFrom.getInteger("centerX"), readFrom.getInteger("centerZ"),
            readFrom.getInteger("radius"));
    }

    public void writeToNBT(NBTTagCompound toSave) {
        toSave.setInteger("centerX", getCenterX());
        toSave.setInteger("centerZ", getCenterZ());
        toSave.setInteger("radius", getRadius());
    }

    /**
     * Checks if a chunk is contained within this {@link VSChunkClaim}
     *
     * @param chunkX The X value of the chunk
     * @param chunkZ The Y value of the chunk
     * @return True if the specified chunk is contained within this {@link VSChunkClaim}
     */
    public boolean containsChunk(int chunkX, int chunkZ) {
        boolean inX = (chunkX >= minX()) && (chunkX <= maxX());
        boolean inZ = (chunkZ >= minZ()) && (chunkZ <= maxZ());
        return inX && inZ;
    }

    public boolean containsChunk(ChunkPos pos) {
        return containsChunk(pos.x, pos.z);
    }

    /**
     * Checks if a block is contained within this {@link VSChunkClaim}
     *
     * @return True if the specified block is contained within this {@link VSChunkClaim}
     */
    public boolean containsBlock(BlockPos pos) {
        return containsChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public ChunkPos absoluteToRelative(ChunkPos pos) {
        return new ChunkPos(pos.x - minX(), pos.z - minZ());
    }

    public ChunkPos relativeToAbsolute(ChunkPos pos) {
        return new ChunkPos(pos.x + minX(), pos.z + minZ());
    }

    @Override
    public String toString() {
        return getCenterX() + ":" + getCenterZ() + ":" + getRadius();
    }

    /**
     * @return A stream of the {@link ChunkPos} of every chunk inside of this claim.
     */
    public Stream<ChunkPos> stream() {
        return Streams.stream(new ChunkPosIterator());
    }

    /**
     * @return the maxX
     */
    public int maxX() {
        return getCenterX() + getRadius();
    }

    /**
     * @return the maxZ
     */
    public int maxZ() {
        return getCenterZ() + getRadius();
    }

    /**
     * @return the minZ
     */
    public int minZ() {
        return getCenterZ() - getRadius();
    }

    /**
     * @return the minX
     */
    public int minX() {
        return getCenterX() - getRadius();
    }

    /**
     * @return the size of this chunk claim. E.g., if the chunk claim has a radius of 2, then it is
     * 5x5 and the dimension is 5
     */
    public int dimension() {
        return getRadius() * 2 + 1;
    }

    public BlockPos regionCenter() {
        return new BlockPos(this.getCenterX() * 16, 128, this.getCenterZ() * 16);
    }

    public int chunkLengthX() {
        return maxX() - minX() + 1;
    }

    public int chunkLengthZ() {
        return maxZ() - minZ() + 1;
    }

    class ChunkPosIterator implements Iterator<ChunkPos> {
        int index = 0;

        @Override
        public boolean hasNext() {
            return index < dimension() * dimension();
        }

        @Override
        public ChunkPos next() {
            if (!hasNext()) throw new NoSuchElementException();

            int x = (index % dimension()) + minX();
            int z = (index / dimension()) + minZ();
            index++;
            return new ChunkPos(x, z);
        }
    }
}
