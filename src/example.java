import com.amd.aparapi.Kernel.EXECUTION_MODE;
import com.amd.aparapi.Range;
import com.er453r.aparapi.rng.XORShiftKernel;

public class example {
	public static void main(String[] _args){
		final int size = 1024;
		final int passes = 1024;

		final float result[] = new float[] { 0 };
		final int subsum[] = new int[size];
		
		for(int n = 0; n < size; n++)
			subsum[n] = 0; 
				
		XORShiftKernel kernel = new XORShiftKernel(size){
			@Override
			public void run() {
				if(getGlobalSize(0) > 1){
					int gid = getGlobalId();
					
					for(int n = 0; n < size; n++){						
						float x = random01();
						float y = random01();
	
						if(x * x + y * y <= 1)
							subsum[gid]++;
					}
				}
				else{					
					float sum = 0;
					
					for(int n = 0; n < size; n++)
						sum += subsum[n];
					
					result[0] = 4 * (sum / (size * size * passes));
				}
			}
		};

		kernel.setExecutionMode(EXECUTION_MODE.GPU);
		
		kernel.setExplicit(true);
		kernel.put(subsum).put(result);
		
		kernel.execute(Range.create(size), passes);
		kernel.execute(Range.create(1));
		
		kernel.get(result);
		

		System.out.printf("pi = %1.20f\n", result[0]);

		System.out.printf("Execution mode = %s, time = %d ms.\n", kernel.getExecutionMode(), kernel.getAccumulatedExecutionTime());

		kernel.dispose();
	}
}
