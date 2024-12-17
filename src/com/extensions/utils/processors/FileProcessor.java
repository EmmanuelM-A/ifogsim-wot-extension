package com.extensions.utils.processors;

import java.io.File;

/**
 * A functional interface for processing a file.
 */
public interface FileProcessor<T> {
    public T process(File file) throws Exception;
}
