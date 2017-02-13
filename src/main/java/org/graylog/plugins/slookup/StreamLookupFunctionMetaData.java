package org.graylog.plugins.slookup;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class StreamLookupFunctionMetaData implements PluginMetaData {
    private static final String PLUGIN_PROPERTIES = "org/graylog/plugins/graylog-plugin-slookup-function/graylog-plugin.properties";

    @Override
    public String getUniqueId() {
        return "StreamLookupFunction";
    }

    @Override
    public String getName() {
        return "Stream Lookup (SLookup) pipeline function";
    }

    @Override
    public String getAuthor() {
        return "Bill Murrin <bill@billmurrin.com>";
    }

    @Override
    public URI getURL() {
        return URI.create("https://github.com/billmurrin/graylog-plugin-slookup-function");
    }

    @Override
    public Version getVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "version", Version.from(1, 0, 0, "unknown"));
    }

    @Override
    public String getDescription() {
        return "VLOOKUP for the Process Pipeline! Matches the value of a source field with a destination field in a remote stream. Will return the value of the field name provided as the return field if a match is found.";
    }

    @Override
    public Version getRequiredVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "graylog.version", Version.from(2, 1, 3));
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
