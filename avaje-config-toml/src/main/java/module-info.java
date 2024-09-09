import io.avaje.config.toml.TomlParser;

module io.avaje.config.toml {

  requires io.avaje.config;
  requires org.tomlj;

  exports io.avaje.config.toml;

  provides io.avaje.config.ConfigExtension with TomlParser;

}
