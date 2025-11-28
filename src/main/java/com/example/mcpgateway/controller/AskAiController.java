package com.example.mcpgateway.controller;

import com.example.mcpgateway.dto.AskAiRequest;
import com.example.mcpgateway.dto.AskAiResponse;
import com.example.mcpgateway.service.LLMService;
import com.example.mcpgateway.service.MCPServerStarter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ask-ai")
@CrossOrigin(origins = "*") // Allow CORS for web interface
public class AskAiController {

    private static final Logger log = LoggerFactory.getLogger(AskAiController.class);

    private final LLMService llmService;
    private final MCPServerStarter mcpServerStarter;

    public AskAiController(LLMService llmService, MCPServerStarter mcpServerStarter) {
        this.llmService = llmService;
        this.mcpServerStarter = mcpServerStarter;
    }

    @PostMapping
    public ResponseEntity<AskAiResponse> ask(@Valid @RequestBody AskAiRequest request) {
        System.out.println("===== CONTROLLER CALLED =====");
        System.out.println("===== Question: " + request.getQuestion());

        try {
            log.error("===== Received question: {}", request.getQuestion());

            if (llmService == null) {
                System.out.println("===== LLM SERVICE IS NULL =====");
                return ResponseEntity.ok(new AskAiResponse("ERROR: LLM Service is null"));
            }

            System.out.println("===== Calling LLM service =====");
            String answer = llmService.ask(request.getQuestion());

            System.out.println("===== Answer received: " + answer);
            System.out.println("===== Answer is null? " + (answer == null));
            System.out.println("===== Answer length: " + (answer != null ? answer.length() : -1));

            log.error("===== Answer from service: '{}'", answer);

            if (answer == null) {
                answer = "ERROR: Service returned null";
            }

            if (answer.isEmpty()) {
                answer = "ERROR: Service returned empty string";
            }

            AskAiResponse response = new AskAiResponse(answer);
            System.out.println("===== Response created with answer: " + response.getAnswer());

            log.error("===== Returning response =====");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("===== EXCEPTION in controller: " + e.getMessage());
            e.printStackTrace();
            log.error("===== ERROR in controller", e);
            return ResponseEntity
                    .ok(new AskAiResponse("EXCEPTION: " + e.getMessage() + " | " + e.getClass().getName()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "running",
                "mcpServer", mcpServerStarter.getServerStatus()));
    }
}
