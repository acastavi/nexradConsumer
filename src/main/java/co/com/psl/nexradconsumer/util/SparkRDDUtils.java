package co.com.psl.nexradconsumer.util;

import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import ucar.nc2.dt.RadialDatasetSweep;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by acastanedav on 20/12/16.
 */
public class SparkRDDUtils {

    JavaRDD<Float> filterRDDWithOutNaN(JavaRDD<Float> originalRDD) {
        return originalRDD.filter(number -> !number.isNaN());
    }

    JavaDoubleRDD getJavaDoubleRDDFromData(JavaSparkContext sparkContext, List<Float> data) {
        JavaRDD<Float> filteredRDD = filterRDDWithOutNaN(sparkContext.parallelize(data));
        return filteredRDD.mapToDouble(number -> (double) number);
    }

    private StructType getStructForElevation() {
        List<StructField> fields = new ArrayList<>();
        fields.add(DataTypes.createStructField("timestamp", DataTypes.TimestampType, true));
        fields.add(DataTypes.createStructField("elevation", DataTypes.StringType, true));
        fields.add(DataTypes.createStructField("mean", DataTypes.DoubleType, true));
        return DataTypes.createStructType(fields);
    }

    private StructType getStructForAzimuth() {
        List<StructField> fields = new ArrayList<>();
        fields.add(DataTypes.createStructField("timestamp", DataTypes.TimestampType, true));
        fields.add(DataTypes.createStructField("azimuth", DataTypes.StringType, true));
        fields.add(DataTypes.createStructField("mean", DataTypes.DoubleType, true));
        return DataTypes.createStructType(fields);
    }

    public List<Row> getDataForElevations(JavaSparkContext javaSparkContext, NexRADUtils nexRADUtils, RadialDatasetSweep.RadialVariable radialVariable, Date startDate, Logger logger) throws IOException {
        Set<Float> elevations = nexRADUtils.getElevationsSet(radialVariable, logger);
        return elevations.stream()
                .map(elevation -> {
                    JavaDoubleRDD dataForElevation = getJavaDoubleRDDFromData(javaSparkContext, nexRADUtils.getDataForElevation(radialVariable, elevation, logger));
                    return RowFactory.create(new Timestamp(startDate.getTime()), String.valueOf(elevation), dataForElevation.mean());
                })
                .collect(Collectors.toList());
    }

    public List<Row> getDataForAzimuths(JavaSparkContext javaSparkContext, NexRADUtils nexRADUtils, RadialDatasetSweep.RadialVariable radialVariable, Date startDate, Logger logger) throws IOException {
        Set<Float> azimuths = nexRADUtils.getAzimuthSet(radialVariable, logger);
        return azimuths.stream()
                .map(elevation -> {
                    JavaDoubleRDD dataForAzimuth = getJavaDoubleRDDFromData(javaSparkContext, nexRADUtils.getDataForAzimuth(radialVariable, elevation, logger));
                    return RowFactory.create(new Timestamp(startDate.getTime()), String.valueOf(elevation), dataForAzimuth.mean());
                })
                .collect(Collectors.toList());
    }

    public DataFrame loadObservationsForElevation(SQLContext sqlContext, JavaRDD<Row> data) {
        return sqlContext.createDataFrame(data,getStructForElevation());
    }

    public DataFrame loadObservationsForAzimuth(SQLContext sqlContext, JavaRDD<Row> data) {
        return sqlContext.createDataFrame(data,getStructForAzimuth());
    }
}
