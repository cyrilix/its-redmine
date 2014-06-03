package org.cyrilix.gerrit.plugins.redmine;

/*
 * #%L
 * Gerrit - Redmine support
 * %%
 * Copyright (C) 2014 Cyrille Nofficial
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.hooks.ItsHookModule;
import com.googlesource.gerrit.plugins.hooks.its.ItsFacade;

/**
 * @author Cyrille Nofficial
 * 
 */
public class RedmineModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedmineModule.class);

    private final String pluginName;
    private final Config gerritConfig;
    private final PluginConfigFactory pluginCfgFactory;

    /**
     * Constuctor
     */
    @Inject
    public RedmineModule(@PluginName final String pluginName, @GerritServerConfig final Config config,
            PluginConfigFactory pluginCfgFactory) {
        this.pluginName = pluginName;
        this.gerritConfig = config;
        this.pluginCfgFactory = pluginCfgFactory;
    }

    /**
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {

        if (gerritConfig.getString(pluginName, null, "url") != null) {
            LOGGER.info("Bugzilla is configured as ITS");
            bind(ItsFacade.class).toInstance(new RedmineItsFacade(pluginName, gerritConfig));

            install(new ItsHookModule(pluginName, pluginCfgFactory));
        }
    }
}
