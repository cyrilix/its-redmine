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

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.pgm.init.AllProjectsConfig;
import com.google.gerrit.pgm.init.AllProjectsNameOnInitProvider;
import com.google.gerrit.pgm.init.InitFlags;
import com.google.gerrit.pgm.init.Section;
import com.google.gerrit.pgm.util.ConsoleUI;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.hooks.its.InitIts;
import com.googlesource.gerrit.plugins.hooks.validation.ItsAssociationPolicy;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;

/**
 * Module initialisation for redmine-its plugin
 * 
 * @author Cyrille Nofficial
 * 
 */
@Singleton
class InitBugzilla extends InitIts {
  private final String pluginName;
  private final Section.Factory sections;
  private Section redmine;
  private Section redmineComment;
  private String redmineUrl;
  private String redmineApiKey;

  @Inject
  InitBugzilla(@PluginName String pluginName, ConsoleUI ui,
      Section.Factory sections, AllProjectsConfig allProjectsConfig,
      AllProjectsNameOnInitProvider allProjects, InitFlags flags) {
    super(pluginName, "Redmine", ui, allProjectsConfig, allProjects);
    this.pluginName = pluginName;
    this.sections = sections;
  }

  @Override
  public void run() throws IOException, ConfigInvalidException {
    super.run();

    ui.message("\n");
    ui.header("Redmine connectivity");

    init();
  }

  private void init() {
    this.redmine = sections.get(pluginName, null);
    this.redmineComment = sections.get(COMMENT_LINK_SECTION, pluginName);


    do {
      enterRedmineConnectivity();
    } while (redmineUrl != null
        && (isConnectivityRequested(redmineUrl) && !isRedmineConnectSuccessful()));

    if (redmineUrl == null) {
      return;
    }

    ui.header("Redmine issue-tracking association");
    redmineComment.string("Redmine issue number regex", "match",
        "#([1-9][0-9]*)");
    redmineComment.set("html", String.format(
        "<a href=\"http://localhost:3000/issues/$1\">#$1</a>", redmineUrl));
    redmineComment.select("Issue number enforced in commit message",
        "association", ItsAssociationPolicy.SUGGESTED);
  }

  public void enterRedmineConnectivity() {
    redmineUrl = redmine.string("Redmine URL (empty to skip)", "host", null);
    if (redmineUrl != null) {
      redmineApiKey = redmine.string("Redmine api_key", "apiKey", "");
    }
  }

  private boolean isRedmineConnectSuccessful() {
    ui.message("Checking Redmine connectivity ... ");
    try {
      RedmineManager redmineManager =
          new RedmineManager(redmineUrl, redmineApiKey);
      redmineManager.getCurrentUser();
      ui.message("[OK]\n");
      return true;
    } catch (RedmineException e) {
      ui.message("*FAILED* (%s)\n", e.toString());
      return false;
    }
  }
}
