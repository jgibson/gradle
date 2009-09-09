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
package org.gradle.foundation;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.foundation.ipc.gradle.ExecuteGradleCommandServerProtocol;
import org.gradle.gradleplugin.foundation.DOM4JSerializer;
import org.gradle.gradleplugin.foundation.GradlePluginLord;
import org.gradle.gradleplugin.foundation.request.Request;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for initializing various test objects related.
 *
 * @author mhunsicker
 */
public class TestUtility {
    private static long uniqueNameCounter = 1; //used to make unique names for JMock objects.

    /**
       Creates a mock project with the specified properties.

       Note: depth is 0 for a root project. 1 for a root project's subproject, etc.
    */
    public static Project createMockProject(JUnit4Mockery context, final String name, final String buildFilePath, final int depth, Project[] subProjectArray, Task[] tasks, String[] defaultTasks, Project... dependsOnProjects) {
        final Project project = context.mock(Project.class, "[project]_" + name + '_' + uniqueNameCounter++);

        context.checking(new Expectations() {{
            allowing(project).getName();
            will(returnValue(name));
            allowing(project).getBuildFile();
            will(returnValue(new File(buildFilePath)));
            allowing(project).getDepth();
            will(returnValue(depth));
        }});

        attachSubProjects(context, project, subProjectArray);
        attachTasks(context, project, tasks);
        assignDefaultTasks(context, project, defaultTasks);
        assignDependsOnProjects(context, project, dependsOnProjects);

        return project;
    }

    /**
       This makes the sub projects children of the parent project.
       If you call this repeatedly on the same parentProject, any previous
       sub projects will be replaced with the new ones.

       @param  context         the mock context
       @param  parentProject   where to attach the sub projects. This must be a mock object.
       @param  subProjectArray the sub projects to attach to the parent. These must be mock objects.
                               Pass in null or an empty array to set no sub projects.
    */
    public static void attachSubProjects(JUnit4Mockery context, final Project parentProject, Project... subProjectArray) {
        final Set<Project> set = new LinkedHashSet<Project>();   //using a LinkedHashSet rather than TreeSet (which is what gradle uses) so I don't have to deal with compareTo() being called on mock objects.

        if (subProjectArray != null && subProjectArray.length != 0) {
            set.addAll(Arrays.asList(subProjectArray));

            //set the parent project of the sub projects
            for (int index = 0; index < subProjectArray.length; index++) {
                final Project subProject = subProjectArray[index];

                context.checking(new Expectations() {{
                    allowing(subProject).getParent();
                    will(returnValue(parentProject));
                }});
            }
        }

        //populate the subprojects (this may be an empty set)
        context.checking(new Expectations() {{
            allowing(parentProject).getSubprojects();
            will(returnValue(set));
        }});
    }

    /**
       Creates a mock task with the specified properites.
    */
    public static Task createTask(JUnit4Mockery context, final String name, final String description) {
        final Task task = context.mock(Task.class, "[task]_" + name + '_' + uniqueNameCounter++);

        context.checking(new Expectations() {{
            allowing(task).getName();
            will(returnValue(name));
            allowing(task).getDescription();
            will(returnValue(description));
        }});

        return task;
    }

    /**
       This makes the tasks children of the parent project.
       If you call this repeatedly on the same parentProject, any previous tasks
       will be replaced with the new ones.

       @param  context       the mock context
       @param  parentProject where to attach the sub projects. This must be a mock object.
       @param  taskArray     the tasks to attach to the parent. these must be mock objects.
                             Pass in null or an empty array to set no tasks.
    */
    public static void attachTasks(JUnit4Mockery context, final Project parentProject, Task... taskArray) {
        //first, make our project return our task container
        final TaskContainer taskContainer = context.mock(TaskContainer.class, "[taskcontainer]_" + parentProject.getName() + '_' + uniqueNameCounter++);

        context.checking(new Expectations() {{
            allowing(parentProject).getTasks();
            will(returnValue(taskContainer));
        }});

        final Set<Task> set = new LinkedHashSet<Task>();   //using a LinkedHashSet rather than TreeSet (which is what gradle uses) so I don't have to deal with compareTo() being called on mock objects.

        if (taskArray != null && taskArray.length != 0) {
            set.addAll(Arrays.asList(taskArray));

            //set the parent project of the tasks
            for (int index = 0; index < taskArray.length; index++) {
                final Task task = taskArray[index];

                context.checking(new Expectations() {{
                    allowing(task).getProject();
                    will(returnValue(parentProject));
                }});
            }
        }

        //populate the task container (this may be an empty set)
        context.checking(new Expectations() {{
            allowing(taskContainer).getAll();
            will(returnValue(set));
        }});
    }

