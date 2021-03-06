package org.valkyrienskies.mod.common.physics.management.chunkcache;

import java.util.Map.Entry;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.valkyrienskies.fixes.IPhysicsChunk;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.physmanagement.chunk.VSChunkClaim;

/**
 * The ClaimedChunkCacheController is a chunk cache controller used by the {@link PhysicsObject}. It
 * keeps all of a ship's chunks in cache for fast access.
 */
@Log4j2
public class ClaimedChunkCacheController {

    /**
     * You should ideally not be accessing this directly
     */
    private final PhysicsObject parent;
    private final World world;

    private Chunk[][] claimedChunks;

    /**
     * This constructor is expensive; it loads all the chunks when it's called. Be warned.
     *
     * @param parent The PhysicsObject that is using this ChunkCacheController
     * @param loaded Whether or not the chunks that are being cached have been loaded before. e.g.,
     *               whether they are being loaded from NBT or from the world.
     */
    public ClaimedChunkCacheController(PhysicsObject parent, boolean loaded) {
        this.world = parent.world();
        this.parent = parent;

        int dimension = parent.getOwnedChunks().dimension();

        if (loaded) {
            loadLoadedChunks();
        } else {
            loadNewChunks();
        }
    }

    /**
     * Retrieves a chunk from cache from its absolute position.
     *
     * @param chunkX The X position of the chunk
     * @param chunkZ The Z position of the chunk
     * @return The chunk from the cache
     */
    public Chunk getChunkAt(int chunkX, int chunkZ) {
        VSChunkClaim claim = parent.getOwnedChunks();

        throwIfOutOfBounds(claim, chunkX, chunkZ);

        return getChunkRelative(chunkX - claim.minX(), chunkZ - claim.minZ());
    }

    /**
     * Retrieves a chunk from cache from its position relative to the chunk claim.
     *
     * @param relativeX The X value relative to the chunk claim
     * @param relativeZ the Z value relative to the chunk claim
     * @return The chunk from the cache.
     */
    public Chunk getChunkRelative(int relativeX, int relativeZ) {
        return claimedChunks[relativeX][relativeZ];
    }

    /**
     * Retrieves a chunk from cache from its position relative to the chunk claim.
     *
     * @param relativeX The X value relative to the chunk claim
     * @param relativeZ the Z value relative to the chunk claim
     * @param chunk     The chunk to cache.
     */
    public void setChunkRelative(int relativeX, int relativeZ, Chunk chunk) {
        claimedChunks[relativeX][relativeZ] = chunk;
    }

    /**
     * Retrieves a chunk from cache from its absolute position.
     *
     * @param chunkX The X position of the chunk
     * @param chunkZ The Z position of the chunk
     * @param chunk  The chunk to cache.
     */
    public void setChunkAt(int chunkX, int chunkZ, Chunk chunk) {
        VSChunkClaim claim = parent.getOwnedChunks();

        throwIfOutOfBounds(claim, chunkX, chunkZ);

        setChunkRelative(chunkX - claim.minX(), chunkZ - claim.minZ(), chunk);
    }

    private static void throwIfOutOfBounds(VSChunkClaim claim, int chunkX, int chunkZ) {
        if (!claim.containsChunk(chunkX, chunkZ)) {
            throw new ChunkNotInClaimException(chunkX, chunkZ);
        }
    }

    /**
     * Let's try not to use this
     */
    @Deprecated
    public Chunk[][] getCacheArray() {
        return claimedChunks;
    }

    /**
     * Loads chunks that have been generated before into the cache
     */
    private void loadLoadedChunks() {
        VSChunkClaim chunkClaim = parent.getOwnedChunks();

        claimedChunks = new Chunk[(chunkClaim.getRadius() * 2) + 1][
            (chunkClaim.getRadius() * 2) + 1];
        for (int x = chunkClaim.minX(); x <= chunkClaim.maxX(); x++) {
            for (int z = chunkClaim.minZ(); z <= chunkClaim.maxZ(); z++) {
                // Added try catch to prevent ships deleting themselves because of a failed tile entity load.
                try {
                    Chunk chunk = world.getChunk(x, z);
                    // Do this to get it re-integrated into the world
                    if (!world.isRemote) {
                        injectChunkIntoWorld(chunk, x, z, false);
                    }
                    for (Entry<BlockPos, TileEntity> entry : chunk.tileEntities.entrySet()) {
                        parent.onSetTileEntity(entry.getKey(), entry.getValue());
                    }
                    claimedChunks[x - chunkClaim.minX()][z - chunkClaim
                        .minZ()] = chunk;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Loads chunks that haven't been generated before into the cache. At the moment make sure to only call this from
     * the game thread. Running it on a separate thread will lead to data races.
     */
    private void loadNewChunks() {
        VSChunkClaim chunkClaim = parent.getOwnedChunks();

        claimedChunks = new Chunk[(chunkClaim.getRadius() * 2) + 1][
            (chunkClaim.getRadius() * 2) + 1];

        for (int x = chunkClaim.minX(); x <= chunkClaim.maxX(); x++) {
            for (int z = chunkClaim.minZ(); z <= chunkClaim.maxZ(); z++) {
                Chunk chunk = new Chunk(world, x, z);
                injectChunkIntoWorld(chunk, x, z, true);
                claimedChunks[x - chunkClaim.minX()][z - chunkClaim.minZ()] = chunk;
            }
        }
    }

    public void injectChunkIntoWorld(Chunk chunk, int x, int z, boolean putInId2ChunkMap) {
        VSChunkClaim chunkClaim = parent.getOwnedChunks();
        chunk.generateSkylightMap();
        chunk.checkLight();

        // Make sure this chunk knows we own it.
        ((IPhysicsChunk) chunk).setParentPhysicsObject(Optional.of(this.parent));

        ChunkProviderServer provider = (ChunkProviderServer) world.getChunkProvider();
        chunk.dirty = true;
        claimedChunks[x - chunkClaim.minX()][z - chunkClaim.minZ()] = chunk;

        if (putInId2ChunkMap) {
            provider.loadedChunks.put(ChunkPos.asLong(x, z), chunk);
        }

        chunk.onLoad();
        // We need to set these otherwise certain events like Sponge's PhaseTracker will refuse to work properly with ships!
        chunk.setTerrainPopulated(true);
        chunk.setLightPopulated(true);
        // Inject the entry into the player chunk map.
        // Sanity check first
        if (!((WorldServer) world).isCallingFromMinecraftThread()) {
            throw new IllegalThreadStateException("We cannot call this crap from another thread!");
        }
        PlayerChunkMap map = ((WorldServer) world).getPlayerChunkMap();
        PlayerChunkMapEntry entry = map.getOrCreateEntry(x, z);
        entry.sentToPlayers = true;
        entry.players = parent.getWatchingPlayers();
    }

}
