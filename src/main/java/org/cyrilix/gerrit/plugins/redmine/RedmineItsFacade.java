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

import static java.lang.Integer.valueOf;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.hooks.its.InvalidTransitionException;
import com.googlesource.gerrit.plugins.hooks.its.ItsFacade;
import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueStatus;

/**
 * Redmine based implementation of {@link ItsFacade}
 * 
 * @author Cyrille Nofficial
 * 
 */
public class RedmineItsFacade implements ItsFacade {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RedmineItsFacade.class);

  private static final int MAX_ATTEMPTS = 3;
  private static final String GERRIT_CONFIG_HOST = "host";
  private static final String GERRIT_CONFIG_API_KEY = "api_key";

  private RedmineManager redmineManager = null;

  private Config config;

  private String pluginName;

  /**
   * Constructor
   * 
   * @param pluginName plugin name
   * @param config gerrit configuration
   */
  @Inject
  public RedmineItsFacade(@PluginName String pluginName,
      @GerritServerConfig Config config) {
    this.pluginName = pluginName;
    this.config = config;
    String host = getHost();
    String apiAccessKey =
        config.getString(pluginName, null, GERRIT_CONFIG_API_KEY);
    LOGGER.trace("Initialize redmine-its to {} host", host);
    this.redmineManager = new RedmineManager(host, apiAccessKey);

  }

  /**
   * Return redmine hostname
   * 
   * @return redmine hostname
   */
  String getHost() {
    return this.config.getString(this.pluginName, null, GERRIT_CONFIG_HOST);
  }

  /**
   * Constructor
   * 
   * @param redmineManager redmine client
   */
  public RedmineItsFacade(RedmineManager redmineManager) {
    this.redmineManager = redmineManager;
  }

  /**
   * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#addComment(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void addComment(final String issueId, final String comment)
      throws IOException {

    if (comment == null || comment.trim().isEmpty()) {
      return;
    }

    execute(new Callable<String>() {
      @Override
      public String call() throws Exception {
        Issue issue = new Issue();
        issue.setId(convertIssueId(issueId));
        issue.setNotes(comment);

        try {
          redmineManager.update(issue);
        } catch (RedmineException e) {
          LOGGER.error("Error in add comment: {}", e.getMessage(), e);
          throw new IOException(e.getMessage(), e);
        }
        return issueId;
      }
    });
  }

  private Integer convertIssueId(String issueId) throws IOException {
    if (!issueIdIsValid(issueId)) {
      LOGGER.warn("Issue {} is not a valid issue id", issueId);
      throw new IOException("Issue " + issueId + " is not a valid issue id");
    }
    return Integer.valueOf(issueId);
  }

  private boolean issueIdIsValid(String issueId) {
    return issueId != null && issueId.matches("^\\d+$");
  }

  /**
   * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#addRelatedLink(java.lang.String,
   *      java.net.URL, java.lang.String)
   */
  @Override
  public void addRelatedLink(final String issueKey, final URL relatedUrl,
      String description) throws IOException {
    addComment(
        issueKey,
        "Related URL: "
            + createLinkForWebui(relatedUrl.toExternalForm(), description));
  }

  /**
   * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#createLinkForWebui(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public String createLinkForWebui(String url, String text) {
    String ret = url;
    if (text != null && !text.equals(url) && !text.trim().isEmpty()) {
      ret = "\"" + text + "\":" + url;
    }
    return ret;
  }

  /**
   * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#exists(java.lang.String)
   */
  @Override
  public boolean exists(final String issueId) throws IOException {
    return execute(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {

        if (!issueIdIsValid(issueId)) {
          return false;
        }

        try {
          return redmineManager.getIssueById(convertIssueId(issueId)) != null;
        } catch (NotFoundException e) {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                "Issue " + issueId + " doesn't exit: " + e.getMessage(), e);
          }
          return false;
        } catch (RedmineException e) {
          LOGGER.error(e.getMessage(), e);
          throw new IOException(e.getMessage());
        }
      }
    });
  }

  /**
   * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#healthCheck(com.googlesource.gerrit.plugins.hooks.its.ItsFacade.Check)
   */
  @Override
  public String healthCheck(Check check) throws IOException {

    if (Check.ACCESS.equals(check)) {
      return healthCheckAccess();
    }
    return healthCheckSysinfo();

  }

  private String healthCheckAccess() throws IOException {
    return execute(new Callable<String>() {
      @Override
      public String call() throws RedmineException {
        com.taskadapter.redmineapi.bean.User user =
            redmineManager.getCurrentUser();
        final String result =
            "{\"status\"=\"ok\",\"username\"=\"" + user.getLogin() + "\"}";
        LOGGER.debug("Healtheck on access result: {}", result);
        return result;
      }
    });
  }

  private String healthCheckSysinfo() throws IOException {

    return execute(new Callable<String>() {
      @Override
      public String call() throws RedmineException {
        redmineManager.getCurrentUser();
        final String result =
            "{\"status\"=\"ok\",\"system\"=\"Redmine\",\"url\"=\"" + getHost()
                + "\"}";
        LOGGER.debug("Healtheck on sysinfo result: {}", result);
        return result;
      }
    });
  }

  /**
   * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#name()
   */
  @Override
  public String name() {
    return "Redmine";
  }

  /**
   * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#performAction(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void performAction(final String issueId, final String actionName)
      throws IOException {
    execute(new Callable<String>() {
      @Override
      public String call() throws RedmineException, IOException {
        doPerformAction(issueId, actionName);
        return issueId;
      }
    });
  }

  private void doPerformAction(final String issueKey, final String actionName)
      throws IOException, RedmineException {

    Integer statusId = getStatusId(actionName);

    if (statusId != null) {
      LOGGER.debug("Executing action " + actionName + " on issue " + issueKey);
      Issue issue = new Issue();
      issue.setId(valueOf(issueKey));
      issue.setStatusId(statusId);
      redmineManager.update(issue);
    } else {
      LOGGER.error("Action " + actionName
          + " not found within available actions");
      throw new InvalidTransitionException("Action " + actionName
          + " not executable on issue " + issueKey);
    }
  }

  private Integer getStatusId(String actionName) throws RedmineException {

    for (IssueStatus issueStatus : redmineManager.getStatuses()) {
      if (issueStatus.getName().equalsIgnoreCase(actionName)) {
        return issueStatus.getId();
      }
    }

    return null;
  }

  private <P> P execute(Callable<P> function) throws IOException {

    int attempt = 0;
    while (true) {
      try {
        return function.call();
      } catch (Exception ex) {
        if (isRecoverable(ex) && ++attempt < MAX_ATTEMPTS) {
          LOGGER.debug("Call failed - retrying, attempt {} of {}", attempt,
              MAX_ATTEMPTS);
          continue;
        }

        if (ex instanceof IOException) {
          throw ((IOException) ex);
        }
        throw new IOException(ex);
      }
    }
  }

  private boolean isRecoverable(Exception ex) {
    return false;
  }
}
