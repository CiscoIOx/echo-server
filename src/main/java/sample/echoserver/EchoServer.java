package sample.echoserver;

import java.io.File;
import java.util.concurrent.Semaphore;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;

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
		setLogDirectory();
		pm = PlatformLocator.factory.createPlatformManager();
		startRestServer();
		addShutdownHook();
		semaphore.acquire();
	}

	private static void setLogDirectory() {
		// Read log directory location
		
		String currentDirectory = System.getProperty("user.dir");
		String logDir = System.getenv("CAF_APP_LOG_DIR");
		
		if(logDir == null) {
			logDir = currentDirectory;
		}
		
		log.info("Log Directory Location -> {} ", logDir);

		String fileLimit = LogManager.getLogManager().getProperty("java.util.logging.FileHandler.limit");
		String count = LogManager.getLogManager().getProperty("java.util.logging.FileHandler.count");
		
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger("");
		String fileName = logDir + File.separatorChar + "application-log.%u.%g.txt";
		
		FileHandler fileHandler;
		try {
			fileHandler = new FileHandler(fileName, Integer.parseInt(fileLimit), Integer.parseInt(count), true);
			logger.addHandler(fileHandler);
		} catch (Exception e) {
			log.error("Error while setting log directory", e);
		}
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
