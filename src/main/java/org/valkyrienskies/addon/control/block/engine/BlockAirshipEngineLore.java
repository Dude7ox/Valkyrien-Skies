package org.valkyrienskies.addon.control.block.engine;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.tileentity.TileEntityPropellerEngine;
import org.valkyrienskies.mod.common.coordinates.VectorImmutable;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;

/**
 * The same as a normal engine, but says speed in the tooltip
 */
public abstract class BlockAirshipEngineLore extends BlockAirshipEngine {

    private String[] lore;

    public BlockAirshipEngineLore(Material materialIn, double enginePower) {
        super(materialIn, enginePower);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation, ITooltipFlag advanced) {
        Collections.addAll(itemInformation, lore);
    }

    public abstract String getEnginePowerTooltip();

    @Override
    public void setEnginePower(double power) {
        super.setEnginePower(power);
        lore = new String[]{
            "" + TextFormatting.GRAY + TextFormatting.ITALIC + TextFormatting.BOLD + "Force:",
            "  " + this.getEnginePowerTooltip() + " Newtons"};
    }

    @Override
    public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state,
        double secondsToApply) {
        return true;
    }

    @Override
    public Vector getCustomBlockForcePosition(World world, BlockPos pos, IBlockState state,
        PhysicsObject physicsObject,
        double secondsToApply) {
        TileEntityPropellerEngine engineTile = (TileEntityPropellerEngine) world.getTileEntity(pos);
        if (engineTile != null) {
            VectorImmutable forceOutputNormal = engineTile.getForceOutputNormal(secondsToApply,
                physicsObject);
            // System.out.println(forceOutputNormal.getX() + ":" + forceOutputNormal.getY()
            // + ":" + forceOutputNormal.getZ());
            return new Vector(pos.getX() + .5D - forceOutputNormal.getX() * .75,
                pos.getY() + .5D - forceOutputNormal.getY() * .75,
                pos.getZ() + .5D - forceOutputNormal.getZ() * .75);
        } else {
            return null;
        }
    }

}
