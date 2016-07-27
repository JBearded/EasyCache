package com.asm;

import com.annotation.CacheAnnotationInfo;
import com.annotation.CacheAnnotationScanner;
import com.bean.BeanClassLoader;
import com.bean.BeanFactory;
import org.objectweb.asm.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 谢俊权
 * @create 2016/7/25 16:49
 */
public class CacheInjector {

    private Class<?> beanFactoryClazz;

    public CacheInjector() {
        this(BeanFactory.class);
    }

    public CacheInjector(Class<?> beanFactoryClazz) {
        this.beanFactoryClazz = beanFactoryClazz;
    }

    public void run(String pkage) throws Exception {
        Map<String, byte[]> codeMap = new ConcurrentHashMap<>();
        Map<String, CacheAnnotationInfo> map =  CacheAnnotationScanner.scan(pkage);
        Iterator<String> it = map.keySet().iterator();
        while(it.hasNext()){
            String key = it.next();
            CacheAnnotationInfo info = map.get(key);
            Class<?> clazz = info.getClazz();
//            byte[] code = inject(info);
            byte[] code = getFileByte();
            codeMap.put("test.UserService", code);
        }
        BeanClassLoader classLoader = new BeanClassLoader(codeMap);
        Class<?> clazz = classLoader.loadClass("test.UserService");
        Method method = clazz.getMethod("getUserName", int.class, String.class);
        Object object = clazz.newInstance();
        System.out.println("invoke: "+method.invoke(object, 12, "hao"));
        BeanFactory.set(object);
    }

    private byte[] inject(CacheAnnotationInfo info) throws Exception {

        Class<?> clazz = info.getClazz();
        Method method = info.getMethod();
        Class<?> returnType = method.getReturnType();
        final String methodName = method.getName();
        Class<?> cacheClazz = info.getCacheClazz();
        String cacheKey = info.getKey();
        int cacheExpire = info.getExpireTime();

        final Map<String, String> methodDescMap = new HashMap<>();

        ClassReader classReader = new ClassReader(clazz.getName());
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor methodChangeVisitor = new ClassVisitor(Opcodes.ASM5, classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (methodName.equals(name)) {
                    methodDescMap.put(name, desc);
                    String newName = String.valueOf(name + "$1");
                    return cv.visitMethod(Opcodes.ACC_PRIVATE, newName, desc, signature, exceptions);
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        };
        classReader.accept(methodChangeVisitor, Opcodes.ASM5);

        {
            MethodVisitor methodVisitor = classWriter.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    methodName,
                    methodDescMap.get(methodName),
                    null,
                    null
            );

            String beanFactoryName = beanFactoryClazz.getName().replace(".", "/");
            String cacheClazzName = cacheClazz.getName().replace(".", "/");
            boolean isBasicReturnType = true;
            String returnBasicTypeName = returnType.getName();
            String returnBasicTypeIns = TypeInstructionMapping.getTypeInsFromBasicType(returnBasicTypeName);
            String returnObjectTypeName = TypeInstructionMapping.getObjectTypeFromBasicType(returnBasicTypeName);
            if(returnObjectTypeName == null){
                isBasicReturnType = false;
                returnObjectTypeName = returnBasicTypeName;
            }
            returnObjectTypeName = returnObjectTypeName.replace(".", "/");
            String clazzName = clazz.getName().replace(".", "/");
            String desc = methodDescMap.get(methodName);
            List<String> paramTypeList = getParamTypeFromDesc(desc);
            Map<Integer, String> paramIndexMap = getKeyParamIndexMap(cacheKey);
            int paramSize  = paramTypeList.size();
            int cacheKeyStackIndex = (paramSize > 3) ? paramSize + 2 : paramSize + 1;
            int cacheObjectStackIndex = cacheKeyStackIndex + 1;
            int returnValueStackIndex = cacheObjectStackIndex + 1;
            int max_stack = (paramSize > 3) ? paramSize + 1 : 4;
            int max_locals = paramSize + 1 + 3;

            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(18, l0);

            String defaultKey = clazzName.replace("/","") + methodName;
            if(paramIndexMap.isEmpty()){
                methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
                methodVisitor.visitInsn(Opcodes.DUP);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

                methodVisitor.visitLdcInsn(defaultKey);
                methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                Iterator<Integer> it = paramIndexMap.keySet().iterator();
                while(it.hasNext()){
                    Integer index = it.next();
                    String getMethodName = paramIndexMap.get(index);
                    String type = paramTypeList.get(index - 1);
                    if(type.startsWith("L")){
                        type = type + ";";
                    }
                    int loadIndex = index;
                    if(index > 4){
                        loadIndex = index + 1;
                    }

                    int load = TypeInstructionMapping.getLoadFromTypeIns(type.substring(0,1));
                    if(getMethodName == null){
                        methodVisitor.visitVarInsn(load, loadIndex);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "("+type+")Ljava/lang/StringBuilder;", false);
                    }else{
                        String paramTypeClazzName = type.substring(1, type.length() - 1).replace("/", ".");
                        Class<?> paramTypeClazz = Class.forName(paramTypeClazzName);
                        Method getMethod = paramTypeClazz.getDeclaredMethod(getMethodName, null);
                        Class<?> getMethodReturnType = getMethod.getReturnType();
                        String getMethodReturnTypeName = getMethodReturnType.getName();
                        String getMethodReturnObjectTypeName = TypeInstructionMapping.getObjectTypeFromBasicType(getMethodReturnTypeName);
                        if(getMethodReturnObjectTypeName == null){
                            getMethodReturnObjectTypeName = getMethodReturnTypeName;
                        }
                        String getMethodReturnTypeAsmName = getMethodReturnObjectTypeName.replace(".", "/");
                        String paramTypeName = paramTypeList.get(index);
                        paramTypeName = paramTypeName.substring(1);
                        methodVisitor.visitVarInsn(load, loadIndex);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, paramTypeName, getMethodName, "()L"+getMethodReturnTypeAsmName+";", false);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
                    }
                }
                methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            }else{
                methodVisitor.visitLdcInsn(defaultKey);
            }

