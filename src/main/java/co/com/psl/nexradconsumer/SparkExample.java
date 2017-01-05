package co.com.psl.nexradconsumer;

import co.com.psl.nexradconsumer.dtos.DataForService;
import co.com.psl.nexradconsumer.dtos.IndexForTimeSeries;
import co.com.psl.nexradconsumer.util.*;
import com.cloudera.sparkts.DateTimeIndex;
import com.cloudera.sparkts.MillisecondFrequency;
import com.cloudera.sparkts.api.java.DateTimeIndexFactory;
import com.cloudera.sparkts.api.java.JavaTimeSeriesRDD;
import com.cloudera.sparkts.api.java.JavaTimeSeriesRDDFactory;
import org.apache.commons.io.FileUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.slf4j.Logger;
import ucar.nc2.dt.RadialDatasetSweep;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by acastanedav on 23/12/16.
 */
public class SparkExample {

    private PropertiesUtils propertiesUtils;
    private UserUtils userUtils;
    private AmazonServiceUtils amazonServiceUtils;
    private ZipUtils zipUtils;
    private NexRADUtils nexRADUtils;
    private SparkRDDUtils sparkRDDUtils;
    private MathUtils mathUtils;

    private JavaSparkContext javaSparkContext;
    private SQLContext sqlContext;
    private Logger logger;
    private List<Long> dates;

    public static void main(String[] args) throws IOException {
        new SparkExample().run();
    }

    private void run() throws IOException {
        setUtils();
        setSparkContext();

        Optional<DataForService> dataForService = getDataFromUser();
        if (dataForService.isPresent()) {
            List<String> compressedData = amazonServiceUtils.getFilesList(propertiesUtils.nexradBucket(), dataForService.get(), logger);
            int index = userUtils.getIndexFromUser(compressedData.size());
            List<String> unCompressedData = downloadAndUnzipData(compressedData.subList(0, index));

            if (unCompressedData.size() > 2) {
                List<Row> data = processDataForVariable(unCompressedData);

                deleteDirectoriesForOutput();
                JavaRDD<Row> rowJavaRDD = getJavaRDD(data);
                DataFrame elevationObs = sparkRDDUtils.loadObservationsForElevation(sqlContext, rowJavaRDD);

                ZoneId zoneId = ZoneId.systemDefault();
                System.out.println("Creating Uniform TimeSeries");
                workWithSparkTSUniformSeries(getUniformIndex(zoneId), elevationObs);
                System.out.println("Creating Ununiform TimeSeries");
                workWithSparkTSIrregularSeries(getIrregularIndex(zoneId), elevationObs);
            } else {
                System.out.println("There is no enough useful data in the selected date");
            }
        }

    }

    private DateTimeIndex getUniformIndex(ZoneId zone) {
        IndexForTimeSeries bestMatch = mathUtils.getBestIndexForDates(dates);

        return DateTimeIndexFactory.uniformFromInterval(
                ZonedDateTime.of(Instant.ofEpochMilli(bestMatch.getStart()).atZone(zone).toLocalDateTime(), zone),
                ZonedDateTime.of(Instant.ofEpochMilli(bestMatch.getEnd()).atZone(zone).toLocalDateTime(), zone),
                new MillisecondFrequency(Math.toIntExact(bestMatch.getInterval() / 2)));
    }

    private DateTimeIndex getIrregularIndex(ZoneId zone) {
        List<ZonedDateTime> index = dates.stream().map(millis -> ZonedDateTime.of(Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime(), zone)).collect(Collectors.toList());

        return DateTimeIndexFactory.irregular(index.toArray(new ZonedDateTime[index.size()]), zone);
    }

    private void directory(String folder) throws IOException {
        File file = new File(folder);
        FileUtils.forceMkdir(file);
        FileUtils.cleanDirectory(file);
    }

    private void deleteDirectoriesForOutput() throws IOException {
        FileUtils.deleteDirectory(new File(propertiesUtils.resultsFolder()));
        FileUtils.deleteDirectory(new File(propertiesUtils.uniformOriginFolder()));
        FileUtils.deleteDirectory(new File(propertiesUtils.uniformOutFolder()));
        FileUtils.deleteDirectory(new File(propertiesUtils.irregularOriginFolder()));
        FileUtils.deleteDirectory(new File(propertiesUtils.irregularOutFolder()));
    }

