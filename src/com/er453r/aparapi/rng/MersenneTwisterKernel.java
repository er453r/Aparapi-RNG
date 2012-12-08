package com.er453r.aparapi.rng;

import org.uncommons.maths.binary.BinaryUtils;
import org.uncommons.maths.random.DefaultSeedGenerator;

import com.amd.aparapi.Range;

/**
 * PRNG implementing MersenneTwisterRNG from Uncommons Maths library ( http://maths.uncommons.org/ )
 * 
 * A Java port of the fast and reliable Mersenne Twister RNG originally developed by Makoto Matsumoto and Takuji Nishimura.
 * It is faster than java.util.Random, does not have the same statistical flaws as that RNG and also has a long period (219937). 
 * The Mersenne Twister is an excellent general purpose RNG.
 * 
 * @author Marcin Kotz
 */
public abstract class MersenneTwisterKernel extends PRNGKernel{
	/** XORShift needs 4 initial states, so it needs 4 seeds */
	public final static int SEED_SIZE = 4;
	
	/** Magic numbers */
	private static final int N = 624;
    private static final int M = 397;
    private static final int UPPER_MASK = 0x80000000;
    private static final int LOWER_MASK = 0x7fffffff;
    private static final int BOOTSTRAP_SEED = 19650218;
    private static final int BOOTSTRAP_FACTOR = 1812433253;
    private static final int SEED_FACTOR1 = 1664525;
    private static final int SEED_FACTOR2 = 1566083941;
    private static final int GENERATE_MASK1 = 0x9d2c5680;
    private static final int GENERATE_MASK2 = 0xefc60000;

    /** States and indexes for each thread of the MersenneTwister kernel */
    final int[] mt; // State vector.
    final int[] mtIndex; // Index into state vector.
    final int[] MAG01 = {0, 0x9908b0df}; // Magic numbers - read only    

	/**
	 * Initializes the MersenneTwister PRNG
	 * 
	 * @param maximumRange maximum range that will be used with this kernel (sets seed values)
	 * @param seeds set seeds explicitly
	 */
	public MersenneTwisterKernel(Range maximumRange, int[] seeds){
		if(maximumRange.getDims() != 1)
			throw new IllegalArgumentException("Only 1-dimensional ranges supported!");
		
		int maxThreads = maximumRange.getGlobalSize(0);
		
		mt = new int[maxThreads * N];
		mtIndex = new int[maxThreads];
		
		if(seeds == null)
			seeds = BinaryUtils.convertBytesToInts( DefaultSeedGenerator.getInstance().generateSeed(SEED_SIZE * maxThreads * INTEGER_SIZE) );
		
		if(SEED_SIZE * maxThreads != seeds.length)
			throw new IllegalArgumentException(String.format("Wrong size of seeds for threads! Expected %d, got %d, for %d threads.", SEED_SIZE * maxThreads, seeds.length, maxThreads));
		
		for(int n = 0; n < maxThreads; n++){
			int[] localSeeds = new int[SEED_SIZE];
			
			for(int l = 0; l < SEED_SIZE; l++)
				localSeeds[l] = seeds[n * SEED_SIZE + l];
			
			initSeeds(localSeeds, n);
		}
	}
	
	private void initSeeds(int[] seedInts, int threadIndex){
		int indexOffset = threadIndex;
		int stateOffset = indexOffset * N;
		
		mt[stateOffset + 0] = BOOTSTRAP_SEED;
		
        for(mtIndex[indexOffset + 0] = 1; mtIndex[indexOffset + 0] < N; mtIndex[indexOffset + 0]++)
            mt[stateOffset + mtIndex[indexOffset + 0]] = (BOOTSTRAP_FACTOR * (mt[stateOffset + mtIndex[indexOffset + 0] - 1] ^ (mt[stateOffset + mtIndex[indexOffset + 0] - 1] >>> 30)) + mtIndex[indexOffset + 0]);

        int i = 1, j = 0;

        for(int k = Math.max(N, seedInts.length); k > 0; k--){
            mt[stateOffset + i] = (mt[stateOffset + i] ^ ((mt[stateOffset + i - 1] ^ (mt[stateOffset + i - 1] >>> 30)) * SEED_FACTOR1)) + seedInts[j] + j;
            
            i++;
            j++;
            
            if(i >= N){
                mt[stateOffset + 0] = mt[stateOffset + N - 1];
                i = 1;
            }
            
            if(j >= seedInts.length)
                j = 0;
        }
        
        for(int k = N - 1; k > 0; k--){
            mt[stateOffset + i] = (mt[stateOffset + i] ^ ((mt[stateOffset + i - 1] ^ (mt[stateOffset + i - 1] >>> 30)) * SEED_FACTOR2)) - i;
            
            i++;
            
            if(i >= N){
                mt[stateOffset + 0] = mt[stateOffset + N - 1];
                i = 1;
            }
        }
        
        mt[stateOffset + 0] = UPPER_MASK; // Most significant bit is 1 - guarantees non-zero initial array.
	}
	
	public MersenneTwisterKernel(int maximumRange){
		this(Range.create(maximumRange), null);
	}
	
	public MersenneTwisterKernel(int maximumRange, int[] seeds){
		this(Range.create(maximumRange), seeds);
	}
	
	public int random(){
		int indexOffset = getGlobalId();
		int stateOffset = indexOffset * N;
		
		int y;

        if(mtIndex[indexOffset + 0] >= N){
            int kk;
            
            for (kk = 0; kk < N - M; kk++){
                y = (mt[stateOffset + kk] & UPPER_MASK) | (mt[stateOffset + kk + 1] & LOWER_MASK);
                mt[stateOffset + kk] = mt[stateOffset + kk + M] ^ (y >>> 1) ^ MAG01[y & 0x1];
            }
            
            for (; kk < N - 1; kk++){
                y = (mt[stateOffset + kk] & UPPER_MASK) | (mt[stateOffset + kk + 1] & LOWER_MASK);
                mt[stateOffset + kk] = mt[stateOffset + kk + (M - N)] ^ (y >>> 1) ^ MAG01[y & 0x1];
            }
            
            y = (mt[stateOffset + N - 1] & UPPER_MASK) | (mt[stateOffset + 0] & LOWER_MASK);
            mt[stateOffset + N - 1] = mt[stateOffset + M - 1] ^ (y >>> 1) ^ MAG01[y & 0x1];
            mtIndex[indexOffset + 0] = 0;
        }

        y = mt[stateOffset + mtIndex[indexOffset + 0]++];

        y ^= (y >>> 11);
        y ^= (y << 7) & GENERATE_MASK1;
        y ^= (y << 15) & GENERATE_MASK2;
        y ^= (y >>> 18);

        return y;
	}
}
