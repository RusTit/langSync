package com.rustit;

import com.sun.istack.internal.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

class FolderProcessor {
    private FolderProcessor() {
        path = EMPTY_STRING;
        System.out.println("Default folder processor created. Using current directory");
    }

    private FolderProcessor(final String folderPath) {
        path = folderPath;
        System.out.printf("Folder processor: %s%s", folderPath, System.lineSeparator());
    }

    private static final String EMPTY_STRING = "";
    private static final String FOLDER_KEY = "-f";
    private static final String READONLY_KEY = "-readonly";
    private static final String BACKUP_KEY = "-backup";
    private static boolean READ_ONLY = false;
    private static boolean BACKUP = false;

    private static final String LANG_EN_FILE = "en_US.lang";

    @Nullable
    private static String getAbsolutePath(@NotNull final Path path) {
        String result = null;
        try {
            result = path.toAbsolutePath().toString();
        } catch (java.io.IOError | SecurityException e) {
            System.out.printf("Absolute path error. %s%s", e.getMessage(), System.lineSeparator());
        }
        System.out.printf("Absolute path: %s%s", result, System.lineSeparator());
        return result;
    }

    @Nullable
    private Path getDirPath() {
        try {
            return Paths.get(path);
        } catch (InvalidPathException e) {
            System.out.printf("Cannot get path to directory. %s%s", e.getMessage(), System.lineSeparator());
            return null;
        }
    }
    private String path;
    private LangFile template;
    private File[] langFiles;

    public static FolderProcessor[] getProcessorsByArgs(String[] args) {
        List<FolderProcessor> list = new ArrayList<>();
        for (int index = 0; index < args.length; index++) {
            String arg = args[index].toLowerCase(Locale.ENGLISH);
            if (FOLDER_KEY.contains(arg) && (index + 1) < args.length) {
                FolderProcessor processor = new FolderProcessor(args[index + 1]);
                if (processor.tryInitialization()) {
                    list.add(processor);
                }
            } else if (READONLY_KEY.contains(arg)) {
                READ_ONLY = true;
            } else if (BACKUP_KEY.contains(arg)) {
                BACKUP = true;
            }
        }
        if (list.isEmpty()) {
            FolderProcessor defaultProcessor = new FolderProcessor();
            if (defaultProcessor.tryInitialization()) {
                list.add(defaultProcessor);
            }
        }

        return list.toArray(new FolderProcessor[0]);
    }

    @Nullable
    private static File getFileByAbsolutePath(@NotNull String path) {
        return new File(path);
    }

    private static File[] getLangFiles(@NotNull File folder) {
        FileFilter filter = pathname -> pathname.getName().endsWith(".lang") && pathname.exists() && pathname.canRead() && pathname.canWrite() && pathname.isFile();
        return folder.listFiles(filter);
    }

    private LangFile[] processInputFiles(final File[] inputFiles) {
        List<LangFile> langfiles = new ArrayList<>(inputFiles.length);
        for (final File file : inputFiles) {
            LangFile langFile = new LangFile(template.getCount());
            langFile.readFile(file);
            langfiles.add(langFile);
        }
        return langfiles.toArray(new LangFile[0]);
    }

    private File[] setTemplate(final File[] files) {
        List<File> langfiles = new ArrayList<>(files.length);
        for (File file : files) {
            String name = file.getName();
            if (name.endsWith(LANG_EN_FILE)) {
                template = new LangFile();
                template.readFile(file);
                continue;
            }
            langfiles.add(file);
        }
        return langfiles.toArray(new File[0]);
    }

    private boolean processFolder(@Nullable File folder) {
        if ((folder == null) || (!folder.isDirectory())) {
            return false;
        }

        langFiles = setTemplate(getLangFiles(folder));

        if (template == null) {
            System.out.printf("Template not found %s", System.lineSeparator());
            return false;
        }

        return true;
    }

    private boolean tryInitialization() {
        Path directory = getDirPath();
        if (directory == null) {
            return false;
        }
        String absPath = getAbsolutePath(directory);
        if (absPath == null) {
            return false;
        }
        File folder = getFileByAbsolutePath(absPath);
        return processFolder(folder);
    }

    void process() {
        process(READ_ONLY, BACKUP);
    }

    private void process(boolean readOnly, boolean backup) {
        System.out.printf("Processing (%s), readonly %s, BACKUP %s%s", path, readOnly, backup, System.lineSeparator());
        LangFile[] lng = processInputFiles(langFiles);

        for (LangFile l : lng) {
            l.update(template, readOnly);
            if (!readOnly) {
                l.writeFile(backup);
            }
        }
    }
}