            methodVisitor.visitVarInsn(Opcodes.ASTORE, cacheKeyStackIndex);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLineNumber(19, l1);
            methodVisitor.visitLdcInsn(Type.getType("L"+cacheClazzName+";"));
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, beanFactoryName, "get", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, cacheClazzName);
            methodVisitor.visitVarInsn(Opcodes.ASTORE, cacheObjectStackIndex);
            Label l2 = new Label();
            methodVisitor.visitLabel(l2);
            methodVisitor.visitLineNumber(20, l2);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, cacheObjectStackIndex);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, cacheKeyStackIndex);
            methodVisitor.visitLdcInsn(Type.getType("L"+returnObjectTypeName+";"));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cacheClazzName, "get", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", false);
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, returnObjectTypeName);
            methodVisitor.visitVarInsn(Opcodes.ASTORE, returnValueStackIndex);
            Label l3 = new Label();
            methodVisitor.visitLabel(l3);
            methodVisitor.visitLineNumber(21, l3);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, returnValueStackIndex);
            Label l4 = new Label();
            methodVisitor.visitJumpInsn(Opcodes.IFNONNULL, l4);
            Label l5 = new Label();
            methodVisitor.visitLabel(l5);
            methodVisitor.visitLineNumber(22, l5);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            int beginLoadIndex = 1;
            for(int i = 0; i < paramTypeList.size(); i++){
                String paramType = paramTypeList.get(i);
                if(paramType.startsWith("L"))
                    paramType = "L";
                int load = TypeInstructionMapping.getLoadFromTypeIns(paramType);
                if(beginLoadIndex == 5)
                    beginLoadIndex++;
                methodVisitor.visitVarInsn(load, beginLoadIndex++);
            }
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, clazzName, methodName+"$1", methodDescMap.get(methodName), false);
            if(isBasicReturnType){
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, returnObjectTypeName, "valueOf", "("+returnBasicTypeIns+")L"+returnObjectTypeName+";", false);
            }
            methodVisitor.visitVarInsn(Opcodes.ASTORE, returnValueStackIndex);
            Label l6 = new Label();
            methodVisitor.visitLabel(l6);
            methodVisitor.visitLineNumber(23, l6);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, returnValueStackIndex);
            methodVisitor.visitJumpInsn(Opcodes.IFNULL, l4);
            Label l7 = new Label();
            methodVisitor.visitLabel(l7);
            methodVisitor.visitLineNumber(24, l7);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, cacheObjectStackIndex);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, cacheKeyStackIndex);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, returnValueStackIndex);
            methodVisitor.visitLdcInsn(new Integer(cacheExpire));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cacheClazzName, "set", "(Ljava/lang/String;Ljava/lang/Object;I)Ljava/lang/Object;", false);
            methodVisitor.visitInsn(Opcodes.POP);
            methodVisitor.visitLabel(l4);
            methodVisitor.visitLineNumber(28, l4);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 3, new Object[]{"java/lang/String", cacheClazzName, returnObjectTypeName}, 0, null);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, returnValueStackIndex);
            if(isBasicReturnType){
                methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, returnObjectTypeName, returnBasicTypeName+"Value", "()"+returnBasicTypeIns, false);
                methodVisitor.visitInsn(TypeInstructionMapping.getReturnFromTypeIns(returnBasicTypeIns));
            }else{
                methodVisitor.visitInsn(Opcodes.ARETURN);
            }
            Label l8 = new Label();
            methodVisitor.visitLabel(l8);
            methodVisitor.visitLocalVariable("this", "L"+clazzName+";", null, l0, l8, 0);
            int beginLocalVarIndex = 1;
            for(int i = 0; i < paramTypeList.size(); i++){
                String paramType = paramTypeList.get(i);
                if(paramType.startsWith("L")){
                    paramType = paramType + ";";
                }
                if(beginLocalVarIndex == 5){
                    beginLocalVarIndex++;
                }
                methodVisitor.visitLocalVariable("arg"+beginLocalVarIndex, paramType, null, l0, l7, beginLocalVarIndex);
                beginLocalVarIndex++;
            }
            methodVisitor.visitLocalVariable("key", "Ljava/lang/String;", null, l1, l8, cacheKeyStackIndex);
            methodVisitor.visitLocalVariable("easyCache", "L"+cacheClazzName+";", null, l2, l8, cacheObjectStackIndex);
            methodVisitor.visitLocalVariable("returnValue", "L"+returnObjectTypeName+";", null, l3, l8, returnValueStackIndex);
            methodVisitor.visitMaxs(max_stack, max_locals);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        byte[] code = classWriter.toByteArray();
        String sourcePath = ClassLoader.getSystemResource("").getPath();
        String filePath = sourcePath.substring(1) + "test/UserService.class";
        FileOutputStream os = new FileOutputStream(filePath);
        os.write(code);
        os.close();

        return code;
    }

    private byte[] getFileByte() throws IOException {
        byte[] code = null;
        InputStream is = null;
        try {
            is = new FileInputStream("E:\\UserService.class");
            code = new byte[is.available()];
            is.read(code);
        } catch (IOException e) {
            if(is != null){
                is.close();
            }
        }
        return code;
    }

    private static Map<Integer, String> getKeyParamIndexMap(String key){
        Map<Integer, String> paramIndexMap = new HashMap<>();
        String regex = "\\$([1-9]{1})(\\.\\w+)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(key);
        while(matcher.find()){
            String item = matcher.group();
            int methodIndex = item.indexOf(".");
            String index = item.substring(1,2);
            int paramIndex = Integer.valueOf(index);
            String getMethod = (methodIndex >= 0) ? item.substring(methodIndex+1) : null;
            if(getMethod != null && !"".equals(getMethod)){
                getMethod = "get" + getMethod.toUpperCase().substring(0,1) + getMethod.substring(1);
            }
            paramIndexMap.put(paramIndex, getMethod);
        }
        return paramIndexMap;
    }

    private static List<String> getParamTypeFromDesc(String desc){
        List<String> typeList = new ArrayList<>();
        String regex = "\\((.*)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(desc);
        if(matcher.find()){
            desc = matcher.group(1);
        }
        String[] paramTypes = desc.split(";");
        for(int i = 0; i < paramTypes.length; i++){
            String type = paramTypes[i];
            if(type.startsWith("L")){   //L表示对象类型
                typeList.add(type);
            }else{
                for(int j = 0; j < type.length(); j++){
                    String ch = String.valueOf(type.charAt(j));
                    if(TypeInstructionMapping.getLoadFromTypeIns(ch) > 0){
                        if("L".equals(ch)){
                            String lType = type.substring(j);
                            typeList.add(lType);
                            break;
                        }else{
                            typeList.add(ch);
                        }
                    }
                }
            }
        }
        return typeList;
    }
}
