package com.banyaibalazs.variantmaker

import spock.lang.Specification
import org.gradle.testfixtures.ProjectBuilder

class AndroidTestVariantMakerTest extends Specification {
    def "plugin can be applied to project"() {
        setup:
        def project = new ProjectBuilder().build()

        when:
        project.apply plugin: AndroidTestVariantMaker

        then:
        project.plugins.hasPlugin(AndroidTestVariantMaker)
    }
}
