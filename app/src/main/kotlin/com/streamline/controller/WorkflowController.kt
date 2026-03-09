package com.streamline.controller

import com.streamline.model.*
import com.streamline.service.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/workflows")
class WorkflowController(
    private val workflowService: WorkflowService,
    private val eventService: EventService
) {
    @GetMapping
    fun list(): List<WorkflowResponse> =
        workflowService.findAll().map { it.toResponse() }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ResponseEntity<WorkflowDetailResponse> {
        val workflow = workflowService.findById(id) ?: return ResponseEntity.notFound().build()
        val eventCount = eventService.findByWorkflow(id).size
        return ResponseEntity.ok(workflow.toDetailResponse(eventCount))
    }

    @PostMapping
    fun create(@Valid @RequestBody request: CreateWorkflowRequest): ResponseEntity<WorkflowResponse> {
        val workflow = workflowService.create(request.name, request.description, request.steps)
        return ResponseEntity.status(HttpStatus.CREATED).body(workflow.toResponse())
    }

    @PutMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: UUID,
        @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<WorkflowResponse> {
        val workflow = workflowService.updateStatus(id, request.status)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(workflow.toResponse())
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        return if (workflowService.delete(id)) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }
}

// Request/Response DTOs
data class CreateWorkflowRequest(
    @field:NotBlank @field:Size(max = 100)
    val name: String,
    @field:Size(max = 500)
    val description: String? = null,
    val steps: List<StepRequest> = emptyList()
)

data class UpdateStatusRequest(val status: WorkflowStatus)

data class WorkflowResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val status: WorkflowStatus,
    val stepCount: Int,
    val createdAt: String,
    val updatedAt: String
)

data class WorkflowDetailResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val status: WorkflowStatus,
    val steps: List<StepResponse>,
    val eventCount: Int,
    val createdAt: String,
    val updatedAt: String
)

data class StepResponse(
    val id: UUID,
    val name: String,
    val type: StepType,
    val config: String?,
    val stepOrder: Int,
    val retryCount: Int,
    val timeoutSeconds: Int
)

fun Workflow.toResponse() = WorkflowResponse(
    id = id, name = name, description = description,
    status = status, stepCount = steps.size,
    createdAt = createdAt.toString(), updatedAt = updatedAt.toString()
)

fun Workflow.toDetailResponse(eventCount: Int) = WorkflowDetailResponse(
    id = id, name = name, description = description, status = status,
    steps = steps.map { it.toResponse() }, eventCount = eventCount,
    createdAt = createdAt.toString(), updatedAt = updatedAt.toString()
)

fun WorkflowStep.toResponse() = StepResponse(
    id = id, name = name, type = type, config = config,
    stepOrder = stepOrder, retryCount = retryCount, timeoutSeconds = timeoutSeconds
)
