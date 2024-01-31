package io.avaje.aws.appconfig;

import io.avaje.config.ConfigParser;
import io.avaje.config.Configuration;
import io.avaje.config.ConfigurationSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import static java.lang.System.Logger.Level.*;

public final class AppConfigPlugin implements ConfigurationSource {

  private static final System.Logger log = System.getLogger("io.avaje.config.AppConfigPlugin");

  @Override
  public void load(Configuration configuration) {
    if (!configuration.getBool("aws.appconfig.enabled", true)) {
      log.log(INFO, "AppConfigPlugin is not enabled");
    }

    var loader = new Loader(configuration);
    loader.schedule();
    loader.reload();
  }


  static final class Loader {

    private final Configuration configuration;
    private final AppConfigFetcher fetcher;
    private final ConfigParser yamlParser;
    private final long frequency;

    private String currentVersion = "";

    Loader(Configuration configuration) {
      this.configuration = configuration;
      this.frequency = configuration.getLong("aws.appconfig.frequency", 60L);
      this.yamlParser = configuration.parser("yaml").orElse(null);

      var app = configuration.get("aws.appconfig.application");
      var env = configuration.get("aws.appconfig.environment");
      var con = configuration.get("aws.appconfig.configuration");

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
          log.log(TRACE, "AwsAppConfig unchanged, version", currentVersion);
        } else {
          String contentType = result.contentType();
          if (log.isLoggable(TRACE)) {
            log.log(TRACE, "AwsAppConfig fetched version:{0} contentType:{1} body:{2}", result.version(), contentType, result.body());
          }
          if (contentType.endsWith("yaml")) {
            if (yamlParser == null) {
              log.log(ERROR, "No Yaml Parser registered to parse AWS AppConfig");
            } else {
              Map<String, String> keyValues = yamlParser.load(new StringReader(result.body()));
              configuration.eventBuilder("AwsAppConfig")
                  .putAll(keyValues)
                  .publish();
              currentVersion = result.version();
              debugLog(result, keyValues.size());
            }
          } else {
            // assuming properties content
            Properties properties = new Properties();
            properties.load(new StringReader(result.body()));
            configuration.eventBuilder("AwsAppConfig")
              .putAll(properties)
              .publish();
            currentVersion = result.version();
            debugLog(result, properties.size());
          }
        }

      } catch (AppConfigFetcher.FetchException | IOException e) {
        log.log(ERROR, "Error fetching or processing AppConfig", e);
      }
    }

    private static void debugLog(AppConfigFetcher.Result result, int size) {
      if (log.isLoggable(DEBUG)) {
        log.log(DEBUG, "AwsAppConfig loaded version {0} with {1} properties", result.version(), size);
      }
    }
  }
}
