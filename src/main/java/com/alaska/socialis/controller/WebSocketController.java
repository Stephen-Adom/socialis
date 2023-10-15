package com.alaska.socialis.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.alaska.socialis.model.Message;

@Controller
public class WebSocketController {
    @SubscribeMapping("/chat")
    // @SendTo("/feed/messages")
    public Message sendMessage() {
        Message message = new Message("message from server");
        return message;
    }
}
