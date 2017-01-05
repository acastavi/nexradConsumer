package co.com.psl.nexradconsumer.util;

import com.holdenkarau.spark.testing.JavaRDDComparisons;
import com.holdenkarau.spark.testing.SharedJavaSparkContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import ucar.nc2.dt.RadialDatasetSweep;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by acastanedav on 22/12/16.
 */
public class SparkRDDUtilsTest {

    SharedJavaSparkContext sharedJavaSparkContext;
    TestUtils testUtils = new TestUtils();


    @Before
    public void createContext() {
        sharedJavaSparkContext = new SharedJavaSparkContext();
        sharedJavaSparkContext.runBefore();
    }

    @Test
    public void whenNoNaNValuesShouldReturnTheSameArray() {
        List<Float> inputList = Arrays.asList(1.0f, 2.0f, 3.0f);
        JavaRDD<Float> input = sharedJavaSparkContext.jsc().parallelize(inputList);
        SparkRDDUtils sparkRDDUtils = new SparkRDDUtils();
        JavaRDD<Float> output = sparkRDDUtils.filterRDDWithOutNaN(input);
        JavaRDDComparisons.assertRDDEquals(input, output);
    }

    @Test
    public void whenHasNaNValuesShouldReturnTheFilteredArray() {
        List<Float> inputList = Arrays.asList(1.0f, 2.0f, 3.0f, Float.NaN);
        List<Float> expectedList = Arrays.asList(1.0f, 2.0f, 3.0f);
        JavaRDD<Float> input = sharedJavaSparkContext.jsc().parallelize(inputList);
        JavaRDD<Float> expected = sharedJavaSparkContext.jsc().parallelize(expectedList);
        SparkRDDUtils sparkRDDUtils = new SparkRDDUtils();
        JavaRDD<Float> output = sparkRDDUtils.filterRDDWithOutNaN(input);
        JavaRDDComparisons.assertRDDEquals(expected, output);
    }

    @Test
    public void whenFloatArrayReturnDoubleRDD() {
        float[] inputList = {1.0f, 2.0f, 3.0f};
        List<Double> expectedList = testUtils.getDoublesFromFloat(inputList);
        JavaSparkContext sparkContext = sharedJavaSparkContext.jsc();
        JavaDoubleRDD expected = sparkContext.parallelizeDoubles(expectedList);
        SparkRDDUtils sparkRDDUtils = new SparkRDDUtils();
        JavaDoubleRDD output = sparkRDDUtils.getJavaDoubleRDDFromData(sparkContext, Arrays.asList(ArrayUtils.toObject(inputList)));
        JavaRDDComparisons.assertRDDEquals(expected.glom(), output.glom());
    }

    @Test
    public void whenVariableDataShouldReturnMeansByElevation() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        TestUtils testUtils = new TestUtils();
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        JavaSparkContext sparkContext = sharedJavaSparkContext.jsc();

