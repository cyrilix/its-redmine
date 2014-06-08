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
import static java.util.Arrays.asList;
import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.eclipse.jgit.lib.Config;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlesource.gerrit.plugins.hooks.its.ItsFacade.Check;
import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.User;

/**
 * Test of class {@link RedmineItsFacade}
 * 
 * @author Cyrille Nofficial
 * 
 */
public class RedmineItsFacadeTest {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RedmineItsFacadeTest.class);

  /**
   * Constructor
   */
  public RedmineItsFacadeTest() {
  }

  /**
   * Test of method {@link RedmineItsFacade#name()}
   */
  @Test
  public void testName() {
    try {
      LOGGER.info("------ testName -------");

      assertThat(new RedmineItsFacade(null).name(), is("Redmine"));

    } catch (Exception e) {
      LOGGER.error("Unexpected error: " + e.getMessage(), e);
      fail("Unexpected error: " + e.getMessage());
    }
  }

  /**
   * Test of method {@link RedmineItsFacade#addComment(String, String)}
   */
  @Test
  public void testAddComment() {
    try {
      LOGGER.info("------ testAddComment -------");

      IMocksControl control = EasyMock.createStrictControl();
      RedmineManager redmineManager = control.createMock(RedmineManager.class);
      Issue expectedIssue = new Issue();
      expectedIssue.setId(42);
      expectedIssue.setNotes("test comment");

      RedmineItsFacade instance = new RedmineItsFacade(redmineManager);
      control.reset();
      redmineManager.update(expectedIssue);
      expectLastCall();
      control.replay();

      instance.addComment("42", "test comment");

      control.verify();

      // Invalid id syntax
      control.reset();
      control.replay();

      try {
        instance.addComment("#42", "test comment");
        fail("An exception must be throw");
      } catch (IOException e) {
        LOGGER.debug("Exception thrown as expected: {}", e.getMessage(), e);
      }

      control.verify();

      control.reset();
      control.replay();

      try {
        instance.addComment(null, "test comment");
        fail("An exception must be throw");
      } catch (IOException e) {
        LOGGER.debug("Exception thrown as expected: {}", e.getMessage(), e);
      }

      control.verify();
      control.reset();
      control.replay();

      // Comment empty
      control.reset();
      control.replay();
      instance.addComment("42", null);
      instance.addComment("42", "");
      instance.addComment("42", "  ");
      control.verify();

      // Other error
      control.reset();
      redmineManager.update(expectedIssue);
      expectLastCall().andThrow(new RedmineException("Test error"));
      control.replay();

      try {
        instance.addComment("42", "test comment");
        fail("An exception must be throw");
      } catch (IOException e) {
        LOGGER.debug("Exception thrown as expected: {}", e.getMessage(), e);
      }

    } catch (Exception e) {
      LOGGER.error("Unexpected error: " + e.getMessage(), e);
      fail("Unexpected error: " + e.getMessage());
    }
  }

  /**
   * Test de la méthode {@link RedmineItsFacade#exists(String)}
   */
  @Test
  public void testExists() {
    try {
      LOGGER.info("------ testExists -------");

      IMocksControl control = EasyMock.createStrictControl();
      RedmineManager redmineManager = control.createMock(RedmineManager.class);

      Issue issue = new Issue();
      issue.setId(42);
      issue.setNotes("test issue");

      RedmineItsFacade instance = new RedmineItsFacade(redmineManager);
      control.reset();
      expect(redmineManager.getIssueById(valueOf(42))).andReturn(issue);
      expectLastCall();
      control.replay();

      assertThat(instance.exists("42"), is(true));

      control.verify();

      // Issue not exist
      control.reset();
      expect(redmineManager.getIssueById(valueOf(42))).andReturn(null);
      expectLastCall();
      control.replay();

      assertThat(instance.exists("42"), is(false));

      control.verify();

      control.reset();
      expect(redmineManager.getIssueById(valueOf(42))).andThrow(
          new NotFoundException("Issue doesn't exist"));
      control.replay();

      assertThat(instance.exists("42"), is(false));

      control.verify();

      // Invalid id syntax
      control.reset();
      control.replay();

      assertThat(instance.exists("#42"), is(false));
      assertThat(instance.exists(" "), is(false));
      assertThat(instance.exists(null), is(false));

      control.verify();

      // Other error
      control.reset();
      redmineManager.getIssueById(valueOf(42));
      expectLastCall().andThrow(new RedmineException("Test error"));
      control.replay();

      try {
        instance.exists("42");
        fail("An exception must be throw");
      } catch (IOException e) {
        LOGGER.debug("Exception thrown as expected: {}", e.getMessage(), e);
      }
    } catch (Exception e) {
      LOGGER.error("Unexpected error: " + e.getMessage(), e);
      fail("Unexpected error: " + e.getMessage());
    }
  }

  /**
   * Test of method
   * {@link RedmineItsFacade#healthCheck(com.googlesource.gerrit.plugins.hooks.its.ItsFacade.Check)}
   */
  @Test
  public void testHealthCheck() {
    try {
      LOGGER.info("------ testHealthCheck -------");

      IMocksControl control = EasyMock.createStrictControl();
      RedmineManager redmineManager = control.createMock(RedmineManager.class);

      User user = new User();
      user.setLogin("loginTest");

      RedmineItsFacade instance = new RedmineItsFacade(redmineManager) {

        /**
         * @see org.cyrilix.gerrit.plugins.redmine.RedmineItsFacade#getHost()
         */
        @Override
        String getHost() {
          return "host_test";
        }
      };

      // ACCESS
      control.reset();
      expect(redmineManager.getCurrentUser()).andReturn(user);
      expectLastCall();
      control.replay();

      String result = instance.healthCheck(Check.ACCESS);
      assertThat(result, is("{\"status\"=\"ok\",\"username\"=\"loginTest\"}"));
      control.verify();

      // SYSINFO
      control.reset();
      expect(redmineManager.getCurrentUser()).andReturn(user);
      expectLastCall();
      control.replay();

      result = instance.healthCheck(Check.SYSINFO);
      assertThat(
          result,
          is("{\"status\"=\"ok\",\"system\"=\"Redmine\",\"url\"=\"host_test\"}"));
      control.verify();

      // Other error
      control.reset();
      expect(redmineManager.getCurrentUser()).andThrow(
          new RedmineException("Test error"));
      control.replay();

      try {
        instance.healthCheck(Check.ACCESS);
        fail("An exception must be throw");
      } catch (IOException e) {
        LOGGER.debug("Exception thrown as expected: {}", e.getMessage(), e);
      }
    } catch (Exception e) {
      LOGGER.error("Unexpected error: " + e.getMessage(), e);
      fail("Unexpected error: " + e.getMessage());
    }
  }

  /**
   * Test of method
   * {@link RedmineItsFacade#addRelatedLink(String, java.net.URL, String)}
   */
  @Test
  public void testAddRelatedLink() {
    try {
      LOGGER.info("------ testAddRelatedLink -------");

      IMocksControl control = EasyMock.createStrictControl();
      RedmineManager redmineManager = control.createMock(RedmineManager.class);
      Issue expectedIssue = new Issue();
      expectedIssue.setId(42);
      expectedIssue
          .setNotes("Related URL: \"description\":http://myredmine.org");

      RedmineItsFacade instance = new RedmineItsFacade(redmineManager);
      control.reset();
      redmineManager.update(expectedIssue);
      expectLastCall();
      control.replay();

      instance.addRelatedLink("42", new URL("http://myredmine.org"),
          "description");
      control.verify();

    } catch (Exception e) {
      LOGGER.error("Unexpected error: " + e.getMessage(), e);
      fail("Unexpected error: " + e.getMessage());
    }
  }

  /**
   * Test of method {@link RedmineItsFacade#createLinkForWebui}
   */
  @Test
  public void testcreateLinkForWebui() {
    try {
      LOGGER.info("------ testcreateLinkForWebui -------");


      RedmineItsFacade instance = new RedmineItsFacade(null);

      assertThat(instance.createLinkForWebui("http://myredmine.org", "text"),
          is("\"text\":http://myredmine.org"));

      assertThat(instance.createLinkForWebui("http://myredmine.org",
          "http://myredmine.org"), is("http://myredmine.org"));
      assertThat(instance.createLinkForWebui("http://myredmine.org", null),
          is("http://myredmine.org"));
      assertThat(instance.createLinkForWebui("http://myredmine.org", ""),
          is("http://myredmine.org"));
      assertThat(instance.createLinkForWebui("http://myredmine.org", " "),
          is("http://myredmine.org"));

    } catch (Exception e) {
      LOGGER.error("Unexpected error: " + e.getMessage(), e);
      fail("Unexpected error: " + e.getMessage());
    }
  }

  /**
   * Test of method {@link RedmineItsFacade#performAction(String, String)}
   */
  @Test
  public void testPerformAction() {
    try {
      LOGGER.info("------ testPerformAction -------");

      IMocksControl control = createStrictControl();
      RedmineManager redmineManager = control.createMock(RedmineManager.class);

      RedmineItsFacade instance = new RedmineItsFacade(redmineManager);

      List<IssueStatus> statuses =
          asList(new IssueStatus(1, "assigned"), new IssueStatus(2, "close"));
      Issue issueExpected = new Issue();
      issueExpected.setId(valueOf(42));
      issueExpected.setStatusId(valueOf(2));

      control.reset();
      expect(redmineManager.getStatuses()).andReturn(statuses);
      redmineManager.update(issueExpected);
      expectLastCall();
      control.replay();

      instance.performAction("42", "Close");

      control.verify();

      // Status unknown
      control.reset();
      expect(redmineManager.getStatuses()).andReturn(statuses);
      expectLastCall();
      control.replay();

      try {
        instance.performAction("42", "StatusUnknown");
        fail("An exception must be throw");
      } catch (IOException e) {
        LOGGER.debug("Exception thrown as expected: {}", e.getMessage(), e);
      }
      control.verify();

    } catch (Exception e) {
      LOGGER.error("Unexpected error: " + e.getMessage(), e);
      fail("Unexpected error: " + e.getMessage());
    }
  }

  /**
   * Test of method RedmineItsFacade#getHost
   */
  @Test
  public void testGetHost() {
    try {
      LOGGER.info("------ testGetHost -------");

      Config config = new Config();
      config.setString("its-redmine", null, "host", "http://myredmine.org");
      RedmineItsFacade instance = new RedmineItsFacade("its-redmine", config);

      assertThat(instance.getHost(), is("http://myredmine.org"));

    } catch (Exception e) {
      LOGGER.error("Unexpected error: " + e.getMessage(), e);
      fail("Unexpected error: " + e.getMessage());
    }
  }
}
