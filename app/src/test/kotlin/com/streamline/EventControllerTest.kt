package com.streamline

import com.fasterxml.jackson.databind.ObjectMapper
import com.streamline.controller.CreateWorkflowRequest
import com.streamline.controller.SubmitEventRequest
import com.streamline.controller.UpdateStatusRequest
import com.streamline.model.StepType
import com.streamline.model.WorkflowStatus
import com.streamline.service.StepRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper

    private fun createActiveWorkflow(): UUID {
        val request = CreateWorkflowRequest(
            name = "Event Test Pipeline",
            steps = listOf(StepRequest("Validate", StepType.VALIDATE))
        )

        val result = mockMvc.perform(
            post("/api/workflows")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        val id = objectMapper.readTree(result.response.contentAsString)["id"].asText()
        val workflowId = UUID.fromString(id)

        // Activate the workflow
        mockMvc.perform(
            put("/api/workflows/$workflowId/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateStatusRequest(WorkflowStatus.ACTIVE)))
        )

        return workflowId
    }

    @Test
    fun `submit event to active workflow returns 202`() {
        val workflowId = createActiveWorkflow()
        val request = SubmitEventRequest(workflowId, """{"message":"hello"}""")

        mockMvc.perform(
            post("/api/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.workflowId").value(workflowId.toString()))
    }

    @Test
    fun `submit event to nonexistent workflow returns 400`() {
        val request = SubmitEventRequest(UUID.randomUUID(), """{"data":"test"}""")

        mockMvc.perform(
            post("/api/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `get event stats returns 200`() {
        mockMvc.perform(get("/api/events/stats"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total").exists())
    }

    @Test
    fun `get nonexistent event returns 404`() {
        mockMvc.perform(get("/api/events/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `list events by workflow returns 200`() {
        val workflowId = createActiveWorkflow()
        mockMvc.perform(get("/api/events/workflow/$workflowId"))
            .andExpect(status().isOk)
    }
}
