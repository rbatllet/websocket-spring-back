package com.example.WebsocketSpringBack.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a chat message in the WebSocket communication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String name;
    private String message;
    
    @lombok.Builder.Default
    private String timestamp = Instant.now().toString();
    
    private MessageType type;

    /**
     * The type of message
     */
    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        ERROR,
        USER_COUNT,
        PING,
        PONG
    }

    // Static factory methods for creating different message types

    /**
     * Create a standard chat message
     *
     * @param name    Sender name
     * @param message Message content
     * @return A new ChatMessage instance
     */
    public static ChatMessage createChatMessage(String name, String message) {
        return ChatMessage.builder()
                .name(name)
                .message(message)
                .type(MessageType.CHAT)
                .build();
    }

    /**
     * Create a join notification message
     *
     * @param name User name who joined
     * @return A new ChatMessage instance
     */
    public static ChatMessage createJoinMessage(String name) {
        return ChatMessage.builder()
                .name(name)
                .message(name + " has joined the chat")
                .type(MessageType.JOIN)
                .build();
    }

    /**
     * Create a leave notification message
     *
     * @param name User name who left
     * @return A new ChatMessage instance
     */
    public static ChatMessage createLeaveMessage(String name) {
        return ChatMessage.builder()
                .name(name)
                .message(name + " has left the chat")
                .type(MessageType.LEAVE)
                .build();
    }

    /**
     * Create an error message
     *
     * @param errorMessage Error description
     * @return A new ChatMessage instance
     */
    public static ChatMessage createErrorMessage(String errorMessage) {
        return ChatMessage.builder()
                .name("System")
                .message(errorMessage)
                .type(MessageType.ERROR)
                .build();
    }
    
    /**
     * Create a user count message
     * 
     * @param count Number of active users
     * @return A new ChatMessage instance
     */
    public static ChatMessage createUserCountMessage(int count) {
        String message = count + " user" + (count != 1 ? "s" : "") + " online";
        return ChatMessage.builder()
                .name("System")
                .message(message)
                .type(MessageType.USER_COUNT)
                .build();
    }
    
    /**
     * Create a ping message for connection heartbeat
     * 
     * @return A new ChatMessage instance for ping
     */
    public static ChatMessage createPingMessage() {
        return ChatMessage.builder()
                .name("System")
                .message("ping")
                .type(MessageType.PING)
                .build();
    }
    
    /**
     * Create a pong message response to ping
     * 
     * @return A new ChatMessage instance for pong
     */
    public static ChatMessage createPongMessage() {
        return ChatMessage.builder()
                .name("System")
                .message("pong")
                .type(MessageType.PONG)
                .build();
    }

    /**
     * Create a welcome message for new connections
     * 
     * @param appName The application name
     * @return A new ChatMessage instance for welcome message
     */
    public static ChatMessage createWelcomeMessage(String appName) {
        return ChatMessage.builder()
                .name("System")
                .message("Welcome to " + appName)
                .type(MessageType.CHAT)
                .build();
    }
}
