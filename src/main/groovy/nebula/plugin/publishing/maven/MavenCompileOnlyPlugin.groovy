/*
 * Copyright 2015-2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.plugin.publishing.maven

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.XmlProvider
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.maven.MavenPublication

class MavenCompileOnlyPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.plugins.apply MavenBasePublishPlugin

        project.pluginManager.withPlugin('java') {
            project.publishing {
                publications {
                    withType(MavenPublication) { publication ->
                        publication.pom.withXml { XmlProvider xml ->
                            // Only configure publications that publish the java component
                            if (publication.component != project.components.java) {
                                return
                            }
                            project.plugins.withType(JavaBasePlugin) {
                                def root = xml.asNode()
                                def dependencies = project.configurations.compileOnly.dependencies
                                if (dependencies.size() > 0) {
                                    def deps = root.dependencies ? root.dependencies[0] : root.appendNode('dependencies')
                                    dependencies.each { dep ->
                                        deps.appendNode('dependency').with {
                                            appendNode('groupId', dep.group)
                                            appendNode('artifactId', dep.name)
                                            appendNode('version', dep.version)
                                            appendNode('scope', 'provided')
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