    private static void assignDefaultTasks(JUnit4Mockery context, final Project project, final String... defaultTasksArray) {
        final List<String> defaultTaskList = new ArrayList<String>();

        if (defaultTasksArray != null && defaultTasksArray.length != 0)
            defaultTaskList.addAll(Arrays.asList(defaultTasksArray));

        context.checking(new Expectations() {{
            allowing(project).getDefaultTasks();
            will(returnValue(defaultTaskList));
        }});
    }

    private static void assignDependsOnProjects(JUnit4Mockery context, final Project project, final Project... dependsOnProjects) {
        final Set<Project> set = new LinkedHashSet<Project>();   //using a LinkedHashSet rather than TreeSet (which is what gradle uses) so I don't have to deal with compareTo() being called on mock objects.

        if (dependsOnProjects != null && dependsOnProjects.length != 0)
            set.addAll(Arrays.asList(dependsOnProjects));

        //populate the subprojects (this may be an empty set)
        context.checking(new Expectations() {{
            allowing(project).getDependsOnProjects();
            will(returnValue(set));
        }});
    }

    public static <T> void assertListContents(List<T> actualObjects, T... expectedObjectsArray) {
        assertListContents(actualObjects, Arrays.asList(expectedObjectsArray));
    }

    public static <T> void assertListContents(List<T> actualObjects, List<T> expectedObjects) {
        assertUnorderedListContents(actualObjects, expectedObjects);
    }

    /**
       This asserts the contents of the list are as expected. The important
       aspect of this function is that we don't care about ordering. We just
       want to make sure the contents are the same.

       @param  actualObjecs    the list to check
       @param  expectedObjects what we expect in the list
    */
    public static <T> void assertUnorderedListContents(List<T> actualObjecs, List<T> expectedObjects) {
        List<T> expectedObjecsList = new ArrayList<T>(expectedObjects);   //make a copy of it, so we can modify it.

        while (!expectedObjecsList.isEmpty()) {
            T expectedObject = expectedObjecsList.remove(0);

            if (!actualObjecs.contains(expectedObject))
                throw new AssertionFailedError("Failed to locate object. Sought object:\n" + expectedObject + "\n\nExpected:\n" + dumpList(expectedObjects) + "\nActual:\n" + dumpList(actualObjecs));
        }

        if (actualObjecs.size() != expectedObjects.size())
            throw new AssertionFailedError("Expected " + expectedObjects.size() + " items but found " + actualObjecs.size() + "\nExpected:\n" + dumpList(expectedObjects) + "\nActual:\n" + dumpList(actualObjecs));
    }

