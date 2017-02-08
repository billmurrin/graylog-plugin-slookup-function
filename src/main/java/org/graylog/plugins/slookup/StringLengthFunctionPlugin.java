package org.graylog.plugins.slookup;

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;

import java.util.Collection;
import java.util.Collections;

public class StringLengthFunctionPlugin implements Plugin {
    @Override
    public PluginMetaData metadata() {
        return new StreamLookupFunctionMetaData();
    }

    @Override
    public Collection<PluginModule> modules () {
        return Collections.<PluginModule>singletonList(new StreamLookupFunctionModule());
    }
}
