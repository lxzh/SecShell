
[toc]

## 一、Gradle插件

### Gradle插件简介

Gradle 的核心为真实世界提供了很少的自动化. 所有的实用特性,类似编译java源码的能力, 是由插件提供的. 插件添加了新的任务(如:JavaCompile),域对象(如:SourceSet),公约(如:Java资源位置是src/main/java)以及来自其他插件延伸核心对象和对象。

### Gradle插件的作用
#### 1. google官方gradle插件的作用

1. 为项目配置依赖
2. 为项目配置约定，比如约定源代码的存放位置
3. 为项目添加任务，完成测试、编译、打包等任务
4. 为项目中的核心对象和其他插件的对象添加拓展类型

插件|简介|作用
---|---|---
com.android.application | 安卓应用插件 | 负责app的资源解析、源码编译、字节码混淆、apk打包、签名等
com.android.library | 安卓组件插件 | 负责源码编译、资源解析、aar打包

#### 2. 常见第三方gradle插件的作用

1. 编译前资源文件、配置文件、参数配置检查
2. 自动生成资源、配置参数
3. 自动注入代码（组件注册，自动初始化，函数插桩统计分析等）
4. 复用构建编译打包自动化流程

插件|简介|作用
---|---|---
io.fabric.tools | Fabric插件 | 复杂检查Manifest是否配置ApiKey、自动生成build_id字符串资源
com.google.gms.google-services | Firebase的gms服务插件 |负责解析google-services.json配置文件，
com.mob.sdk| ShareSDK 分享管理插件 | 负责解析gradle配置，引入各个分享SDK与平台SDK，自动生成Manifest配置



### 自定义Gradle插件

#### 新建插件工程

1. 在当前工程下新建`Java Library`的Module，取名字为`gtsdk`
2. 将`main/src/java`目录修改为`main/src/groovy`
3. 新建文件, 如`GTPlugin`, 修改文件名为`GTPlugin.groovy`
4. main下添加`resources/META_INF/gradle-plugins`文件夹，存放用于标识gradle插件的meta-data
5. 新建`gtsdk.properties`配置文件, 命名与配置内容稍后介绍

一个简单的gradle插件完整工程结构如下：
![Gradle插件Module目录结构](imgs/Gradle插件Module目录结构.png)

#### 说明

