package estg.ipp.pt.sd_grupo6_client.models;

import java.time.LocalDateTime;

public class Report {
    private LocalDateTime reportDate;
    private String line;
    private int numWarnings = 0;
    private int numPassengersWarned = 0;

    public Report(LocalDateTime reportDate, String line, int numWarnings, int numPassengersWarned) {
        this.reportDate = reportDate;
        this.line = line;
        this.numWarnings = numWarnings;
        this.numPassengersWarned = numPassengersWarned;
    }

    public LocalDateTime getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDateTime reportDate) {
        this.reportDate = reportDate;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public int getNumWarnings() {
        return numWarnings;
    }

    public int getNumPassengersWarned() {
        return numPassengersWarned;
    }
}
