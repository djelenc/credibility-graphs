package credibilitygraphs.model;

import atb.interfaces.Experience;
import org.junit.Assert;
import org.junit.Test;


public class OrdersTest {

    @Test
    public void pastSimple() {
        final Orders.Past past = new Orders.Past();
        past.experiences[0] = new Experience(0, 0, 0, 0.0);
        past.experiences[1] = new Experience(0, 0, 1, 0.1);
        past.experiences[2] = new Experience(0, 0, 2, 0.2);
        past.experiences[3] = new Experience(0, 0, 3, 0.3);

        final double[] data = past.weightedExperience(3);

        Assert.assertTrue(data[0] > 0);
        Assert.assertTrue(data[1] > 0);
    }

    @Test
    public void pastEmpty() {
        final Orders.Past past = new Orders.Past();
        final double[] data = past.weightedExperience(3);
        Assert.assertEquals(data[0], 0, 0.00001);
        Assert.assertEquals(data[1], 0, 0.00001);
    }
}
