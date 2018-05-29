import com.aparapi.Kernel;
import com.aparapi.Range;

import java.util.Arrays;

public class GpuApp {
    public static void main(String[] args) {
        final float data1[] = {1, 2, 3};
        final float data2[] = {3, 4, 5};
        final float result[] = new float[data1.length];

        final Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int i = getGlobalId();
                result[i] = data1[i] + data2[i];
            }
        };

        final Range range = Range.create(result.length);
        kernel.execute(range);

        System.out.println(Arrays.toString(result));
    }
}
