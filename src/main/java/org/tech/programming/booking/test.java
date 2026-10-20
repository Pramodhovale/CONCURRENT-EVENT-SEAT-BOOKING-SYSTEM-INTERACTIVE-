[ Main Thread ] (JVM Entry Point)
              │
              ├──> 1. Starts Background Monitor Thread
              │         └──> [ StatusMonitor ] (Runs periodically using volatile 'running' flag)
              │
              └──> 2. Starts Interactive Scanner Console Loop
                        │
                        └──> Submits Booking Requests to ──> [ ExecutorService Thread Pool ]
                                                                 (Fixed Pool of 3 Worker Threads)
                                                                       │
                                              ┌────────────────────────┴────────────────────────┐
                                              ▼                                                 ▼
                                     [ pool-1-thread-1 ]                               [ pool-1-thread-2 ]
                                              │                                                 │
                                              └──────────────────┐ ┌────────────────────────────┘
                                                                 ▼ ▼
                                                    [ BookingService ] (Shared Resource)
                                                    ┌──────────────────────────────────┐
                                                    │ synchronized bookSeat()          │
                                                    │  ├── 1. validateSeat()           │
                                                    │  ├── 2. check availableSeats     │
                                                    │  ├── 3. remove & create Booking  │
                                                    │  └── 4. add to bookings list     │
                                                    └──────────────────────────────────┘
                                                                 │
                                                       (Protects against race conditions 
                                                        & double-booking collisions)
