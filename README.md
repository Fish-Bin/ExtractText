# ExtractText
A plugin for extract text.
## 一、引言
新进一家公司，给的第一个任务便是做硬编码优化，要做的事情大概是，把xml中写死的dp、sp、color、文字等，全部映射到dimen.xml和strings.xml中，初步分析，对于dp、sp、color这些并不难，利用正则很快就能替换完，可是字符串又该如何替换呢？难道要一个个布局文件找，并且一个个改么，作为程序员，自然不愿意做这种体力活，如是今天的主角ExtractText登场了，废话不多说，看动画效果。

![动图](https://github.com/Fish-Bin/ExtractText/blob/master/src/com/fish/bin/image/plugin.gif)

## 二、使用方法

 1. 在studio中安装插件，搜索ExtractText即可，安装方法自行查阅，也可去JetBrains直接下载插件的jar,文末已给出下载地址。
 2. 鼠标选中layout文件下的布局文件，点开工具栏中的Code→ExtractText,也可以利用快捷键 ctrl+alt+B，此时会看到底部显示翻译中，成功后会有提示。

## 三、实现原理
插件开发这一块不在此赘述，有兴趣的可以自行百度。我说下ExtactText的实现原理：

 1. 根据用户选中的布局文件，获取到布局文件中每个android:text="originValue"以及android:hint="originValue"标签中的originValue，并放入集合中。
 2. 然后调用google的翻译接口，将集合中的originValue翻译为英文resultValue。
 3. 定义字符串的key，此处我采用的规则是以布局文件的名字拼接originValue，如果originValue的值长度超过5我会用数字代替，当然，如果觉得这种命名规则不妥可以修改插件源码，定制自己想要的规则，文末已给出源码地址。
 4. 在strings.xml中写入映射文件，根据生成的key和第一步获取到的value,拼接成需要的内容并写入strings.xml中。
 5. 替换布局文件中的值，将android:text="originValue"替换为android:text="@string/key",这里是读取到布局文件的内容，做字符串替换来实现的。

## 四、注意问题
 1. 使用此插件需保持网络通畅，因为需要调用google翻译的接口。
 2. strings.xml中生成的映射文件是从倒数第二行开始写入的，所以为了保证写入的内容在《resources》标签内部，务必保证《resuources》标签下面没有多余的行。
 
 ## 五、资源地址
   [项目源码](https://github.com/Fish-Bin/ExtractText)
   
   [插件下载地址](https://plugins.jetbrains.com/plugin/13144-extract-text/versions)
