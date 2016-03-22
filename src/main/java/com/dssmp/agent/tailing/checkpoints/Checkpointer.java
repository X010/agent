package com.dssmp.agent.tailing.checkpoints;

import com.dssmp.agent.Logging;
import com.dssmp.agent.tailing.FileFlow;
import com.dssmp.agent.tailing.IRecord;
import com.dssmp.agent.tailing.RecordBuffer;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.slf4j.Logger;

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
public class Checkpointer<R extends IRecord> {
    private static final Logger LOGGER = Logging.getLogger(Checkpointer.class);

    @Getter
    private final FileCheckpointStore store;
    @Getter
    private final FileFlow<R> flow;
    private long committed = -1;

    public Checkpointer(FileFlow<R> flow, FileCheckpointStore store) {
        this.flow = flow;
        this.store = store;
    }

    /**
     * @param buffer The buffer that was sent and that triggered the checkpoint
     *               update.
     * @return The checkpoint that was created, or {@code null} if the buffer
     * would overwrite a previous checkpoint.
     */
    public synchronized FileCheckpoint saveCheckpoint(RecordBuffer<?> buffer) {
        // SANITYCHECK: Can remove when done with debugging
        Preconditions.checkArgument(buffer.id() != committed);
        // Only store the checkpoint if it has an increasing sequence number
        if (buffer.id() > committed) {
            committed = buffer.id();
            return store.saveCheckpoint(buffer.checkpointFile(), buffer.checkpointOffset());
        } else {
            LOGGER.trace("Buffer {} has lower sequence number than the last committed value {}. " +
                    "No checkpoints will be updated.", buffer, committed);
            // TODO: Add metrics?
            return null;
        }
    }
}
