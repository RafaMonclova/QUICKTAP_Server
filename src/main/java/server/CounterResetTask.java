package server;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class CounterResetTask extends TimerTask {
    private int counter = 0;

    public void run() {
        // Reiniciar el contador a cero si es medianoche
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.HOUR_OF_DAY) == 0 && now.get(Calendar.MINUTE) == 0) {
            counter = 0;
        }
    }


    
}