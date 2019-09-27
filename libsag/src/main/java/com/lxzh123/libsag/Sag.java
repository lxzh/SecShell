package com.lxzh123.libsag;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Sag {
//    private final static String OUTPUT_FOLDER = "D:\\Android\\Code\\SecShell\\corestub\\src\\main\\java";
//    private final static String OUTPUT_FOLDER = "D:\\Android\\Code\\SecShell\\corestub\\src\\main\\java";

    private final static List<String> NORMAL_METHOD = Arrays.asList(new String[]{"wait", "equals", "notify", "notifyAll", "toString", "hashCode", "getClass"});
    private final static List<String> ENUM_METHOD = Arrays.asList(new String[]{"values", "valueOf", "name", "compareTo", "getDeclaringClass", "ordinal"});
    private final static int BASIC_TYPE_COUNT = 8;
    private final static String[] BASIC_TYPE = {"byte", "short", "int", "long", "boolean", "char", "float", "double"};
    private final static String[] DEFAULT_VALUE = {"0", "0", "0", "0", "false", "\'\0\'", "0.0f", "0.0"};

    private final static String TAB = "    ";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("add a filepath to extract api");
            return;
        }
        String filepath = args[0];
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("file not exists");
            return;
        }
        System.out.println(file.toString() + " " + file.getAbsolutePath());
        String folder = file.getAbsoluteFile().getParent();
