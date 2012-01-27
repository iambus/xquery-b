package org.libj.xquery.compiler;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class Caster implements Opcodes {
    public static Class castObjectTo(MethodVisitor mv, Class to) {
        // convert object to object or primitive value, based on target type
        // Integer -> int
        return cast(mv, Object.class, to);
    }

    public static Class castToObject(MethodVisitor mv, Class<?> from) {
        // based on source type
        // int -> Integer
        return cast(mv, from, Object.class);
    }

    public static Class cast(MethodVisitor mv, Class<?> from, Class<?> to) {
        // Object -> Object
        // Integer -> int
        // int -> Integer
        // int -> int
        if (!from.isPrimitive() && !to.isPrimitive()) {
            return from;
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
        throw new RuntimeException("Not Implemented!");
    }
}
