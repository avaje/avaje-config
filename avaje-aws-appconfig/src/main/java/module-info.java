import io.avaje.config.awsappconfig.AwsAppConfigPlugin;

module io.avaje.config.awsappconfig {

  exports io.avaje.config.awsappconfig;

  requires io.avaje.config;
  requires java.net.http;
  provides io.avaje.config.ConfigurationSource with AwsAppConfigPlugin;
}
