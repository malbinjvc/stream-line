package com.streamline.service

import com.streamline.model.*
import com.streamline.repository.WorkflowRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class WorkflowService(
    private val workflowRepo: WorkflowRepository
) {
    fun findAll(): List<Workflow> = workflowRepo.findAll()

    fun findById(id: UUID): Workflow? = workflowRepo.findByIdWithSteps(id)

    fun findByStatus(status: WorkflowStatus): List<Workflow> = workflowRepo.findByStatus(status)

    @Transactional
    fun create(name: String, description: String?, steps: List<StepRequest>): Workflow {
        val workflow = Workflow(name = name, description = description)

        steps.forEachIndexed { index, step ->
            workflow.steps.add(
                WorkflowStep(
                    name = step.name,
                    type = step.type,
                    config = step.config,
                    stepOrder = index,
                    retryCount = step.retryCount ?: 3,
                    timeoutSeconds = step.timeoutSeconds ?: 30,
                    workflow = workflow
                )
            )
        }

        return workflowRepo.save(workflow)
    }

    @Transactional
    fun updateStatus(id: UUID, status: WorkflowStatus): Workflow? {
        val workflow = workflowRepo.findById(id).orElse(null) ?: return null
        workflow.status = status
        workflow.updatedAt = Instant.now()
        return workflowRepo.save(workflow)
    }

    @Transactional
    fun delete(id: UUID): Boolean {
        if (!workflowRepo.existsById(id)) return false
        workflowRepo.deleteById(id)
        return true
    }
}

data class StepRequest(
    val name: String,
    val type: StepType,
    val config: String? = null,
    val retryCount: Int? = 3,
    val timeoutSeconds: Int? = 30
)
