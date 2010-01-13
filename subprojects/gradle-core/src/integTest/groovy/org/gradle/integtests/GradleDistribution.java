/*
 * Copyright 2009 the original author or authors.
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

package org.gradle.integtests;

import org.gradle.util.TestFile;

import java.io.File;

public interface GradleDistribution {
    /**
     * The user home dir used for the current test. This is usually shared with other tests.
     */
    TestFile getUserHomeDir();

    /**
     * The distribution for the current test. This is usually shared with other tests.
     */
    TestFile getGradleHomeDir();

    /**
     * The samples from the distribution.
     */
    TestFile getSamplesDir();

    TestFile getUserGuideInfoDir();

    TestFile getUserGuideOutputDir();

    /**
     * The directory containing the distribution Zips
     */
    TestFile getDistributionsDir();

    /**
     * Returns true if the given file is either part of the distributions, samples, or test files.
     */
    boolean isFileUnderTest(File file);

    /**
     * Returns a scratch-pad directory for the current test.
     */
    TestFile getTestDir();

    /**
     * Returns a scratch-pad file for the current test. Equivalent to getTestDir().file(path)
     */
    TestFile testFile(Object... path);
}