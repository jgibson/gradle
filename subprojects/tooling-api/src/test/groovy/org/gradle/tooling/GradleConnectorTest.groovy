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
package org.gradle.tooling

import spock.lang.Specification
import org.gradle.tooling.internal.consumer.ConnectionFactory
import org.gradle.tooling.internal.consumer.DistributionFactory
import org.gradle.tooling.internal.consumer.Distribution

class GradleConnectorTest extends Specification {
    final ConnectionFactory connectionFactory = Mock()
    final DistributionFactory distributionFactory = Mock()
    final Distribution distribution = Mock()
    final File projectDir = new File('project-dir')
    final GradleConnector connector = new GradleConnector(connectionFactory, distributionFactory)

    def canCreateAConnectionGivenAProjectDirectory() {
        GradleConnection connection = Mock()

        when:
        def result = connector.forProjectDirectory(projectDir).connect()

        then:
        result == connection
        1 * distributionFactory.currentDistribution >> distribution
        1 * connectionFactory.create(distribution, projectDir) >> connection
    }

    def canSpecifyAGradleInstallationToUse() {
        GradleConnection connection = Mock()
        File gradleHome = new File('install-dir')

        when:
        def result = connector.useInstallation(gradleHome).forProjectDirectory(projectDir).connect()

        then:
        result == connection
        1 * distributionFactory.getDistribution(gradleHome) >> distribution
        1 * connectionFactory.create(distribution, projectDir) >> connection
    }

    def canSpecifyAGradleDistributionToUse() {
        GradleConnection connection = Mock()
        URI gradleDist = new URI('http://server/dist.zip')

        when:
        def result = connector.useDistribution(gradleDist).forProjectDirectory(projectDir).connect()

        then:
        result == connection
        1 * distributionFactory.getDistribution(gradleDist) >> distribution
        1 * connectionFactory.create(distribution, projectDir) >> connection
    }

    def canSpecifyAGradleVersionToUse() {
        GradleConnection connection = Mock()

        when:
        def result = connector.useGradleVersion('1.0').forProjectDirectory(projectDir).connect()

        then:
        result == connection
        1 * distributionFactory.getDistribution('1.0') >> distribution
        1 * connectionFactory.create(distribution, projectDir) >> connection
    }

    def mustSpecifyAProjectDirectory() {
        when:
        connector.connect()

        then:
        IllegalStateException e = thrown()
        e.message == 'A project directory must be specified before creating a connection.'
    }

    def stopsConnectionFactoryWhenClosed() {
        when:
        connector.close()

        then:
        1 * connectionFactory.stop()
    }
}
