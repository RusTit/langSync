package com.rustit;

import com.sun.istack.internal.*;
import javafx.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

public class LangFile {
    private static final Pattern translationPatter = Pattern.compile("(.+)=(.+)");

    private List<Pair<String, String>> rows;
    @Nullable
    private File lFile;

    int getCount() {
        return rows.size();
    }

    void readFile(@NotNull File file) {
        lFile = file;
        rows.clear();
        try (final FileReader reader = new FileReader(file)) {
            try (final Scanner scanner = new Scanner(reader)) {
                while (scanner.hasNextLine()) {
                    final String str = scanner.nextLine().trim();
                    Matcher matcher = translationPatter.matcher(str);
                    if (matcher.matches()) {
                        rows.add(new Pair<>(matcher.group(1), matcher.group(2)));
                    } else {
                        rows.add(new Pair<>(str, null));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void writeFile(boolean backup) {
        if (lFile == null) {
            return;
        }
        if (backup) {
            File backupFile = new File(lFile.getAbsolutePath() + ".backup");
            try {
                Files.copy(lFile.toPath(), backupFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try (final FileWriter writer = new FileWriter(lFile, false)) {
            final String NewLine = System.lineSeparator();
            for (Pair<String, String> pair : rows) {
                String row = pair.getKey() + ((pair.getValue() != null) ? ("=" + pair.getValue()) : EMPTY_STRING) + NewLine;
                writer.write(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    LangFile(final int initialSize) {
        rows = new ArrayList<>(initialSize);
    }

    LangFile() {
        this(150);
    }

    private static final String EMPTY_STRING = "";
    private static final Pair<String, String> EMPTY = new Pair<>(EMPTY_STRING, null);

    @Nullable
    private Pair<String, String> findValue(final int index, @NotNull final String keyString) {
        Pair<String, String> result = null;
        for (int i = index; i < this.rows.size(); i++) {
            if (rows.get(i).getKey().contains(keyString)) {
                result = rows.get(i);
                rows.remove(i);
                break;
            }
        }
        return result;
    }

    private List<Pair<String, String>> getUnusedRows(final int size) {
        List<Pair<String, String>> result = new ArrayList<>(size - rows.size());
        while (rows.size() > size) {
            result.add(rows.remove(size));
        }
        return result;
    }

    void update(@NotNull final LangFile template, boolean readOnly) {
        Statistic statistic = new Statistic();
        statistic.filePath = lFile.getAbsolutePath();
        statistic.templateRows = template.rows.size();
        statistic.beforeRows = this.rows.size();
        for (int index = 0; index < template.rows.size(); ++index) {
            Pair<String, String> tempRow = template.rows.get(index);
            if (index == this.rows.size()) {
                this.rows.add(tempRow);
                continue;
            }
            Pair<String, String> thisRow = this.rows.get(index);
            if (thisRow.getKey().equals(tempRow.getKey())) {
                ++statistic.equalRows;
                continue;
            }
            if (tempRow.getKey().equals(EMPTY_STRING)) {
                ++statistic.emptyRowsMismatch;
                this.rows.add(index, EMPTY);
            } else {
                Pair<String, String> result = findValue(index, tempRow.getKey());
                if (result == null) {
                    result = tempRow;
                    ++statistic.missingRowsKeyMismatch;
                }
                this.rows.add(index, result);
                ++statistic.insertedRows;
            }
        }
        statistic.afterRows = this.rows.size();
        statistic.addUnknownKeys(getUnusedRows(template.rows.size()));
        if (readOnly) {
            System.out.println(statistic);
        } else {
            statistic.save(new File(lFile.getAbsolutePath() + ".stats"));
        }
    }
}
