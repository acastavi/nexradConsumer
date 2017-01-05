package co.com.psl.nexradconsumer.util;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Created by acastanedav on 21/12/16.
 */
public class UserUtils {

    private PrintStream printStream;
    private InputStream inputStream;

    public UserUtils() {
        printStream = System.out;
        inputStream = System.in;
    }

    void setInputStream(InputStream in) {
        inputStream = in;
    }

    void setPrintStream(PrintStream out) {
        printStream = out;
    }

    public LocalDate getDateFromUser() throws IOException {
        Optional<LocalDate> optional = Optional.empty();
        while (!optional.isPresent()) {
            printStream.println("Please enter a date between 01/06/1991 and today, keep in mind format dd/mm/yyyy");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String entry = br.readLine();

            optional = getDate(entry);
        }
        return optional.get();
    }

    public String getStationFromUser(List<String> stations) throws IOException {
        String station = "";
        while (station.length() == 0) {
            printStream.println("In the selected date, the following stations has data, please select one station:");
            for (String stationName : stations) {
                printStream.print(stationName + " ");
                if ((stations.indexOf(stationName) + 1) % 12 == 0)
                    printStream.println("");
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String entry = br.readLine();
            if (stations.contains(entry))
                station = entry;
            else
                printStream.println("Please select a valid station");
        }
        return station;
    }

    public int getIndexFromUser(int size) throws IOException {
        Optional<Integer> optional = Optional.empty();
        while (!optional.isPresent()) {
            printStream.println("This station has " + size + " files, ¿how many files do you want to work with?, should be higher than 2");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String entry = br.readLine();

            optional = getNumber(entry, size);
        }
        return optional.get();
    }

    private LocalDate getInitialLimit() {
        return LocalDate.of(1991, 6, 1);
    }

    Optional<LocalDate> getDate(String entry) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        try {
            LocalDate localDate = LocalDate.parse(entry, formatter);
            if (localDate.isAfter(getInitialLimit()) && localDate.isBefore(LocalDate.now()))
                return Optional.of(localDate);
        } catch (DateTimeParseException e) {
            printStream.println("Problem with entry " + entry + e.getMessage());
        }
        return Optional.empty();
    }

    Optional<Integer> getNumber(String entry, int size) {
        try {
            int number = Integer.parseInt(entry);
            if (number > 2 && number < size)
                return Optional.of(number);
        } catch (NumberFormatException e) {
            printStream.println("You did not enter a number");
        }
        return Optional.empty();
    }
}
