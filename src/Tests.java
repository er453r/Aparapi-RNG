import org.uncommons.maths.binary.BinaryUtils;
import org.uncommons.maths.random.DefaultSeedGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.XORShiftRNG;

import com.amd.aparapi.Range;
import com.amd.aparapi.Kernel.EXECUTION_MODE;
import com.er453r.aparapi.rng.MersenneTwisterKernel;
import com.er453r.aparapi.rng.XORShiftKernel;

public class Tests {
	public static void main(String[] _args){
		MersenneTwisterTest(0xfffff, 2, EXECUTION_MODE.GPU);
		MersenneTwisterTest(0xfffff, 2, EXECUTION_MODE.JTP);
		
		XORShiftTest(0xfffff, 16, EXECUTION_MODE.GPU);
		XORShiftTest(0xfffff, 16, EXECUTION_MODE.JTP);
	}
	
	public static void XORShiftTest(final int testSize, final int thread, EXECUTION_MODE mode){
		System.out.printf("XORShift test for %d samples in thread %d...\n", testSize, thread);
		
		final int out[] = new int[testSize * thread];
		
		byte[] rngSeed = DefaultSeedGenerator.getInstance().generateSeed(XORShiftKernel.SEED_SIZE * XORShiftKernel.INTEGER_SIZE);
		byte[] kernelSeed = new byte[rngSeed.length * thread];
		
		for(int n = 0; n < kernelSeed.length; n++)
			kernelSeed[n] = rngSeed[n % rngSeed.length];
		
		Range range = Range.create(thread);
						
		XORShiftKernel kernel = new XORShiftKernel(range, BinaryUtils.convertBytesToInts(kernelSeed)){
			@Override public void run(){				
				int gid = getGlobalId();
				
				for(int n = 0; n < testSize; n++)
					out[gid * testSize + n] = random();
			}
		};
		
		kernel.setExecutionMode(mode);
		
		kernel.execute(range);
		
		System.out.printf("Execution mode = %s, time = %d ms.\n", kernel.getExecutionMode(), kernel.getExecutionTime());
		
		kernel.dispose();
		
		XORShiftRNG rng = new XORShiftRNG(rngSeed);
		
		for(int n = 0; n < testSize; n++){
			int random = rng.nextInt();
			int index = (thread - 1) * testSize + n;
			
			if(random != out[index]){
				System.out.printf("Diffirence detected! Sample %d, %d (CPU) != %d (KERNEL)\n", n, random, out[index]);
				
				return;
			}
		}
				
		System.out.printf("%d samples, results OK\n", testSize);
	}
	
	public static void MersenneTwisterTest(final int testSize, final int thread, EXECUTION_MODE mode){
		System.out.printf("MersenneTwister test for %d samples in thread %d...\n", testSize, thread);
		
		final int out[] = new int[testSize * thread];
		
		byte[] rngSeed = DefaultSeedGenerator.getInstance().generateSeed(MersenneTwisterKernel.SEED_SIZE * MersenneTwisterKernel.INTEGER_SIZE);
		byte[] kernelSeed = new byte[rngSeed.length * thread];
		
		for(int n = 0; n < kernelSeed.length; n++)
			kernelSeed[n] = rngSeed[n % rngSeed.length];
		
		Range range = Range.create(thread);
						
		MersenneTwisterKernel kernel = new MersenneTwisterKernel(range, BinaryUtils.convertBytesToInts(kernelSeed)){
			@Override public void run(){				
				int gid = getGlobalId();
				
				for(int n = 0; n < testSize; n++)
					out[gid * testSize + n] = random();
			}
		};
		
		kernel.setExecutionMode(mode);
		
		kernel.execute(range);
		
		System.out.printf("Execution mode = %s, time = %d ms.\n", kernel.getExecutionMode(), kernel.getExecutionTime());
		
		kernel.dispose();
		
		MersenneTwisterRNG rng = new MersenneTwisterRNG(rngSeed);
		
		for(int n = 0; n < testSize; n++){
			int random = rng.nextInt();
			int index = (thread - 1) * testSize + n;
			
			if(random != out[index]){
				System.out.printf("Diffirence detected! Sample %d, %d (CPU) != %d (KERNEL)\n", n, random, out[index]);
				
				return;
			}
		}
				
		System.out.printf("%d samples, results OK\n", testSize);
	}
}
