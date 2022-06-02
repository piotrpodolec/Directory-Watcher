package com.pp;

import java.io.IOException;

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
