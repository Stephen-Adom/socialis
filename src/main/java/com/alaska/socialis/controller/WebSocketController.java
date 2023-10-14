package com.alaska.socialis.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.alaska.socialis.model.Message;

@Controller
public class WebSocketController {
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Message sendMessage(Message message) {
        System.out.println("-------------------------------from client-------------------------------");
        System.out.println(message);
        return message;
    }
}
