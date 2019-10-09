# SecShell

[英文说明](README.md "英文")

## 1. 准备sdk

- 编译sdk
编译好待加固的sdk，如本例中的libcore

```
gradle libcore:build
```

- 解压aar

解压sdk编译生成的aar文件: `libcore/build/outputs/aar/libcore-release.aar`, 从aar包中提取jar文件：`libcore.jar`, 拷贝到`libsag/output`目录用于后续生成stub-sdk

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

本方案中生成stub-sdk基于java的反射原理，通过加载sdk的aar包中中jar/dex文件来生成空的java类，由于类加载过程同时需要加载sdk中引用的import包，对于普通的java-library，依赖的是java原生包，可以在PC端生成；对于android-library，依赖Android框架，需要在Android中生成。

所以生成stub-sdk提供了libsag与sag两个工程。前者为jar，使用java中的URLClassLoader来加载jar包，反射生成stub；后者是Android app，使用Android的DexClassLoader加载dex包，同时引用libsag通过JarFile解析jar包提取文件结构，反射生成stub。

1. 使用libsag生成stub-sdk源文件
```
gradle libsag:build
```
编译libsag来执行生成stub-sdk任务, 编译脚本会自动完成解析jar包并且写入不带函数实现的java文件到stub工程corestub源码目录:`corestub/src/main/java/`

2. 使用sag生成stub-sdk源文件
**TODO**

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
