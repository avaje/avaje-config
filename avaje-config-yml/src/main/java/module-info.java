import io.avaje.config.yml.DefaultYaml;

module io.avaje.config.yml {

  requires io.avaje.config;
  requires static org.yaml.snakeyaml;

  provides io.avaje.config.YamlLoaderProvider with DefaultYaml;
}
