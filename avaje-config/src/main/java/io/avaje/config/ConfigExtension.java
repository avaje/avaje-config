package io.avaje.config;

import io.avaje.spi.Service;

/**
 * Super interface for all extensions to avaje-config which are loaded via ServiceLoader.
 * <p>
 * Extend avaje-config by implementing one of the following extension interfaces below and
 * have it registered with {@link java.util.ServiceLoader} as a {@link ConfigExtension}.
 *
 * <h4>Extensions</h4>
 * <ul>
 *   <li>{@link ConfigParser}</li>
 *   <li>{@link ConfigurationLog}</li>
 *   <li>{@link ConfigurationPlugin}</li>
 *   <li>{@link ConfigurationSource}</li>
 *   <li>{@link ResourceLoader}</li>
 *   <li>{@link ModificationEventRunner}</li>
 * </ul>
 */
@Service
public interface ConfigExtension {
}
