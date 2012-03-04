package org.libj.xquery.compiler;

import org.libj.xquery.runtime.Nil;
import org.libj.xquery.xml.XML;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class Caster implements Opcodes {

    public static Class cast(MethodVisitor mv, Class<?> from, Class<?> to) {
        // Object -> Object
        // Integer -> int
        // int -> Integer
        // int -> int
        if (to == String.class) {
            if (from == String.class) {
                return String.class;
            }
            else if (from == XML.class) {
                mv.visitMethodInsn(INVOKEINTERFACE, Constants.XML_INTERFACE, "text", "()Ljava/lang/String;");
                return String.class;
            }
            else if (!from.isPrimitive()) {
                mv.visitMethodInsn(INVOKESTATIC, Constants.FN, "string", "(Ljava/lang/Object;)Ljava/lang/String;");
                return String.class;
            }
//            else if (!from.isPrimitive()) {
//                mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
//                return String.class;
//            }
            else if (from == int.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;");
                return String.class;
            }
            else if (from == double.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;");
                return String.class;
            }
            else {
                throw new RuntimeException("Not Implemented!");
            }
        }
        else if (!from.isPrimitive() && !to.isPrimitive()) {
            if (to.isAssignableFrom(from)) {
                return from;
            }
            else {
                mv.visitTypeInsn(CHECKCAST, to.getName().replace('.', '/'));
                return from;
            }
        }
        else if (!from.isPrimitive() && to.isPrimitive()) {
            // object to primitive
            if (to == int.class) {
                return castToInt(mv);
            }
            else {
                throw new RuntimeException("Not Implemented: "+to);
            }
        }
        else if (from.isPrimitive() && !to.isPrimitive()) {
            // primitive to object
            if (from == int.class) {
                return castToIntegerObject(mv);
            }
            else if (from == double.class) {
                return castToDoubleObject(mv);
            }
            else if (from == boolean.class) {
                return castToBooleanObject(mv);
            }
            else if (from == long.class) {
                return castToLongObject(mv);
            }
            else if (from == void.class) {
                // XXX: is it OK?
                mv.visitFieldInsn(GETSTATIC, Constants.NIL, "NIL", "L"+Constants.NIL+";");
                return Nil.class;
//                throw new RuntimeException("Not Implemented! "+from+" to "+to);
            }
            else {
                throw new RuntimeException("Not Implemented: "+from);
            }
        }
        else {
            // primitive to primitive
            return castBetweenPrimitives(mv, from, to);
        }
    }

    public static Class castToPrimitiveValue(MethodVisitor mv, Class<?> from) {
        // cast a primitive object (e.g. Integer) to it's primitive value, based on source type
        if (from.isPrimitive()) {
            return from;
        }
        if (from == Integer.class) {
            return castToInt(mv);
        }
        else if (from == Double.class) {
            return castToDouble(mv);
        }
        throw new RuntimeException("Not Implemented: -> "+from);
    }

    public static Class castToInt(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        return Integer.class;
    }
    public static Class castToDouble(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(I)Ljava/lang/Double;");
        return Double.class;
    }
    public static Class castToIntegerObject(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        return Integer.class;
    }
    public static Class castToDoubleObject(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
        return Double.class;
    }
    public static Class castToLongObject(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
        return Boolean.class;
    }
    public static Class castToBooleanObject(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
        return Boolean.class;
    }
    public static Class castBetweenPrimitives(MethodVisitor mv, Class from, Class to) {
        if (!from.isPrimitive() || !to.isPrimitive()) {
            throw new RuntimeException("Not Implemented!");
        }
        if (from == to) {
            return to;
        }
        if (from == int.class && to == double.class) {
            mv.visitInsn(I2D);
            return double.class;
        }
        if (from == double.class && to == int.class) {
            mv.visitInsn(D2I);
            return int.class;
        }
        if (from == int.class && to == boolean.class) {
            return boolean.class;
        }
        if (from == int.class && to == long.class) {
            mv.visitInsn(I2L);
            return long.class;
        }
        throw new RuntimeException("Not Implemented! "+from+" to "+to);
    }

    public static void castMany(MethodVisitor mv, Class[] from, Class[] to) {
        if (from.length != to.length) {
            throw new RuntimeException("Incorrect many cast");
        }
        int i = 0;
        while (i < from.length && !needCast(from[i], to[i])) {
            i++;
        }
        if (i >= from.length) {
            // nothing to cast
            return;
        }
        int n = from.length - i;
        if (n == 1) {
            cast(mv, from[i], to[i]);
            return;
        }
        if (n == 2) {
            if (from[i] == double.class || from[i] == long.class ||
                from[i+1] == double.class || from[i+1] == long.class ||
                to[i] == double.class || to[i] == long.class ||
                to[i+1] == double.class || to[i+1] == long.class) {
                throw new RuntimeException("Not Implemented!");
            }
            mv.visitInsn(SWAP);
            cast(mv, from[i], to[i]);
            mv.visitInsn(SWAP);
            cast(mv, from[i+1], to[i+1]);
            return;
        }
        throw new RuntimeException("Not Implemented!");
    }
    private static boolean needCast(Class from, Class to) {
        if (from == to) {
            return false;
        }
        if (!from.isPrimitive() && !to.isPrimitive()) {
            return false;
        }
        if (!from.isPrimitive() || !to.isPrimitive()) {
            return true;
        }
        else {
            throw new RuntimeException("Not Implemented!");
        }
    }
}
