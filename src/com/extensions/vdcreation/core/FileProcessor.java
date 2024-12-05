package com.extensions.vdcreation.core;

import java.io.File;

/**
 * A functional interface for processing a file.
 */
public interface FileProcessor<T> {
    public T process(File file) throws Exception;
}
