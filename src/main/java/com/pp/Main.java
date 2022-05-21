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
    private final static String HOME = "HOME";
    private final static String DEV = "DEV";
    private final static String TEST = "TEST";
    private final static String COUNT_FILE_PATH = "HOME/count.txt";
    private final static int CHECKING_INTERVAL = 1000;

    public static void main(String[] args) {
        try{
            createDirectory(HOME);
            createDirectory(DEV);
            createDirectory(TEST);
            createCountingFile();

            watchForFilesInHome();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void createDirectory(String dir) {
        File newDir = new File(dir);
        if (! newDir.exists()){
            newDir.mkdir();
        }
    }

    private static void createCountingFile() throws IOException {
        Path path = Paths.get(COUNT_FILE_PATH);
        try{
            Files.createFile(path);
            Files.writeString(path, "0");
        } catch (FileAlreadyExistsException e) {
            System.out.println(COUNT_FILE_PATH + " already exists.");
        }

    }

    private static void watchForFilesInHome() throws IOException, InterruptedException {
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

    private static void moveFile(String fileName) throws IOException {
        Path path = Paths.get(HOME + "\\" + fileName);
        BasicFileAttributes fileAttributes = Files.readAttributes(path.toAbsolutePath(), BasicFileAttributes.class);
        if (fileAttributes.creationTime().to(TimeUnit.HOURS) % 2 == 0) {
            Files.move(path, Paths.get(DEV + "\\" + fileName));
        } else {
            Files.move(path, Paths.get(TEST + "\\" + fileName));
        }
    }

    private static void increaseFileCount() {
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
