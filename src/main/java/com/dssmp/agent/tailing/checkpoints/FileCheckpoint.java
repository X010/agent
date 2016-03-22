package com.dssmp.agent.tailing.checkpoints;

import com.dssmp.agent.tailing.FileId;
import com.dssmp.agent.tailing.TrackedFile;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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
@ToString
@EqualsAndHashCode(exclude = {"file"})
public class FileCheckpoint {
    @Getter
    private final TrackedFile file;
    @Getter
    private final long offset;
    @Getter
    private final FileId fileId;
    @Getter
    private final String flowId;

    public TrackedFile getFile() {
        return file;
    }

    public long getOffset() {
        return offset;
    }

    public FileId getFileId() {
        return fileId;
    }

    public String getFlowId() {
        return flowId;
    }

    public FileCheckpoint(TrackedFile file, long offset) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(offset >= 0, "The offset (%s) must be a non-negative integer", offset);
        this.file = file;
        this.offset = offset;
        fileId = file.getId();
        flowId = file.getFlow().getId();
    }

}
