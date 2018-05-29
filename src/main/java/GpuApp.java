import com.aparapi.Kernel;
import com.aparapi.Range;

import java.util.Arrays;

final class Example {
    float val() {
        return 1f;
    }
}

public class GpuApp {
    public static void main(String[] args) {
        final float data[] = new float[3];
        // final float inB[] = {3, 4, 5};
        final Example[] randoms = new Example[data.length];
        for (int i = 0; i < randoms.length; i++) {
            randoms[i] = new Example();
        }
        final float result[] = new float[data.length];

        final Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int i = getGlobalId();
                result[i] = data[i] + randoms[i].val();
            }
        };

        final Range range = Range.create(result.length);
        kernel.execute(range);

        System.out.println(Arrays.toString(result));
    }
}
