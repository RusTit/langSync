package com.rustit;

public class Main {
    public static void main(String[] args) {
        System.out.println("App started:");
        FolderProcessor[] processors = FolderProcessor.getProcessorsByArgs(args);
        for (FolderProcessor processor : processors) {
            processor.process();
        }
        System.out.print("App finished.");
    }
}
