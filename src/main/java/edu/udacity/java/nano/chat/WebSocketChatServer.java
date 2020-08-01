package edu.udacity.java.nano.chat;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket Server
 *
 * @see ServerEndpoint WebSocket Client
 * @see Session   WebSocket Session
 */

@Component
@ServerEndpoint(value="/chat/{username}",decoders = MessageDecoder.class, encoders = MessageEncoder.class )
public class WebSocketChatServer {

    /**
     * All chat sessions.
     */
    private Session session;
    private static Set<WebSocketChatServer> chatEndpoints  = new CopyOnWriteArraySet<>();
    private static HashMap<String, String> users = new HashMap<>();

    private static void sendMessageToAll(Message message) throws IOException{
        chatEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    endpoint.session.getBasicRemote().
                            sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Open connection, 1) add session, 2) add user.
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException {
        this.session = session;
        chatEndpoints.add(this);
        users.put(session.getId(), username);

        Message message = new Message();
        message.setUsername(username);
        message.setType("ENTER");
        message.setOnlineCount(users.size());
        sendMessageToAll(message);
    }

    /**
     * Send message, 1) get username and session, 2) send message to all.
     */
    @OnMessage
    public void onMessage(Session session, Message message) throws IOException {
        message.setUsername(users.get(session.getId()));
        message.setType("SPEAK");
        message.setOnlineCount(users.size());
        sendMessageToAll(message);
    }

    /**
     * Close connection, 1) remove session, 2) update user.
     */
    @OnClose
    public void onClose(Session session) throws IOException {
        chatEndpoints.remove(this);
        Message message = new Message();
        message.setUsername(users.get(session.getId()));
        message.setType("LEAVE");
        users.remove(session.getId());
        message.setOnlineCount(users.size());
        sendMessageToAll(message);
    }

    /**
     * Print exception.
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

}
