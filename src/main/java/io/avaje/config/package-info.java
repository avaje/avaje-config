/**
 * Application configuration based on loading properties and yaml files.
 *
 * <h3>Examples</h3>
 * <pre>{@code
 *
 *  int port = Config.getInt("app.port", 8090);
 *
 *  String topicName = Config.get("app.topic.name");
 *
 *  List<Integer> codes = Config.getList().ofInt("my.codes", 42, 54);
 *
 * }</pre>
 */
package io.avaje.config;