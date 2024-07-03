/*
Copyright (c) 2024 Andrew Auclair

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Main {
    static void deprecateFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));

        List<String> lines = new ArrayList<>();

        boolean alreadyDeprecated = false;
        int classLine = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);

            if (classLine == 0 && line.startsWith("@Deprecated")) {
                alreadyDeprecated = true;
            }

            if (classLine == 0) {
                if (line.startsWith("public @interface") ||
                        line.startsWith("public interface") ||
                        line.startsWith("public class") ||
                        line.startsWith("public enum")) {
                    classLine = lines.size() - 1;
                }
            }
        }

        if (!alreadyDeprecated && classLine != 0) {
            lines.add(classLine, "@Deprecated(since = \"0.12.0\", forRemoval = true)");
            lines.add(classLine, "// all classes moved from ModernDocking package to io.github.andrewauclair.moderndocking");
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
        for (String text : lines) {
            writer.write(text + '\n');
        }

        reader.close();
        writer.close();
    }

    static void deprecateFolder(File folder) throws IOException {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.getName().endsWith(".java")) {
                deprecateFile(file);
            }
            else if (file.isDirectory()) {
                deprecateFolder(file);
            }
        }
    }

    static void changePackageFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));

        List<String> lines = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("package ModernDocking")) {
                line = line.replace("package ModernDocking", "package io.github.andrewauclair.moderndocking");
            }
            else if (line.startsWith("import ModernDocking")) {
                line = line.replace("import ModernDocking", "import io.github.andrewauclair.moderndocking");
            }
            else if (line.startsWith("import static ModernDocking")) {
                line = line.replace("import static ModernDocking", "import static io.github.andrewauclair.moderndocking");
            }

            lines.add(line);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
        for (String text : lines) {
            writer.write(text + '\n');
        }

        reader.close();
        writer.close();
    }

    static void changePackageFolder(File folder) throws IOException {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.getName().endsWith(".java")) {
                changePackageFile(file);
            }
            else if (file.isDirectory()) {
                changePackageFolder(file);
            }
        }
    }

    static void renameFolders(File sourceFolder) throws IOException {
        File modernDocking = new File(sourceFolder + "/ModernDocking");

        if (!modernDocking.exists()) {
            throw new RuntimeException("No ModernDocking folder found in " + sourceFolder.getAbsolutePath());
        }

        File newModernDocking = new File(sourceFolder + "/io/github/andrewauclair/moderndocking");
        newModernDocking.mkdirs();

        FileUtils.copyDirectory(modernDocking, newModernDocking);

        // go through every file in the old source folder and add a deprecated annotation
        deprecateFolder(modernDocking);
        // change the package names in the new files
        changePackageFolder(newModernDocking);
    }

    public static void main(String[] args) throws IOException {
        renameFolders(new File("docking-api/src"));
        renameFolders(new File("docking-single-app/src"));
        renameFolders(new File("docking-multi-app/src"));
        renameFolders(new File("docking-ui/src"));
    }
}