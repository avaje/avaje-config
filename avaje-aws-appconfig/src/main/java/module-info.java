import io.avaje.aws.appconfig.AppConfigPlugin;

module io.avaje.aws.appconfig {

  exports io.avaje.aws.appconfig;

  requires io.avaje.config;
  requires java.net.http;
  provides io.avaje.config.ConfigurationSource with AppConfigPlugin;
}
