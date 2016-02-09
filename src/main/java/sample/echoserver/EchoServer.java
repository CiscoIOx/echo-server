package sample.echoserver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.concurrent.Semaphore;
import java.util.logging.LogManager;

import org.apache.commons.io.FileUtils;
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
		System.out.println(EchoServer.class.getClassLoader().getResource("logging.properties"));

		pm = PlatformLocator.factory.createPlatformManager();
		startRestServer();
		addShutdownHook();
		semaphore.acquire();
	}

	private static void setLogDirectory() {
		// Read log directory location
		String logDir = System.getenv("CAF_APP_LOG_DIR");
		log.info("CAF_APP_LOG_DIR -> {} ", logDir);

		String logConfigFileLoc = System.getProperty("java.util.logging.config.file");
		log.info("java.util.logging.config.file -> {} ", logConfigFileLoc);
		if (logConfigFileLoc != null) {
			File logPropertiesFile = new File(logConfigFileLoc);

			// Read logging.properties file
			String loggingConfiguration;
			try {
				loggingConfiguration = FileUtils.readFileToString(logPropertiesFile);
				// replace variable ${logsDir} with actual log directory location
				loggingConfiguration = loggingConfiguration.replace("${logsDir}", logDir.replace('\\', '/'));

				// Read Properties file again
				LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(loggingConfiguration.getBytes()));
			} catch (Exception e) {
				log.error("Error while setting log directory", e);
			}
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
