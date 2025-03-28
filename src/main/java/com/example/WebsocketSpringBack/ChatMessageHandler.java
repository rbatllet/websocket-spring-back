package com.example.WebsocketSpringBack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.WebsocketSpringBack.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ChatMessageHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${app.name}")
    private String appName;
    
    // Store sessions with usernames
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUsernames = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("New WebSocket connection established! Session ID: {}", session.getId());
        logger.info("Remote address: {}", session.getRemoteAddress());
        logger.info("Session attributes: {}", session.getAttributes());
        
        // Add session to the map with a default username (can be updated later)
        sessions.put(session.getId(), session);
        sessionUsernames.put(session.getId(), "User-" + session.getId().substring(0, 5));
        
        // Send welcome message to the new client
        ChatMessage welcomeMessage = ChatMessage.createWelcomeMessage(appName);
        sendMessageToSession(session, welcomeMessage);
        
        // Notify all users about the count update
        broadcastUserCount();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.debug("Connection closed. Session ID: {}. Status: {}", session.getId(), status.getReason());
        
        // Get the username before removing session
        String username = sessionUsernames.get(session.getId());
        
        // Remove session from maps
        sessions.remove(session.getId());
        sessionUsernames.remove(session.getId());
        
        // Notify other users that someone left
        if (username != null) {
            ChatMessage leaveMessage = ChatMessage.createLeaveMessage(username);
            broadcastMessage(leaveMessage);
        }
        
        // Update user count
        broadcastUserCount();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        String payload = textMessage.getPayload();
        logger.debug("Message received from session {}: {}", session.getId(), payload);
        
        try {
            ChatMessage message = objectMapper.readValue(payload, ChatMessage.class);
            
            // Check message type to handle different scenarios
            switch (message.getType()) {
                case PING:
                    // Respond to ping with pong
                    sendMessageToSession(session, ChatMessage.createPongMessage());
                    break;
                
                case JOIN:
                    // Update the username if provided
                    if (message.getName() != null && !message.getName().trim().isEmpty()) {
                        String username = sanitizeUsername(message.getName());
                        String oldUsername = sessionUsernames.get(session.getId());
                        sessionUsernames.put(session.getId(), username);
                        
                        // Notify all users that someone updated their name
                        ChatMessage joinMessage = ChatMessage.createJoinMessage(username);
                        broadcastMessage(joinMessage);
                    }
                    break;
                    
                case CHAT:
                    // Get the username for this session
                    String username = sessionUsernames.get(session.getId());
                    
                    // Create a properly formatted chat message with the assigned username
                    ChatMessage chatMessage = ChatMessage.createChatMessage(
                        username, message.getMessage());
                    
                    // Broadcast to all clients
                    broadcastMessage(chatMessage);
                    break;
                    
                default:
                    // For other message types, just log and ignore
                    logger.debug("Received message of type {}: {}", message.getType(), message.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage());
            
            // Try to parse as simple text if JSON parsing fails
            String username = sessionUsernames.get(session.getId());
            ChatMessage chatMessage = ChatMessage.createChatMessage(username, payload);
            broadcastMessage(chatMessage);
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error in session {}: {}", session.getId(), exception.getMessage());
        logger.error("Error details:", exception);
        
        // If there's a serious transport error, close the session
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }
    
    /**
     * Send a message to a specific session
     * 
     * @param session The target session
     * @param message The message to send
     */
    private void sendMessageToSession(WebSocketSession session, ChatMessage message) {
        try {
            if (session.isOpen()) {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            logger.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
        }
    }
    
    /**
     * Broadcast a message to all connected sessions
     * 
     * @param message The message to broadcast
     */
    private void broadcastMessage(ChatMessage message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
            
            for (WebSocketSession session : sessions.values()) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception e) {
            logger.error("Error broadcasting message: {}", e.getMessage());
        }
    }
    
    /**
     * Broadcast current user count to all sessions
     */
    private void broadcastUserCount() {
        int userCount = sessions.size();
        ChatMessage countMessage = ChatMessage.createUserCountMessage(userCount);
        broadcastMessage(countMessage);
    }
    
    /**
     * Sanitize username to prevent security issues
     * 
     * @param username The raw username
     * @return Sanitized username
     */
    private String sanitizeUsername(String username) {
        // Remove any HTML tags and trim
        String sanitized = username.replaceAll("<[^>]*>", "").trim();
        
        // Limit length
        if (sanitized.length() > 30) {
            sanitized = sanitized.substring(0, 30);
        }
        
        // If empty after sanitizing, generate a default one
        if (sanitized.isEmpty()) {
            sanitized = "User-" + System.currentTimeMillis() % 10000;
        }
        
        return sanitized;
    }
}
