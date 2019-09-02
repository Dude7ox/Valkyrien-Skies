package org.valkyrienskies.mod.common.physmanagement.interaction;

import static com.googlecode.cqengine.query.QueryFactory.equal;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;

@MethodsReturnNonnullByDefault
@SuppressWarnings("WeakerAccess")
public class QueryableShipData {

    // The key used to store/read the allShips collection from nbt.
    private static final String NBT_STORAGE_KEY = ValkyrienSkiesMod.MOD_ID + "QueryableShipDataNBT";
    // Where every ship data instance is stored, regardless if the corresponding PhysicsObject is
    // loaded in the World or not.
    private ConcurrentIndexedCollection<ShipData> allShips = new ConcurrentIndexedCollection<>();

    public QueryableShipData() {
        allShips.addIndex(HashIndex.onAttribute(ShipData.NAME));
        allShips.addIndex(UniqueIndex.onAttribute(ShipData.UUID));
        allShips.addIndex(UniqueIndex.onAttribute(ShipData.CHUNKS));
    }

    public boolean renameShip(ShipData data, String newName) {
        Query<ShipData> query = equal(ShipData.NAME, newName);
        if (allShips.retrieve(query).isEmpty()) {
            ShipData newData = new ShipData.Builder(data)
                .setName(newName)
                .build();

            allShips.remove(data);
            allShips.add(newData);

            return true;
        }
        return false;
    }

    public List<ShipData> getShips() {
        return new ArrayList<>(allShips);
    }

    public UUID getShipUUIDFromPos(int chunkX, int chunkZ) {
        return getShipUUIDFromPos(ChunkPos.asLong(chunkX, chunkZ));
    }

    public UUID getShipUUIDFromPos(long chunkLong) {
        return getShipFromChunk(chunkLong).getUUID();
    }

    public ShipData getShipFromChunk(long chunkLong) {
        Query<ShipData> query = equal(ShipData.CHUNKS, chunkLong);

        return allShips.retrieve(query).uniqueResult();
    }

    public Optional<ShipData> getShip(UUID uuid) {
        Query<ShipData> query = equal(ShipData.UUID, uuid);
        ResultSet<ShipData> resultSet = allShips.retrieve(query);

        if (resultSet.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(resultSet.uniqueResult());
        }
    }

    public Optional<ShipData> getShip(PhysicsWrapperEntity wrapperEntity) {
        return getShip(wrapperEntity.getPersistentID());
    }

    public ShipData getOrCreateShip(PhysicsWrapperEntity wrapperEntity) {
        Optional<ShipData> data = getShip(wrapperEntity.getPersistentID());
        return data.orElseGet(() -> {
            ShipData shipData = new ShipData.Builder(wrapperEntity).build();
            allShips.add(shipData);
            return shipData;
        });
    }

    public Optional<ShipData> getShipFromName(String name) {
        Query<ShipData> query = equal(ShipData.NAME, name);
        ResultSet<ShipData> shipDataResultSet = allShips.retrieve(query);

        if (shipDataResultSet.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(shipDataResultSet.uniqueResult());
        }
    }

    public void removeShip(PhysicsWrapperEntity wrapper) {
        removeShip(wrapper.getPersistentID());
    }

    public void removeShip(UUID uuid) {
        Optional<ShipData> shipOptional = getShip(uuid);

        shipOptional.ifPresent(ship -> allShips.remove(ship));
    }

    public void addShip(ShipData ship) {
        allShips.add(ship);
    }

    public void addShip(PhysicsWrapperEntity wrapperEntity) {
        Query<ShipData> query = equal(ShipData.UUID, wrapperEntity.getPersistentID());

        // If this ship is already added, don't add it again?
        if (allShips.retrieve(query).isEmpty()) {
            addShip(new ShipData.Builder(wrapperEntity).build());
        }
    }

    public void updateShipPosition(PhysicsWrapperEntity wrapper) {
        ShipData shipData = getOrCreateShip(wrapper);
        if (shipData.positionData == null) {
            shipData.positionData = new ShipPositionData(wrapper);
        }
        shipData.positionData.updateData(wrapper);
    }

    @SuppressWarnings("unchecked")
    public void readFromNBT(NBTTagCompound nbt) {
        long start = System.currentTimeMillis();

        Kryo kryo = ValkyrienSkiesMod.INSTANCE.getKryo();
        Input input = new Input(nbt.getByteArray(NBT_STORAGE_KEY));
        try {
            allShips = kryo.readObject(input, ConcurrentIndexedCollection.class);
        } catch (Exception e) {
            // Error reading allShips from memory, just make a new empty one.
            e.printStackTrace();
            allShips = new ConcurrentIndexedCollection<>();
        }
        if (allShips == null) {
            // This should NEVER EVER happen! So I don't feel bad crashing the game, for now.
            throw new IllegalStateException(
                "Kryo read allships as null! Making a new empty allships instance");
        }

        System.out.println("Price of read: " + (System.currentTimeMillis() - start) + "ms");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        long start = System.currentTimeMillis();

        Kryo kryo = ValkyrienSkiesMod.INSTANCE.getKryo();
        Output output = new Output(1024, -1);
        kryo.writeObject(output, allShips);
        compound.setByteArray(NBT_STORAGE_KEY, output.getBuffer());

        System.out.println("Price of write: " + (System.currentTimeMillis() - start) + "ms");

        return compound;
    }

}
