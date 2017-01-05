package co.com.psl.nexradconsumer.util;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import ucar.nc2.dt.RadialDatasetSweep;

import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * Created by acastanedav on 27/12/16.
 */
public class NexRADUtilsTest {

    TestUtils testUtils = new TestUtils();

    @Test
    public void whenNaNShouldReturn0AndMapUnchanged(){
        HashMap<Float, Integer> valuesAndMatch = new HashMap<>();
        valuesAndMatch.put(0.1f,1);
        valuesAndMatch.put(0.2f,1);
        valuesAndMatch.put(0.3f,1);
        valuesAndMatch.put(0.4f,1);
        NexRADUtils nexRADUtils = new NexRADUtils();

        int result = nexRADUtils.getMatchForValue(Float.NaN, valuesAndMatch);

        Assert.assertEquals(0, result);
        Assert.assertEquals(4,valuesAndMatch.size());
        Assert.assertEquals(Integer.valueOf(1),valuesAndMatch.get(0.1f));
        Assert.assertEquals(Integer.valueOf(1),valuesAndMatch.get(0.2f));
        Assert.assertEquals(Integer.valueOf(1),valuesAndMatch.get(0.3f));
        Assert.assertEquals(Integer.valueOf(1),valuesAndMatch.get(0.4f));
    }

    @Test
    public void whenValueInMapShouldReturnNewMap(){
        HashMap<Float, Integer> valuesAndMatch = new HashMap<>();
        valuesAndMatch.put(0.1f,1);
        valuesAndMatch.put(0.2f,1);
        valuesAndMatch.put(0.3f,1);
        valuesAndMatch.put(0.4f,1);
        NexRADUtils nexRADUtils = new NexRADUtils();

        int result = nexRADUtils.getMatchForValue(0.1f, valuesAndMatch);

        Assert.assertEquals(2, result);
        Assert.assertEquals(4,valuesAndMatch.size());
        Assert.assertEquals(Integer.valueOf(2),valuesAndMatch.get(0.1f));
        Assert.assertEquals(Integer.valueOf(1),valuesAndMatch.get(0.2f));
        Assert.assertEquals(Integer.valueOf(1),valuesAndMatch.get(0.3f));
        Assert.assertEquals(Integer.valueOf(1),valuesAndMatch.get(0.4f));
    }

    @Test
    public void whenNewValueShouldReturnMatchAndBiggerMap() {
        HashMap<Float, Integer> valuesAndMatch = new HashMap<>();
        valuesAndMatch.put(0.1f,1);
        valuesAndMatch.put(0.2f,1);
        valuesAndMatch.put(0.3f,1);
        valuesAndMatch.put(0.4f,1);
        NexRADUtils nexRADUtils = new NexRADUtils();

        int result = nexRADUtils.getMatchForValue(0.5f, valuesAndMatch);

        Assert.assertEquals(1, result);
        Assert.assertEquals(5, valuesAndMatch.size());
        Assert.assertEquals(Integer.valueOf(1),valuesAndMatch.get(0.1f));
        Assert.assertEquals(Integer.valueOf(1),valuesAndMatch.get(0.2f));
        Assert.assertEquals(Integer.valueOf(1),valuesAndMatch.get(0.3f));
        Assert.assertEquals(Integer.valueOf(1),valuesAndMatch.get(0.4f));
        Assert.assertEquals(Integer.valueOf(1),valuesAndMatch.get(0.5f));
    }

