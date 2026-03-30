package com.keepin.agent.controller;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;

@RestController
@RequestMapping(value ="/stream")
public class StreamController {
    // ⚠️ 测试用：请替换为你的有效 API Key
    private static final String API_KEY = "sk-438000d276d3465c8601f0f598d2b58f";
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    @GetMapping(value = "/fakeStream")
    public String output() {
        String requestBody = """
                {
                    "model": "qwen-plus",
                    "messages": [
                        {
                            "role": "system",
                            "content": "You are a helpful assistant."
                        },
                        {
                            "role": "user",
                            "content": "你好，介绍下 JAVA？"
                        }
                    ],
                    "stream": true
                }
                """;


        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .header("X-DashScope-SSE", "enable")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return response.body();
    }

    @GetMapping("/sse")
    public SseEmitter sse() {
        SseEmitter emitter =  new SseEmitter(60000L);
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    emitter.send("Message" + i );
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                emitter.complete();
            }
        });
        return emitter;
    }

    @GetMapping(value="/entity")
    public ResponseEntity<StreamingResponseBody> chat() {
        StreamingResponseBody body = output -> {
            for (int i = 0; i < 200; i++) {
                String data = "Message" + i;
                output.write(data.getBytes());
                output.flush();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
         };

        // new
        StreamingResponseBody body1 = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                for (int i = 0; i < 20; i++) {
                    outputStream.write(("Message" + i).getBytes());
                    outputStream.flush();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE).body( body1);
    }

    @GetMapping(value="/flux")
    public Flux<String> fluxStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(seq -> "Message" + seq + "\n");
    }


}
