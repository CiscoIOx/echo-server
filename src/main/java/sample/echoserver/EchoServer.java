package sample.echoserver;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

public class EchoServer {

	private static final Logger log = LoggerFactory.getLogger(EchoServer.class);

	private static PlatformManager pm;

	static Semaphore semaphore = new Semaphore(0);

	private static HttpServer httpServer;

	public static void main(String[] args) throws Exception {
		System.out.println(EchoServer.class.getClassLoader().getResource("logging.properties"));

		pm = PlatformLocator.factory.createPlatformManager();
		startRestServer();
		addShutdownHook();
		semaphore.acquire();
	}

	private static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					if (httpServer != null) {
						httpServer.close();
					}
					pm.stop();
				} catch (Exception e) {
					log.error("Error while stopping http server", e);
				} finally {
					semaphore.release();
				}
			}
		});
	}

	private static void startRestServer() {
		httpServer = pm.vertx().createHttpServer();

		httpServer.requestHandler(new Handler<HttpServerRequest>() {
		    public void handle(final HttpServerRequest request) {
		    	request.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						log.info("A new request has arrived on the echo server!");
						request.response().end(buffer);
					}
				});
		    }
		});

		httpServer.listen(8080, "0.0.0.0");
	}
}
