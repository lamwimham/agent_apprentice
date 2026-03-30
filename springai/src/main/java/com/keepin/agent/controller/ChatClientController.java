package com.keepin.agent.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClient;
import org.springframework.ai.chat.client.DefaultChatClientBuilder;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping(value = "/client")
public class ChatClientController  implements InitializingBean {

    @Autowired
    private ChatModel chatModel;
    private ChatClient chatClient;

    @GetMapping(value="/call/messages")
    public Flux<String> clientCall(String message, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");

        SystemMessage sysMsg = new SystemMessage("请详细的回答我的问题");
        UserMessage msg = new UserMessage(message);
        return chatClient
                .prompt()
                .system("你是个AI助手")
                .messages(sysMsg, msg)
                .stream().content();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化 ChatClient
        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }
}
