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
package org.gradle.api.internal.tasks.copy;

import org.gradle.api.internal.tasks.copy.pattern.PatternMatcher;
import org.gradle.api.internal.tasks.copy.pattern.PatternMatcherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Directory walker supporting {@link PatternMatcher}s for includes and excludes.
 * The file system is traversed breadth first - all files in a directory will be
 * visited before any child directory is visited.
 *
 * A file or directory will only be visited if it matches all includes and no
 * excludes.
 *
 * @author Steve Appling
 */
public class BreadthFirstDirectoryWalker implements DirectoryWalker {
    private static Logger logger = LoggerFactory.getLogger(BreadthFirstDirectoryWalker.class);

    private FileVisitor visitor;
    private List<PatternMatcher> includes;
    private List<PatternMatcher> excludes;
    private boolean caseSensitive;

    public BreadthFirstDirectoryWalker(boolean caseSensitive, FileVisitor visitor) {
        this.caseSensitive = caseSensitive;
        this.visitor = visitor;
        includes = new ArrayList<PatternMatcher>();
        excludes = new ArrayList<PatternMatcher>();
    }

    public void addIncludes(List<String> includes) {
        this.includes.clear();
        for (String include : includes) {
            this.includes.add(PatternMatcherFactory.getPatternMatcher(true, caseSensitive, include));
        }
    }

    public void addExcludes(List<String> excludes){
        this.excludes.clear();
        for (String exclude : excludes) {
            this.excludes.add(PatternMatcherFactory.getPatternMatcher(false, caseSensitive, exclude));
        }
    }

    /**
     * Process the specified file or directory.  Note that the startFile parameter
     * may be either a directory or a file.  If it is a directory, then it's contents
     * (but not the directory itself) will be checked with isAllowed and notified to
     * the listener.  If it is a file, the file will be checked and notified.
     * @param startFile
     * @throws IOException
     */
    public void start(File startFile) throws IOException {
        File root = startFile.getCanonicalFile();
        if (root.exists()) {
            if (root.isFile()) {
                processSingleFile(root);
            } else {
               // need to get appropriate start dirs from the includes
               walkDir(root, new RelativePath(false));
            }
        } else {
            logger.info("file or directory '"+startFile.toString()+"', not found");
        }
    }

    private void processSingleFile(File file) {
        RelativePath path = new RelativePath(true, file.getName());
        if (isAllowed(path)) {
            notifyFile(file, path);
        }
    }

    private void walkDir(File file, RelativePath path) {
        File[] children = file.listFiles();
        ArrayList<File> dirs = new ArrayList<File>();
        for (File child : children) {
            boolean isFile = child.isFile();
            RelativePath childPath = new RelativePath(isFile, path, child.getName());
            if (isAllowed(childPath)) {
                if (isFile) {
                    notifyFile(child, childPath);
                } else {
                    dirs.add(child);
                }
            }
        }

        // now handle dirs
        for (File dir : dirs) {
            RelativePath dirPath = new RelativePath(false, path, dir.getName());
            notifyDir(dir, dirPath);
            walkDir(dir, dirPath);
        }
    }

    boolean isAllowed(RelativePath path) {
        if (!includes.isEmpty()) {
            boolean accepted = false;
            for (PatternMatcher include : includes) {
                accepted = include.isSatisfiedBy(path);
                if (accepted) { 
                    break;
                }
            }
            if (!accepted) {
                return false;
            }
        }

        if (!excludes.isEmpty()) {
            for (PatternMatcher exclude : excludes) {
                if (exclude.isSatisfiedBy(path)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void notifyDir(File dir, RelativePath path) {
        visitor.visitDir(dir, path);
    }

    private void notifyFile(File file, RelativePath path) {
        visitor.visitFile(file, path);
    }
}