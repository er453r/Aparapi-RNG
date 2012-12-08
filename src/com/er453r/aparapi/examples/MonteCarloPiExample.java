package com.er453r.aparapi.examples;

import com.amd.aparapi.Kernel.EXECUTION_MODE;
import com.er453r.aparapi.rng.XORShiftKernel;

public class MonteCarloPiExample{
	public static void main(String[] _args){
		final int threads = 512;
		final int samplesPerPass = 1024;
		final int passes = 512;

		final long totalSamples = (long)threads * (long)samplesPerPass * (long)passes;
				
		final long hits[] = new long[]{0};
		final int subsum[] = new int[threads];
				
		XORShiftKernel kernel = new XORShiftKernel(threads){
			@Override public void run(){
				if(getGlobalSize(0) > 1){
					int gid = getGlobalId();
					
					for(int n = 0; n < samplesPerPass; n++){						
						float x = randomn11();
						float y = randomn11();
	
						if(x * x + y * y < 1)
							subsum[gid]++;
					}
				}
				else{					
					long sum = 0;
					
					for(int n = 0; n < threads; n++)
						sum += subsum[n];
					
					hits[0] = sum;
				}
			}
		};

		kernel.setExecutionMode(EXECUTION_MODE.GPU);
				
		kernel.execute(threads, passes);
		kernel.execute(1);
		
		Double pi = 4 * hits[0] / (double) totalSamples;
		
		System.out.printf("pi = %1.20f, %,d hits from %,d samples\n", pi, hits[0], totalSamples);

		System.out.printf("Execution mode = %s, time = %d ms.\n", kernel.getExecutionMode(), kernel.getAccumulatedExecutionTime());

		kernel.dispose();
	}
}
