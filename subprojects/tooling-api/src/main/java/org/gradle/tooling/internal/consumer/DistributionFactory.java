/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.tooling.internal.consumer;

import org.gradle.StartParameter;
import org.gradle.api.internal.DefaultClassPathProvider;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.api.tasks.wrapper.internal.DistributionLocator;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.util.GradleVersion;
import org.gradle.util.UncheckedException;
import org.gradle.wrapper.Download;
import org.gradle.wrapper.Install;
import org.gradle.wrapper.PathAssembler;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

public class DistributionFactory {
    public Distribution getCurrentDistribution() {
        return new Distribution() {
            public Set<File> getToolingImplementationClasspath() {
                DefaultClassPathProvider provider = new DefaultClassPathProvider();
                return provider.findClassPath("GRADLE_RUNTIME");
            }
        };
    }

    public Distribution getDistribution(final File gradleHomeDir) {
        return new Distribution() {
            public Set<File> getToolingImplementationClasspath() {
                Set<File> files = new LinkedHashSet<File>();
                File libDir = new File(gradleHomeDir, "lib");
                for (File file : libDir.listFiles()) {
                    if (file.getName().endsWith(".jar")) {
                        files.add(file);
                    }
                }
                return files;
            }
        };
    }

    public Distribution getDistribution(String gradleVersion) {
        if (gradleVersion.equals(new GradleVersion().getVersion())) {
            return getCurrentDistribution();
        }
        URI distUri;
        try {
            distUri = new URI(new DistributionLocator().getDistributionFor(new GradleVersion(gradleVersion)));
        } catch (URISyntaxException e) {
            throw UncheckedException.asUncheckedException(e);
        }
        return getDistribution(distUri);
    }

    public Distribution getDistribution(URI gradleDistribution) {
        try {
            Install install = new Install(false, false, new Download(), new PathAssembler(StartParameter.DEFAULT_GRADLE_USER_HOME));
            File installDir = install.createDist(gradleDistribution, PathAssembler.GRADLE_USER_HOME_STRING, Wrapper.DEFAULT_DISTRIBUTION_PARENT_NAME, PathAssembler.GRADLE_USER_HOME_STRING, Wrapper.DEFAULT_DISTRIBUTION_PARENT_NAME);
            return getDistribution(installDir);
        } catch (Exception e) {
            throw new GradleConnectionException(String.format("Could not install Gradle distribution from '%s'.", gradleDistribution), e);
        }
    }
}
