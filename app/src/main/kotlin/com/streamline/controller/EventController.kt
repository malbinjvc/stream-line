package com.streamline.controller

import com.streamline.model.*
import com.streamline.service.EventService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/events")
class EventController(
    private val eventService: EventService
) {
    @PostMapping
    fun submit(@Valid @RequestBody request: SubmitEventRequest): ResponseEntity<EventResponse> {
        return try {
            val event = eventService.submit(request.workflowId, request.payload)
            ResponseEntity.status(HttpStatus.ACCEPTED).body(event.toResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ResponseEntity<EventResponse> {
        val event = eventService.findById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(event.toResponse())
    }

    @GetMapping("/workflow/{workflowId}")
    fun listByWorkflow(@PathVariable workflowId: UUID): List<EventResponse> =
        eventService.findByWorkflow(workflowId).map { it.toResponse() }

    @GetMapping("/status/{status}")
    fun listByStatus(@PathVariable status: EventStatus): List<EventResponse> =
        eventService.findByStatus(status).map { it.toResponse() }

    @GetMapping("/stats")
    fun stats(): Map<String, Any> = eventService.getStats()
}

data class SubmitEventRequest(
    val workflowId: UUID,
    @field:NotBlank
    val payload: String
)

data class EventResponse(
    val id: UUID,
    val workflowId: UUID,
    val status: EventStatus,
    val currentStep: Int,
    val result: String?,
    val errorMessage: String?,
    val retryAttempt: Int,
    val createdAt: String,
    val completedAt: String?
)

fun Event.toResponse() = EventResponse(
    id = id, workflowId = workflowId, status = status,
    currentStep = currentStep, result = result,
    errorMessage = errorMessage, retryAttempt = retryAttempt,
    createdAt = createdAt.toString(), completedAt = completedAt?.toString()
)