//        List<String[]> list = null;
//        try {
////            list = getJarMethod(path);
////            list = getApiMethod(path, OUTPUT_FOLDER);
//            list = getApiMethod(path, folder);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        if (list != null) {
//            int len = list.size();
//            for (int i = 0; i < len; i++) {
//                System.out.println(Arrays.toString(list.get(i)));
//            }
//        }
        //通过URLClassLoader.loadClass方法得到具体某个类
        URL url = null;
        try {
            url = new URL("file:" + filepath);
        } catch (Exception ex) {

        }
        if (url != null) {
            URLClassLoader myClassLoader = new URLClassLoader(new URL[]{url}, Thread.currentThread().getContextClassLoader());
            generateSdkApi(folder, filepath, myClassLoader);
        }
    }

    public static void generateSdkApi(String outputPath, String fileName, ClassLoader classLoader) {
        List<String[]> list = null;
        try {
//            list = getJarMethod(path);
//            list = getApiMethod(path, OUTPUT_FOLDER);
            list = getApiMethod(fileName, outputPath, classLoader);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (list != null) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                System.out.println(Arrays.toString(list.get(i)));
            }
        }
    }

    private static List<String[]> getJarMethod(String jarFile) throws Exception {
        String NORMAL_METHOD = "waitequalsnotifynotifyAlltoStringhashCodegetClass";
        List<String[]> a = new ArrayList<>();
        try {
            //通过jarFile 和JarEntry得到所有的类
            JarFile jar = new JarFile(jarFile);//"D:/sip-test.jar"
            Enumeration e = jar.entries();
            while (e.hasMoreElements()) {
                JarEntry entry = (JarEntry) e.nextElement();
                //entry.getMethod()
                if (entry.getName().indexOf("META-INF") < 0) {
                    String sName = entry.getName();
                    String substr[] = sName.split("/");
                    String pName = "";
                    for (int i = 0; i < substr.length - 1; i++) {
                        if (i > 0)
                            pName = pName + "/" + substr[i];
                        else
                            pName = substr[i];
                    }
                    if (sName.indexOf(".class") < 0) {
                        sName = sName.substring(0, sName.length() - 1);
                    } else {
                        //通过URLClassLoader.loadClass方法得到具体某个类
                        URL url1 = new URL("file:" + jarFile);
                        URLClassLoader myClassLoader = new URLClassLoader(new URL[]{url1}, Thread.currentThread().getContextClassLoader());
                        String ppName = sName.replace("/", ".").replace(".class", "");
                        Class myClass = myClassLoader.loadClass(ppName);
                        //通过getMethods得到类中包含的方法
                        Method m[] = myClass.getMethods();
                        for (int i = 0; i < m.length; i++) {
                            String sm = m[i].getName();
                            if (NORMAL_METHOD.indexOf(sm) < 0) {
                                String[] c = {sm, sName};
                                a.add(c);
                            }
                        }
                    }
                    String[] b = {sName, pName};
                    a.add(b);
                }
            }
            return a;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return a;
    }

    private static List<String[]> getApiMethod(String jarFile, String folder, ClassLoader classLoader) throws Exception {
        List<String[]> a = new ArrayList<>();
        try {
            //通过jarFile 和JarEntry得到所有的类
            JarFile jar = new JarFile(jarFile);//"D:/sip-test.jar"
            Enumeration e = jar.entries();
            while (e.hasMoreElements()) {
                JarEntry entry = (JarEntry) e.nextElement();
                //entry.getMethod()
                if (entry.getName().indexOf("META-INF") < 0) {
                    String sName = entry.getName();
                    String substr[] = sName.split("/");
                    String pName = "";
                    for (int i = 0; i < substr.length - 1; i++) {
                        if (i > 0)
                            pName = pName + "/" + substr[i];
                        else
                            pName = substr[i];
                    }
                    if (sName.indexOf(".class") < 0) {
                        sName = sName.substring(0, sName.length() - 1);
                    } else {
                        String ppName = sName.replace("/", ".").replace(".class", "");
                        System.out.println("ppName:" + ppName);
                        Class myClass = classLoader.loadClass(ppName);

                        StringBuffer buffer = new StringBuffer();
                        String fileName = exportJavaInfo(myClass, buffer, folder);
                        writeBufferToFile(buffer, fileName);

                        //通过getMethods得到类中包含的方法
                        Method m[] = myClass.getMethods();
                        if (m.length > 0) {
                            System.out.println(myClass.toString() + " " + myClass.getTypeName() + " " + Modifier.toString(myClass.getModifiers()));
                            System.out.println((myClass.isInterface() ? "interface" : "class") + " " + myClass.getPackage().getName() + " " + myClass.getSimpleName() + " " + Modifier.toString(myClass.getModifiers()));

                        }
                        for (int i = 0; i < m.length; i++) {
                            Method method = m[i];
                            String sm = method.getName();
                            if (NORMAL_METHOD.indexOf(sm) < 0) {
                                System.out.println("method " + method.getName() + " " + Modifier.toString(method.getModifiers()));
                                String[] c = {sm, sName};
                                a.add(c);
                                Parameter[] parameters = method.getParameters();
                                int len = parameters.length;
                                for (int j = 0; j < len; j++) {
                                    Parameter parameter = parameters[j];
                                    System.out.println("parameter " + parameter.toString());
                                }
                            }
                        }
                    }
                    String[] b = {sName, pName};
                    a.add(b);
                }
            }
            return a;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return a;
    }

    /**
     * 以jar的形式来获取包下的所有Class
     *
     * @param packageName
     * @param entries
     * @param packageDirName
     * @param recursive
     * @param classes
     */
    private static void findClassesInPackageByJar(String packageName, Enumeration<JarEntry> entries, String packageDirName, final boolean recursive, Set<Class<?>> classes) {
        // 同样的进行循环迭代
        while (entries.hasMoreElements()) {
            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            // 如果是以/开头的
            if (name.charAt(0) == '/') {
                // 获取后面的字符串
                name = name.substring(1);
            }
            // 如果前半部分和定义的包名相同
            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf('/');
                // 如果以"/"结尾 是一个包
                if (idx != -1) {
                    // 获取包名 把"/"替换成"."
                    packageName = name.substring(0, idx).replace('/', '.');
                }
                // 如果可以迭代下去 并且是一个包
                if ((idx != -1) || recursive) {
                    // 如果是一个.class文件 而且不是目录
                    if (name.endsWith(".class") && !entry.isDirectory()) {
                        // 去掉后面的".class" 获取真正的类名
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        try {
                            // 添加到classes
                            classes.add(Class.forName(packageName + '.' + className));
                        } catch (ClassNotFoundException e) {
                            // .error("添加用户自定义视图类错误 找不到此类的.class文件");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static String exportJavaInfo(Class clz, StringBuffer strBuffer, String rootPath) {
        String name = clz.getName();
//        String simpleName = clz.getSimpleName();
//        String pathName = name.replace("/", ".");
        String fileName = rootPath + File.separator + name.replace(".", File.separator) + ".java";

        strBuffer.append("package " + clz.getPackage().getName() + ";\n\n");

        if (clz.isEnum()) {
            strBuffer.append("public enum " + clz.getSimpleName() + " {\n");
        } else if (clz.isAnnotation()) {
            List<String> importList = new ArrayList<>();
            List<String> annotionList = new ArrayList<>();
            Annotation[] annotations = clz.getAnnotations();
            System.out.println("Annotations********************************");
            for (int i = 0; i < annotations.length; i++) {
                Annotation annotation = annotations[i];
                String anoTypeName = annotation.annotationType().getName();
                if (!importList.contains(anoTypeName)) {
                    importList.add(anoTypeName);
                }
                if (!importList.contains("java.lang.annotation.RetentionPolicy")) {
                    importList.add("java.lang.annotation.RetentionPolicy");
                }
                String annStr = annotation.toString();
                annotionList.add(annStr.substring(annStr.lastIndexOf(".") + 1).replace("value=", "RetentionPolicy."));
                System.out.println(annotation.annotationType() + "," + annotation.toString() + "," + annotation.getClass().getName());
            }
            for (int i = 0; i < importList.size(); i++) {
                strBuffer.append("import " + importList.get(i) + ";\n");
            }
            strBuffer.append("\n");
            for (int i = 0; i < annotionList.size(); i++) {
                strBuffer.append("@" + annotionList.get(i) + "\n");
            }
            System.out.println("Annotations********************************");
            strBuffer.append("public @interface " + clz.getSimpleName() + " {}\n");
            return fileName;
        } else if (clz.isInterface()) {
            strBuffer.append("public interface " + clz.getSimpleName() + " {\n");
        } else {
            strBuffer.append(Modifier.toString(clz.getModifiers()) + " class " + clz.getSimpleName() + " {\n");
        }

        /**
         * parse fields in class, attention the difference between enum and other type
         */
        Field[] fields = clz.getFields();
        int fLen = fields.length;
        if (clz.isEnum()) {
            strBuffer.append(TAB);
            for (int i = 0; i < fLen; i++) {
                Field field = fields[i];
                strBuffer.append(field.getName());
                if (i < fLen - 1) {
                    strBuffer.append(", ");
                }
            }
            strBuffer.append("\n");
        } else {
            for (int i = 0; i < fLen; i++) {
                Field field = fields[i];
                strBuffer.append(TAB + Modifier.toString(field.getModifiers()) + " " +
                        field.getType().getName() + " " + field.getName());
                if (Modifier.isStatic(field.getModifiers())) {
                    try {
                        field.setAccessible(true);
                        Object object = field.get(null);
                        System.out.println();
                        String value = null;
                        if (object != null) {
                            value = object.toString();
                        }
                        if (value != null) {
                            value = getDefaultValue(field.getType().getName());
                        }
                        strBuffer.append(" = " + value);
                        System.out.println("===" + field + " = " + value);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                strBuffer.append(";\n");
            }
        }

        /**
         * parse methods in class
         */
        Method[] methods = clz.getMethods();
        int mLen = methods.length;
        for (int i = 0; i < mLen; i++) {
            Method method = methods[i];
            //filter basic method in object or basic enum
            if ((clz.isEnum() && ENUM_METHOD.contains(method.getName())) ||
                    (NORMAL_METHOD.contains(method.getName()))) {
                continue;
            }
            Class rtnType = method.getReturnType();
            String rtnTypeName = rtnType.getName();
            strBuffer.append(TAB + (clz.isInterface() || clz.isAnnotation() ? "" :
                    Modifier.toString(method.getModifiers())) + " " +
                    rtnTypeName + " " + method.getName() + "(");
            Parameter[] parameters = method.getParameters();
            int len = parameters.length;
            for (int j = 0; j < len; j++) {
                Parameter parameter = parameters[j];
                strBuffer.append(parameter.getType().getName() + " " + parameter.getName());
                if (j < len - 1) {
                    strBuffer.append(", ");
                }
            }
            //method of interface or annotation has no method body
            if (clz.isInterface() || clz.isAnnotation()) {
                strBuffer.append(");\n");
            } else {
                //clear method body with returning default value statement;
                strBuffer.append(") {\n");
                String defaultValue = getDefaultValue(rtnTypeName);
                if (defaultValue == null) {
                    strBuffer.append(TAB + TAB + "return;\n");
                } else {
                    strBuffer.append(TAB + TAB + "return " + getDefaultValue(rtnTypeName) + ";\n");
                }
                strBuffer.append(TAB + "}\n");
            }
        }
        strBuffer.append("}");
        return fileName;
    }

    private static String getDefaultValue(String type) {
        String value = null;
        for (int j = 0; j < BASIC_TYPE_COUNT; j++) {
            if (type.equals(BASIC_TYPE[j])) {
                value = DEFAULT_VALUE[j];
            }
        }
        if (type == "void") {
            value = null;
        } else {
            if (value == null) {
                System.out.println("type");
                value = "null";
            }
        }
        return value;
    }

    private static void writeBufferToFile(StringBuffer buffer, String fileName) {
        System.out.println("writeBufferToFile fileName:" + fileName);
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName);
            fileWriter.write(buffer.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.flush();
                    fileWriter.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