    //function for getting a prettier dump of a list.
    public static String dumpList(List list) {
        if (list == null)
            return "[null]";
        if (list.isEmpty())
            return "[empty]";

        StringBuilder builder = new StringBuilder();
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object == null)
                builder.append("**** [null object in list] ****\n");
            else
                builder.append(object.toString()).append('\n');
        }

        return builder.toString();
    }

    /**
       This is an ExportInteraction implemention meant to be used by tests.
       You pass it a file to use and we'll return that in promptForFile. This also
       checks to ensure something doesn't happen where we get into an endless loop
       if promptForFile is called repeatedly. This can happen if promptForFile is
       called and its return value fails some form of validation which makes
       promptForFile get called again or if you deny overwriting the file. You'll
       get prompted again.
    */
    public static class TestExportInteraction implements DOM4JSerializer.ExportInteraction {
        private File file;
        private boolean confirmOverwrite;
        private int promptCount = 0;

        public TestExportInteraction(File file, boolean confirmOverwrite) {
            this.file = file;
            this.confirmOverwrite = confirmOverwrite;
        }

        public File promptForFile(FileFilter fileFilters) {
            if (promptCount == 100)
                throw new AssertionFailedError("Possible endless loop. PromptForFile has been called 100 times.");

            promptCount++;
            return file;
        }

        /**
        The file already exists. Confirm whether or not you want to overwrite it.

        @param file the file in question
        @return true to overwrite it, false not to.
        */
        public boolean confirmOverwritingExisingFile(File file) {
            return confirmOverwrite;
        }

        public void reportError(String error) {
            throw new AssertionFailedError("Unexpected error: " + error);
        }
    }

    /**
       This is an ImportInteraction implemention meant to be used by tests.
       See TestExportInteraction for more information.
    */
    public static class TestImportInteraction implements DOM4JSerializer.ImportInteraction {
        private File file;
        private int promptCount = 0;

        public TestImportInteraction(File file) {
            this.file = file;
        }

        public File promptForFile(FileFilter fileFilters) {
            if (promptCount == 100)
                throw new AssertionFailedError("Possible endless loop. PromptForFile has been called 100 times.");

            promptCount++;
            return file;
        }

        public void reportError(String error) {
            throw new AssertionFailedError("Unexpected error: " + error);
        }
    }

    //wrapper around File.createTempFile just so we don't have to deal with the exception for tests.
    public static File createTemporaryFile(String prefix, String suffix) {
        try {
            return File.createTempFile(prefix, suffix);
        }
        catch (IOException e) {
            throw new AssertionFailedError("Unexpected exception: " + e);
        }
    }

    /**
       This refreshes the projects but blocks until it is complete (its being
       executed in a separate process).

       @param  gradlePluginLord     the plugin lord (will be used to execute the
                                    command and store the results).
       @param  executionInteraction provides feedback about the execution.
       @param  maximumWaitSeconds   how many seconds to wait before considering
                                    this a failure.
    */
    public static void refreshProjectsBlocking(GradlePluginLord gradlePluginLord, int maximumWaitSeconds) {
        refreshProjectsBlocking(gradlePluginLord, new ExecuteGradleCommandServerProtocol.ExecutionInteraction() {
            public void reportExecutionStarted() {
            }

            public void reportExecutionFinished(boolean wasSuccessful, String message, Throwable throwable) {
            }

            public void reportTaskStarted(String message, float percentComplete) {
            }

            public void reportTaskComplete(String message, float percentComplete) {
            }

            public void reportLiveOutput(String message) {
            }
        }, maximumWaitSeconds);
    }

    public static void refreshProjectsBlocking(GradlePluginLord gradlePluginLord, final ExecuteGradleCommandServerProtocol.ExecutionInteraction executionInteraction, int maximumWaitSeconds) {
        gradlePluginLord.startExecutionQueue();   //make sure its started

        final BooleanHolder isComplete = new BooleanHolder();

        Request request = gradlePluginLord.addRefreshRequestToQueue(new ExecuteGradleCommandServerProtocol.ExecutionInteraction() {
            public void reportExecutionStarted() {
                executionInteraction.reportExecutionStarted();
            }

            public void reportExecutionFinished(boolean wasSuccessful, String message, Throwable throwable) {
                executionInteraction.reportExecutionFinished(wasSuccessful, message, throwable);
                isComplete.value = true;
            }

            public void reportTaskStarted(String message, float percentComplete) {
                executionInteraction.reportTaskStarted(message, percentComplete);
            }

            public void reportTaskComplete(String message, float percentComplete) {
                executionInteraction.reportTaskComplete(message, percentComplete);
            }

            public void reportLiveOutput(String message) {
                executionInteraction.reportLiveOutput(message);
            }
        });

        //make sure we've got a request
        Assert.assertNotNull(request);

        //now sleep until we're complete, but bail if we wait too long
        int totalWaitTime = 0;
        while (!isComplete.value && totalWaitTime <= maximumWaitSeconds) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            totalWaitTime += 1;
        }

        if (!isComplete.value) //its still running. Something is wrong.
        {
            request.cancel(); //just to clean up after ourselves a little, cancel the request.
            throw new AssertionFailedError("Failed to complete refresh in alotted time: " + maximumWaitSeconds + " seconds. Considering this failed.");
        }
    }

    private static class BooleanHolder {
        private boolean value = false;
    }
}
