# SecShell

[英文说明](README.md "英文")

## 1. 准备sdk

- 编译sdk

编译好待加固的sdk，如本例中的libcore

```
gradle libcore:build
```

- 解压aar

解压sdk编译生成的aar文件: `libcore/build/outputs/aar/libcore.aar`, 从aar包中提取jar文件：`libcore.jar`, 拷贝到`libsag/output`目录用于后续生成stub-sdk

- jar转换为dex

因为BaseDexClassLoader无法识别与加载java打包jar的字节码，需要通过dx工具来优化Dalvik字节码，所以我们可以用AndroidSDK自带的dx工具进行转换：

```
dx --dex --output=libcore.dex libcore.jar
```

或者用第三方工具d2j-jar2dex.bat:

```
d2j-jar2dex.bat libcore.jar -o libcore.dex
```

转换后拷贝到`libmix/output`用于生成加固dex

## 2. sdk混淆

- 加密sdk

 使用libmix加密sdk

```
gradle libmix:build
```

编译libmix来执行加密任务, 编译脚本会自动完成加密dex并拷贝到`libshell/src/main/assets/`


- 生成stub-sdk

为了隐藏我们sdk的关键代码并且确保用户能够正常开发，我们提供了stub-sdk的方式供用户compileOnly引用，实际sdk通过shell工具进行脱壳加载。
生成stub-sdk基于java的反射原理，通过加载sdk的aar包中中jar文件来生成空的java类。

```
gradle libsag:build
```

编译libsag来执行生成stub-sdk任务, 编译脚本会自动完成解析jar包并且写入不带函数实现的java文件到stub工程corestub源码目录:`corestub/src/main/java/`

- 编译stub-sdk

```
gradle corestub:build
```

编译corestub来生成`corestub.aar`用于用户app通过**compileOnly**引用, 本例中编译脚本会自动拷贝aar到`demo/geetestsdk/`中

## 3. build demo

```
gradle demo:build
```

编译demo工程生成演示apk
