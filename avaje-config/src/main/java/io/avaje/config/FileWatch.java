package io.avaje.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.util.*;

final class FileWatch {

  private final ConfigurationLog log;
  private final Configuration configuration;
  private final Map<String, ConfigParser> parsers;
  private final List<Entry> files;
  private final long delay;
  private final long period;

  FileWatch(CoreConfiguration configuration, List<File> loadedFiles, Map<String, ConfigParser> parsers) {
    this.log = configuration.log();
    this.configuration = configuration;
    this.delay = configuration.getLong("config.watch.delay", 60);
    this.period = configuration.getLong("config.watch.period", 10);
    this.parsers = parsers;
    this.files = initFiles(loadedFiles);
    if (files.isEmpty()) {
      log.log(Level.ERROR, "No files to watch?");
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
    final Map<String, String> keyValues = new LinkedHashMap<>();
    for (Entry file : files) {
      if (file.reload()) {
        log.log(Level.DEBUG, "reloading configuration from {0}", file);
        if (file.isCustom()) {
          reloadYaml(file, keyValues);
        } else {
          reloadProps(file, keyValues);
        }
      }
    }
    final var builder = configuration.eventBuilder("reload");
    keyValues.forEach(builder::put);
    builder.publish();
  }

  private void reloadProps(Entry file, Map<String, String> keyValues) {
    try (InputStream is = file.inputStream()) {
      final var properties = new Properties();
      properties.load(is);
      Enumeration<?> enumeration = properties.propertyNames();
      while (enumeration.hasMoreElements()) {
        final String key = (String) enumeration.nextElement();
        keyValues.put(key, properties.getProperty(key));
      }
    } catch (Exception e) {
      log.log(Level.ERROR, "Unexpected error reloading config file " + file, e);
    }
  }

  private void reloadYaml(Entry file, Map<String, String> keyValues) {
    var parser = parsers.get(file.extension);
    if (parser == null) {
      log.log(Level.ERROR, "Unexpected - no parser to reload config file " + file);
    } else {
      try (InputStream is = file.inputStream()) {
        keyValues.putAll(parser.load(is));
      } catch (Exception e) {
        log.log(Level.ERROR, "Unexpected error reloading config file " + file, e);
      }
    }
  }

  private static class Entry {
    private final File file;
    private final boolean customExtension;
    private final String extension;
    private long lastMod;
    private long lastLength;

    Entry(File file) {
      this.file = file;
      this.lastMod = file.lastModified();
      this.lastLength = file.length();
      var name = file.getName();
      this.extension = name.substring(name.lastIndexOf(".") + 1);
      this.customExtension = !"properties".equals(extension);
    }

    @Override
    public String toString() {
      return file.toString();
    }

    boolean isCustom() {
      return customExtension;
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
