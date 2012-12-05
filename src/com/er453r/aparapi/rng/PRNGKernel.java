package com.er453r.aparapi.rng;
import com.amd.aparapi.Kernel;

/**
 * Pseudo Random Number Generator Kernel
 * 
 * with basic methods for different random ranges 
 * 
 * @author Marcin Kotz
 */
public abstract class PRNGKernel extends Kernel{
	/** Integer size used for seed sizes */
	public final static int INTEGER_SIZE = 4;
	
	/**
	 * Each PRNG based on this class should implement this as returning int from range: <Integer.MIN_VALUE; Integer.MAX_VALUE>
	 * 
	 * @return pseudo random int from range: &lt;Integer.MIN_VALUE; Integer.MAX_VALUE>
	 */
	public abstract int random();
	
	/** 
	 * Float version of random
	 * 
	 * @return pseudo random float from range: &lt;0.0; 1.0>
	 */
	public float random01(){
        float value = random();
        
        if(value < 0)
        	return value / Integer.MIN_VALUE;
        else if (value > 0)
        	return value / Integer.MAX_VALUE;
        else 
        	return value;
	}
}
