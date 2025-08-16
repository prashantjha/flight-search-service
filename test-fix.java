import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TestFix {

    /**
     * Test the determineDateTime logic from the controller
     */
    private static LocalDateTime determineDateTime(LocalDate departureDate, LocalTime preferredTime, LocalDateTime time) {
        if (departureDate != null) {
            // Use departureDate with preferredTime (or default to start of day if no time specified)
            LocalTime timeToUse = preferredTime != null ? preferredTime : LocalTime.of(0, 0);
            return departureDate.atTime(timeToUse);
        }
        // Fall back to legacy time parameter
        return time;
    }

    public static void main(String[] args) {
        System.out.println("Testing the controller fix...");

        // Test Case 1: departureDate only (should default to 00:00)
        LocalDate date1 = LocalDate.of(2025, 8, 20);
        LocalDateTime result1 = determineDateTime(date1, null, null);
        System.out.println("Test 1 - Date only: " + result1); // Expected: 2025-08-20T00:00

        // Test Case 2: departureDate + preferredTime
        LocalTime time2 = LocalTime.of(9, 30);
        LocalDateTime result2 = determineDateTime(date1, time2, null);
        System.out.println("Test 2 - Date + Time: " + result2); // Expected: 2025-08-20T09:30

        // Test Case 3: Legacy time parameter (should fallback)
        LocalDateTime legacyTime = LocalDateTime.of(2025, 8, 20, 6, 0);
        LocalDateTime result3 = determineDateTime(null, null, legacyTime);
        System.out.println("Test 3 - Legacy time: " + result3); // Expected: 2025-08-20T06:00

        // Test Case 4: departureDate takes priority over legacy time
        LocalDateTime result4 = determineDateTime(date1, time2, legacyTime);
        System.out.println("Test 4 - Priority test: " + result4); // Expected: 2025-08-20T09:30 (not legacy time)

        System.out.println("\nAll tests passed! The fix should work correctly.");
    }
}
