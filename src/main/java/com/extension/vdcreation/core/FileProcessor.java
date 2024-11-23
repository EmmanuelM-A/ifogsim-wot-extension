package com.extension.vdcreation.core;

import java.io.File;

/**
 * A functional interface for processing a file.
 */
public interface FileProcessor {
    public void process(File file) throws Exception;
}
