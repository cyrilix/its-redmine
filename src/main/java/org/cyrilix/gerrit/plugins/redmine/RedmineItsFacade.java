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

import java.io.IOException;
import java.net.URL;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.hooks.its.ItsFacade;

/**
 * @author Cyrille Nofficial
 * 
 */
public class RedmineItsFacade implements ItsFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedmineItsFacade.class);

    private final String pluginName;
    private final Config gerritConfig;

    /**
     * Constructor
     */
    @Inject
    public RedmineItsFacade(@PluginName String pluginName, @GerritServerConfig Config cfg) {
        this.pluginName = pluginName;
        this.gerritConfig = cfg;

    }

    /**
     * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#addComment(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void addComment(String arg0, String arg1) throws IOException {

    }

    /**
     * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#addRelatedLink(java.lang.String,
     *      java.net.URL, java.lang.String)
     */
    @Override
    public void addRelatedLink(String arg0, URL arg1, String arg2) throws IOException {

    }

    /**
     * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#createLinkForWebui(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public String createLinkForWebui(String arg0, String arg1) {

        return null;
    }

    /**
     * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#exists(java.lang.String)
     */
    @Override
    public boolean exists(String arg0) throws IOException {

        return false;
    }

    /**
     * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#healthCheck(com.googlesource.gerrit.plugins.hooks.its.ItsFacade.Check)
     */
    @Override
    public String healthCheck(Check arg0) throws IOException {

        return null;
    }

    /**
     * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#name()
     */
    @Override
    public String name() {

        return null;
    }

    /**
     * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#performAction(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void performAction(String arg0, String arg1) throws IOException {

    }
}
