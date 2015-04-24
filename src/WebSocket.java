import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/websocket")
public class WebSocket {
	private static final AtomicInteger connectionIds = new AtomicInteger(0);
	private static final Set<WebSocket> connections = new CopyOnWriteArraySet<WebSocket>();

	public Session session;

	public WebSocket() {
		connectionIds.getAndIncrement();
	}

	@OnOpen	
	public void start(Session session) {
		this.session = session;
		connections.add(this);
		System.out.println(session.getId());
	}

	@OnClose
	public void end() {
		connections.remove(this);

	}

	@OnMessage
	public void receive(String message) {
		// Never trust the client

	}

	public static void broadcast(String msg) {
		for (WebSocket client : connections) {
			try {
				synchronized (client) {
					client.session.getBasicRemote().sendText(msg);
				}
			} catch (IOException e) {
				connections.remove(client);
				try {
					client.session.close();
				} catch (IOException e1) {
				}
			}
		}
	}
}
