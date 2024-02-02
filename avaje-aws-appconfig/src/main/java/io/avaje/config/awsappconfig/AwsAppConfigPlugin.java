package io.avaje.config.awsappconfig;

import io.avaje.config.ConfigParser;
import io.avaje.config.Configuration;
import io.avaje.config.ConfigurationSource;

import java.io.StringReader;
import java.util.Map;

import static java.lang.System.Logger.Level.*;

/**
 * Plugin that loads AWS AppConfig as Yaml or Properties.
 * <p>
 * By default, will periodically reload the configuration if it has changed.
 */
public final class AwsAppConfigPlugin implements ConfigurationSource {

  private static final System.Logger log = System.getLogger("io.avaje.config.AwsAppConfig");

  @Override
  public void load(Configuration configuration) {
    if (!configuration.enabled("aws.appconfig.enabled", true)) {
      log.log(INFO, "AwsAppConfig plugin is disabled");
      return;
    }
    var loader = new Loader(configuration);
    loader.schedule();
    loader.reload();
  }

  static final class Loader {

    private final Configuration configuration;
    private final AppConfigFetcher fetcher;
    private final ConfigParser yamlParser;
    private final ConfigParser propertiesParser;
    private final long frequency;

    private String currentVersion = "none";

    Loader(Configuration configuration) {
      this.configuration = configuration;
      this.frequency = configuration.getLong("aws.appconfig.frequency", 60L);
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
    }

    void schedule() {
      configuration.schedule(frequency * 1000L, frequency * 1000L, this::reload);
    }

    void reload() {
      try {
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

      } catch (Exception e) {
        log.log(ERROR, "Error fetching or processing AwsAppConfig", e);
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
