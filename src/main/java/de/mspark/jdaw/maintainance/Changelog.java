package de.mspark.jdaw.maintainance;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Changelog {
    private List<Version> versions;

    public Changelog(File file) throws FileNotFoundException {
        versions = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            Version currentVersion = null;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Matcher matcher = Pattern.compile("^Version (\\d+\\.\\d+\\.\\d+)$").matcher(line);
                if (matcher.matches()) {
                    if (currentVersion != null) {
                        versions.add(currentVersion);
                    }
                    currentVersion = new Version(matcher.group(1));
                } else if (line.startsWith("- ")) {
                    currentVersion.addChange(line.substring(2));
                } else if (line.equals("####")) {
                    versions.add(currentVersion);
                    currentVersion = null;
                }
            }
            if (currentVersion != null) {
                versions.add(currentVersion);
            }
        }
    }

    public List<Version> getVersions() {
        return versions;
    }

    public static class Version {
        private String number;
        private List<String> changes;

        public Version(String number) {
            this.number = number;
            changes = new ArrayList<>();
        }

        public String getNumber() {
            return number;
        }

        public List<String> getChanges() {
            return changes;
        }

        public void addChange(String change) {
            changes.add(change);
        }
    }
}
