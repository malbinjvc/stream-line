package com.streamline.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "workflows")
data class Workflow(
    @Id
    val id: UUID = UUID.randomUUID(),

    @field:NotBlank
    @field:Size(max = 100)
    @Column(nullable = false, length = 100)
    var name: String = "",

    @field:Size(max = 500)
    @Column(length = 500)
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: WorkflowStatus = WorkflowStatus.DRAFT,

    @OneToMany(mappedBy = "workflow", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("stepOrder ASC")
    val steps: MutableList<WorkflowStep> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
)

enum class WorkflowStatus {
    DRAFT, ACTIVE, PAUSED, ARCHIVED
}
