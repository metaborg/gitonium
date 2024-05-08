#!groovy
@Library('metaborg.jenkins.pipeline@develop') _

gradlePipeline(
  // Git
  mainBranch: "master",

  // Gradle
  gradleBuildTasks: "build",
  gradlePublishTasks: "publish",

  // Releases
  releaseTagPattern: "release-*.*.*",
  publishUsernameProperty: "metaborg-artifacts.username",
  publishPasswordProperty: "metaborg-artifacts.password",

  // Slack
  slack: true,
  slackChannel: "#spoofax3-dev"
)
