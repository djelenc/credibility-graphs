package credibilitygraphs.model;

import atb.interfaces.Experience;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;


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

    @Test
    public void empty() {
        final Orders.Past past = new Orders.Past();
        Assert.assertEquals(past.weightedRights(0), 0, 0.00001);
        Assert.assertEquals(past.weightedWrongs(0), 0, 0.00001);
        Assert.assertEquals(past.weightedExperience(0)[0], 0, 0.00001);
        Assert.assertEquals(past.weightedExperience(0)[1], 0, 0.00001);
    }


    @Test
    public void store() {
        final Orders.Past past = new Orders.Past();
        for (int i = 0; i < 10; i++) {
            past.addRight(i);
            past.addWrong(i);
        }

        Assert.assertArrayEquals(past.rights, new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0});
        Assert.assertArrayEquals(past.wrongs, new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0});
    }
}
