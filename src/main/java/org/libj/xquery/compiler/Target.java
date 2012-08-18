package org.libj.xquery.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Target {
    private ClassInfo mainClass;
    private List<ClassInfo> innerClasses;

    public Target(ClassInfo mainClass, List<ClassInfo> innerClasses) {
        this.mainClass = mainClass;
        this.innerClasses = innerClasses;
    }

    public ClassInfo getMainClass() {
        return mainClass;
    }

    public List<ClassInfo> getInnerClasses() {
        return innerClasses;
    }

    // utilities

    public byte[] toByteArray() {
        if (!getInnerClasses().isEmpty()) {
            throw new RuntimeException("There are inner classes!");
        }
        return getMainClass().getBytes();
    }

    public void toDir(File dir) {
        ArrayList<ClassInfo> classes = new ArrayList<ClassInfo>(getInnerClasses());
        classes.add(getMainClass());
        try {
            for (ClassInfo c : classes) {
                File path = new File(dir, c.getClassName() + ".class");
                path.getParentFile().mkdirs();
                FileOutputStream output = new FileOutputStream(path);
                try {
                    output.write(c.getBytes());
                } finally {
                    output.close();
                }
            }
        } catch (IOException e) {
            throw new CompilerException(e);
        }
    }

    public void toDir(String dir) {
        toDir(new File(dir));
    }

    public void toFile(File path) {
        try {
            byte[] bytes = toByteArray();
            FileOutputStream output = new FileOutputStream(path);
            try {
                output.write(bytes);
            } finally {
                output.close();
            }
        } catch (IOException e) {
            throw new CompilerException(e);
        }
    }

    public void toFile(String path) {
        toFile(new File(path));
    }

}
