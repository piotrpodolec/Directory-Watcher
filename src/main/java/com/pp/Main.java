package com.pp;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    public static void main(String[] args) {
        final Logger logger = LogManager.getLogger(Main.class);

        try {
            DirectoryWatcher directoryWatcher = new DirectoryWatcher();
            directoryWatcher.watchForFilesInHome();
        } catch (IOException | InterruptedException e) {
            logger.error(e);
        }
    }
}
