package ValkyrienWarfareBase.Physics;

import ValkyrienWarfareBase.Math.Vector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBlockForceProvider {

	//Multiply your power usage values or whatever by the secondsToApply, otherwise you'll have issues (Example: <0,400,0> -(.01)->  <0,40,0>
	/**
	 * ALWAYS RETURN A FRESH INSTANCE OF THE FORCE VECTOR, OTHERWISE YOU'LL MESS UP THE MATH
	 * @param world
	 * @param pos
	 * @param state
	 * @param secondsToApply
	 * @return
	 */
	Vector getBlockForce(World world,BlockPos pos,IBlockState state,double secondsToApply);
	
	boolean isForceLocalCoords(World world,BlockPos pos,IBlockState state,double secondsToApply);
	
}