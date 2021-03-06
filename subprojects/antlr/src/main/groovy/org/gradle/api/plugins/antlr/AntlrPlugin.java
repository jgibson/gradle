/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.plugins.antlr;

import java.io.File;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.DynamicObjectAware;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.JavaPlugin;

import static org.gradle.api.plugins.JavaPlugin.COMPILE_CONFIGURATION_NAME;

import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.antlr.internal.AntlrSourceVirtualDirectoryImpl;
import org.gradle.api.tasks.ConventionValue;
import org.gradle.api.tasks.SourceSet;

/**
 * A plugin for adding Antlr support to {@link JavaPlugin java projects}.
 *
 * @author Steve Ebersole
 */
public class AntlrPlugin implements Plugin<Project> {
    public static final String ANTLR_CONFIGURATION_NAME = "antlr";

    public void apply(final Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        // set up a configuration named 'antlr' for the user to specify the antlr libs to use in case
        // they want a specific version etc.
        Configuration antlrConfiguration = project.getConfigurations().add(ANTLR_CONFIGURATION_NAME).setVisible(false)
                .setTransitive(false).setDescription("The Antlr libraries to be used for this project.");
        project.getConfigurations().getByName(COMPILE_CONFIGURATION_NAME).extendsFrom(antlrConfiguration);

        final ProjectInternal projectInternal = (ProjectInternal) project;
        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(
                new Action<SourceSet>() {
                    public void execute(SourceSet sourceSet) {
                        // for each source set we will:
                        // 1) Add a new 'antlr' virtual directory mapping
                        final AntlrSourceVirtualDirectoryImpl antlrDirectoryDelegate
                                = new AntlrSourceVirtualDirectoryImpl(((DefaultSourceSet) sourceSet).getDisplayName(),
                                projectInternal.getFileResolver());
                        ((DynamicObjectAware) sourceSet).getConvention().getPlugins().put(
                                AntlrSourceVirtualDirectory.NAME, antlrDirectoryDelegate);
                        final String srcDir = String.format("src/%s/antlr", sourceSet.getName());
                        antlrDirectoryDelegate.getAntlr().srcDir(srcDir);
                        sourceSet.getAllSource().add(antlrDirectoryDelegate.getAntlr());

                        // 2) create an AntlrTask for this sourceSet following the gradle
                        //    naming conventions via call to sourceSet.getTaskName()
                        final String taskName = sourceSet.getTaskName("generate", "GrammarSource");
                        AntlrTask antlrTask = project.getTasks().add(taskName, AntlrTask.class);
                        antlrTask.setDescription(String.format("Processes the %s Antlr grammars.",
                                sourceSet.getName()));

                        // 3) set up convention mapping for default sources (allows user to not have to specify)
                        antlrTask.conventionMapping("defaultSource", new ConventionValue() {
                            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                                return antlrDirectoryDelegate.getAntlr();
                            }
                        });

                        // 4) set up convention mapping for handling the 'antlr' dependency configuration
                        antlrTask.getConventionMapping().map("antlrClasspath", new ConventionValue() {
                            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                                return project.getConfigurations().getByName(ANTLR_CONFIGURATION_NAME).copy()
                                        .setTransitive(true);
                            }
                        });

                        // 5) Set up the Antlr output directory (adding to javac inputs!)
                        final String outputDirectoryName = String.format("%s/generated-src/antlr/%s",
                                project.getBuildDir(), sourceSet.getName());
                        final File outputDirectory = new File(outputDirectoryName);
                        antlrTask.setOutputDirectory(outputDirectory);
                        sourceSet.getJava().srcDir(outputDirectory);

                        // 6) register fact that antlr should be run before compiling
                        project.getTasks().getByName(sourceSet.getCompileJavaTaskName()).dependsOn(taskName);
                    }
                });
    }
}
