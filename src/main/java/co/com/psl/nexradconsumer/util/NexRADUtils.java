package co.com.psl.nexradconsumer.util;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Created by acastanedav on 27/12/16.
 */
public class NexRADUtils {

    public RadialDatasetSweep rds(String file, Logger logger) throws IOException {
        CancelTask emptyCancelTask = new CancelTask() {
            @Override
            public boolean isCancel() {
                return false;
            }

            @Override
            public void setError(String arg0) {
                logger.error("Error reading file: " + arg0);
            }

            @Override
            public void setProgress(String msg, int progress) {
                logger.info("Progress: " + progress + "\t" + msg);
            }
        };

        return (RadialDatasetSweep)
                FeatureDatasetFactoryManager.open(
                        FeatureType.RADIAL,
                        file,
                        emptyCancelTask,
                        new Formatter()
                );
    }

    Set<Float> getElevationsSet(RadialDatasetSweep.RadialVariable radialVariable, Logger logger) throws IOException {
        return getValuesWithHigherRepetition(radialVariable, (sweep, i) -> {
            try {
                return sweep.getElevation(i);
            } catch (IOException e) {
                logger.error("Error reading elevation " + e.getMessage(), e);
            }
            return Float.NaN;
        });
    }

    Set<Float> getAzimuthSet(RadialDatasetSweep.RadialVariable radialVariable, Logger logger) throws IOException {
        return getValuesWithHigherRepetition(radialVariable, (sweep, i) -> {
            try {
                return sweep.getAzimuth(i);
            } catch (IOException e) {
                logger.error("Error reading azimuth " + e.getMessage(), e);
            }
            return Float.NaN;
        });
    }

    private Set<Float> getValuesWithHigherRepetition(RadialDatasetSweep.RadialVariable radialVariable, BiFunction<RadialDatasetSweep.Sweep, Integer, Float> fun) {
        HashMap<Float, Integer> valuesAndMatch = new HashMap<>();
        int min = radialVariable.getSweep(0).getRadialNumber();
        int max = 0;

        for (int i = 0; i < radialVariable.getNumSweeps(); i++) {
            RadialDatasetSweep.Sweep sweep = radialVariable.getSweep(i);
            for (int j = 0; j < sweep.getRadialNumber(); j++) {
                Float value = fun.apply(sweep, j);
                if (!value.isNaN()) {
                    int match;
                    if (valuesAndMatch.containsKey(value)) {
                        match = valuesAndMatch.get(value) + 1;
                    } else {
                        match = 1;
                    }
                    valuesAndMatch.put(value, match);
                    min = Integer.min(min, match);
                    max = Integer.max(max, match);
                }
            }
        }
        int limit = (max - min) / 2 + min;
        return valuesAndMatch.keySet().stream().filter(value -> valuesAndMatch.get(value) >= limit).collect(Collectors.toSet());
    }

    List<Float> getDataForElevation(RadialDatasetSweep.RadialVariable radialVariable, Float elevation, Logger logger) {
        return getDataFor(radialVariable, elevation, (sweep, i) -> {
            try {
                return sweep.getElevation(i);
            } catch (IOException e) {
                logger.error("Error reading elevation " + e.getMessage(), e);
            }
            return Float.NaN;
        }, logger);
    }

    List<Float> getDataForAzimuth(RadialDatasetSweep.RadialVariable radialVariable, Float azimuth, Logger logger) {
        return getDataFor(radialVariable, azimuth, (sweep, i) -> {
            try {
                return sweep.getAzimuth(i);
            } catch (IOException e) {
                logger.error("Error reading azimuth " + e.getMessage(), e);
            }
            return Float.NaN;
        }, logger);
    }

    private List<Float> getDataFor(RadialDatasetSweep.RadialVariable radialVariable, Float variable, BiFunction<RadialDatasetSweep.Sweep, Integer, Float> fun, Logger logger) {
        List<Float> dataForAzimuth = new ArrayList<>();
        for (int i = 0; i < radialVariable.getNumSweeps(); i++) {
            RadialDatasetSweep.Sweep sweep = radialVariable.getSweep(i);
            for (int j = 0; j < sweep.getRadialNumber(); j++) {
                Float value = fun.apply(sweep, j);
                if (!value.isNaN() && variable.equals(value)) {
                    try {
                        dataForAzimuth.addAll(Arrays.asList(ArrayUtils.toObject(sweep.readData(j))));
                    } catch (IOException e) {
                        logger.error("Error reading data for gate " + j + "sweep" + i + " " + e.getMessage(), e);
                    }
                }
            }
        }
        return dataForAzimuth;
    }
}
