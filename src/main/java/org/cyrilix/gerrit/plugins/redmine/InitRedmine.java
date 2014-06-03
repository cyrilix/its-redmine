package org.cyrilix.gerrit.plugins.redmine;

/*
 * #%L Gerrit - Redmine support %% Copyright (C) 2014 Cyrille Nofficial %%
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. #L%
 */

import java.io.IOException;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.pgm.init.AllProjectsConfig;
import com.google.gerrit.pgm.init.AllProjectsNameOnInitProvider;
import com.google.gerrit.pgm.init.InitFlags;
import com.google.gerrit.pgm.init.Section;
import com.google.gerrit.pgm.util.ConsoleUI;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.hooks.its.InitIts;

/**
 * @author Cyrille Nofficial
 * 
 */
@Singleton
class InitRedmine extends InitIts {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitRedmine.class);

    private final String pluginName;
    private final Section.Factory sections;
    private final InitFlags flags;

    /**
     * Constructor
     */
    @Inject
    InitRedmine(@PluginName String pluginName, ConsoleUI ui, Section.Factory sections,
            AllProjectsConfig allProjectsConfig, AllProjectsNameOnInitProvider allProjects, InitFlags flags) {
        super(pluginName, "Redmine", ui, allProjectsConfig, allProjects);
        this.pluginName = pluginName;
        this.sections = sections;
        this.flags = flags;
    }

    @Override
    public void run() throws IOException, ConfigInvalidException {
        super.run();

        ui.message("\n");
        ui.header("Redmine connectivity");

    }

}
