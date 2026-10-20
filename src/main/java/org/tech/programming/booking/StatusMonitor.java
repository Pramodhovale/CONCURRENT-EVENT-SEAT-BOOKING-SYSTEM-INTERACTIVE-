package org.tech.programming.booking;

public class StatusMonitor implements Runnable {

    private volatile boolean running = true;
    private final long intervalMillis;

    public StatusMonitor(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    @Override
    public void run() {
        while (running) {
//           System.out.println("Booking system is running...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Status monitoring stopped.");
    }

    public void stopMonitoring() {
        running=false;
    }

}