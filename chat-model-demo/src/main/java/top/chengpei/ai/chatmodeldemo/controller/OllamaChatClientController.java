package top.chengpei.ai.chatmodeldemo.controller;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OllamaChatClientController {

    @Resource
    private OllamaChatModel ollamaChatModel;

    private ChatClient chatClient;

    @PostConstruct
    public void init() {
        chatClient = ChatClient.builder(ollamaChatModel).defaultSystem("You are a helpful assistant.")
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(ChatOptions.builder().temperature(0.7d).build()).build();
    }

    @GetMapping("/ai/ollama/chatClient")
    public ChatResponse chat2(@RequestParam(value = "message", defaultValue = "给我讲个笑话") String message) {
        return chatClient.prompt(message).call().chatResponse();
    }
}
