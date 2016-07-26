package com.asm;

import com.annotation.CacheAnnotationInfo;
import com.annotation.CacheAnnotationScanner;
import com.bean.BeanFactory;
import org.objectweb.asm.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 谢俊权
 * @create 2016/7/25 16:49
 */
public class CacheAop {

    private Class<?> beanFactoryClazz;

    public CacheAop() {
        this(BeanFactory.class);
    }

    public CacheAop(Class<?> beanFactoryClazz) {
        this.beanFactoryClazz = beanFactoryClazz;
    }

    public void run(String pkage) throws IOException {
        Map<String, CacheAnnotationInfo> map =  CacheAnnotationScanner.scan(pkage);
        Iterator<String> it = map.keySet().iterator();
        while(it.hasNext()){
            String key = it.next();
            CacheAnnotationInfo info = map.get(key);
            aop(info);
        }
    }

    private void aop(CacheAnnotationInfo info) throws IOException {

        Class<?> clazz = info.getClazz();
        Method method = info.getMethod();
        Class<?> returnType = method.getReturnType();
        final String methodName = method.getName();
        Class<?> cacheClazz = info.getCacheClazz();
        String cacheKey = info.getKey();
        int cacheExpire = info.getExpireTime();
        //SIPUSH指令只支持-32767~32767, 这里取整32400s
        if(cacheExpire >= 32400){
            cacheExpire = 32400;
        }

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
            String returnTypeName = returnType.getName().replace(".", "/");
            String clazzName = clazz.getName().replace(".", "/");
            String localVarName = "easyCacheObject";
            String desc = methodDescMap.get(methodName);
            List<String> paramTypeList = getParamTypeFromDesc(desc);
            int paramSize = paramTypeList.size();

            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(18, l0);
            methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(Opcodes.DUP);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitLdcInsn("getUserName");
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 3);
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "test/MyValue", "getId", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(Opcodes.ASTORE, 4);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLineNumber(19, l1);
            methodVisitor.visitLdcInsn(Type.getType("Lcom/cache/LocalCache;"));
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "com/bean/BeanFactory", "get", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, "com/cache/LocalCache");
            methodVisitor.visitVarInsn(Opcodes.ASTORE, 5);
            Label l2 = new Label();
            methodVisitor.visitLabel(l2);
            methodVisitor.visitLineNumber(20, l2);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 5);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 4);
            methodVisitor.visitLdcInsn(Type.getType("Ljava/lang/Integer;"));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/cache/LocalCache", "get", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", false);
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
            methodVisitor.visitVarInsn(Opcodes.ASTORE, 6);
            Label l3 = new Label();
            methodVisitor.visitLabel(l3);
            methodVisitor.visitLineNumber(21, l3);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 6);
            Label l4 = new Label();
            methodVisitor.visitJumpInsn(Opcodes.IFNONNULL, l4);
            Label l5 = new Label();
            methodVisitor.visitLabel(l5);
            methodVisitor.visitLineNumber(22, l5);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 3);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "test/UserService", "getUserName$1", "(Ljava/lang/String;Ljava/lang/String;Ltest/MyValue;)I", false);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            methodVisitor.visitVarInsn(Opcodes.ASTORE, 6);
            Label l6 = new Label();
            methodVisitor.visitLabel(l6);
            methodVisitor.visitLineNumber(23, l6);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 6);
            methodVisitor.visitJumpInsn(Opcodes.IFNULL, l4);
            Label l7 = new Label();
            methodVisitor.visitLabel(l7);
            methodVisitor.visitLineNumber(24, l7);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 5);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 4);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 6);
            methodVisitor.visitLdcInsn(new Integer(1000000));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/cache/LocalCache", "set", "(Ljava/lang/String;Ljava/lang/Object;I)Ljava/lang/Object;", false);
            methodVisitor.visitInsn(Opcodes.POP);
            methodVisitor.visitLabel(l4);
            methodVisitor.visitLineNumber(28, l4);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 3, new Object[]{"java/lang/String", "com/cache/LocalCache", "java/lang/Integer"}, 0, null);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 6);
            methodVisitor.visitInsn(Opcodes.ARETURN);
            Label l8 = new Label();
            methodVisitor.visitLabel(l8);
            methodVisitor.visitLocalVariable("this", "Ltest/UserService;", null, l0, l8, 0);
            methodVisitor.visitLocalVariable("arg1", "Ljava/lang/String;", null, l0, l8, 1);
            methodVisitor.visitLocalVariable("arg2", "Ljava/lang/String;", null, l0, l8, 2);
            methodVisitor.visitLocalVariable("myValue", "Ltest/MyValue;", null, l0, l8, 3);
            methodVisitor.visitLocalVariable("key", "Ljava/lang/String;", null, l1, l8, 4);
            methodVisitor.visitLocalVariable("easyCacheObject", "Lcom/cache/LocalCache;", null, l2, l8, 5);
            methodVisitor.visitLocalVariable("returnValue", "Ljava/lang/Integer;", null, l3, l8, 6);
            methodVisitor.visitMaxs(4, 7);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        byte[] code = classWriter.toByteArray();
        String sourcePath = ClassLoader.getSystemResource("").getPath();
        String filePath = sourcePath.substring(1) + "test/UserService.class";
        FileOutputStream os = new FileOutputStream(filePath);
        os.write(code);
        os.close();
    }

    private List<String> getParamTypeFromDesc(String desc){
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
                    if(ConstantMapping.getLoadInstruction(ch) != null){
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

    public static void main(String[] args){

    }
}
