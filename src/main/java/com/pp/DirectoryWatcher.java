package com.pp;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

public class DirectoryWatcher {
    private final String HOME = "HOME";
    private final String DEV = "DEV";
    private final String TEST = "TEST";
    private final String COUNT_FILE_PATH = "HOME/count.txt";
    private final int CHECKING_INTERVAL = 1000;

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
        Path path = Paths.get(COUNT_FILE_PATH);
        try{
            Files.createFile(path);
            Files.writeString(path, "0");
        } catch (FileAlreadyExistsException e) {
            System.out.println(COUNT_FILE_PATH + " already exists.");
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
                    increaseFileCount();
                }
                key.reset();
            } else {
                Thread.sleep(CHECKING_INTERVAL);
            }
        }
    }

    private void moveFile(String fileName) throws IOException {
        Path path = Paths.get(HOME + "\\" + fileName);
        BasicFileAttributes fileAttributes = Files.readAttributes(path.toAbsolutePath(), BasicFileAttributes.class);
        if (fileAttributes.creationTime().to(TimeUnit.HOURS) % 2 == 0) {
            Files.move(path, Paths.get(DEV + "\\" + fileName));
        } else {
            Files.move(path, Paths.get(TEST + "\\" + fileName));
        }
    }

    private void increaseFileCount() {
        Path path = Paths.get(COUNT_FILE_PATH);
        try {
            int count = Integer.parseInt(Files.readString(path));
            count++;
            Files.writeString(path, Integer.toString(count));
        } catch (IOException e) {
            System.err.println("Cannot read file: " + COUNT_FILE_PATH);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Content of this file cannot be parsed to number: " + COUNT_FILE_PATH);
            e.printStackTrace();
        }
    }

}
