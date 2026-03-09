package com.streamline.repository

import com.streamline.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface WorkflowRepository : JpaRepository<Workflow, UUID> {
    fun findByStatus(status: WorkflowStatus): List<Workflow>

    @Query("SELECT w FROM Workflow w LEFT JOIN FETCH w.steps WHERE w.id = :id")
    fun findByIdWithSteps(id: UUID): Workflow?
}

@Repository
interface WorkflowStepRepository : JpaRepository<WorkflowStep, UUID>

@Repository
interface EventRepository : JpaRepository<Event, UUID> {
    fun findByWorkflowIdOrderByCreatedAtDesc(workflowId: UUID): List<Event>
    fun findByStatus(status: EventStatus): List<Event>
    fun findByStatusIn(statuses: List<EventStatus>): List<Event>

    @Query("SELECT e.status, COUNT(e) FROM Event e GROUP BY e.status")
    fun countByStatus(): List<Array<Any>>

    @Query("SELECT COUNT(e) FROM Event e WHERE e.workflowId = :workflowId")
    fun countByWorkflowId(workflowId: UUID): Long
}
