package com.asm;

import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 谢俊权
 * @create 2016/7/26 10:28
 */
public class TypeInstructionMapping {

    private static Map<String, TypeInstruction> typeInsMap = new HashMap<>();
    private static Map<String, TypeInstruction> basicTypeMap = new HashMap<>();
    static {

        typeInsMap.put("Z", new TypeInstruction("Z", "boolean", "java.lang.Boolean", Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN));
        typeInsMap.put("B", new TypeInstruction("B", "byte", "java.lang.Byte", Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN));
        typeInsMap.put("C", new TypeInstruction("C", "char", "java.lang.Char", Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN));
        typeInsMap.put("S", new TypeInstruction("S", "short", "java.lang.Short", Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN));
        typeInsMap.put("I", new TypeInstruction("I", "int", "java.lang.Integer", Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN));
        typeInsMap.put("J", new TypeInstruction("J", "short", "java.lang.Long", Opcodes.LLOAD, Opcodes.LSTORE, Opcodes.LRETURN));
        typeInsMap.put("F", new TypeInstruction("F", "short", "java.lang.Float", Opcodes.FLOAD, Opcodes.FSTORE, Opcodes.FRETURN));
        typeInsMap.put("D", new TypeInstruction("D", "short", "java.lang.Double", Opcodes.DLOAD, Opcodes.DSTORE, Opcodes.DRETURN));
        typeInsMap.put("L", new TypeInstruction("L", null, null, Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.ARETURN));

        basicTypeMap.put("boolean", typeInsMap.get("Z"));
        basicTypeMap.put("byte", typeInsMap.get("B"));
        basicTypeMap.put("char", typeInsMap.get("C"));
        basicTypeMap.put("short", typeInsMap.get("S"));
        basicTypeMap.put("int", typeInsMap.get("I"));
        basicTypeMap.put("long", typeInsMap.get("L"));
        basicTypeMap.put("float", typeInsMap.get("F"));
        basicTypeMap.put("double", typeInsMap.get("D"));
    }

    public static int getLoadFromTypeIns(String typeIns){
        if(typeInsMap.get(typeIns) == null){
            return -1;
        }
        return typeInsMap.get(typeIns).getLoadInstruction();
    }

    public static int getStoreFromTypeIns(String typeIns){
        if(typeInsMap.get(typeIns) == null){
            return -1;
        }
        return typeInsMap.get(typeIns).getStoreInstruction();
    }

    public static int getReturnFromTypeIns(String typeIns){
        if(typeInsMap.get(typeIns) == null){
            return -1;
        }
        return typeInsMap.get(typeIns).getReturnInstruction();
    }

    public static String getBasicTypeFromTypeIns(String typeIns){
        if(typeInsMap.get(typeIns) == null){
            return null;
        }
        return typeInsMap.get(typeIns).getBasicType();
    }

    public static String getObjectTypeFromTypeIns(String typeIns){
        if(typeInsMap.get(typeIns) == null){
            return null;
        }
        return typeInsMap.get(typeIns).getObjectType();
    }





    public static int getLoadFromBasicType(String basicType){
        if(basicTypeMap.get(basicType) == null){
            return -1;
        }
        return basicTypeMap.get(basicType).getLoadInstruction();
    }

    public static int getStoreFromBasicType(String basicType){
        if(basicTypeMap.get(basicType) == null){
            return -1;
        }
        return basicTypeMap.get(basicType).getStoreInstruction();
    }

    public static int getReturnFromBasicType(String basicType){
        if(basicTypeMap.get(basicType) == null){
            return -1;
        }
        return basicTypeMap.get(basicType).getReturnInstruction();
    }

    public static String getObjectTypeFromBasicType(String basicType){
        if(basicTypeMap.get(basicType) == null){
            return null;
        }
        return basicTypeMap.get(basicType).getObjectType();
    }
    public static String getTypeInsFromBasicType(String basicType){
        if(basicTypeMap.get(basicType) == null){
            return null;
        }
        return basicTypeMap.get(basicType).getTypeIns();
    }




    private static class TypeInstruction{

        private String typeIns;
        private String basicType;
        private String objectType;
        private int loadInstruction;
        private int storeInstruction;
        private int returnInstruction;

        public TypeInstruction(String type, String basicType, String objectType, int loadInstruction, int storeInstruction, int returnInstruction) {
            this.typeIns = type;
            this.basicType = basicType;
            this.objectType = objectType;
            this.loadInstruction = loadInstruction;
            this.storeInstruction = storeInstruction;
            this.returnInstruction = returnInstruction;
        }

        public String getTypeIns() {
            return typeIns;
        }

        public String getBasicType() {
            return basicType;
        }

        public String getObjectType() {
            return objectType;
        }

        public int getStoreInstruction() {
            return storeInstruction;
        }

        public int getLoadInstruction() {
            return loadInstruction;
        }

        public int getReturnInstruction() {
            return returnInstruction;
        }
    }

}
