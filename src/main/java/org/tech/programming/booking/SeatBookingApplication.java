package org.tech.programming.booking;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SeatBookingApplication {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================================");
        System.out.println("CONCURRENT EVENT SEAT BOOKING SYSTEM (INTERACTIVE)");
        System.out.println("==================================================");

        // 1. Dynamic Initialization (Custom event name & total seats)
        System.out.print("Enter Event Name (Press Enter for default 'Tech Meetup 2026'): ");
        String eventNameInput = scanner.nextLine().trim();
        String eventName = eventNameInput.isEmpty() ? "Tech Meetup 2026" : eventNameInput;

        System.out.print("Enter Total Number of Seats (Press Enter for default 10): ");
        String seatsInput = scanner.nextLine().trim();
        int totalSeats = seatsInput.isEmpty() ? 10 : Integer.parseInt(seatsInput);

        Event event = new Event(eventName, totalSeats);
        BookingService service = new BookingService(event);
        System.out.println("[OK] Created Event '" + event.getEventName() + "' with "
                + event.getTotalSeats() + " seats (Seats 1 to " + event.getTotalSeats() + ").\n");

        // 2. Start the Status Monitor background thread
        StatusMonitor monitor = new StatusMonitor(5000); // logs every 5 seconds as per guide
        Thread monitorThread = new Thread(monitor, "status-monitor");
//        monitorThread.start();

        // 3. Create Fixed Thread Pool Executor Service (3 worker threads)
        ExecutorService executor = Executors.newFixedThreadPool(3);

        boolean running = true;
        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Single Seat Booking");
            System.out.println("2. Submit Multiple Concurrent Bookings (Batch/Conflict Test)");
            System.out.println("3. View Available Seats");
            System.out.println("4. View Confirmed Bookings");
            System.out.println("5. Exit & Teardown");
            System.out.print("Choose an option (1-5): ");

            String choiceStr = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid input. Please enter a number between 1 and 5.");
                continue;
            }

            switch (choice) {
                case 1:
                    // Single Seat Booking
                    System.out.print("Enter Customer Name: ");
                    String custName = scanner.nextLine().trim();
                    System.out.print("Enter Seat Number (1 to " + event.getTotalSeats() + "): ");
                    try {
                        int seatNum = Integer.parseInt(scanner.nextLine().trim());
                        executor.submit(new BookingTask(service, custName, seatNum));
                    } catch (NumberFormatException e) {
                        System.out.println("⚠️ Invalid seat number format.");
                    }
                    break;

                case 2:
                    // Concurrent Multi-Booking (Collision / Race Condition Test)
                    System.out.print("How many concurrent booking requests in this batch? ");
                    try {
                        int count = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println("[APPLICATION] Launching " + count + " requests SIMULTANEOUSLY across thread pool...");
                        for (int i = 1; i <= count; i++) {
                            System.out.print("  -> Request " + i + " Customer Name: ");
                            String batchCust = scanner.nextLine().trim();
                            System.out.print("  -> Request " + i + " Seat Number: ");
                            int batchSeat = Integer.parseInt(scanner.nextLine().trim());

                            executor.submit(new BookingTask(service, batchCust, batchSeat));
                        }
                        // Give worker threads a small moment to flush output cleanly to console
                        Thread.sleep(500);
                    } catch (NumberFormatException | InterruptedException e) {
                        System.out.println("⚠️ Invalid batch input format.");
                    }
                    break;

                case 3:
                    // View Available Seats
                    System.out.println("[AVAILABLE SEATS]: " + service.getAvailableSeats());
                    break;

                case 4:
                    // View Confirmed Bookings
                    System.out.println("[CONFIRMED BOOKINGS]:");
                    for (Booking b : service.getBookings()) {
                        System.out.println("  " + b);
                    }
                    break;

                case 5:
                    // Graceful Teardown
                    running = false;
                    break;

                default:
                    System.out.println("⚠️ Invalid choice. Select between 1 and 5.");
            }
        }

        scanner.close();

        // 4. Shutdown Executor and Stop Monitor
        System.out.println("\n[APPLICATION] Shutting down thread pool executor...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("[APPLICATION] Stopping background status monitor...");
        monitor.stopMonitoring();
        try {
            monitorThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 5. Final Summary Output
        System.out.println("\n==================== BOOKING SUMMARY ====================");
        System.out.println("Event: " + event.getEventName() + " | Total: " + event.getTotalSeats()
                + " | Booked: " + service.getBookings().size()
                + " | Remaining: " + service.getAvailableSeats().size());
        System.out.println("Final Confirmed Bookings: " + service.getBookings());
        System.out.println("=========================================================");
    }
}