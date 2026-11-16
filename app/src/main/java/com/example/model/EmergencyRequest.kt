package com.example.model

enum class SeverityLevel(val value: Int, val displayName: String) {
    LOW(1, "1 - Low"),
    MEDIUM(2, "2 - Medium"),
    HIGH(3, "3 - High"),
    CRITICAL(4, "4 - Critical");

    companion object {
        fun fromValue(v: Int): SeverityLevel = when (v) {
            4 -> CRITICAL
            3 -> HIGH
            2 -> MEDIUM
            else -> LOW
        }
    }
}

enum class RequestStatus(val displayName: String) {
    PENDING("Request Received"),
    REVIEWING("Dispatcher Reviewing"),
    ASSIGNED("Ambulance Assigned"),
    IN_PROGRESS("En Route"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class EmergencyRequest(
    val id: String,
    val callerName: String,
    val phoneNumber: String,
    val location: String,
    val serviceType: String,
    val severity: Int,
    val description: String,
    val sequenceNumber: Long,
    val timestamp: String,
    val userId: String = "",
    val userAddress: String = "",
    val gpsCoordinates: String = "",
    val creationEpoch: Long = System.currentTimeMillis(),
    val status: RequestStatus = RequestStatus.PENDING,
    val assignedPersonnel: String = "Unassigned",
    val resolutionTime: String = "",
    val isDispatcherReviewRequired: Boolean = false,
    val screeningSymptoms: List<String> = emptyList()
)

enum class NotificationType {
    CRITICAL_ALERT,
    ASSIGNMENT,
    STATUS_UPDATE,
    CANCELLATION
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType,
    val relatedRequestId: String = ""
)