1. 由于AS当前未提供创建gradle插件module的模板，以上通过新建Java Library的module，然后修改工程结构完成。/src/main目录一般依据不同的编程语言或文件类别创建不同的目录，如常见的java、cpp/jni、kotlin、aidl、res, 这里groovy类似，基于groovy语法来编写插件。
2. **[Groovy](http://www.groovy-lang.org/index.html)** 是 用于Java虚拟机的一种敏捷的动态语言，它是一种成熟的面向对象编程语言，既可以用于面向对象编程，又可以用作纯粹的脚本语言。使用该种语言不必编写过多的代码，同时又具有闭包和动态语言中的其他特性。
3. `resources/META_INF/gradle-plugins`这个文件夹结构是强制要求的，否则不能识别成插件
4. properties配置文件的名称对应该插件的Id，参考[官方文档](https://docs.gradle.org/current/userguide/custom_plugins.html#example_wiring_for_a_custom_plugin)

#### 编写插件

1. 创建类GTPlugin实现Plugin接口并重写apply(T garget)方法

```groovy
package com.geetest

import org.gradle.api.Plugin
import org.gradle.api.Project

class GTPlugin implements Plugin<Project> {
    def final TAG = "GTPlugin"
    @Override
    void apply(Project project) {
        println(TAG+":apply start****************")
        project.configurations.all { configuration ->
            def name = configuration.name
            System.out.println("this configuration is ${name}")
            if (name != "implementation" && name != "compile") {
                return
            }
            configuration.dependencies.add(project.dependencies.create("com.google.code.gson:gson:2.8.6"))
        }
    }
}
```
该插件通过遍历当前引用的项目的所有配置，遇到`implementation`或`compile`时，增加一个google的gson依赖项, 也就是自动为当前项目引入依赖。

其中apply方法稍微修改一下也可以改成:

```groovy
    @Override
    void apply(Project project) {
        println(TAG+":apply start****************")
        def implementation = project.configurations.getByName("implementation")
        implementation.dependencies.add(project.dependencies.create("com.alibaba:fastjson:1.1.55.android"))
        def compile = project.configurations.getByName("compile")
        compile.dependencies.add(project.dependencies.create("com.alibaba:fastjson:1.1.55.android"))
    }
```

> gradle插件从**3.0**开始弃用compile，推荐使用**implementation**

2. 修改properties配置文件

修改配置文件**resources/META_INF/gradle-plugins/gtsdk.properties**，声明插件主类

```
implementation-class=com.geetest.GTPlugin
```

> 这个配置跟开发jar包时在gradle中配置的Main-Class类似

```
manifest { attributes 'Main-Class': 'com.xxx.MainClass' }
```

2. 修改build.gradle文件

应用`groovy`与`maven`插件，并添加`gradleApi`与`localGroovy`两个依赖

```groovy
apply plugin: 'groovy'
apply plugin: 'maven'

dependencies {
    implementation gradleApi()
    implementation localGroovy()
}

def artifact_version='1.0.0'        // 项目引用的版本号
def artifact_group = 'com.geetest'  // 唯一包名
def artifact_id = 'gtsdk'           // 插件Id
group = artifact_group
version = artifact_version

task sourcesJar(type: Jar) {
    from project.file('src/main/groovy')
    classifier = 'sources'
}

artifacts {
    archives sourcesJar
}

uploadArchives {
    repositories {
        mavenDeployer {
            repository(url: uri('../repo')) //deploy到本地repo目录
            pom.groupId = artifact_group
            pom.artifactId = artifact_id    //插件Id
            pom.version = artifact_version
        }
    }
}
```

这里的`uploadArchives`表示打包上传，本例打包后发布到项目根目录下的repo目录，这种方式依赖于maven插件。需要上传到maven仓库或者jcenter也可以在这里配置。
通过在终端执行`gradle gtsdk:uploadArchives`或者点击右侧Gradle任务均可完成打包发布:
![打包发布Gradle插件](imgs/打包发布Gradle插件.png)

发布后本地目录:

![](imgs/发布后本地插件仓库目录结构.png)

发布到Jcenter参考:https://www.jianshu.com/p/275fc9d54e4a
f发布到maven参考: 

####  测试验证插件

1. 在根项目的build.gradle中添加`本地maven仓库`与`classpath`依赖

```groovy
buildscript {
    repositories {
        maven{ url rootProject.file("repo") }
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.5.1'
        classpath 'com.geetest:gtsdk:1.0.0'
    }
}
```

2. 在模块的build.gradle中应用插件

```
apply plugin: 'gtsdk'
apply plugin: com.geetest.GTPlugin
```

以上两种方式均可：
前者根据插件的Id来查找插件，对应于properties文件名，以及发布时的pom.artifactId属性;
后者根据插件主类的全限定名匹配。

运行右侧Gradle中的模块build任务，查看任务日志可看到打印`GTPlugin`开头的日志即表示插件成功应用

## 二、ShareSDK gradle插件调研

### 插件源码获取

AndroidStudio添加插件仓库并在模块中启用插件后，会自动缓存插件到本地，路径如下：
`$GRADLE_USER_HOME$/caches/modules-2/files-2.1/com.mob.sdk/MobSDK-Impl`
在目录下会找到一个`MobSDK-Impl-xxversion-sources.jar`的jar包，即为插件的源码包

### 插件分析

1. 平台分享接入配置

根据官方提供的Demo，用户在使用ShareSDK时需要在Gradle中配置appKey等参数以及各个目标分享平台的配置参数，[参考这里](https://github.com/MobClub/ShareSDK-for-Android/blob/master/SampleFresh/MobSDK.gradle), 如下所示：

```groovy
MobSDK {
    appKey "moba0b0c0d0"
    appSecret "5713f0d88511f9f4cf100cade0610a34"

    MobLink {
        uriScheme "moba0b0c0d0://cn.sharesdk.demo"
        appLinkHost "ahmn.t4m.cn"
    }
    ShareSDK {
        loopShare true
        //平台配置信息
        devInfo {
            SinaWeibo {
                id 1
                sortId 59
                appKey "568898243"
                appSecret "38a4f8204cc784f81f9f0daaf31e02e3"
                callbackUri "http://www.sharesdk.cn"
                shareByAppClient true
                enable true
            }
            Wechat {
                id 4
                sortId 4
                appId "wx4868b35061f87885"
                appSecret "64020361b8ec4c99936c0e3999a9f249"
                userName "gh_afb25ac019c9"
                path "pages/index/index.html?id=1"
                withShareTicket true
                miniprogramType 0
                bypassApproval false
                enable true
            }
            WechatMoments {
                id 5
                sortId 5
                appId "wx4868b35061f87885"
                appSecret "64020361b8ec4c99936c0e3999a9f249"
                bypassApproval false
                enable true
            }

            QQ {
                id 7
                sortId 7
                appId "100371282"
                appKey "aed9b0303e3ed1e27bae87c33761161d"
                shareByAppClient false
                bypassApproval false
                enable true
            }
        }
    }
}
```

2. 插件源码解析

插件源码目录结构
![目录结构](imgs/com.mob.sdk_src_struct.png)

![各产品配置](imgs/com.mob.sdk_products.png)

![自动增删依赖配置](imgs/com.mob.sdk_dependency.png)

`\src\main\groovy\com\mob\MobSDKConfig.groovy`解析gradle，根据接入的sdk配置动态调整依赖sdk

![分享平台与三方sdk映射集](imgs/com.mob.sdk_devinfo.png)

`\src\main\groovy\com\mob\products\sharesdk\DevInfo.groovy`根据分享平台配置判断是否需要引入三方sdk的jar包

![自动生成AndroidManifest配置](imgs/com.mob.sdk_configcreator.png)

`src\main\groovy\com\mob\ConfigCreator.groovy`
根据接入的分享平台自动生成AndroidManifest配置，包括新增权限、meta-data配置、uses-feature特性列表、SDK/平台Activity声明等，构建结束后追加到原始AndroidManifest.xml文件中

### 插件功能概述

引入ShareSDK插件后，根据gradle配置解析接入的分享平台与功能模块，自动引入模块SDK与三方平台SDK，并且动态生成配置在构建结束后修改原始配置文件

### 插件与SDK设计

- 所有的分享模块与自家其他功能模块，统一配置参数，SDK的接入转换为相似的参数配置
- 通过插件管理SDK接入配置，减轻用户接入负担
- 插件统一管理接入模块与版本，用户无需去各大平台官方文档找插件名、版本等配置
- 分享模块统一重设计，统一包装，简化用户接入
- 统一版本同时更新，自家仓库发布，
 
> 学习与借鉴:
> 多SDK开发过程中，尽量保证对外接口统一且精简，用户APP往往会接入很多个三方SDK，繁杂的接入步骤与配置参数只会带来更多的沟通成本与问题

## 三、Gradle与Groovy语言

### 1.Gradle依赖缓存管理策略

1. Gradle将maven依赖分为：

[参考](https://blog.csdn.net/yu757371316/article/details/101292749)

```
dynamic version (e.g. xxxx:xxx:1.+)  动态版本
concrete version (e.g. xxxx:xxx:1.2) 固定版本
```

2. 构建时动态更新
```
gradle build --refresh-dependencies
```

3. 依赖缓存策略[(官方Doc)](https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/ResolutionStrategy.html):
```groovy
cacheChangingModulesFor(int,java.util.concurrent.TimeUnit)
cacheDynamicVersionsFor(int, java.util.concurrent.TimeUnit)
```

```groovy
project.configurations.all {
    //采用动态版本声明的依赖缓存2小时
    resolutionStrategy.cacheDynamicVersionsFor 2, 'hours'
    //每隔1小时检查远程依赖是否存在更新
    resolutionStrategy.cacheChangingModulesFor 60 * 60, 'seconds'
}
```

### 2. 管理依赖的版本

- 传递(transitive)
- 排除(exclude)
- 强制(force)
- 动态版本(+)

1. 开启关闭传递依赖
```groovy
implementation('xxx:xxx:x.x.x' {
    transitive = true
})
```

2. 排除依赖
```groovy
configurations {
    //编译期排除commons模块
    compile.exclude module: 'commons'
    //在整个构建过程中排除pkaq.tiger：share
    all*.exclude group: 'pkaq.tiger', module: 'share'
}

dependencies {
    compile("pkaq.tiger:web:1.0") {
        exclude module: 'share'
    }       
}
```

3. 强制版本
```groovy
compile('org.hibernate:hibernate:3.1') {
    force = true
}
//全局配置强制使用某个版本的依赖来解决依赖冲突中出现的依赖
configurations.all {
   resolutionStrategy {
       force 'org.hamcrest:hamcrest-core:1.3'
   }
}
```

4. 综合示例
```groovy
compile('org.hibernate:hibernate:3.1') {
    // 冲突时优先使用该版本
    force = true

    // 依据构建名称排除
    exclude module: 'cglib' 
    // 依据组织名称排除
    exclude group: 'org.jmock' 
    // 依据组织名称+构件名称排除
    exclude group: 'org.unwanted', module: 'iAmBuggy' 

    // 为本依赖关闭依赖传递特性
    transitive = false
}
```

5. 其他
各种仓库依赖、排除依赖传递、自定义依赖方式、仓库密码保护、黑名单替代特定版本 [参考](https://yq.aliyun.com/articles/445015)


### Groovy

1. Groovy简介

Groovy 是 JVM 的一个替代语言 —替代 是指可以用 Groovy 在 Java 平台上进行 Java 编程，使用方式基本与使用 Java 代码的方式相同。在编写新应用程序时，Groovy 代码能够与 Java 代码很好地结合，也能用于扩展现有代码。

Groovy 的一个好处是，它的语法与 Java 语言的语法很相似。也源于 Smalltalk 和 Ruby 这类语言的理念，但是可以将它想像成 Java 语言的一种更加简单、表达能力更强的变体。

2. Groovy特性

- 无类型定义
- 灵活的范围与集合定义
```groovy
//定义范围
"0..4" 表明包含 整数 0、1、2、3、4
"0..<4" 表示 0、1、2、3
"a..e" 相当于 a、b、c、d、e
"a..<e" 表示 a、b、c、d
//定义集合
def range = 0..4
def coll = ["Groovy", "Java", "Ruby"]
//集合添加项
coll.add("Python")
coll << "Smalltalk"
coll[5] = "Perl"

def numbers = [1,2,3,4]
numbers + 5     //增加一项  [1,2,3,4,5]
numbers - [2,3] //减少两项  [1,4]
```

- getter/setter关联属性
- 闭包

```groovy
//遍历集合
def acoll = ["Groovy", "Java", "Ruby"]
acoll.each{
 println it
}
acoll.each{ value ->
 println value
}
//遍历映射/Map
def hash = [name:"Andy", "VPN-#":45]
hash.each{ key, value ->
 println "${key} : ${value}"
}
//局部匿名函数
def excite = { word ->
 return "${word}!!"
}
```

2. 运行时扩展

- 对现有类扩展

a. 通过`metaClass`与`"<<"`符号可对现有类扩充方法
b. 通过`metaClass`与`"="`符号可覆写现有类的方法
```groovy
    //增加getter属性
    TestClass.metaClass.getPro << { -> proper }

    //重写toString方法
    TestClass.metaClass.toString = { ->
        def buffer = new StringBuffer()
        for (def key : TestClass.metaClass.getProperties()) {
            if (key.field != null) {
                buffer.append("[" + key.field.name + "," +
                        TestClass.metaClass.getProperty(delegate, key.field.name) + "]\n")
            }
        }
        return buffer.toString()
    }
```

- 函数、属性Hook
![Groovy拦截机制](imgs/Groovy拦截机制.webp)

a. 实现`groovy.lang.GroovyInterceptable`接口后可通过invokeMethod拦截任何方法调用

b. 通过`methodMissing`可拦截任意未实现的方法调用

c. 通过`propertyMissing`可拦截任意未定义的属性访问

- Categories分类，向类添加功能
