import io.avaje.config.appconfig.AppConfigPlugin;

module io.avaje.config.appconfig {

  exports io.avaje.config.appconfig;

  requires io.avaje.config;
  requires java.net.http;
  requires transitive io.avaje.applog;
  provides io.avaje.config.ConfigurationSource with AppConfigPlugin;
}
