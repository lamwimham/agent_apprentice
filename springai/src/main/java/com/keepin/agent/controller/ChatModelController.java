package com.keepin.agent.controller;


import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/model")
public class ChatModelController {

    @Autowired
    private DashScopeChatModel dashScopeChatModel;

    @GetMapping(value="/call/string")
    public String stream(String message) {

        return dashScopeChatModel.call( message);

    }

    @GetMapping(value="/call/messages")
    public String messageCall(String message) {
        SystemMessage sysMsg = new SystemMessage("You are a helpful assistant. you will translate the input of user to English.");
        UserMessage msg = new UserMessage(message);
        return dashScopeChatModel.call(sysMsg, msg);

    }

    @GetMapping(value="/call/stream")
    public Flux<String> streamCall(String message, HttpServletResponse  response) {
        response.setCharacterEncoding("utf-8");

        SystemMessage sysMsg = new SystemMessage("请详细的回答我的问题");
        UserMessage msg = new UserMessage(message);
        return dashScopeChatModel.stream(sysMsg, msg);
    }

    @GetMapping(value="/call/prompt")
    public String promptCall(String message) {
        SystemMessage sysMsg = new SystemMessage("You are a helpful assistant. you will translate the input of user to English.");
        UserMessage msg = new UserMessage(message);
        ChatOptions options = ChatOptions.builder().model("deepseek-r1").build();
        Prompt prompt = Prompt.builder().messages(sysMsg, msg).chatOptions(options).build();
        return dashScopeChatModel.call( prompt).getResult().getOutput().getText();
    }
}
