package com.example

import com.example.data.EmergencyMaxHeap
import com.example.model.EmergencyRequest
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testHeapOrderingAndTieBreaking() {
        val heap = EmergencyMaxHeap()

        // Insert Level 2
        heap.insert(EmergencyRequest("REQ-1", "Caller 1", "9000000001", "Loc 1", "Ambulance", 2, "Test", 1, "2026-11-03 10:00:00"))
        // Insert Level 4
        heap.insert(EmergencyRequest("REQ-2", "Caller 2", "9000000002", "Loc 2", "Fire", 4, "Test", 2, "2026-11-03 10:01:00"))
        // Insert another Level 4 (arrived later, seq=3)
        heap.insert(EmergencyRequest("REQ-3", "Caller 3", "9000000003", "Loc 3", "Police", 4, "Test", 3, "2026-11-03 10:02:00"))
        // Insert Level 1
        heap.insert(EmergencyRequest("REQ-4", "Caller 4", "9000000004", "Loc 4", "Other", 1, "Test", 4, "2026-11-03 10:03:00"))

        // First extracted must be REQ-2 (Severity 4, earlier arrival seq 2)
        val first = heap.extractMax()
        assertNotNull(first)
        assertEquals("REQ-2", first?.id)
        assertEquals(4, first?.severity)

        // Second extracted must be REQ-3 (Severity 4, later arrival seq 3)
        val second = heap.extractMax()
        assertNotNull(second)
        assertEquals("REQ-3", second?.id)

        // Third extracted must be REQ-1 (Severity 2)
        val third = heap.extractMax()
        assertNotNull(third)
        assertEquals("REQ-1", third?.id)

        // Fourth extracted must be REQ-4 (Severity 1)
        val fourth = heap.extractMax()
        assertNotNull(fourth)
        assertEquals("REQ-4", fourth?.id)

        // Empty extraction
        assertNull(heap.extractMax())
    }

    @Test
    fun testChangeSeverity() {
        val heap = EmergencyMaxHeap()
        heap.insert(EmergencyRequest("REQ-A", "Alice", "9111111111", "Loc A", "Ambulance", 1, "Test", 1, "10:00"))
        heap.insert(EmergencyRequest("REQ-B", "Bob", "9222222222", "Loc B", "Police", 2, "Test", 2, "10:01"))

        // Currently REQ-B has higher priority than REQ-A
        assertEquals("REQ-B", heap.peek()?.id)

        // Escalate REQ-A to Critical (Severity 4)
        val changed = heap.changeSeverity("REQ-A", 4)
        assertTrue(changed)

        // Now REQ-A must be at the root
        assertEquals("REQ-A", heap.peek()?.id)
        assertEquals(4, heap.peek()?.severity)
    }

    @Test
    fun testBuildHeap() {
        val heap = EmergencyMaxHeap()
        val items = listOf(
            EmergencyRequest("REQ-1", "A", "9000000001", "L1", "Ambulance", 1, "", 1, "10:00"),
            EmergencyRequest("REQ-2", "B", "9000000002", "L2", "Fire", 3, "", 2, "10:01"),
            EmergencyRequest("REQ-3", "C", "9000000003", "L3", "Police", 4, "", 3, "10:02")
        )

        heap.buildHeap(items)
        assertEquals(3, heap.size)
        assertEquals("REQ-3", heap.peek()?.id)
    }

    @Test
    fun testAmbulanceConfirmationAndDispatcherReview() {
        val heap = EmergencyMaxHeap()

        // Non-critical ambulance request flagged for review
        val screenedRequest = EmergencyRequest(
            id = "REQ-MED-1",
            callerName = "John Doe",
            phoneNumber = "9876543210",
            location = "City Clinic",
            serviceType = "Medical Emergency (Ambulance)",
            severity = 2,
            description = "Mild sprain [Screening: None of these / unsure]",
            sequenceNumber = 10,
            timestamp = "12:00",
            isDispatcherReviewRequired = true,
            screeningSymptoms = listOf("None of these / unsure")
        )
        heap.insert(screenedRequest)
        assertEquals(1, heap.size)
        assertTrue(heap.peek()?.isDispatcherReviewRequired == true)

        // Critical cardiac emergency arrives
        val cardiac = EmergencyRequest(
            id = "REQ-MED-2",
            callerName = "Jane Smith",
            phoneNumber = "9876543211",
            location = "Main St",
            serviceType = "Medical Emergency (Ambulance)",
            severity = 4,
            description = "Chest pain and breathing difficulty [Screening: Chest pain, Difficulty breathing]",
            sequenceNumber = 11,
            timestamp = "12:01",
            isDispatcherReviewRequired = false,
            screeningSymptoms = listOf("Chest pain", "Difficulty breathing")
        )
        heap.insert(cardiac)

        // Binary Max-Heap guarantees cardiac is served FIRST despite arriving later
        val nextDispatched = heap.peek()
        assertNotNull(nextDispatched)
        assertEquals("REQ-MED-2", nextDispatched?.id)
        assertEquals(4, nextDispatched?.severity)
    }
}