    private void workWithSparkTSUniformSeries(DateTimeIndex dtIndex, DataFrame elevationObs) throws IOException {
        JavaTimeSeriesRDD elevationsTSRDD = JavaTimeSeriesRDDFactory.timeSeriesRDDFromObservations(
                dtIndex, elevationObs, "timestamp", "elevation", "mean");
        elevationsTSRDD.saveAsTextFile(propertiesUtils.uniformOriginFolder());

        JavaTimeSeriesRDD observationsFilledWithLinearInterpolation = elevationsTSRDD.fill("linear");
        observationsFilledWithLinearInterpolation.saveAsTextFile(propertiesUtils.uniformOutFolder());
    }

    private void workWithSparkTSIrregularSeries(DateTimeIndex dtIndex, DataFrame elevationObs) throws IOException {
        JavaTimeSeriesRDD elevationsTSRDD = JavaTimeSeriesRDDFactory.timeSeriesRDDFromObservations(
                dtIndex, elevationObs, "timestamp", "elevation", "mean");
        elevationsTSRDD.saveAsTextFile(propertiesUtils.irregularOriginFolder());

        JavaRDD statsForObservations = elevationsTSRDD.seriesStats();
        statsForObservations.saveAsTextFile(propertiesUtils.irregularOutFolder());
    }

    private JavaRDD<Row> getJavaRDD(List<Row> data) throws IOException {
        JavaRDD<Row> javaRDD = javaSparkContext.parallelize(data);
        javaRDD.saveAsTextFile(propertiesUtils.resultsFolder());
        return javaRDD;
    }

    private List<Row> processDataForVariable(List<String> nexradFiles) {
        return nexradFiles.stream().map(nexrad -> {
            try {
                RadialDatasetSweep radialDatasetSweep = nexRADUtils.rds(nexrad, logger);
                RadialDatasetSweep.RadialVariable radialVariable = (RadialDatasetSweep.RadialVariable) radialDatasetSweep.getDataVariable(propertiesUtils.variableName());
                Date startDate = radialDatasetSweep.getStartDate();
                dates.add(startDate.getTime());
                System.out.println("Processing " + nexrad + ", date " + startDate);
                return sparkRDDUtils.getDataForElevations(javaSparkContext, nexRADUtils, radialVariable, startDate, logger);
            } catch (IOException e) {
                logger.error("Problem reading data from file: " + nexrad + " " + e.getMessage(), e);
            }
            return new ArrayList<Row>();
        }).collect(ArrayList::new, List::addAll, List::addAll);
    }

    private List<String> downloadAndUnzipData(List<String> compressedData) {
        List<String> fileNames = new ArrayList<>();
        String gzFolder = propertiesUtils.gzFolder();
        String uncompressedFolder = propertiesUtils.uncompressedFolder();
        try {
            directory(gzFolder);
            directory(uncompressedFolder);
            for (String fileName : compressedData) {
                String gzName = fileName.split("/")[4];
                String uncompressedName = uncompressedFolder + gzName.split("\\.")[0];

                File gzFile = new File(gzFolder + gzName);

                URL url = new URL(propertiesUtils.nexradBucket() + "/" + fileName);
                FileUtils.copyURLToFile(url, gzFile);
                System.out.println(fileName + " downloaded");

                String result = zipUtils.unZip(gzFolder + gzName, uncompressedName, logger);
                if (result.length() > 0)
                    fileNames.add(result);
            }
        } catch (MalformedURLException e) {
            logger.error("Problem with url: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Problem with service: " + e.getMessage(), e);
        }
        return fileNames;
    }

    private Optional<DataForService> getDataFromUser() throws IOException {
        DataForService dataForService = new DataForService();
        LocalDate date = userUtils.getDateFromUser();
        dataForService.setLocalDate(date);
        logger.info("The date entered is: " + date.toString());

        List<String> stations = amazonServiceUtils.getStations(propertiesUtils.nexradBucket(), date, logger);
        if (stations.isEmpty()) {
            System.out.println("There is no data in the selected station");
            return Optional.empty();
        }
        dataForService.setStation(userUtils.getStationFromUser(stations));

        return Optional.of(dataForService);
    }

    private void setSparkContext() {
        SparkConf conf = new SparkConf().
                setAppName(propertiesUtils.appName());
        javaSparkContext = new JavaSparkContext(conf);
        sqlContext = new SQLContext(javaSparkContext);
        logger = conf.log();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    private void setUtils() throws IOException {
        propertiesUtils = PropertiesUtils.getInstance();
        userUtils = new UserUtils();
        amazonServiceUtils = new AmazonServiceUtils();
        zipUtils = new ZipUtils();
        nexRADUtils = new NexRADUtils();
        sparkRDDUtils = new SparkRDDUtils();
        mathUtils = new MathUtils();
        dates = new ArrayList<>();
    }

}
