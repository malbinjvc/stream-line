package com.streamline.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

@Entity
@Table(name = "workflow_steps")
data class WorkflowStep(
    @Id
    val id: UUID = UUID.randomUUID(),

    @field:NotBlank
    @field:Size(max = 100)
    @Column(nullable = false, length = 100)
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var type: StepType = StepType.TRANSFORM,

    @Column(columnDefinition = "TEXT")
    var config: String? = null, // JSON config for the step

    @Column(nullable = false)
    var stepOrder: Int = 0,

    @Column(nullable = false)
    var retryCount: Int = 3,

    @Column(nullable = false)
    var timeoutSeconds: Int = 30,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    @JsonIgnore
    var workflow: Workflow? = null
)

enum class StepType {
    TRANSFORM, FILTER, ENRICH, VALIDATE, NOTIFY, HTTP_CALL, DELAY
}
