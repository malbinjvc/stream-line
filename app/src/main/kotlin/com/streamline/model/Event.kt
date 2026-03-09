package com.streamline.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "events", indexes = [
    Index(name = "idx_events_workflow", columnList = "workflowId"),
    Index(name = "idx_events_status", columnList = "status"),
    Index(name = "idx_events_created", columnList = "createdAt")
])
data class Event(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val workflowId: UUID = UUID.randomUUID(),

    @Column(columnDefinition = "TEXT", nullable = false)
    var payload: String = "{}",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: EventStatus = EventStatus.PENDING,

    @Column(nullable = false)
    var currentStep: Int = 0,

    @Column(columnDefinition = "TEXT")
    var result: String? = null,

    @Column(length = 1000)
    var errorMessage: String? = null,

    @Column(nullable = false)
    var retryAttempt: Int = 0,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    var completedAt: Instant? = null
)

enum class EventStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, RETRYING
}
