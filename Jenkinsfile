#!groovy
@Library('metaborg.jenkins.pipeline') _

gradlePipeline(
  // Git
  mainBranch: "master",

  // Gradle
  gradleBuildTasks: "build",
  gradlePublishTasks: "publish",

  // Releases
  releaseTagPattern: "release-*.*.*",

  // Slack
  slack: true,
  slackChannel: "#spoofax3-dev"
)
