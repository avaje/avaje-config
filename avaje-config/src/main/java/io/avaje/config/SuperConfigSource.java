package io.avaje.config;

import java.util.Map;

public interface SuperConfigSource extends ConfigExtension {

  String label();

  Map<String, String> load(ConfigLoadCTX configuration);
}
