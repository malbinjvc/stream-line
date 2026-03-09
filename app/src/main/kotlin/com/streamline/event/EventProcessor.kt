package com.streamline.event

import com.streamline.model.EventStatus
import com.streamline.repository.EventRepository
import com.streamline.repository.WorkflowRepository
import com.streamline.service.EventService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class EventProcessor(
    private val eventRepo: EventRepository,
    private val workflowRepo: WorkflowRepository,
    private val eventService: EventService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 2000)
    fun processPendingEvents() {
        val pending = eventRepo.findByStatusIn(
            listOf(EventStatus.PENDING, EventStatus.RETRYING)
        )

        for (event in pending) {
            try {
                val workflow = workflowRepo.findByIdWithSteps(event.workflowId) ?: continue
                log.info("Processing event {} for workflow '{}'", event.id, workflow.name)

                eventService.process(event, workflow.steps)

                log.info("Event {} completed with status {}", event.id, event.status)
            } catch (e: Exception) {
                log.error("Failed to process event {}: {}", event.id, e.message)
            }
        }
    }
}
