package estg.ipp.pt.sd_grupo6_client.localTrafficManager;

import com.google.gson.JsonObject;
import estg.ipp.pt.sd_grupo6_client.models.Report;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ReportThread extends Thread {
    private PrintWriter centralServerOut = null;
    private Timer t;

    public ReportThread(Socket centralServerSocket) {
        try {
            centralServerOut = new PrintWriter(centralServerSocket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Periodicamente envia um relatório com a data e hora de envio, a linha a que pertence o relatório,
     * o número de notificações efetuadas e o número de passageiros notificados.
     */
    public void run() {
        t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                Report report = new Report(
                        LocalDateTime.now(),
                        "Linha 1",
                        LocalTrafficServer.getWarningsCounter(),
                        LocalTrafficServer.getPassengersWarnedCounter());

                JsonObject reportObject = new JsonObject();
                reportObject.addProperty("ReportDate", report.getReportDate().toString());
                reportObject.addProperty("Line", report.getLine());
                reportObject.addProperty("TotalWarnings", report.getNumWarnings());
                reportObject.addProperty("TotalPassengersWarned", report.getNumPassengersWarned());

                LocalTrafficServer.resetCounters();

                centralServerOut.println(Arrays.toString(new String[]{"SendReport", reportObject.toString()}));
            }

            ;
        };
        t.schedule(tt, new Date(), 60000 * 30);
    }

    /**
     * Fecha a conexão
     */
    public void close() {
        t.cancel();
    }
}
