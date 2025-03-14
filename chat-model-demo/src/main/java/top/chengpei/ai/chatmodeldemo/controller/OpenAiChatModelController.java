package top.chengpei.ai.chatmodeldemo.controller;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@RestController
public class OpenAiChatModelController {

    @Resource
    private OpenAiChatModel openAiChatModel;

    private final List<Message> chatHistoryList = new ArrayList<>();

    @PostConstruct
    public void init() {
        chatHistoryList.add(new SystemMessage("You are a helpful assistant."));
    }

    @GetMapping("/ai/openai/chat")
    public ChatResponse test(@RequestParam(value = "message", defaultValue = "给我讲个笑话") String message) {
        chatHistoryList.add(new UserMessage(message));
        Prompt prompt = new Prompt(chatHistoryList);
        ChatResponse chatResponse = openAiChatModel.call(prompt);
        if (chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
            chatHistoryList.add(chatResponse.getResult().getOutput());
        }
        return chatResponse;
    }

    @GetMapping(value = "/ai/openai/chatStream")
    public Flux<String> test2(@RequestParam(value = "message", defaultValue = "给我讲个笑话") String message) {
        chatHistoryList.add(new UserMessage(message));
        Prompt prompt = new Prompt(chatHistoryList);
        Flux<ChatResponse> stream = openAiChatModel.stream(prompt);
        StringBuilder responseMessage = new StringBuilder();
        return stream.map(chatResponse -> {
            String content = chatResponse.getResult().getOutput().getContent();
            responseMessage.append(content);
            return content;
        }).doOnComplete(() -> chatHistoryList.add(new AssistantMessage(responseMessage.toString())));
    }

    @GetMapping("/ai/openai/chatClear")
    public String chatClear() {
        chatHistoryList.clear();
        chatHistoryList.add(new SystemMessage("You are a helpful assistant."));
        return "操作成功";
    }
}
