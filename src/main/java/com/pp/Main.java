package com.pp;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        try {
            DirectoryWatcher directoryWatcher = new DirectoryWatcher();
            directoryWatcher.watchForFilesInHome();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
