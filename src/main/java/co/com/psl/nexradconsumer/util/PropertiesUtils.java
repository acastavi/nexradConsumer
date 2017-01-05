package co.com.psl.nexradconsumer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by acastanedav on 28/12/16.
 */
public class PropertiesUtils {

    private static PropertiesUtils instance = null;
    private Properties properties;

    private PropertiesUtils() throws IOException {
        properties = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("configuration.properties");
        properties.load(inputStream);
    }

    public static PropertiesUtils getInstance() throws IOException {
        if (instance == null)
            instance = new PropertiesUtils();
        return instance;
    }

    public String appName() {
        return properties.getProperty("appName");
    }

    public String nexradBucket() {
        return properties.getProperty("nexradBucket");
    }

    public String gzFolder() {
        return properties.getProperty("gzFolder");
    }

    public String uncompressedFolder() {
        return properties.getProperty("uncompressedFolder");
    }

    public String resultsFolder() {
        return properties.getProperty("resultsFolder");
    }

    public String uniformOriginFolder() {
        return properties.getProperty("uniformOriginFolder");
    }

    public String uniformOutFolder() {
        return properties.getProperty("uniformOutFolder");
    }

    public String irregularOriginFolder() {
        return properties.getProperty("irregularOriginFolder");
    }

    public String irregularOutFolder() {
        return properties.getProperty("irregularOutFolder");
    }

    public String variableName() {
        return properties.getProperty("variableName");
    }
}
