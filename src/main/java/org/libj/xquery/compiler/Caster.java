package org.libj.xquery.compiler;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class Caster implements Opcodes {
    public static void castTo(MethodVisitor mv, Class type) {
        if (type.isPrimitive()) {
            String name = type.getName();
            if (name.equals("int")) {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
            }
            else {
                throw new RuntimeException("Not Implemented: "+name);
            }
        }
    }

    public static void castFrom(MethodVisitor mv, Class<?> type) {
        if (type.isPrimitive()) {
            String name = type.getName();
            if (name.equals("int")) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            }
            else {
                throw new RuntimeException("Not Implemented: "+name);
            }
        }
    }
}
