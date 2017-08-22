package com.banyaibalazs.variantmaker

import com.android.build.gradle.internal.TaskContainerAdaptor
import com.android.build.gradle.internal.dsl.BuildType
import com.android.builder.core.VariantType
import org.gradle.api.Project
import org.gradle.api.Plugin

import static com.android.builder.core.BuilderConstants.CONNECTED
import static com.android.builder.core.BuilderConstants.FD_ANDROID_RESULTS
import static com.android.builder.core.BuilderConstants.FD_ANDROID_TESTS
import static com.android.builder.core.BuilderConstants.FD_REPORTS
import static com.android.builder.model.AndroidProject.FD_OUTPUTS

class AndroidTestVariantMaker implements Plugin<Project> {

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        List buildTypesRequireAndroidTests = []

        BuildType.metaClass.withAndroidTests = { it ->
            buildTypesRequireAndroidTests.add(delegate)
            buildTypeAdded(delegate)
        }

        project.afterEvaluate {
            project.plugins.withId('com.android.application') { plugin ->
                plugin.variantManager.variantDataList.each { variantData ->
                    if (buildTypesRequireAndroidTests.any { it.name == variantData.name }) {

                        def testVariantData = plugin.variantManager.createTestVariantData(variantData, VariantType.ANDROID_TEST)
                        def taskFactory = new TaskContainerAdaptor(project.getTasks());

                        // TODO see https://android.googlesource.com/platform/tools/base/+/gradle_2.3.0/build-system/gradle-core/src/main/java/com/android/build/gradle/internal/tasks/DeviceProviderInstrumentTestTask.java
                        def resultTarget = "${project.buildDir}/$FD_OUTPUTS/${variantData.name}${FD_ANDROID_RESULTS.capitalize()}"
                        def reportTarget = "${project.buildDir}/$FD_REPORTS/${variantData.name}${FD_ANDROID_TESTS.capitalize()}"

                        testVariantData.variantDependency = variantData.variantDependency
                        testVariantData.scope.assembleTask = variantData.scope.assembleTask

                        variantData.taskManager.createAndroidTestVariantTasks(taskFactory, testVariantData)
                        def taskName = "$CONNECTED${variantData.name.capitalize()}${VariantType.ANDROID_TEST.getSuffix()}"
                        def task = project.tasks[taskName]
                        task.resultsDir = project.file(resultTarget)
                        task.reportsDir = project.file(reportTarget)
                    }
                }
            }
        }
    }

    void buildTypeAdded(def buildType) {
        project.configurations.getByName(buildType.name+'Compile').extendsFrom project.configurations.androidTestCompile
    }

}
