# NexRad Consumer

The Next Generation Weather Radar (NEXRAD) is a network of 160 high-resolution Doppler radar sites that detects precipitation and atmospheric movement and disseminates data in approximately 5 minute intervals from each site. Amazon and the US National Oceanic and Atmospheric Administration (NOAA) are making this data available as part as a research agreement, more info https://aws.amazon.com/noaa-big-data/.

SparkTS is a library for Analyzing Time-Series Data with Apache Spark. http://sryza.github.io/spark-timeseries/

This POC uses the NEXRAD data available in Amazon from June to 1991 to present to exemplify what can be done with Apache Spark and SparkTS.

## Building NexRadConsumer

Spark is built using [Apache Maven](http://maven.apache.org/).
To build NexRadConsumer, run:

    mvn clean package

## Use NexRadConsumer

This POC needs Spark 1.4 available in http://spark.apache.org/downloads.html

To run NexRadConsumer run:

    bin/spark-submit --class co.com.psl.nexradconsumer.SparkExample path_to_jar/nexrad-1.0-SNAPSHOT-jar-with-dependencies.jar

## Out

This POC will create one unprocessed data in the folder $SPARK_HOME/nexrad/out/ with the mean reflectivity for elevation by time, with this data will create two TimeSeries, $SPARK_HOME/nexrad/uniform/origin/ and $SPARK_HOME/nexrad/irregular/origin/, the first one has an uniform time index and the second is build with the time index in which data have been collected. Because the data doesn't have an uniform interval between files, the first time series will be created with multiple gaps and the POC uses it to exemplify the linear interpolation available in sparkTS to fill some of the gaps in $SPARK_HOME/nexrad/uniform/out/. The second time series will have gaps for the elevations that no present data in every instant of time and the POC uses the time series to exemplify the stats available in sparkTS $SPARK_HOME/nexrad/irregular/out/.

The POC is using Reflectivity.
