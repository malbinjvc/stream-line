package com.streamline.service

import com.streamline.model.*
import com.streamline.repository.EventRepository
import com.streamline.repository.WorkflowRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class EventService(
    private val eventRepo: EventRepository,
    private val workflowRepo: WorkflowRepository
) {
    fun findById(id: UUID): Event? = eventRepo.findById(id).orElse(null)

    fun findByWorkflow(workflowId: UUID): List<Event> =
        eventRepo.findByWorkflowIdOrderByCreatedAtDesc(workflowId)

    fun findByStatus(status: EventStatus): List<Event> = eventRepo.findByStatus(status)

    fun getStats(): Map<String, Any> {
        val counts = eventRepo.countByStatus()
        val statusMap = counts.associate { (it[0] as EventStatus).name to (it[1] as Long) }
        return mapOf(
            "total" to eventRepo.count(),
            "byStatus" to statusMap
        )
    }

    @Transactional
    fun submit(workflowId: UUID, payload: String): Event {
        val workflow = workflowRepo.findById(workflowId).orElse(null)
            ?: throw IllegalArgumentException("Workflow not found: $workflowId")

        if (workflow.status != WorkflowStatus.ACTIVE) {
            throw IllegalStateException("Workflow '${workflow.name}' is not active (status: ${workflow.status})")
        }

        val event = Event(
            workflowId = workflowId,
            payload = payload,
            status = EventStatus.PENDING
        )

        return eventRepo.save(event)
    }

    @Transactional
    fun process(event: Event, steps: List<WorkflowStep>): Event {
        event.status = EventStatus.PROCESSING

        try {
            var currentPayload = event.payload

            for (step in steps.sortedBy { it.stepOrder }) {
                if (step.stepOrder < event.currentStep) continue

                currentPayload = executeStep(step, currentPayload)
                event.currentStep = step.stepOrder + 1
            }

            event.status = EventStatus.COMPLETED
            event.result = currentPayload
            event.completedAt = Instant.now()
        } catch (e: Exception) {
            if (event.retryAttempt < (steps.getOrNull(event.currentStep)?.retryCount ?: 3)) {
                event.status = EventStatus.RETRYING
                event.retryAttempt++
            } else {
                event.status = EventStatus.FAILED
                event.completedAt = Instant.now()
            }
            event.errorMessage = e.message?.take(1000)
        }

        return eventRepo.save(event)
    }

    private fun executeStep(step: WorkflowStep, payload: String): String {
        return when (step.type) {
            StepType.TRANSFORM -> {
                // Apply JSON transformation (simplified: wrap in step metadata)
                """{"step":"${step.name}","data":$payload}"""
            }
            StepType.FILTER -> {
                // Filter step: pass through (in real impl, evaluate condition from config)
                payload
            }
            StepType.ENRICH -> {
                // Enrich: add timestamp
                val enriched = payload.trimEnd('}')
                """$enriched,"enriched_at":"${Instant.now()}"}"""
            }
            StepType.VALIDATE -> {
                // Validate: check payload is valid JSON
                try {
                    com.fasterxml.jackson.databind.ObjectMapper().readTree(payload)
                    payload
                } catch (e: Exception) {
                    throw IllegalStateException("Validation failed: invalid JSON payload")
                }
            }
            StepType.NOTIFY -> {
                // Notify: log notification (in real impl, send to webhook)
                payload
            }
            StepType.HTTP_CALL -> {
                // HTTP call: simulated (in real impl, make HTTP request using config URL)
                payload
            }
            StepType.DELAY -> {
                // Delay step: sleep for configured time
                val delayMs = step.config?.toLongOrNull() ?: 100
                Thread.sleep(delayMs.coerceAtMost(5000))
                payload
            }
        }
    }
}
