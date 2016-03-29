package com.palantir.npmrun

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

class NpmRunPluginIntegrationSpec extends IntegrationSpec {

    def setup() {
        copyResources("fixtures/package.json", "package.json")
    }

    def "tasks call underlying package.json scripts block"() {
        setup:
        buildFile << """
            apply plugin: "com.palantir.npm-run"
        """.stripIndent()

        when:
        ExecutionResult result = runTasksSuccessfully(runTaskName)

        then:
        result.success
        filesMustExist.every { fileExists(it) }

        where:
        runTaskName | filesMustExist
        "clean"     | ["did.clean"]
        "test"      | ["did.test"]
        "check"     | ["did.test"]
        "build"     | ["did.test", "did.build"]
        "buildDev"  | ["did.test", "did.buildDev"]
    }

    def "override default npm run commands"() {
        setup:
        buildFile << """
            apply plugin: "com.palantir.npm-run"

            npmRun {
                clean       "other-clean"
                test        "other-test"
                build       "other-build"
                buildDev    "other-buildDev"
            }
        """.stripIndent()

        when:
        ExecutionResult result = runTasksSuccessfully(runTaskName)

        then:
        result.success
        filesMustExist.every { fileExists(it) }

        where:
        runTaskName | filesMustExist
        "clean"     | ["other.clean"]
        "test"      | ["other.test"]
        "check"     | ["other.test"]
        "build"     | ["other.test", "other.build"]
        "buildDev"  | ["other.test", "other.buildDev"]
    }

    def "no duplicates"() {
        setup:
        buildFile << """
            apply plugin: "com.palantir.npm-run"
        """.stripIndent()

        when:
        ExecutionResult result = runTasksSuccessfully("clean", "check", "build", "buildDev")

        then:
        result.success
        ["did.clean", "did.test", "did.build", "did.buildDev"].every { fileExists(it) }
    }
}