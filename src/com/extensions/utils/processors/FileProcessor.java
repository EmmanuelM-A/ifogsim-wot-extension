package com.extensions.utils.processors;

import java.io.File;
import java.io.IOException;

/**
 * A functional interface for processing a file.
 */
public interface FileProcessor<T> {
    T process(File file) throws IOException;
}
