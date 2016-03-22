package com.dssmp.agent.tailing;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

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
@EqualsAndHashCode
public class FileId {
    @Getter
    private final String id;

    /**
     * @see #get(BasicFileAttributes)
     */
    public static FileId get(Path file) throws IOException {
        if (!Files.exists(file)) {
            return null;
        }
        Preconditions.checkArgument(Files.isRegularFile(file),
                "Can only get ID for real files (no directories and symlinks): "
                        + file);
        BasicFileAttributes attr = Files.readAttributes(file,
                BasicFileAttributes.class);
        return get(attr);
    }

    /**
     * TODO: this might not be portable as we rely on inner representation of
     * @param attr
     * @return
     * @throws IOException
     */
    public static FileId get(BasicFileAttributes attr) throws IOException {
        return new FileId(attr.fileKey().toString());
    }

    public FileId(String id) {
        Preconditions.checkNotNull(id);
        this.id = id;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
