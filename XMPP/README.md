简洁XMPP
===========

# **简洁XMPP Android 客户端项目简析** #

*注：本文假设你已经有Android开发环境*

启动Eclipse，点击菜单新建一个Android工程，然后将本项目代码覆盖过去，请确保你当前的Android SDK是最新版。<br>
如果编译出错，请修改项目根目录下的 project.properties 文件。<br>
推荐使用Android 4.0 以上版本的SDK,请使用JDK1.6编译：

> target=android-18

**本项目采用 GPL 授权协议，欢迎大家在这个基础上进行改进，并与大家分享。**

下面将简单的解析下项目：

## **一、项目的目录结构** ##
> 根目录<br>
> ├ src<br>
> ├ libs<br>
> ├ res<br>
> ├ AndroidManifest.xml<br>
> ├ LICENSE.txt<br>
> ├ proguard.cfg<br>
> └ project.properties<br>

**1、src目录**<br>
src目录用于存放项目的包及java源码文件。

下面是src目录的子目录：
> src<br>
> ├ com.way.activity<br>
> ├ com.way.adapter<br>
> ├ com.way.app<br>
> ├ com.way.db<br>
> ├ com.way.exception<br>
> ├ com.way.fragment<br>
> ├ com.way.service<br>
> ├ com.way.smack<br>
> ├ com.way.ui.xx<br>
> └ com.way.util<br>

- com.way.activity — APP所有的Activity包
- com.way.adapter — APP所有的适配器包
- com.way.app — APP启动及管理包
- com.way.db — APP数据库包
- com.way.exception — APP所有自定义异常包
- com.way.fragment — APP所有Fragment包
- com.way.service — APP关键服务包
- com.way.smack — APP对asmack.jar部分功能重新抽取封装包
- com.way.ui.xx — APP第三方控件包，“xx”代表所有控件
- com.way.util — APP通用工具包


**2、libs目录**<br>
libs目录用于存放项目引用到的jar包文件。

下面是libs目录里的jar包文件：
> libs<br>
> ├ android-support-v4.jar<br>
> ├ asmack-android-6.jar<br>
> └ nineoldandroids-2.4.0.jar<br>

- android-support-v4.jar — Android V4支持包
- asmack-android-6.jar — Android Xmpp支持包
- nineoldandroids-2.4.0.jar — Android nine动画支持包

**3、res目录**<br>
res目录用于存放项目的图片、布局、样式等资源文件。

下面是res目录的子目录：
> res<br>
> ├ anim<br>
> ├ color<br>
> ├ drawable<br>
> ├ drawable-hdpi<br>
> ├ drawable-xdpi<br>
> ├ layout<br>
> ├ raw<br>
> ├ values<br>
> ├ values-hdpi<br>
> ├ values-ldpi<br>
> ├ values-mdpi<br>
> └ values-xhdpi<br>


- anim — 动画效果
- color — 颜色
- drawable/drawable-hdpi/drawable-xhdpi — 图标、图片
- layout — 界面布局
- raw — 通知音和changelog.txt
- values — 语言包、风格主题和尺寸
- xml — 系统设置

**4、AndroidManifest.xml**<br>
AndroidManifest.xml用于设置应用程序的版本、主题、用户权限及注册Activity等。


## 联系我

way:
  * [邮箱](mailto:way.ping.li@gmail.com "给我发邮件")
  * [博客](http://blog.csdn.net/way_ping_li/article/details/17385379 "CSDN博客")


## 测试截图
![Screenshot 13](http://git.oschina.net/way/XMPP/raw/master/Screenshot/13.png "Screenshot 13")
![Screenshot 1](http://git.oschina.net/way/XMPP/raw/master/Screenshot/1.jpg "Screenshot 1")
![Screenshot 2](http://git.oschina.net/way/XMPP/raw/master/Screenshot/2.jpg "Screenshot 2")
![Screenshot 3](http://git.oschina.net/way/XMPP/raw/master/Screenshot/3.jpg "Screenshot 3")
![Screenshot 4](http://git.oschina.net/way/XMPP/raw/master/Screenshot/4.jpg "Screenshot 4")
![Screenshot 5](http://git.oschina.net/way/XMPP/raw/master/Screenshot/5.jpg "Screenshot 5")
![Screenshot 6](http://git.oschina.net/way/XMPP/raw/master/Screenshot/6.jpg "Screenshot 6")
![Screenshot 7](http://git.oschina.net/way/XMPP/raw/master/Screenshot/7.jpg "Screenshot 7")
![Screenshot 8](http://git.oschina.net/way/XMPP/raw/master/Screenshot/8.jpg "Screenshot 8")
![Screenshot 9](http://git.oschina.net/way/XMPP/raw/master/Screenshot/9.jpg "Screenshot 9")
![Screenshot 10](http://git.oschina.net/way/XMPP/raw/master/Screenshot/10.jpg "Screenshot 10")
![Screenshot 11](http://git.oschina.net/way/XMPP/raw/master/Screenshot/11.jpg "Screenshot 11")
![Screenshot 12](http://git.oschina.net/way/XMPP/raw/master/Screenshot/12.jpg "Screenshot 12")
