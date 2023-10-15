package com.alaska.socialis.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.alaska.socialis.services.JwtService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MyChannelInterceptor implements ChannelInterceptor {

    private JwtService jwtservice;

    public MyChannelInterceptor(JwtService service) {
        this.jwtservice = service;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        System.out.println(StompCommand.CONNECT);
        System.out.println(accessor.getCommand());

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // This is a connection request
            String jwtToken = extractJwtToken(accessor);

            // Validate the JWT token (e.g., using Spring Security's authentication
            // provider)
            // Your token validation logic here...
            boolean isTokenNotValid = validateJwtToken(jwtToken);

            if (isTokenNotValid) {
                // If the token is invalid, you can close the connection
                // or handle it according to your application's requirements
                return null;
            }
        }

        return message;
    }

    private String extractJwtToken(StompHeaderAccessor accessor) {
        // Extract the JWT token from the headers
        return accessor.getFirstNativeHeader("Authorization");
    }

    private boolean validateJwtToken(String jwtToken) {

        String jwt = jwtToken.split(" ")[1];

        return this.jwtservice.isTokenExpired(jwt);
    }
}
