# Singapore Vertical Farming Research Management System (SVFRMS)
SE301 G2 Group 4

==================================================
       CONCURRENCY STRESS TEST RESULTS            
==================================================
Expected Total Alerts Generated: 250000

--- LEGACY CODE (Broken) ---
Generated: 206551 (DATA LOST due to int++ race condition!)

--- REFACTORED CODE (Fixed) ---
Generated: 250000 (100% ACCURATE via AtomicInteger)
Archived:  250000 (100% ACCURATE)
==================================================
? SUCCESS: Refactored code is 100% thread-safe.

"To prove the necessity of our concurrency fixes, we built a stress test simulating 50 threads generating 5,000 alerts each. The legacy int++ approach consistently lost data due to race conditions. Our refactored AtomicInteger approach achieved 100% accuracy without the performance overhead of heavy synchronized blocks."
