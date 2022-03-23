package io.avaje.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

final class FileWatch {

  private final Configuration configuration;
  private final YamlLoader yamlLoader;
  private final List<Entry> files;
  private final long delay;
  private final long period;

  FileWatch(Configuration configuration, List<File> loadedFiles, YamlLoader yamlLoader) {
    this.configuration = configuration;
    this.delay = configuration.getLong("config.watch.delay", 60);
    this.period = configuration.getInt("config.watch.period", 10);
    this.yamlLoader = yamlLoader;
    this.files = initFiles(loadedFiles);
    if (files.isEmpty()) {
      Config.log.log(Level.ERROR, "No files to watch?");
    } else {
      configuration.schedule(delay * 1000, period * 1000, this::check);
    }
  }

  @Override
  public String toString() {
    return "Watch[period:" + period + " delay:" + delay + " files:" + files + "]";
  }

  private List<Entry> initFiles(List<File> loadedFiles) {
    List<Entry> entries = new ArrayList<>(loadedFiles.size());
    for (File loadedFile : loadedFiles) {
      entries.add(new Entry(loadedFile));
    }
    return entries;
  }

  boolean changed() {
    for (Entry file : files) {
      if (file.changed()) {
        return true;
      }
    }
    return false;
  }

  void check() {
    for (Entry file : files) {
      if (file.reload()) {
        Config.log.log(Level.DEBUG, "reloading configuration from {0}", file);
        if (file.isYaml()) {
          reloadYaml(file);
        } else {
          reloadProps(file);
        }
      }
    }
  }

  private void reloadProps(Entry file) {
    Properties properties = new Properties();
    try (InputStream is = file.inputStream()) {
      properties.load(is);
      put(properties);
    } catch (Exception e) {
      Config.log.log(Level.ERROR, "Unexpected error reloading config file " + file, e);
    }
  }

  private void put(Properties properties) {
    Enumeration<?> enumeration = properties.propertyNames();
    while (enumeration.hasMoreElements()) {
      String key = (String) enumeration.nextElement();
      String property = properties.getProperty(key);
      configuration.setProperty(key, property);
    }
  }

  private void reloadYaml(Entry file) {
    if (yamlLoader == null) {
      Config.log.log(Level.ERROR, "Unexpected - no yamlLoader to reload config file " + file);
    } else {
      try (InputStream is = file.inputStream()) {
        yamlLoader.load(is).forEach(configuration::setProperty);
      } catch (Exception e) {
        Config.log.log(Level.ERROR, "Unexpected error reloading config file " + file, e);
      }
    }
  }

  private static class Entry {
    private final File file;
    private final boolean yaml;
    private long lastMod;
    private long lastLength;

    Entry(File file) {
      this.file = file;
      this.lastMod = file.lastModified();
      this.lastLength = file.length();
      this.yaml = isYaml(file.getName());
    }

    @Override
    public String toString() {
      return file.toString();
    }

    boolean isYaml() {
      return yaml;
    }

    private boolean isYaml(String name) {
      final String lowerName = name.toLowerCase();
      return lowerName.endsWith(".yaml") || lowerName.endsWith(".yml");
    }

    boolean reload() {
      if (!changed()) {
        return false;
      }
      lastMod = file.lastModified();
      lastLength = file.length();
      return true;
    }

    boolean changed() {
      return file.lastModified() > lastMod || file.length() != lastLength;
    }

    InputStream inputStream() {
      try {
        return new FileInputStream(file);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
