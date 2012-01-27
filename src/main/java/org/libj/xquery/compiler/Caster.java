package org.libj.xquery.compiler;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class Caster implements Opcodes {
    public static void castTo(MethodVisitor mv, Class to) {
        cast(mv, Object.class, to);
    }

    public static void castFrom(MethodVisitor mv, Class<?> from) {
        cast(mv, from, Object.class);
    }

    public static void cast(MethodVisitor mv, Class<?> from, Class<?> to) {
        if (!from.isPrimitive() && !to.isPrimitive()) {
            return;
        }
        else if (!from.isPrimitive() && to.isPrimitive()) {
            // object to primitive
            String name = to.getName();
            if (name.equals("int")) {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
            }
            else {
                throw new RuntimeException("Not Implemented: "+name);
            }
        }
        else if (from.isPrimitive() && !to.isPrimitive()) {
            // primitive to object
            String name = from.getName();
            if (name.equals("int")) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            }
            else {
                throw new RuntimeException("Not Implemented: "+name);
            }
        }
        else {
            // primitive to primitive
            throw new RuntimeException("Not Implemented! Cast from "+from+" to "+to);
        }
    }
}