        Logger logger = mock(Logger.class);
        SparkRDDUtils sparkRDDUtils = new SparkRDDUtils();
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Row> result = sparkRDDUtils.getDataForElevations(sparkContext, nexRADUtils, radialVariable, new Date(), logger);
        HashMap<Float, Double> expected = testUtils.getMeansByElevation(radialVariable);
        Assert.assertEquals(30, result.size());
        for (Row row : result) {
            Float elevation = Float.valueOf(row.getString(1));
            Double mean = row.getDouble(2);
            Assert.assertEquals(expected.get(elevation), mean, 0.0000001);
        }
    }

    @Test
    public void whenVariableDataWithRepeatedElevationsShouldReturnMeansByElevation() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        TestUtils testUtils = new TestUtils();
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);

        Float repeatedElevation = sweep0.getElevation(testUtils.random.nextInt(10));
        when(sweep1.getElevation(4)).thenReturn(repeatedElevation);
        when(sweep2.getElevation(8)).thenReturn(repeatedElevation);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);

        JavaSparkContext sparkContext = sharedJavaSparkContext.jsc();

        Logger logger = mock(Logger.class);
        SparkRDDUtils sparkRDDUtils = new SparkRDDUtils();
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Row> result = sparkRDDUtils.getDataForElevations(sparkContext, nexRADUtils, radialVariable, new Date(), logger);
        HashMap<Float, Double> expected = testUtils.getMeansByElevation(radialVariable);
        Assert.assertEquals(1, result.size());
        for (Row row : result) {
            Float elevation = Float.valueOf(row.getString(1));
            Double mean = row.getDouble(2);
            Assert.assertEquals(expected.get(elevation), mean, 0.0000001);
        }
    }

    @Test
    public void whenVariableDataShouldReturnMeansByAzimuth() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        TestUtils testUtils = new TestUtils();
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        JavaSparkContext sparkContext = sharedJavaSparkContext.jsc();

        Logger logger = mock(Logger.class);
        SparkRDDUtils sparkRDDUtils = new SparkRDDUtils();
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Row> result = sparkRDDUtils.getDataForAzimuths(sparkContext, nexRADUtils, radialVariable, new Date(), logger);
        HashMap<Float, Double> expected = testUtils.getMeansByAzimuth(radialVariable);
        Assert.assertEquals(30, result.size());
        for (Row row : result) {
            Float elevation = Float.valueOf(row.getString(1));
            Double mean = row.getDouble(2);
            Assert.assertEquals(expected.get(elevation), mean, 0.0000001);
        }
    }

    @Test
    public void whenVariableDataWithRepeatedAzimuthsShouldReturnMeansByAzimuth() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        TestUtils testUtils = new TestUtils();
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);

        Float repeatedAzimuth = sweep0.getAzimuth(testUtils.random.nextInt(10));
        when(sweep1.getAzimuth(4)).thenReturn(repeatedAzimuth);
        when(sweep2.getAzimuth(8)).thenReturn(repeatedAzimuth);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);

        JavaSparkContext sparkContext = sharedJavaSparkContext.jsc();

        Logger logger = mock(Logger.class);
        SparkRDDUtils sparkRDDUtils = new SparkRDDUtils();
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Row> result = sparkRDDUtils.getDataForAzimuths(sparkContext, nexRADUtils, radialVariable, new Date(), logger);
        HashMap<Float, Double> expected = testUtils.getMeansByAzimuth(radialVariable);
        Assert.assertEquals(1, result.size());
        for (Row row : result) {
            Float elevation = Float.valueOf(row.getString(1));
            Double mean = row.getDouble(2);
            Assert.assertEquals(expected.get(elevation), mean, 0.0000001);
        }
    }

    @Test
    public void whenVariableDataShouldReturnDataFrameWithElevations() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        TestUtils testUtils = new TestUtils();
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        JavaSparkContext sparkContext = sharedJavaSparkContext.jsc();
        SQLContext sqlContext = new SQLContext(sparkContext);

        Logger logger = mock(Logger.class);
        SparkRDDUtils sparkRDDUtils = new SparkRDDUtils();
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Row> rowList = sparkRDDUtils.getDataForElevations(sparkContext, nexRADUtils, radialVariable, new Date(), logger);
        DataFrame dataFrame = sparkRDDUtils.loadObservationsForElevation(sqlContext,sparkContext.parallelize(rowList));

        Assert.assertEquals(30,dataFrame.count());
        System.out.println(dataFrame.first());
    }

    @Test
    public void whenVariableDataShouldReturnDataFrameWithAzimuth() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        TestUtils testUtils = new TestUtils();
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        JavaSparkContext sparkContext = sharedJavaSparkContext.jsc();
        SQLContext sqlContext = new SQLContext(sparkContext);

        Logger logger = mock(Logger.class);
        SparkRDDUtils sparkRDDUtils = new SparkRDDUtils();
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Row> rowList = sparkRDDUtils.getDataForAzimuths(sparkContext, nexRADUtils, radialVariable, new Date(), logger);
        DataFrame dataFrame = sparkRDDUtils.loadObservationsForAzimuth(sqlContext,sparkContext.parallelize(rowList));

        Assert.assertEquals(30,dataFrame.count());
        System.out.println(dataFrame.first());
    }
}
