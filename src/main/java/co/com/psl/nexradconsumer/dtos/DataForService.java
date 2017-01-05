package co.com.psl.nexradconsumer.dtos;


import java.time.LocalDate;

/**
 * Created by acastanedav on 5/01/17.
 */
public class DataForService {

    private LocalDate localDate;
    private String station;

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }
}
