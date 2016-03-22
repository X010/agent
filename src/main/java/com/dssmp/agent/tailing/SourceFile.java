package com.dssmp.agent.tailing;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@EqualsAndHashCode(exclude = {"pathMatcher"})
public class SourceFile {

    @Getter
    private final FileFlow<?> flow;
    @Getter
    private final Path directory;
    @Getter
    private final Path filePattern;
    private final PathMatcher pathMatcher;

    public SourceFile(FileFlow<?> flow, String filePattern) {
        this.flow = flow;
        // fileName
        Preconditions.checkArgument(!filePattern.endsWith("/"), "File name component is empty!");
        Path filePath = FileSystems.getDefault().getPath(filePattern);
        // TODO: this does not handle globs in directory component: e.g. /opt/*/logs/app.log, /opt/**/app.log*
        this.directory = filePath.getParent();
        validateDirectory(this.directory);
        this.filePattern = filePath.getFileName();
        this.pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + this.filePattern.toString());
    }

    /**
     * @return List of {@link Path} objects contained in the given directory
     * and that match the file name pattern, sorted by {@code lastModifiedTime}
     * descending (newest at the top). An empty list is returned if
     * {@link #directory} does not exist, or if there are no files
     * that match the pattern.
     * @throws IOException If there was an error reading the directory or getting
     *                     the {@code lastModifiedTime} of a directory. Note that if
     *                     the {@link #directory} doesn't exist no exception will be thrown
     *                     but an empty list is returned instead.
     */
    public TrackedFileList listFiles() throws IOException {
        if (!Files.exists(this.directory))
            return TrackedFileList.emptyList();

        List<TrackedFile> files = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(this.directory)) {
            for (Path p : directoryStream) {
                if (this.pathMatcher.matches(p.getFileName())) {
                    files.add(new TrackedFile(flow, p));
                }
            }
        }
        // sort the files by decsending last modified time and return
        Collections.sort(files, new TrackedFile.NewestFirstComparator());
        return new TrackedFileList(files);
    }

    /**
     * @return The number of files on the file system that match the given input
     * pattern. More lightweight than {@link #listFiles()}.
     * @throws IOException If there was an error reading the directory or getting
     *                     the {@code lastModifiedTime} of a directory. Note that if
     *                     the {@link #directory} doesn't exist no exception will be thrown
     *                     but {@code 0} is returned instead.
     */
    public int countFiles() throws IOException {
        int count = 0;
        if (Files.exists(this.directory)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(this.directory)) {
                for (Path p : directoryStream) {
                    if (this.pathMatcher.matches(p.getFileName())) {
                        ++count;
                    }
                }
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return this.directory + "/" + this.filePattern;
    }

    /**
     * Performs basic validation on the directory parameter making sure it fits
     * within the supported functionality of this class.
     *
     * @param dir
     */
    private void validateDirectory(Path dir) {
        Preconditions.checkArgument(dir != null, "Directory component is empty!");
        // TODO: validate that the directory component has no glob characters
    }
}
