package com.er453r.aparapi.examples;

import com.amd.aparapi.Kernel.EXECUTION_MODE;
import com.er453r.aparapi.rng.XORShiftKernel;

public class MonteCarloPiExample{
	public static void main(String[] _args){
		final int threads = 2 * 1024;
		final int samplesPerPass = 1024;
		final int passes = 1024;

		final long totalSamples = (long)threads * (long)samplesPerPass * (long)passes;
				
		final int subsum[] = new int[threads];
				
		XORShiftKernel kernel = new XORShiftKernel(threads){
			@Override public void run(){
				int gid = getGlobalId();
				
				for(int n = 0; n < samplesPerPass; n++){						
					float x = randomn11();
					float y = randomn11();

					if(x * x + y * y < 1)
						subsum[gid]++;
				}
			}
		};

		kernel.setExecutionMode(EXECUTION_MODE.GPU);
				
		kernel.execute(threads, passes);
		
		long sum = 0;
		
		for(int n = 0; n < threads; n++)
			sum += subsum[n];
		
		Double pi = 4 * sum / (double) totalSamples;
		
		System.out.printf("pi = %1.20f, %,d hits from %,d samples\n", pi, sum, totalSamples);

		System.out.printf("Execution mode = %s, time = %d ms.\n", kernel.getExecutionMode(), kernel.getAccumulatedExecutionTime());

		kernel.dispose();
	}
}
