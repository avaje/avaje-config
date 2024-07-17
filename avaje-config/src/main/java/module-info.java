module io.avaje.config {

  exports io.avaje.config;

  requires transitive org.jspecify;
  requires transitive io.avaje.applog;
  requires static org.yaml.snakeyaml;

  requires static io.avaje.spi;

  uses io.avaje.config.ConfigExtension;

}
