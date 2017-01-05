package co.com.psl.nexradconsumer.util;

import ucar.nc2.dt.RadialDatasetSweep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by acastanedav on 27/12/16.
 */
public class TestUtils {

    Random random = new Random();

    List<Double> getDoublesFromFloat(float[] floats) {
        List<Double> doubles = new ArrayList<>();
        for (int i = 0; i < floats.length; i++) {
            doubles.add((double) floats[i]);
        }
        return doubles;
    }

    float[] randomFloats(int ngates) {
        float[] variables = new float[ngates];
        for (int j = 0; j < ngates; j++) {
            variables[j] = random.nextFloat();
        }
        return variables;
    }

    Double getMean(List<Double> doubles) {
        Double sum = 0.0;
        for (Double d : doubles)
            sum += d;
        double mean = sum / doubles.size();
        return Double.isNaN(mean) ? 0 : mean;
    }

    RadialDatasetSweep.Sweep getMockForSweep(int radialNumber, int ngates) throws IOException {
        RadialDatasetSweep.Sweep sweep = mock(RadialDatasetSweep.Sweep.class);
        for (int i = 0; i < radialNumber; i++) {
            float[] variables = randomFloats(ngates);
            when(sweep.getAzimuth(i)).thenReturn(random.nextFloat());
            when(sweep.getElevation(i)).thenReturn(random.nextFloat());
            when(sweep.readData(i)).thenReturn(variables);
        }
        when(sweep.getRadialNumber()).thenReturn(radialNumber);
        return sweep;
    }

    HashMap<Float, Double> getMeansByElevation(RadialDatasetSweep.RadialVariable radialVariable) throws IOException {
        return getMeansByValues(getListByValues(radialVariable, ((sweep, i) -> {
            try {
                return sweep.getElevation(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Float.NaN;
        })));
    }

    HashMap<Float, Double> getMeansByAzimuth(RadialDatasetSweep.RadialVariable radialVariable) throws IOException {
        return getMeansByValues(getListByValues(radialVariable, ((sweep, i) -> {
            try {
                return sweep.getAzimuth(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Float.NaN;
        })));
    }

    HashMap<Float, List<Double>> getListByValues(RadialDatasetSweep.RadialVariable radialVariable, BiFunction<RadialDatasetSweep.Sweep, Integer, Float> fun) throws IOException {
        HashMap<Float, List<Double>> allValues = new HashMap<>();
        for (int i = 0; i < radialVariable.getNumSweeps(); i++) {
            RadialDatasetSweep.Sweep sweep = radialVariable.getSweep(i);
            for (int j = 0; j < sweep.getRadialNumber(); j++) {
                Float value = fun.apply(sweep, j);
                List<Double> data = getDoublesFromFloat(sweep.readData(j));
                if (allValues.containsKey(value))
                    allValues.get(value).addAll(data);
                else
                    allValues.put(value, data);
            }
        }
        return allValues;
    }

    HashMap<Float, Double> getMeansByValues(HashMap<Float, List<Double>> allValues) {
        HashMap<Float, Double> means = new HashMap<>();
        for (Float value : allValues.keySet())
            means.put(value, getMean(allValues.get(value)));
        return means;
    }
}
