package com.streamline

import com.fasterxml.jackson.databind.ObjectMapper
import com.streamline.controller.CreateWorkflowRequest
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

@SpringBootTest
@AutoConfigureMockMvc
class WorkflowControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper

    @Test
    fun `create workflow returns 201`() {
        val request = CreateWorkflowRequest(
            name = "Test Pipeline",
            description = "A test workflow",
            steps = listOf(
                StepRequest("Validate", StepType.VALIDATE),
                StepRequest("Transform", StepType.TRANSFORM),
                StepRequest("Enrich", StepType.ENRICH)
            )
        )

        mockMvc.perform(
            post("/api/workflows")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Test Pipeline"))
            .andExpect(jsonPath("$.stepCount").value(3))
            .andExpect(jsonPath("$.status").value("DRAFT"))
    }

    @Test
    fun `list workflows returns 200`() {
        mockMvc.perform(get("/api/workflows"))
            .andExpect(status().isOk)
    }

    @Test
    fun `get nonexistent workflow returns 404`() {
        mockMvc.perform(get("/api/workflows/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `create workflow with blank name returns 400`() {
        val request = mapOf("name" to "", "steps" to emptyList<Any>())

        mockMvc.perform(
            post("/api/workflows")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `delete nonexistent workflow returns 404`() {
        mockMvc.perform(
            delete("/api/workflows/00000000-0000-0000-0000-000000000000")
                .with(csrf())
        )
            .andExpect(status().isNotFound)
    }
}
