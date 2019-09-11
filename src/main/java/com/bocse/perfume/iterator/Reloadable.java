package com.bocse.perfume.iterator;

import java.io.File;
import java.io.IOException;

/**
 * Created by bogdan.bocse on 25/07/16.
 */
public interface Reloadable {
    void iterateAndKeep(File file) throws IOException;

    boolean swap();

    void mirror();

    boolean cleanupBackground();

    Boolean shouldReload(File file);
}
