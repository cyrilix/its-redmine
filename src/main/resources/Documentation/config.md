Plugin @PLUGIN@
===============

This plugin allows to associate Redmine bugs to Git commits thanks to
the Gerrit listener interface.

It can be configured per project whether the Redmine integration is
enabled or not. To enable the Redmine integration for a project the
project must have the following entry in its `project.config` file in
the `refs/meta/config` branch:

```
  [plugin "its-redmine"]
    enabled = true
```

If `plugin.its-redmine.enabled` is not specified in the `project.config` file
the value is inherited from the parent project. If it is also not set
on any parent project the Redmine integration is disabled for this
project.

By setting `plugin.its-bugzilla.enabled` to true in the `project.config` of the
`All-Projects` project the Redmine integration can be enabled by default
for all projects. During the initialization of the plugin you are asked
if the Redmine integration should be enabled by default for all projects
and if yes this setting in the `project.config` of the `All-Projects`
project is done automatically.

If child projects must not be allowed to disable the Redmine integration
a project can enforce the Redmine integration for all child projects by
setting `plugin.its-redmine.enabled` to `enforced`.

On the project info screen there is a dropdown list for the
`plugin.its-redmine.enabled` parameter which offers the values `true`,
`false`, `enforced` and `INHERIT`. Project owners can change this
parameter and save it. If the Redmine integration is enforced by a
parent project the dropdown list is disabled.

The Redmine integration can be limited to specific branches by setting
`plugin.its-redmine.branch`. The branches may be configured using explicit
branch names, ref patterns, or regular expressions. Multiple branches
may be specified.

E.g. to limit the Redmine integration to the `master` branch and all
stable branches the following could be configured:

```
  [plugin "its-redmine"]
    enabled = true
    branch = refs/heads/master
    branch = ^refs/heads/stable-.*
```

Comment links
----------------

Git commits are associated to Redmine bugs reusing the existing Gerrit
[commitLink configuration][1] to extract the issue ID from commit comments.

[1]: ../../../Documentation/config-gerrit.html#__a_id_commentlink_a_section_commentlink

Additionally you need to specify the enforcement policy for git commits
with regards to issue-tracker associations; the following values are supported:

MANDATORY
:	 One or more issue-ids are required in the git commit message, otherwise
	 the git push will be rejected.

SUGGESTED
:	 Whenever git commit message does not contain one or more issue-ids,
	 a warning message is displayed as a suggestion on the client.

OPTIONAL
:	 Bug-ids are liked when found on git commit message, no warning are
	 displayed otherwise.

Example:

    [commentLink "its-redmine"]
    match = (refs|fixed|close) *#([1-9][0-9]*)
    html = "<a href=\"http://myredmine.org/issues/$2\">$1 #$2</a>"
    association = SUGGESTED

Once a Git commit with a comment link is detected, the Redmine bug ID
is extracted and a new comment added to the issue, pointing back to
the original Git commit.

Note that the plugin relies on $2 holding the numeric id, so we cannot
have match group 1 spanning over the whole “(Bug 4711)”.

Be sure to label the commentLink “its-redmine” with all lowercase to
match the config section's name below.

Redmine connectivity
---------------------

In order for Gerrit to connect to Redmine Rest api, url and api_key
are required in your gerrit.config / secure.config under the [its-redmine] section.

Example:

    [its-redmine]
    url=http://myredmine.org
    api_key=123456789
    

Redine credentials and connectivity details are asked and verified during the Gerrit init.

Gerrit init integration
-----------------------

Redmine plugin is integrated as a Gerrit init step in order to simplify and guide
through the configuration of Redmine integration and connectivity check, avoiding
bogus settings to prevent Gerrit plugin to start correctly.

Gerrit init example:

    *** Redmine Integration
    ***

    Issue tracker integration for all projects? [DISABLED/?]: enabled
    Branches for which the issue tracker integration should be enabled (ref, ref pattern or regular expression) [refs/heads/*]:

    *** Redmine connectivity
    ***

    Redmine URL (empty to skip)       [http://myredmine.org]:
    Redmine api_key                  []:
    Change admin's password        [y/N]? y
    admin's password               : *****
                  confirm password : *****
    Test connectivity to http://myredmine.org [y/N]: y
    Checking Redmine connectivity ... [OK]

    *** Redmine issue-tracking association
    ***

    Redmine issue number regex       [(refs|fixed|close) *#([1-9][0-9]*)]:
    Issue-id enforced in commit message [MANDATORY/?]: ?
           Supported options are:
           mandatory
           suggested
           optional
    Issue-id enforced in commit message [MANDATORY/?]: suggested

GitWeb integration
----------------

When Gerrit gitweb is configured, an additional direct link from Redmine to GitWeb
will be created, pointing exactly to the Git commit ID containing the Redmine issue ID.


Issues workflow automation
--------------------------

Redmine plugin is able to automate status transition on the issues based on
code-review actions performed on Gerrit; actions are performed on Redmine using
the api_key provided during Gerrit init.
Transition automation is driven by `$GERRIT_SITE/etc/issue-state-transition.config`
file.

Syntax of the status transition configuration file is the following:

    [action "<issue-status-action>"]
    change=<state-change-type>
    verified=<verified-value>
    code-review=<code-review-value>

`<issue-status-action>`
:   Action to be performed on the Redmine issue when all the condition in the stanza are met.

`<state-change-type>`
:   Gerrit state change type on which the action will be triggered.
    Possible values are: `created`, `commented`, `merged`, `abandoned`,
    `restored`

`<verified-value>`
:   Verified label added on the Gerrit change with a value from -1 to +1

`<code-review-value>`
:   Code-Review label added on the Gerrit change with a value from -2 to +2

Note: multiple conditions in the action stanza are possible but at least one must be present.

Example:

    [action "Assigned"]
    change=created

    [action "Resolved"]
    verified=+1
    code-review=+2

    [action "Closed"]
    change=merged

    [action "Abandonned"]
    change=abandoned

The above example defines four status transitions on Redmine, based on the following conditions:

* Whenever a new Change is created on Gerrit, start progress on the  issue
* Whenever a change is verified and reviewed with +2, set the Redmine issue to resolved
* Whenever a change is merged to the branch, close the Redmine issue
* Whenever a change is abandoned, stop the progress on the Jira issue
