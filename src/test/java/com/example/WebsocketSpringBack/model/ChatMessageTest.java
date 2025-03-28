package com.example.WebsocketSpringBack.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

class ChatMessageTest {

    @Test
    void testCreateChatMessage() {
        // Arrange
        String name = "TestUser";
        String content = "Hello, World!";
        
        // Act
        ChatMessage message = ChatMessage.createChatMessage(name, content);
        
        // Assert
        assertEquals(name, message.getName());
        assertEquals(content, message.getMessage());
        assertEquals(ChatMessage.MessageType.CHAT, message.getType());
        assertNotNull(message.getTimestamp());
    }
    
    @Test
    void testCreateJoinMessage() {
        // Arrange
        String name = "TestUser";
        
        // Act
        ChatMessage message = ChatMessage.createJoinMessage(name);
        
        // Assert
        assertEquals(name, message.getName());
        assertEquals(name + " has joined the chat", message.getMessage());
        assertEquals(ChatMessage.MessageType.JOIN, message.getType());
    }
    
    @Test
    void testCreateLeaveMessage() {
        // Arrange
        String name = "TestUser";
        
        // Act
        ChatMessage message = ChatMessage.createLeaveMessage(name);
        
        // Assert
        assertEquals(name, message.getName());
        assertEquals(name + " has left the chat", message.getMessage());
        assertEquals(ChatMessage.MessageType.LEAVE, message.getType());
    }
    
    @Test
    void testCreateErrorMessage() {
        // Arrange
        String errorText = "Connection error";
        
        // Act
        ChatMessage message = ChatMessage.createErrorMessage(errorText);
        
        // Assert
        assertEquals("System", message.getName());
        assertEquals(errorText, message.getMessage());
        assertEquals(ChatMessage.MessageType.ERROR, message.getType());
    }
    
    @Test
    void testCreateUserCountMessage() {
        // Test with multiple users
        ChatMessage message1 = ChatMessage.createUserCountMessage(5);
        assertEquals("System", message1.getName());
        assertEquals("5 users online", message1.getMessage());
        assertEquals(ChatMessage.MessageType.USER_COUNT, message1.getType());
        
        // Test with single user
        ChatMessage message2 = ChatMessage.createUserCountMessage(1);
        assertEquals("1 user online", message2.getMessage());
    }
    
    @Test
    void testCreatePingPongMessages() {
        // Test ping
        ChatMessage ping = ChatMessage.createPingMessage();
        assertEquals("System", ping.getName());
        assertEquals("ping", ping.getMessage());
        assertEquals(ChatMessage.MessageType.PING, ping.getType());
        
        // Test pong
        ChatMessage pong = ChatMessage.createPongMessage();
        assertEquals("System", pong.getName());
        assertEquals("pong", pong.getMessage());
        assertEquals(ChatMessage.MessageType.PONG, pong.getType());
    }
    
    @Test
    void testCreateWelcomeMessage() {
        // Arrange
        String appName = "Test Chat App";
        
        // Act
        ChatMessage message = ChatMessage.createWelcomeMessage(appName);
        
        // Assert
        assertEquals("System", message.getName());
        assertEquals("Welcome to " + appName, message.getMessage());
        assertEquals(ChatMessage.MessageType.CHAT, message.getType());
    }

    @Test
    void testLombokBuilderDefault() {
        // Test that the default timestamp is set
        ChatMessage message = ChatMessage.builder()
                .name("Test")
                .message("Test Message")
                .type(ChatMessage.MessageType.CHAT)
                .build();
        
        assertNotNull(message.getTimestamp());
        
        // Verify timestamp format is ISO-8601 compatible by parsing it
        assertDoesNotThrow(() -> Instant.parse(message.getTimestamp()));
    }
    
    @Test
    void testLombokEqualsAndHashCode() {
        // Create two identical messages
        ChatMessage message1 = ChatMessage.builder()
                .name("Test")
                .message("Test Message")
                .timestamp("2025-03-28T10:15:30.123Z")
                .type(ChatMessage.MessageType.CHAT)
                .build();
        
        ChatMessage message2 = ChatMessage.builder()
                .name("Test")
                .message("Test Message")
                .timestamp("2025-03-28T10:15:30.123Z")
                .type(ChatMessage.MessageType.CHAT)
                .build();
        
        // Verify equals and hashCode
        assertEquals(message1, message2);
        assertEquals(message1.hashCode(), message2.hashCode());
        
        // Modify one message
        message2.setMessage("Modified Message");
        
        // Now they should be different
        assertNotEquals(message1, message2);
        assertNotEquals(message1.hashCode(), message2.hashCode());
    }
}
