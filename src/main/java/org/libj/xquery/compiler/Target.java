package org.libj.xquery.compiler;

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

}
