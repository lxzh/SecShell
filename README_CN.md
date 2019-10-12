# SecShell

[英文说明](README.md "英文")

## 1. 准备加壳工具libshell

> 只需编译一次，后续libshell保持不变，只需替换assets中的sdk.data即可

```
gradle libshell:build
```
编译生成libshell.aar并拷贝到sdk目录

## 2. 准备sdk

- 编译sdk

编译好待加固的sdk，如本例中的libcore

```
gradle libcore:build
```

编译完成后拷贝到sdk目录，并修改gradle.properties中的`SDK_AAR_NAME`配置为对应aar的文件名

## 3. sdk混淆

- 生成stub-sdk

为了隐藏我们sdk的关键代码并且确保用户能够正常开发，我们提供了stub-sdk的方式供用户compileOnly引用，实际sdk通过shell工具进行脱壳加载。
生成stub-sdk基于java的反射原理，通过加载sdk的aar包中中jar文件来生成空的java类。

```
gradle libsag:build
```

编译libsag会依次执行以下任务：

1. 解压sdk的aar文件到sdk/sdk目录，提取其中的classes.jar包到sdk目录并重命名为sdk.jar
2. 解压libshell的aar文件到sdk/shell目录
3. 转换sdk.jar为sdk.dex文件，用于加密用
4. 基于sdk.jar生成空的java类源码到corestub的java目录：`corestub/src/main/java/`，路径由gradle.properties中的指定`SDK_STUB_SRC_FOLDER`

- 加密sdk

 使用libmix加密sdk并重新打包libshell

```
gradle libmix:build
```

编译libmix来执行加密任务, 编译脚本会自动完成加密dex并拷贝到`sdk/shell/assets/`，然后重新打包libshell为aar文件，自动拷贝aar到指定的demo依赖库目录，具体路径由gradle.properties中的`DEMO_LIB_FOLDER`指定


- 编译stub-sdk

```
gradle corestub:build
```

编译corestub来生成`corestub.aar`用于用户app通过**compileOnly**引用, 本例中编译脚本会自动拷贝aar到demo依赖库目录，具体路径由gradle.properties中的`DEMO_LIB_FOLDER`指定

## 4. build demo

```
gradle demo:build
```

编译demo工程生成演示apk
