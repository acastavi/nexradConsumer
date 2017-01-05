package co.com.psl.nexradconsumer.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by acastanedav on 22/12/16.
 */
public class AmazonServiceUtilsTest {

    @Test
    public void whenNumberUnderTenShouldReturnAdditionalZero() {
        AmazonServiceUtils amazonServiceUtils = new AmazonServiceUtils();
        Assert.assertEquals("07", amazonServiceUtils.getNumberString(7));
    }

    @Test
    public void whenNumberOverTenShouldNotReturnAdditionalZero() {
        AmazonServiceUtils amazonServiceUtils = new AmazonServiceUtils();
        Assert.assertEquals("15", amazonServiceUtils.getNumberString(15));
    }
}
