/*
 * Copyright 2007-2008 the original author or authors.
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
 
package org.gradle;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Hans Dockter
 */
public class StartParameter {
    private String buildFileName;
    private List<String> taskNames;
    private File currentDir;
    private boolean recursive;
    private boolean searchUpwards;
    private Map projectProperties = new HashMap();
    private Map systemPropertiesArgs = new HashMap();
    private File gradleUserHomeDir;
    private File defaultImportsFile;
    private File pluginPropertiesFile;
    private boolean useCache;

    public StartParameter() {
    }

    public StartParameter(String buildFileName, List<String> taskNames, File currentDir, boolean recursive, boolean searchUpwards, Map projectProperties, Map systemPropertiesArgs, File gradleUserHomeDir, File defaultImportsFile, File pluginPropertiesFile, boolean useCache) {
        this.buildFileName = buildFileName;
        this.taskNames = taskNames;
        this.currentDir = currentDir;
        this.recursive = recursive;
        this.searchUpwards = searchUpwards;
        this.projectProperties = projectProperties;
        this.systemPropertiesArgs = systemPropertiesArgs;
        this.gradleUserHomeDir = gradleUserHomeDir;
        this.defaultImportsFile = defaultImportsFile;
        this.pluginPropertiesFile = pluginPropertiesFile;
        this.useCache = useCache;
    }

    public static StartParameter newInstance(StartParameter startParameterSrc) {
        StartParameter startParameter = new StartParameter();
        startParameter.buildFileName = startParameterSrc.buildFileName;
        startParameter.taskNames = startParameterSrc.taskNames;
        startParameter.currentDir = startParameterSrc.currentDir;
        startParameter.recursive = startParameterSrc.recursive;
        startParameter.searchUpwards = startParameterSrc.searchUpwards;
        startParameter.projectProperties = startParameterSrc.projectProperties;
        startParameter.systemPropertiesArgs = startParameterSrc.systemPropertiesArgs;
        startParameter.gradleUserHomeDir = startParameterSrc.gradleUserHomeDir;
        startParameter.defaultImportsFile = startParameterSrc.defaultImportsFile;
        startParameter.pluginPropertiesFile = startParameterSrc.pluginPropertiesFile;
        startParameter.useCache = startParameterSrc.useCache;

        return startParameter;
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String getBuildFileName() {
        return buildFileName;
    }

    public void setBuildFileName(String buildFileName) {
        this.buildFileName = buildFileName;
    }

    public List<String> getTaskNames() {
        return taskNames;
    }

    public void setTaskNames(List<String> taskNames) {
        this.taskNames = taskNames;
    }

    public File getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(File currentDir) {
        this.currentDir = currentDir;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isSearchUpwards() {
        return searchUpwards;
    }

    public void setSearchUpwards(boolean searchUpwards) {
        this.searchUpwards = searchUpwards;
    }

    public Map getProjectProperties() {
        return projectProperties;
    }

    public void setProjectProperties(Map projectProperties) {
        this.projectProperties = projectProperties;
    }

    public Map getSystemPropertiesArgs() {
        return systemPropertiesArgs;
    }

    public void setSystemPropertiesArgs(Map systemPropertiesArgs) {
        this.systemPropertiesArgs = systemPropertiesArgs;
    }

    public File getGradleUserHomeDir() {
        return gradleUserHomeDir;
    }

    public void setGradleUserHomeDir(File gradleUserHomeDir) {
        this.gradleUserHomeDir = gradleUserHomeDir;
    }

    public File getDefaultImportsFile() {
        return defaultImportsFile;
    }

    public void setDefaultImportsFile(File defaultImportsFile) {
        this.defaultImportsFile = defaultImportsFile;
    }

    public File getPluginPropertiesFile() {
        return pluginPropertiesFile;
    }

    public void setPluginPropertiesFile(File pluginPropertiesFile) {
        this.pluginPropertiesFile = pluginPropertiesFile;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }
}
