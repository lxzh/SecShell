package com.lxzh123.libsag;

import java.io.File;
import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Sag {
    private final static List<String> NORMAL_METHOD = Arrays.asList(new String[]{"wait", "equals", "notify", "notifyAll", "toString", "hashCode", "getClass"});
    private final static List<String> ENUM_METHOD = Arrays.asList(new String[]{"values", "valueOf", "name", "compareTo", "getDeclaringClass", "ordinal"});
    private final static int BASIC_TYPE_COUNT = 8;
    private final static String[] BASIC_TYPE = {"byte", "short", "int", "long", "boolean", "char", "float", "double"};
    private final static char[] BASIC_TYPE_BYTE_CODE = {'B', 'S', 'I', 'J', 'Z', 'C', 'F', 'D'};
    private final static String[] DEFAULT_VALUE = {"0", "0", "0", "0", "false", "\'\0\'", "0.0f", "0.0"};
    private final static String BASE_PACKAGE = "java.lang";

    private final static String TAG = "Sag";
    private final static String TAB = "    ";

    private ILogger logger;

    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

    private static volatile Sag instance;

    public static Sag get(ILogger logger) {
        if (instance == null) {
            synchronized (Sag.class) {
                if (instance == null) {
                    instance = new Sag();
                    instance.setLogger(logger);
                }
            }
        }
        return instance;
    }

    public void generateSdkApi(String outPath, String jarFile, ClassLoader classLoader) {
        List<String> errorClasses = new ArrayList<>();
        try {
            //parse jar by JarFile to get the JarEntry, then get all class
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
                        logger.d(TAG, "getApiMethod:ppName:" + ppName);
                        Class myClass = null;
                        try {
                            myClass = classLoader.loadClass(ppName);
                        } catch (ClassNotFoundException ex) {
                            errorClasses.add(ppName);
                        }
                        if (myClass == null) {
                            continue;
                        }

                        StringBuffer buffer = new StringBuffer();
                        String fileName = exportJavaInfo(myClass, buffer, outPath);
                        writeBufferToFile(buffer, fileName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int errCnt = errorClasses.size();
        if (errCnt > 0) {
            logger.d(TAG, "getApiMethod:Parse class error with ClassNotFoundException, total:" + errCnt);
        }
        for (int i = 0; i < errCnt; i++) {
            logger.d(TAG, "getApiMethod:class:" + errorClasses.get(i));
        }
    }

    private String exportJavaInfo(Class clz, StringBuffer strBuffer, String rootPath) {
        String name = clz.getName();
        String fileName = rootPath + File.separator + name.replace(".", File.separator) + ".java";
        String pkgName = clz.getPackage().getName();
        strBuffer.append("package " + pkgName + ";\n\n");
        if (clz.isEnum()) {
            strBuffer.append("public enum " + clz.getSimpleName());
        } else if (clz.isAnnotation()) {
            List<String> importList = new ArrayList<>();
            List<String> annotionList = new ArrayList<>();
            Annotation[] annotations = clz.getAnnotations();
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
                logger.d(TAG, "exportJavaInfo:" + annotation.annotationType() + "," + annotation.toString() + "," + annotation.getClass().getName());
            }
            for (int i = 0; i < importList.size(); i++) {
                strBuffer.append("import " + importList.get(i) + ";\n");
            }
            strBuffer.append("\n");
            for (int i = 0; i < annotionList.size(); i++) {
                strBuffer.append("@" + annotionList.get(i) + "\n");
            }
            strBuffer.append("public @interface " + clz.getSimpleName() + " {}\n");
            return fileName;
        } else if (clz.isInterface()) {
            strBuffer.append("public interface " + clz.getSimpleName());
        } else {
            strBuffer.append(Modifier.toString(clz.getModifiers()) + " class " + clz.getSimpleName());
        }
        /**
         * parse type variable, such as <E extends Comparable>
         */
        String typeVar = getClassTypeVariable(pkgName, clz);
        if (typeVar != null) {
            strBuffer.append(typeVar);
        }

        /**
         * parse super class
         */
        Class spClz = clz.getSuperclass();
        if (spClz != null) {
            logger.d(TAG, "exportJavaInfo:class:" + clz.getName() + ",superClass=" + spClz.getName());
        }
        if (spClz != null && !spClz.equals(Object.class) && !spClz.equals(Enum.class)) {
            if (spClz.isInterface()) {
                strBuffer.append(" implements ");
            } else {
                strBuffer.append(" extends ");
            }
            strBuffer.append(getClassName(pkgName, spClz, true));
        }
        strBuffer.append(" {\n");

        /**
         * parse fields in class or interface, attention the difference between enum and other type
         */
        Field[] fields = clz.getFields();
        int fLen = fields.length;
        if (clz.isEnum()) {
            /**
             * parse enum item
             */
            strBuffer.append(TAB);
            for (int i = 0; i < fLen; i++) {
                Field field = fields[i];
                strBuffer.append(field.getName());
                if (i < fLen - 1) {
                    strBuffer.append(", ");
                }
            }
            if (fLen > 0) {
                strBuffer.append("\n");
            } else {
                strBuffer.append(";\n");
            }
        } else {
            /**
             * parse class or interface field
             */
            for (int i = 0; i < fLen; i++) {
                Field field = fields[i];
                String fSignature = getSignature(Field.class, field);
                String cSignature = null;
                String typeName;
                if (fSignature != null) {
                    typeName = getFieldType(pkgName, fSignature);
                } else {
                    cSignature = field.getType().getName();
                    if (cSignature != null && cSignature.startsWith("[")) {
                        typeName = getArrayType(pkgName, cSignature);
                    } else {
                        typeName = getClassName(pkgName, field.getType(), false);
                    }
//                    typeName = getClassName(pkgName, field.getType(), false);
                }
                logger.d(TAG, "clz=" + clz + ",field=" + field + ",FSignature=" + fSignature + ",CSignature=" + cSignature + ",simpleSig=" + typeName);
                strBuffer.append(TAB + Modifier.toString(field.getModifiers()) + " " +
                        typeName + " " + field.getName());
                if (Modifier.isStatic(field.getModifiers())) {
                    try {
                        strBuffer.append(" = " + getDefaultValue(field.getType().getName()));
                        logger.d(TAG, "exportJavaInfo:===" + field + " = " + getDefaultValue(field.getType().getName()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                strBuffer.append(";\n");
            }
        }

        /**
         * parse constructor
         */
        Constructor[] constructors = clz.getConstructors();
        int cLen = constructors.length;
        if (cLen > 0) {
            boolean hasNoneDefaultCtor = false;
            for (int i = 0; i < cLen; i++) {
                Constructor constructor = constructors[i];
                if (constructor.getParameterTypes().length > 0) {
                    hasNoneDefaultCtor = true;
                    break;
                }
            }
            /**
             * parse constructor only if there is more than one non-default constructor
             */
            if (hasNoneDefaultCtor) {
                for (int i = 0; i < cLen; i++) {
                    Constructor constructor = constructors[i];
                    String signature = getSignature(Constructor.class, constructor);
                    strBuffer.append(TAB + Modifier.toString(constructor.getModifiers()) + " " +
                            clz.getSimpleName() + "(");
                    logger.d(TAG, "constructor=" + constructor.toString() + ",signature=" + signature);
                    String paraStr = getParameter(pkgName, signature, constructor.getParameterTypes());
                    if (paraStr != null) {
                        strBuffer.append(paraStr);
                    }
                    strBuffer.append(") { }\n");
                }
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
            String signature = getSignature(Method.class, method);
            logger.d(TAG, "class=" + clz.getSimpleName() + ",method=" + method.getName() +
                    ",signature=" + signature + ",rtnSignature=" +
                    (signature != null ? getRtnTypeName(pkgName, signature) : ""));
            Class rtnType = method.getReturnType();
            String rtnTypeName = rtnType.getName();
            String rtnTypeStr;
            if (signature != null) {
                rtnTypeStr = getRtnTypeName(pkgName, signature);
            } else {
                rtnTypeStr = getClassName(pkgName, rtnType, false);
            }
            strBuffer.append(TAB + (clz.isInterface() || clz.isAnnotation() ? "" :
                    Modifier.toString(method.getModifiers())) + " " +
                    rtnTypeStr + " " + method.getName() + "(");

            String paraStr = getParameter(pkgName, signature, method.getParameterTypes());
            if (paraStr != null) {
                strBuffer.append(paraStr);
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

    /**
     * get simple class name with cutting of package name, cut java.lang or current package name
     *
     * @param pkgName
     * @param clzName
     * @return
     */
    private String getSimpleClassName(String pkgName, String clzName) {
        String rtnName;
        if (clzName.startsWith(BASE_PACKAGE) && clzName.lastIndexOf(".") == BASE_PACKAGE.length()) {
            rtnName = clzName.substring(clzName.lastIndexOf(".") + 1);
        } else if (clzName.startsWith(pkgName) && clzName.lastIndexOf(".") == pkgName.length()) {
            rtnName = clzName.substring(clzName.lastIndexOf(".") + 1);
        } else {
            rtnName = clzName;
        }
        return rtnName;
    }

    /**
     * @param pkgName
     * @param clz
     * @param isClassDefine
     * @return
     */
    private String getClassName(String pkgName, Class clz, boolean isClassDefine) {
        String clzName = clz.getName();
        String rtnName;
        String typeVar = getClassTypeVariableSimple(clz);
        if (!isClassDefine && typeVar != null) {
            return typeVar;
        } else if (clzName.startsWith(BASE_PACKAGE) && clzName.lastIndexOf(".") == BASE_PACKAGE.length()) {
            rtnName = clz.getSimpleName();
        } else if (clzName.startsWith(pkgName) && clzName.lastIndexOf(".") == pkgName.length()) {
            rtnName = clz.getSimpleName();
        } else {
            rtnName = clzName;
        }
        logger.d(TAG, "getClassName:clz=" + clz.toString() + ", clzName=" + clzName + ", rtnName=" + rtnName);
        return rtnName;
    }

    /**
     * get type variable of a generics type class
     *
     * @param pkgName
     * @param typeClz
     * @return
     */
    private String getClassTypeVariable(String pkgName, Class typeClz) {
        TypeVariable<Class<?>>[] typeVariables = typeClz.getTypeParameters();
        logger.d(TAG, "clzInfo:" + typeClz.getCanonicalName() + "," + typeVariables);
        int vLen = typeVariables.length;
        if (vLen > 0) {
            StringBuffer strBuffer = new StringBuffer("<");
            int cnt = 0;
            for (int i = 0; i < vLen; i++) {
                TypeVariableItem item = getTypeVariable(pkgName, typeVariables[i]);
                if (item != null) {
                    if (cnt > 0) {
                        strBuffer.append(", ");
                    }
                    strBuffer.append(item.toString());
                    cnt++;
                }
            }
            strBuffer.append(">");
            return strBuffer.toString();
        }
        return null;
    }

    /**
     * get a type variable item with generic name and parent class name pair from TypeVariable instance
     *
     * @param pkgName
     * @param variable
     * @return
     */
    private TypeVariableItem getTypeVariable(String pkgName, TypeVariable<Class<?>> variable) {
        logger.d(TAG, "clzInfo:" + variable.toString() + "," + variable.getClass().getSuperclass());
        StringBuffer strBuffer = new StringBuffer();
        TypeVariableItem item = new TypeVariableItem();
        item.tName = variable.toString();

        Type[] types = variable.getBounds();
        int tlen = types.length;
        if (tlen > 0) {
            strBuffer.append(" extends ");
            for (int j = 0; j < tlen; j++) {
                item.parentName = getClassName(pkgName, (Class) types[j], true);
                logger.d(TAG, "clzInfo:Bounds:" + variable.toString() + "," + types[j].toString() + "," + ((Class) types[j]).getName());
            }
            return item;
        } else {
            return null;
        }
    }

    private String getClassTypeVariableSimple(Class clz) {
        TypeVariable<Class<?>>[] typeVariables = clz.getTypeParameters();
        logger.d(TAG, "clzInfo:" + clz.getCanonicalName() + "," + typeVariables);
        int vlen = typeVariables.length;
        if (vlen > 0) {
            for (int i = 0; i < vlen; i++) {
                logger.d(TAG, "clzInfo:" + typeVariables[i].toString() + "," + typeVariables[i].getClass().getSuperclass());
                return typeVariables[i].toString();
            }
        }
        return null;
    }

    /**
     * get signature of method, constructor or field by reflection. On the case of template class,
     * we can only parse the real type name by signature when the type is Object or T, on
     * reflection, the type of obj(T type) is java.lang.Object, such as:
     * for method:
     * public Object getValue1(T obj) {
     * return null;
     * }
     * return "(TT;)Ljava/lang/Object;"
     *
     * @param clz Class of obj, method, constructor or field
     * @param obj signature from whom to get
     * @return signature of obj
     */
    private String getSignature(Class<?> clz, Object obj) {
        String signature = null;
        try {
            Field signatureField = clz.getDeclaredField("signature");
            signatureField.setAccessible(true);
            signature = (String) signatureField.get(obj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return signature;
    }

    /**
     * parse return type name of a method
     *
     * @param pkgName
     * @param mSignature
     * @return
     */
    private String getRtnTypeName(String pkgName, String mSignature) {
        String rtnSignature = mSignature.substring(mSignature.lastIndexOf(")") + 2, mSignature.length() - 1).replace("/", ".");
        String rtnTypeString = rtnSignature.substring(1);
        if (rtnSignature.length() == 2) {
            return rtnTypeString;
        } else {
            /**
             * attention generics type interface method in a interface type, such as:
             * Class<? extends OutAnnotation> annotationType();
             * so this is different from parameter of method or constructor and field
             */
            if (rtnSignature.contains("<")) {
                String type = getSimpleClassName(pkgName, rtnSignature.substring(0, rtnSignature.indexOf("<")));
                TypeVariableItem item = new TypeVariableItem();
                item.tName = "?";
                item.parentName = getSimpleClassName(pkgName,
                        rtnSignature.substring(rtnSignature.indexOf("+") + 2, rtnSignature.length() - 2));
                return type + "<" + item.toString() + ">";
            } else {
                return getSimpleClassName(pkgName, rtnSignature);
            }
        }
    }

    /**
     * get parameter type array from method or constructor
     *
     * @param pkgName
     * @param mSignature
     * @return
     */
    private String[] getParamTypeNames(String pkgName, String mSignature) {
        String pSignature = mSignature.substring(mSignature.indexOf("(") + 1, mSignature.indexOf(")"));
        if (pSignature.length() == 0) {
            return null;
        }
        String[] pSignatures = pSignature.replace("/", ".").split(";");
        int pLen = pSignatures.length;
        for (int i = 0; i < pLen; i++) {
            if (pSignatures[i].length() == 2) {
                pSignatures[i] = pSignatures[i].substring(1);
            } else {
                pSignatures[i] = getSimpleClassName(pkgName, pSignatures[i].substring(1));
            }
        }
        return pSignatures;
    }

    private String getFieldType(String pkgName, String fSignature) {
        String simpleSig = fSignature.replace("/", ".")
                .replace(";", ",")
                .replace(",>", ">");
        simpleSig = simpleSig.substring(0, simpleSig.length() - 1);//remove last ','
        StringBuffer buff = new StringBuffer();
        int len = simpleSig.length();
        int idx = 1;
        int start = 0;
        char ch, cs;
        String typeSig;
        String typeName;
        ///TODO
        while (idx < len) {
            cs = simpleSig.charAt(start);
            while ((cs == '<' || cs == '>' || cs == ',' || cs == '+') && start < len - 1) {
                cs = simpleSig.charAt(++start);
            }
            ch = simpleSig.charAt(idx);
            switch (ch) {
                case '<':
                case '>':
                case ',':
                    if (idx - start > 0) {
                        if (idx - start > 2) {
                            if (cs == '[') {
                                typeSig = simpleSig.substring(start + 2, idx);
                            } else {
                                typeSig = simpleSig.substring(start + 1, idx);
                            }
                            typeName = getSimpleClassName(pkgName, typeSig);
                            buff.append(typeName);
                        } else if (idx - start > 1) {
                            typeSig = simpleSig.substring(start + 1, idx);
                            typeName = getBasicType(typeSig.charAt(0));
                            if (typeName != null) {
                                buff.append(typeName);
                            } else {
                                buff.append(typeSig);
                            }
                        } else {

                        }
                        if (cs == '[') {
                            buff.append("[]");
                        }
                    }
                    buff.append(ch);
                    start = idx++;
                    break;
                case '+':
                    buff.append("? extends ");
                    start = idx++;
                    break;
                case '[':
                    if (cs != '[') {
                        idx++;
                    } else {
                        start = idx++;
                    }
                    break;
                default:
                    if (cs == '[' && idx - start == 1) {
                        String tmp = getBasicType(ch);
                        if (tmp != null) {
                            buff.append(tmp + "[]");
                            start = idx++;
                            if (idx < len) {
                                ch = simpleSig.charAt(idx);
                                if (ch == 'L' || ch == '[') {
                                    buff.append(",");
                                }
                            }
                        } else {
                            idx++;
                        }
                    } else {
                        idx++;
                    }
                    break;
            }
        }
        if (idx - start >= 2) {
            typeSig = simpleSig.substring(start + 1, idx);
            typeName = getSimpleClassName(pkgName, typeSig);
            buff.append(typeName);
        }
        return buff.toString().replace(BASE_PACKAGE + ".", "").replace(pkgName + ".", "");
    }

    private String getArrayType(String pkgName, String signature) {
        String typeStr = signature.substring(1);
        String typeName;
        if (typeStr.length() == 1) {
            typeName = getBasicType(typeStr.charAt(0));
        } else {
            typeName = getSimpleClassName(pkgName, typeStr.substring(1, typeStr.length() - 1));
        }
        return typeName + "[]";
    }

    public ParseItem parseTypeFromSignature(String pkgName, String input) {
        String signature = input.replace("/", ".");
        int idx = 0;
        char ch;
        int len = signature.length();
        StringBuffer buff = new StringBuffer();
        int refIdx = -1;
        int genIdx = -1;
        int colIdx = -1;
        String arr = "";
        boolean lastRef = false;
        boolean isFinished = false;
        while (idx < len && !isFinished) {
            ch = signature.charAt(idx);
            switch (ch) {
                case 'Z'://boolean
                case 'B'://byte
                case 'C'://char
                case 'S'://short
                case 'I'://int
                case 'J'://long
                case 'F'://float
                case 'D'://double
                    if (refIdx >= 0) {

                    } else if (colIdx >= 0) {

                    } else {
                        String tmpType = getBasicType(ch);
                        if (tmpType != null) {
                            buff.append(tmpType);
                            isFinished = true;
                        }
                    }
                    break;
                case 'L'://reference type start, must end with ';'
                    if (refIdx < 0) {
                        refIdx = idx;
                    }
                    if (lastRef) {
                        buff.append(", ");
                        lastRef = false;
                    }
                    break;
                case 'T'://generic type start, must end with ';'
                    if (genIdx < 0) {
                        genIdx = idx;
                    }
                    break;
                case ';'://reference type end
                    if (refIdx >= 0) {
                        String tmpType = getSimpleClassName(pkgName, signature.substring(refIdx + 1, idx));
                        if (lastRef) {
                            buff.append(", ");
                            lastRef = false;
                        }
                        buff.append(tmpType);
                        refIdx = -1;
                        lastRef = true;
                    } else if (genIdx >= 0) {
                        String tmpType = getSimpleClassName(pkgName, signature.substring(genIdx + 1, idx));
                        if (lastRef) {
                            buff.append(", ");
                            lastRef = false;
                        }
                        buff.append(tmpType);
                        genIdx = -1;
                        lastRef = true;
                    }
                    if (buff.length() > 0) {
                        isFinished = true;
                    }
                    break;
                case '<'://collection type start
                    if (refIdx < 0) {
                        logger.d(TAG, "Error signature:" + signature);
                        return null;
                    } else {
                        String tmpType = signature.substring(refIdx + 1, idx);
                        buff.append(tmpType + "<");
                        refIdx = -1;
//                        colIdx = idx;
                    }

                    int end = signature.lastIndexOf(">");
                    if (end < idx) {
                        end = len;
                    }
                    if (end > idx) {
                        String subSig = signature.substring(idx + 1, end);
                        ParseItem item = parseTypeFromSignature(pkgName, subSig);
                        String subType = item.result;
                        if (item.parseLength < subSig.length()) {
                            String rightSig = signature.substring(idx + 1 + item.parseLength, end);
                            ParseItem right = parseTypeFromSignature(pkgName, rightSig);
                            String rightType = right.result;
                            if (rightType.equals(">")) {
                                buff.append(subType + rightType);
                                idx += item.parseLength + right.parseLength;
                                isFinished = true;
                            } else {
                                buff.append(subType + ", " + rightType);
                                idx += item.parseLength + right.parseLength;
                            }
                        } else {
                            buff.append(subType + ">");
                            idx = end;
                        }
                    }
                    lastRef = false;
                    break;
                case '>'://collection type end
                    colIdx = -1;
                    buff.append(">");
                    break;
                case '+'://wildcard type, <? extends xxx>
                    if (lastRef) {
                        buff.append(", ");
                        lastRef = false;
                    }
                    buff.append("? extends ");
                    break;
                case '['://array start
                    int arrLen = getArrayLen(signature, idx);
                    for (int i = 0; i < arrLen; i++) {
                        arr += "[]";
                    }
                    String subSig = signature.substring(idx + arrLen);
                    ParseItem item = parseTypeFromSignature(pkgName, subSig);
                    String subType = item.result;
                    if (lastRef) {
                        buff.append(", ");
                        lastRef = false;
                    }
                    buff.append(subType + arr);
                    idx += item.parseLength;
                    if (item.subItem) {
                        isFinished = true;
                    }
                    lastRef = true;
                    idx += arrLen;
                    continue;
            }
            idx++;
        }
        return new ParseItem(buff.toString(), idx, idx < len);
    }

    private int getArrayLen(String signature, int idx) {
        int len = signature.length();
        int i = idx;
        for (; i < len; i++) {
            if (signature.charAt(i) != '[') {
                break;
            }
        }
        return i - idx;
    }

    /**
     * get all parameter in final mode from method or constructor
     *
     * @param pkgName
     * @param signature
     * @param parameters
     * @return
     */
    private String getParameter(String pkgName, String signature, Class[] parameters) {
        /**
         * if signature is not null, we parse the parameter type from signature to distinguish type
         * name from Object and T
         */
        StringBuffer strBuffer = new StringBuffer();
        if (signature != null) {
            String[] params = getParamTypeNames(pkgName, signature);
            if (params != null) {
                int len = params.length;
                for (int i = 0; i < len; i++) {
                    strBuffer.append(params[i] + " arg" + i);
                    if (i < len - 1) {
                        strBuffer.append(", ");
                    }
                }
                return strBuffer.toString();
            }
        }

        /**
         * parse the parameter type by normal reflection, if the signature is null, the type just
         * basic type (eight basic type)
         */
        int len = parameters.length;
        for (int i = 0; i < len; i++) {
            Class parameter = parameters[i];
            strBuffer.append(getClassName(pkgName, parameter, false) + " arg" + i);
            if (i < len - 1) {
                strBuffer.append(", ");
            }
        }
        return strBuffer.length() == 0 ? null : strBuffer.toString();
    }

    /**
     * type map from byte code type to basic type
     * Z   boolean
     * B   byte
     * C   char
     * S   short
     * I   int
     * J   long
     * F   float
     * D   double
     *
     * @param ch
     * @return
     */
    private String getBasicType(char ch) {
        String value = null;
        for (int i = 0; i < BASIC_TYPE_COUNT; i++) {
            if (ch == BASIC_TYPE_BYTE_CODE[i]) {
                value = BASIC_TYPE[i];
                break;
            }
        }
        return value;
    }

    /**
     * get default value of a field of return value of a method, just return a fused value
     *
     * @param type
     * @return
     */
    private String getDefaultValue(String type) {
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
                value = "null";
            }
        }
        return value;
    }

    private void writeBufferToFile(StringBuffer buffer, String fileName) {
        logger.d(TAG, "writeBufferToFile:fileName:" + fileName);
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
