package org.valkyrienskies.mod.common.command.converters;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class WorldConverter implements ITypeConverter<World> {

    @Override
    public World convert(String worldName) throws TypeConversionException {
        World[] worlds = DimensionManager.getWorlds();

        Optional<World> matchingWorld = Arrays.stream(worlds)
            .filter(world -> world.provider.getDimensionType().getName().equals(worldName))
            .findFirst();

        return matchingWorld.orElseThrow(() -> {
            String validWorlds = Arrays.stream(worlds)
                .map(world -> world.provider.getDimensionType().getName())
                .collect(Collectors.joining(", "));

            return new TypeConversionException(
                String.format("Invalid world name! Available options: %s", validWorlds));
        });
    }

}
