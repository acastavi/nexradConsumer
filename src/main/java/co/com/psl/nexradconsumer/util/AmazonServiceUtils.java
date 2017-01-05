package co.com.psl.nexradconsumer.util;

import co.com.psl.nexradconsumer.dtos.DataForService;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by acastanedav on 21/12/16.
 */
public class AmazonServiceUtils {

    String getNumberString(int number) {
        if (number < 10)
            return "0" + number;
        else
            return Integer.toString(number);
    }

    public List<String> getStations(String nexradBucket, LocalDate date, Logger logger) {
        List<String> stationsList = new ArrayList<>();

        String url = nexradBucket + "/?delimiter=/&prefix=" + date.getYear() + "/" + getNumberString(date.getMonthValue()) + "/" + getNumberString(date.getDayOfMonth()) + "/";
        String tagName = "CommonPrefixes";

        List<String> valuesFromService = getValuesFromService(url, tagName, logger);
        for (String value : valuesFromService) {
            String[] prefix = value.split("/");
            stationsList.add(prefix[3]);
        }

        return stationsList;
    }

    public List<String> getFilesList(String nexradBucket, DataForService dataForService, Logger logger) {
        String url = nexradBucket + "/?delimiter=/&prefix=" + dataForService.getLocalDate().getYear() + "/" + getNumberString(dataForService.getLocalDate().getMonthValue()) + "/" + getNumberString(dataForService.getLocalDate().getDayOfMonth()) + "/" + dataForService.getStation() + "/";
        String tagName = "Contents";

        return getValuesFromService(url, tagName, logger);
    }

    private List<String> getValuesFromService(String urlString, String tagName, Logger logger) {
        List<String> valuesFromService = new ArrayList<>();

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document document = factory.newDocumentBuilder().parse(conn.getInputStream());

            NodeList nodeList = document.getElementsByTagName(tagName);
            for (int i = 0; i < nodeList.getLength(); i++) {
                valuesFromService.add(nodeList.item(i).getFirstChild().getFirstChild().getNodeValue());
            }

            conn.disconnect();
        } catch (Exception e) {
            logger.error("Problem with service: " + e.getMessage(), e);
        }

        return valuesFromService;
    }
}
