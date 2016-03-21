package com.dssmp.agent.config;

import com.beust.jcommander.*;
import com.google.common.base.Joiner;
import com.google.common.collect.Range;
import lombok.Getter;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;

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
@Parameters(separators = "=")
public class AgentOptions {


    private static final String DEFAULT_CONFIG_FILE = "/etc/agent/agent.json";
    private static final String[] VALID_LOG_LEVELS = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"};

    @Parameter(names = {"--configuration", "-c"}, description = "Path to the configuration file for the agent.", validateWith = FileReadableValidator.class)
    @Getter
    String configFile = DEFAULT_CONFIG_FILE;

    @Parameter(names = {"--log-file", "-l"}, description = "Path to the agent's log file.")
    @Getter
    String logFile = null;

    @Parameter(names = {"--log-level", "-L"}, description = "Log level. Can be one of: TRACE,DEBUG,INFO,WARN,ERROR.", validateWith = LogLevelValidator.class)
    @Getter
    String logLevel = null;

    @Parameter(names = {"--help", "-h"}, help = true, description = "Display this help message")
    Boolean help;

    public static AgentOptions parse(String[] args) {
        AgentOptions opts = new AgentOptions();
        JCommander jc = new JCommander(opts);
        jc.setProgramName("aws-kinesis-agent");
        try {
            jc.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            jc.usage();
            System.exit(1);
        }
        if (Boolean.TRUE.equals(opts.help)) {
            jc.usage();
            System.exit(0);
        }
        return opts;
    }

    public static class LongRangeValidator implements IParameterValidator {
        private final Range<Long> range;

        public LongRangeValidator(Range<Long> range) {
            this.range = range;
        }

        @Override
        public void validate(String name, String value)
                throws ParameterException {
            try {
                long longVal = Long.parseLong(value);
                if (!this.range.contains(longVal))
                    throw new ParameterException("Parameter " + name
                            + " should be within the range "
                            + this.range.toString() + ". Value " + value
                            + " is not valid.");
            } catch (NumberFormatException e) {
                throw new ParameterException("Parameter " + name
                        + " is not a valid number: " + value);
            }

        }
    }

    public static class LogLevelValidator implements IParameterValidator {
        @Override
        public void validate(String name, String value)
                throws ParameterException {
            if (ArrayUtils.indexOf(VALID_LOG_LEVELS, value) < 0)
                throw new ParameterException("Valid values for parameter "
                        + name + " are: "
                        + Joiner.on(",").join(VALID_LOG_LEVELS) + ". Value "
                        + value + " is not valid.");
        }

    }

    public static class FileReadableValidator implements IParameterValidator {
        @Override
        public void validate(String name, String value)
                throws ParameterException {
            File f = new File(value);
            if (!f.exists()) {
                throw new ParameterException("Parameter " + name
                        + " points to a file that doesn't exist: " + value);
            }
            if (!f.canRead()) {
                throw new ParameterException("Parameter " + name
                        + " points to a file that's not accessible: " + value);
            }

        }

    }
}
