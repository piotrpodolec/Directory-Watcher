package com.pp;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DirectoryWatcher {
    final static Logger logger = LogManager.getLogger(Main.class);
    private final String HOME = "home";
    private final String DEV = "dev";
    private final String TEST = "test";
    private final String JAR_EXTENSION = ".jar";
    private final String XML_EXTENSION = ".xml";
    private final String COUNT_FILE_PATH = HOME + File.separator +"count.txt";
    private final int CHECKING_INTERVAL = 1000;
    private final ReentrantLock lock = new ReentrantLock();

    public DirectoryWatcher() throws IOException {
        createDirectory(HOME);
        createDirectory(DEV);
        createDirectory(TEST);
        createCountingFile();
    }

    private static void createDirectory(String dir) {
        File newDir = new File(dir);
        if (! newDir.exists()){
            newDir.mkdir();
        }
    }

    private void createCountingFile() throws IOException {
        File countFile = new File(COUNT_FILE_PATH);
        countFile.createNewFile();
        if (countFile.length() == 0) {
            Path path = Paths.get(COUNT_FILE_PATH);
            Files.writeString(path, "0");
        }
    }

    public void watchForFilesInHome() throws IOException, InterruptedException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path home = Paths.get(HOME);
        home.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        WatchKey key;
        while (true) {
            if ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    moveFile(event.context().toString());
                }
                key.reset();
            } else {
                Thread.sleep(CHECKING_INTERVAL);
            }
        }
    }

    private void moveFile(String fileName) throws IOException {
        Path path = Paths.get(HOME, fileName);

        if (fileName.endsWith(XML_EXTENSION)) {
            Files.move(path, Paths.get(DEV, fileName));
            increaseFileCount();
            return;
        }

        if (fileName.endsWith(JAR_EXTENSION)) {
            BasicFileAttributes fileAttributes = Files.readAttributes(path.toAbsolutePath(), BasicFileAttributes.class);
            if (fileAttributes.creationTime().to(TimeUnit.HOURS) % 2 == 0) {
                Files.move(path, Paths.get(DEV, fileName));
            } else {
                Files.move(path, Paths.get(TEST, fileName));
            }
            increaseFileCount();
        }
    }

    private void increaseFileCount() {
        Path path = Paths.get(COUNT_FILE_PATH);
        lock.lock();
        try {
            int count = Integer.parseInt(Files.readString(path));
            count++;
            Files.writeString(path, Integer.toString(count));
        } catch (IOException e) {
            logger.error("Cannot read file: " + COUNT_FILE_PATH, e);
        } catch (NumberFormatException e) {
            logger.error("Content of this file cannot be parsed to number: " + COUNT_FILE_PATH, e);
        } finally {
            lock.unlock();
        }
    }
}
