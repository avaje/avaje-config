package io.avaje.config.appconfig;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;

import java.io.StringReader;
import java.lang.System.Logger.Level;
import java.net.ConnectException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import io.avaje.applog.AppLog;
import io.avaje.config.ConfigParser;
import io.avaje.config.Configuration;
import io.avaje.config.ConfigurationSource;
import io.avaje.spi.ServiceProvider;

/**
 * Plugin that loads AWS AppConfig as Yaml or Properties.
 * <p>
 * By default, will periodically reload the configuration if it has changed.
 */
@ServiceProvider
public final class AppConfigPlugin implements ConfigurationSource {

  private static final System.Logger log = AppLog.getLogger("io.avaje.config.AwsAppConfig");

  private Loader loader;

  @Override
  public void load(Configuration configuration) {
    if (!configuration.enabled("aws.appconfig.enabled", true)) {
      log.log(INFO, "AwsAppConfig plugin is disabled");
      return;
    }
    loader = new Loader(configuration);
    int attempts = loader.initialLoad();
    if (attempts > 1){
      log.log(INFO, "AwsAppConfig loaded after {0} attempts", attempts + 1);
    }
  }

  @Override
  public void reload() {
    if (loader != null) {
      loader.reload();
    }
  }

  static final class Loader {

    private final Configuration configuration;
    private final AppConfigFetcher fetcher;
    private final ConfigParser yamlParser;
    private final ConfigParser propertiesParser;
    private final AtomicInteger connectErrorCount = new AtomicInteger();
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicReference<Instant> validUntil;
    private final long nextRefreshSeconds;

    private String currentVersion = "none";

    Loader(Configuration configuration) {
      this.validUntil = new AtomicReference<>(Instant.now().minusSeconds(1));
      this.configuration = configuration;
      this.propertiesParser = configuration.parser("properties").orElseThrow();
      this.yamlParser = configuration.parser("yaml").orElse(null);
      if (yamlParser == null) {
        log.log(WARNING, "No Yaml parser registered");
      }

      var app = configuration.get("aws.appconfig.application");
      var env = configuration.get("aws.appconfig.environment");
      var con = configuration.get("aws.appconfig.configuration", "default");

      this.fetcher = AppConfigFetcher.builder()
        .application(app)
        .environment(env)
        .configuration(con)
        .build();

      log.log(DEBUG, "AwsAppConfig uri {0}", fetcher.uri());

      boolean pollEnabled = configuration.enabled("aws.appconfig.pollingEnabled", true);
      long pollSeconds = configuration.getLong("aws.appconfig.pollingSeconds", 45L);
      this.nextRefreshSeconds = configuration.getLong("aws.appconfig.refreshSeconds", pollSeconds - 1);
      if (pollEnabled) {
        configuration.schedule(pollSeconds * 1000L, pollSeconds * 1000L, this::reload);
      }
    }

    /**
     * Potential race conditional with AWS AppConfig sidecar so use simple retry loop.
     */
    int initialLoad() {
      lock.lock();
      try {
        Exception lastAttempt = null;
        for (int i = 1; i < 11; i++) {
          try {
            loadAndPublish();
            return i;
          } catch (Exception e) {
            // often seeing this with apps that start quickly (and AppConfig sidecar not up yet)
            lastAttempt = e;
            log.log(DEBUG, "retrying, load attempt {0} got {1}", i, e.getMessage());
            LockSupport.parkNanos(250_000_000); // 250 millis
          }
        }
        log.log(ERROR, "Failed initial AwsAppConfig load", lastAttempt);
        return -1;

      } finally{
        lock.unlock();
      }
    }

    void reload() {
      if (reloadRequired()) {
        performReload();
      }
    }

    private boolean reloadRequired() {
      return validUntil.get().isBefore(Instant.now());
    }

    private void performReload() {
      lock.lock();
      try {
        if (!reloadRequired()) {
          return;
        }
        loadAndPublish();

      } catch (ConnectException e) {
        // expected during shutdown when AppConfig sidecar shuts down before the app
        int errCount = connectErrorCount.incrementAndGet();
        Level level = errCount > 1 ? WARNING : INFO;
        log.log(level, "Failed to fetch from AwsAppConfig - likely shutdown in progress");
      } catch (Exception e) {
        log.log(ERROR, "Error fetching or processing AwsAppConfig", e);
      } finally {
        lock.unlock();
      }
    }

    /**
     * Load and publish the configuration from AWS AppConfig.
     */
    private void loadAndPublish() throws AppConfigFetcher.FetchException, ConnectException {
      AppConfigFetcher.Result result = fetcher.fetch();
      if (currentVersion.equals(result.version())) {
        log.log(TRACE, "AwsAppConfig unchanged, version {0}", currentVersion);
      } else {
        String contentType = result.contentType();
        if (log.isLoggable(TRACE)) {
          int contentLength = result.body().length();
          log.log(TRACE, "AwsAppConfig fetched version:{0} contentType:{1} contentLength:{2,number,#}", result.version(), contentType, contentLength);
        }
        Map<String, String> keyValues = parse(result);
        configuration.eventBuilder("AwsAppConfig")
          .putAll(keyValues)
          .publish();
        currentVersion = result.version();
        debugLog(result, keyValues.size());
      }
      // move the next valid until time
      validUntil.set(Instant.now().plusSeconds(nextRefreshSeconds));
    }

    private Map<String, String> parse(AppConfigFetcher.Result result) {
      ConfigParser parser = parser(result.contentType());
      return parser.load(new StringReader(result.body()));
    }

    private ConfigParser parser(String contentType) {
      if (contentType.endsWith("yaml")) {
        return yamlParser;
      } else {
        return propertiesParser;
      }
    }

    private static void debugLog(AppConfigFetcher.Result result, int size) {
      if (log.isLoggable(DEBUG)) {
        log.log(DEBUG, "AwsAppConfig loaded version {0} with {1} properties", result.version(), size);
      }
    }
  }
}
