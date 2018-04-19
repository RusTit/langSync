package com.rustit;

import java.lang.*;
import java.io.*;
import java.util.*;

import javafx.util.*;

public class Statistic {
    String filePath;
    int templateRows;
    int beforeRows;
    int afterRows;
    int equalRows;
    int emptyRowsMismatch;
    int missingRowsKeyMismatch;
    int insertedRows;

    private List<Pair<String, String>> unknownKeys = new ArrayList<>();

    void addUnknownKeys(final List<Pair<String, String>> pairs) {
        unknownKeys.clear();
        for (int index = beforeRows; index < afterRows; ++index) {
            unknownKeys.addAll(pairs);
        }
    }

    void save(final File file) {
        try (final FileWriter writer = new FileWriter(file, false)) {
            writer.write(this.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        final String NewLine = System.lineSeparator();
        stringBuilder.append(String.format("Statistic:%s", NewLine));
        stringBuilder.append(String.format("Path: %s%s", filePath, NewLine));
        stringBuilder.append(String.format("Template rows: %d%s", this.templateRows, NewLine));
        stringBuilder.append(String.format("Before rows: %d%s", this.beforeRows, NewLine));
        stringBuilder.append(String.format("After rows: %d%s", this.afterRows, NewLine));
        stringBuilder.append(String.format("Equal rows: %d%s", this.equalRows, NewLine));
        stringBuilder.append(String.format("Empty rows: %d%s", this.emptyRowsMismatch, NewLine));
        stringBuilder.append(String.format("Missing rows: %d%s", this.missingRowsKeyMismatch, NewLine));
        stringBuilder.append(String.format("Inserted rows: %d%s", this.insertedRows, NewLine));
        if (!unknownKeys.isEmpty()) {
            stringBuilder.append(String.format("Unknown keys:%s", NewLine));
            for (Pair<String, String> pair : unknownKeys) {
                stringBuilder.append(String.format("%s%s%s", pair.getKey(), (pair.getValue() != null) ? ("=" + pair.getValue()) : "", NewLine));
            }
        }
        return stringBuilder.toString();
    }
}
