module io.avaje.config {

  exports io.avaje.config;

  requires transitive io.avaje.applog;

  requires static io.avaje.spi;
  requires static org.yaml.snakeyaml;
  requires static transitive org.jspecify;

  uses io.avaje.config.ConfigExtension;

}
