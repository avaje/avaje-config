package io.avaje.config.awsappconfig;

import io.avaje.config.ConfigParser;
import io.avaje.config.Configuration;
import io.avaje.config.ConfigurationSource;

import java.io.StringReader;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.*;

/**
 * Plugin that loads AWS AppConfig as Yaml or Properties.
 * <p>
 * By default, will periodically reload the configuration if it has changed.
 */
public final class AwsAppConfigPlugin implements ConfigurationSource {

  private static final System.Logger log = System.getLogger("io.avaje.config.AwsAppConfig");

  private Loader loader;

  @Override
  public void load(Configuration configuration) {
    if (!configuration.enabled("aws.appconfig.enabled", true)) {
      log.log(INFO, "AwsAppConfig plugin is disabled");
      return;
    }
    loader = new Loader(configuration);
    loader.reload();
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
      var con = configuration.get("aws.appconfig.configuration", env + "-" + app);

      this.fetcher = AppConfigFetcher.builder()
        .application(app)
        .environment(env)
        .configuration(con)
        .build();

      boolean pollEnabled = configuration.enabled("aws.appconfig.poll.enabled", true);
      long pollSeconds = configuration.getLong("aws.appconfig.poll.seconds", 45L);
      this.nextRefreshSeconds = configuration.getLong("aws.appconfig.refresh.seconds", pollSeconds - 1);
      if (pollEnabled) {
        configuration.schedule(pollSeconds * 1000L, pollSeconds * 1000L, this::reload);
      }
    }

    void reload() {
      if (reloadRequired()) {
        performReload();
      }
    }

    private boolean reloadRequired() {
      return validUntil.get().isAfter(Instant.now());
    }

    private void performReload() {
      lock.lock();
      try {
        if (!reloadRequired()) {
          return;
        }
        AppConfigFetcher.Result result = fetcher.fetch();
        if (currentVersion.equals(result.version())) {
          log.log(TRACE, "AwsAppConfig unchanged, version {0}", currentVersion);
        } else {
          String contentType = result.contentType();
          if (log.isLoggable(TRACE)) {
            log.log(TRACE, "AwsAppConfig fetched version:{0} contentType:{1} body:{2}", result.version(), contentType, result.body());
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

      } catch (Exception e) {
        log.log(ERROR, "Error fetching or processing AwsAppConfig", e);
      } finally {
        lock.unlock();
      }
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
