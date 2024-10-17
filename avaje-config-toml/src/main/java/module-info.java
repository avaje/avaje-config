import io.avaje.config.ConfigExtension;
import io.avaje.config.toml.TomlParser;

module io.avaje.config.toml {

  requires transitive io.avaje.config;
  requires org.tomlj;

  exports io.avaje.config.toml;

  provides ConfigExtension with TomlParser;

}
