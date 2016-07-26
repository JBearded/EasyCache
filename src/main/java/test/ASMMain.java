package test;

import org.objectweb.asm.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 谢俊权
 * @create 2016/7/25 14:18
 */
public class ASMMain {

    private static Map<String, MethodAttr> methodAttrMap = new HashMap<>();

    public static void main(String[] args) throws IOException {

        ClassReader classReader = new ClassReader(UserService.class.getName());
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor methodChangeVisitor = new ClassVisitor(Opcodes.ASM5, classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if ("getUserName".equals(name)) {
                    MethodAttr methodAttr = new MethodAttr(access, name, desc, signature, exceptions);
                    methodAttrMap.put(name, methodAttr);
                    String newName = String.valueOf(name + "$1");
                    return cv.visitMethod(Opcodes.ACC_PUBLIC, newName, desc, signature, exceptions);
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        };
        classReader.accept(methodChangeVisitor, Opcodes.ASM5);

        {
            MethodVisitor methodVisitor = classWriter.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "getUserName",
                    "(Ljava/lang/String;)Ljava/lang/String;",
                    null,
                    null
            );
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(23, l0);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "test/UserService", "localCache", "Lcom/cache/LocalCache;");
            methodVisitor.visitLdcInsn("ni");
            methodVisitor.visitLdcInsn(Type.getType("Ltest/MyValue;"));
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/cache/LocalCache", "get", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", false);
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, "test/MyValue");
            methodVisitor.visitVarInsn(Opcodes.ASTORE, 2);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLineNumber(24, l1);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            Label l2 = new Label();
            methodVisitor.visitJumpInsn(Opcodes.IFNONNULL, l2);
            Label l3 = new Label();
            methodVisitor.visitLabel(l3);
            methodVisitor.visitLineNumber(25, l3);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "test/UserService", "getUserName$1", "(Ljava/lang/String;)Ltest/MyValue;", false);
            methodVisitor.visitVarInsn(Opcodes.ASTORE, 2);
            Label l4 = new Label();
            methodVisitor.visitLabel(l4);
            methodVisitor.visitLineNumber(26, l4);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            methodVisitor.visitJumpInsn(Opcodes.IFNULL, l2);
            Label l5 = new Label();
            methodVisitor.visitLabel(l5);
            methodVisitor.visitLineNumber(27, l5);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "test/UserService", "localCache", "Lcom/cache/LocalCache;");
            methodVisitor.visitLdcInsn("ni");
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            methodVisitor.visitIntInsn(Opcodes.BIPUSH, 60);
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/cache/LocalCache", "set", "(Ljava/lang/String;Ljava/lang/Object;I)Ljava/lang/Object;", false);
            methodVisitor.visitInsn(Opcodes.POP);
            methodVisitor.visitLabel(l2);
            methodVisitor.visitLineNumber(30, l2);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"test/MyValue"}, 0, null);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            methodVisitor.visitInsn(Opcodes.ARETURN);
            Label l6 = new Label();
            methodVisitor.visitLabel(l6);
            methodVisitor.visitLocalVariable("this", "Ltest/UserService;", null, l0, l6, 0);
            methodVisitor.visitLocalVariable("arg1", "Ljava/lang/String;", null, l0, l6, 1);
            methodVisitor.visitLocalVariable("returnValue", "Ltest/MyValue;", null, l1, l6, 2);
            methodVisitor.visitMaxs(4, 3);
            methodVisitor.visitEnd();
        }

        byte[] code = classWriter.toByteArray();
        String sourcePath = ClassLoader.getSystemResource("").getPath();
        String filePath = sourcePath.substring(1) + "test/UserService.class";
        FileOutputStream os = new FileOutputStream(filePath);
        os.write(code);
        os.close();


    }

    static class MethodAttr{
        int access;
        String name;
        String desc;
        String signature;
        String[] exceptions;

        public MethodAttr(int access, String name, String desc, String signature, String[] exceptions) {
            this.access = access;
            this.name = name;
            this.desc = desc;
            this.signature = signature;
            this.exceptions = exceptions;
        }

        public int getAccess() {
            return access;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public String getSignature() {
            return signature;
        }

        public String[] getExceptions() {
            return exceptions;
        }
    }
}
