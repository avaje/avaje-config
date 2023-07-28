module io.avaje.config {

  exports io.avaje.config;

  requires transitive io.avaje.lang;
  requires transitive io.avaje.applog;

  uses io.avaje.config.ConfigurationLog;
  uses io.avaje.config.ModificationEventRunner;
  uses io.avaje.config.ConfigurationSource;
  uses io.avaje.config.ResourceLoader;
  uses io.avaje.config.YamlLoaderProvider;

}
