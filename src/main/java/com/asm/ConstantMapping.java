package com.asm;

import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 谢俊权
 * @create 2016/7/26 10:28
 */
public class ConstantMapping {

    private static Map<String, Integer> loadInstruction = new HashMap<>();
    private static Map<String, String> basicObjectType = new HashMap<>();
    static {

        loadInstruction.put("B", Opcodes.ILOAD);
        loadInstruction.put("C", Opcodes.ILOAD);
        loadInstruction.put("S", Opcodes.ILOAD);
        loadInstruction.put("Z", Opcodes.ILOAD);
        loadInstruction.put("I", Opcodes.ILOAD);
        loadInstruction.put("D", Opcodes.DLOAD);
        loadInstruction.put("F", Opcodes.FLOAD);
        loadInstruction.put("J", Opcodes.LLOAD);
        loadInstruction.put("L", Opcodes.ALOAD);

        basicObjectType.put("boolean", "java.lang.Boolean");
        basicObjectType.put("byte", "java.lang.Byte");
        basicObjectType.put("short", "java.lang.Short");
        basicObjectType.put("int", "java.lang.Integer");
        basicObjectType.put("long", "java.lang.Long");
        basicObjectType.put("float", "java.lang.Float");
        basicObjectType.put("double", "java.lang.Double");
    }

    public static Integer getLoadInstruction(String type){
        return loadInstruction.get(type);
    }

    public static String getBasicObjectType(String type){
        return basicObjectType.get(type);
    }




}
