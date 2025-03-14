package top.chengpei.ai.embeddingdemo.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EmbeddingController {

    @Resource
    private EmbeddingModel embeddingModel;

    @GetMapping(value = "/embedding", produces = "application/json")
    public EmbeddingResponse embed(@RequestParam(value = "message", defaultValue = "给我讲个笑话") String message) {
        return this.embeddingModel.embedForResponse(List.of(message));
    }

    @GetMapping(value = "/embedding2", produces = "application/json")
    public EmbeddingResponse embed2(@RequestParam(value = "message", defaultValue = "给我讲个笑话") String message) {
        return this.embeddingModel.call(new EmbeddingRequest(List.of(message), OpenAiEmbeddingOptions.builder().model("nomic-embed-text:latest").build()));
    }
}
