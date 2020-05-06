package se.arkalix.core.plugin;

import se.arkalix.ArService;
import se.arkalix.plugin.Plugin;
import se.arkalix.plugin.PluginFirst;

/**
 * Cloud plugin.
 * <p>
 * Each implementation of this interface helps one {@link se.arkalix.ArSystem
 * system} to join a local cloud by communicating with the mandatory Arrowhead
 * core services of that. More precisely, it (1) registers and unregisters the
 * {@link se.arkalix.ArSystem#provide(ArService) services provided} by its
 * system, (2) retrieves the public key required to {@link
 * se.arkalix.security.access.AccessByToken validate consumer tokens}, as well
 * as (3) helps resolve {@link se.arkalix.ArSystem#consume() service
 * consumption queries}.
 */
@PluginFirst
public interface ArCloudPlugin extends Plugin {}