    @Test
    public void whenRadialVariableGetElevations() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        Set<Float> result = nexRADUtils.getElevationsSet(radialVariable, logger);
        Assert.assertEquals(30, result.size());
        verify(logger, never()).error(Mockito.anyString());
    }

    @Test
    public void whenRadialVariableGetAzimuths() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        Set<Float> result = nexRADUtils.getAzimuthSet(radialVariable, logger);
        Assert.assertEquals(30, result.size());
        verify(logger, never()).error(Mockito.anyString());
    }

    @Test
    public void whenRadialVariableThrowExceptionForElevationThenLogError() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        int index = testUtils.random.nextInt(10);
        when(radialVariable.getSweep(1).getElevation(index)).thenThrow(new IOException());
        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        Set<Float> result = nexRADUtils.getElevationsSet(radialVariable, logger);
        Assert.assertTrue(result.size() < 30);
        verify(logger, times(1)).error(Mockito.anyString(), Mockito.any(Throwable.class));
    }

    @Test
    public void whenRadialVariableThrowExceptionForAzimuthThenLogError() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        int index = testUtils.random.nextInt(10);
        when(radialVariable.getSweep(1).getAzimuth(index)).thenThrow(new IOException());
        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        Set<Float> result = nexRADUtils.getAzimuthSet(radialVariable, logger);
        Assert.assertTrue(result.size() < 30);
        verify(logger, times(1)).error(Mockito.anyString(), Mockito.any(Throwable.class));
    }

    @Test
    public void whenRadialVariableWithRepeatedElevationsGetElevations() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);

        float repeatedElevation = sweep0.getElevation(0);
        when(sweep1.getElevation(4)).thenReturn(repeatedElevation);
        when(sweep2.getElevation(8)).thenReturn(repeatedElevation);

        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);

        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        Set<Float> result = nexRADUtils.getElevationsSet(radialVariable, logger);
        Assert.assertEquals(1, result.size());
        verify(logger, never()).error(Mockito.anyString());
    }

    @Test
    public void whenRadialVariableWithRepeatedAzimuthGetAzimuths() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);

        float repeatedAzimuth = sweep0.getAzimuth(0);
        when(sweep1.getAzimuth(4)).thenReturn(repeatedAzimuth);
        when(sweep2.getAzimuth(8)).thenReturn(repeatedAzimuth);

        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);

        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        Set<Float> result = nexRADUtils.getAzimuthSet(radialVariable, logger);
        Assert.assertEquals(1, result.size());
        verify(logger, never()).error(Mockito.anyString());
    }

    @Test
    public void whenRadialVariableGetDataForElevation() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);

        int index = testUtils.random.nextInt(10);
        float elevation = sweep0.getElevation(index);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);

        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Float> result = nexRADUtils.getDataForElevation(radialVariable, elevation, logger);
        Assert.assertEquals(12, result.size());
        Assert.assertArrayEquals(ArrayUtils.toObject(sweep0.readData(index)), result.toArray());
        verify(logger, never()).error(Mockito.anyString());
    }

    @Test
    public void whenRadialVariableGetDataForAzimuth() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);

        int index = testUtils.random.nextInt(10);
        float azimuth = sweep0.getAzimuth(index);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);

        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Float> result = nexRADUtils.getDataForAzimuth(radialVariable, azimuth, logger);
        Assert.assertEquals(12, result.size());
        Assert.assertArrayEquals(ArrayUtils.toObject(sweep0.readData(index)), result.toArray());
        verify(logger, never()).error(Mockito.anyString());
    }

    @Test
    public void whenRadialVariableWithRepeatedElevationsGetDataForElevation() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);

        float elevation = sweep0.getElevation(0);
        when(sweep1.getElevation(4)).thenReturn(elevation);
        when(sweep2.getElevation(8)).thenReturn(elevation);

        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);

        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Float> result = nexRADUtils.getDataForElevation(radialVariable, elevation, logger);
        Assert.assertEquals(36, result.size());
        List<Float> expected = new ArrayList<>();
        expected.addAll(Arrays.asList(ArrayUtils.toObject(sweep0.readData(0))));
        expected.addAll(Arrays.asList(ArrayUtils.toObject(sweep1.readData(4))));
        expected.addAll(Arrays.asList(ArrayUtils.toObject(sweep2.readData(8))));
        Assert.assertArrayEquals(expected.toArray(), result.toArray());
    }

    @Test
    public void whenRadialVariableWithRepeatedAzimuthGetDataForAzimuth() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);

        float azimuth = sweep0.getAzimuth(0);
        when(sweep1.getAzimuth(4)).thenReturn(azimuth);
        when(sweep2.getAzimuth(8)).thenReturn(azimuth);

        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);

        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Float> result = nexRADUtils.getDataForAzimuth(radialVariable, azimuth, logger);
        Assert.assertEquals(36, result.size());
        List<Float> expected = new ArrayList<>();
        expected.addAll(Arrays.asList(ArrayUtils.toObject(sweep0.readData(0))));
        expected.addAll(Arrays.asList(ArrayUtils.toObject(sweep1.readData(4))));
        expected.addAll(Arrays.asList(ArrayUtils.toObject(sweep2.readData(8))));
        Assert.assertArrayEquals(expected.toArray(), result.toArray());
    }

    @Test
    public void whenExceptionIsThrownButDoesNotAffectExecutionForElevations() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        int index = testUtils.random.nextInt(10);
        when(radialVariable.getSweep(1).getElevation(index)).thenThrow(new IOException());
        Float elevation = sweep0.getElevation(index);
        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Float> result = nexRADUtils.getDataForElevation(radialVariable, elevation, logger);
        Assert.assertEquals(12, result.size());
        Assert.assertArrayEquals(ArrayUtils.toObject(sweep0.readData(index)), result.toArray());
        verify(logger, times(1)).error(Mockito.anyString(), Mockito.any(Throwable.class));
    }

    @Test
    public void whenExceptionIsThrownButDoesNotAffectExecutionForAzimuths() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        int index = testUtils.random.nextInt(10);
        when(radialVariable.getSweep(1).getAzimuth(index)).thenThrow(new IOException());
        Float azimuth = sweep0.getAzimuth(index);
        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Float> result = nexRADUtils.getDataForAzimuth(radialVariable, azimuth, logger);
        Assert.assertEquals(12, result.size());
        Assert.assertArrayEquals(ArrayUtils.toObject(sweep0.readData(index)), result.toArray());
        verify(logger, times(1)).error(Mockito.anyString(), Mockito.any(Throwable.class));
    }

    @Test
    public void whenExceptionIsThrownAndAffectExecutionForElevationsThenReturnEmptyList() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        int index = testUtils.random.nextInt(10);
        Float elevation = sweep1.getElevation(index);
        when(radialVariable.getSweep(1).getElevation(index)).thenThrow(new IOException());
        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Float> result = nexRADUtils.getDataForElevation(radialVariable, elevation, logger);
        Assert.assertEquals(0, result.size());
        verify(logger, times(1)).error(Mockito.anyString(), Mockito.any(Throwable.class));
    }

    @Test
    public void whenExceptionIsThrownAndAffectExecutionForAzimuthsThenReturnEmptyList() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        int index = testUtils.random.nextInt(10);
        Float azimuth = sweep1.getAzimuth(index);
        when(radialVariable.getSweep(1).getAzimuth(index)).thenThrow(new IOException());
        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Float> result = nexRADUtils.getDataForAzimuth(radialVariable, azimuth, logger);
        Assert.assertEquals(0, result.size());
        verify(logger, times(1)).error(Mockito.anyString(), Mockito.any(Throwable.class));
    }

    @Test
    public void whenExceptionIsThrownAndAffectExecutionForElevationsDataThenReturnEmptyList() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        int index = testUtils.random.nextInt(10);
        Float elevation = sweep1.getElevation(index);
        when(radialVariable.getSweep(1).readData(index)).thenThrow(new IOException());
        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Float> result = nexRADUtils.getDataForElevation(radialVariable, elevation, logger);
        Assert.assertEquals(0, result.size());
        verify(logger, times(1)).error(Mockito.anyString(), Mockito.any(Throwable.class));
    }

    @Test
    public void whenExceptionIsThrownAndAffectExecutionForAzimuthsDataThenReturnEmptyList() throws IOException {
        RadialDatasetSweep.RadialVariable radialVariable = mock(RadialDatasetSweep.RadialVariable.class);
        when(radialVariable.getNumSweeps()).thenReturn(3);
        RadialDatasetSweep.Sweep sweep0 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep1 = testUtils.getMockForSweep(10, 12);
        RadialDatasetSweep.Sweep sweep2 = testUtils.getMockForSweep(10, 12);
        when(radialVariable.getSweep(0)).thenReturn(sweep0);
        when(radialVariable.getSweep(1)).thenReturn(sweep1);
        when(radialVariable.getSweep(2)).thenReturn(sweep2);
        int index = testUtils.random.nextInt(10);
        Float azimuth = sweep1.getAzimuth(index);
        when(radialVariable.getSweep(1).readData(index)).thenThrow(new IOException());
        Logger logger = mock(Logger.class);
        NexRADUtils nexRADUtils = new NexRADUtils();
        List<Float> result = nexRADUtils.getDataForAzimuth(radialVariable, azimuth, logger);
        Assert.assertEquals(0, result.size());
        verify(logger, times(1)).error(Mockito.anyString(), Mockito.any(Throwable.class));
    }
}
