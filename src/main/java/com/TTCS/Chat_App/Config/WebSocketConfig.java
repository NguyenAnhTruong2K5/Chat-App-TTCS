package com.TTCS.Chat_App.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); 
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Increase the maximum message size to 2MB (2 * 1024 * 1024)
        registration.setMessageSizeLimit(2097152);

        // Increase the buffer size to 2MB
        registration.setSendBufferSizeLimit(2097152);

        // Slightly increase the timeout limit to allow time for images to upload (20 seconds)
        registration.setSendTimeLimit(20000);
    }
}
