# 部署文档（以Ubuntu为例）

## 数据库

- 使用Docker或者直接安装MySQL

```bash
docker pull mysql # docker
sudo apt install mysql # 直接安装
```

- 执行数据库SQL脚本`CreateDB.sql`，构建所需的数据库。

- 配置防火墙，放行3306端口。

## Java端

- 运行需要安装JRE（最低要求Java 8）：

```bash
sudo apt update
sudo apt install openjdk-8-jre
```

- 运行前需要将虹软人脸识别SDK的so文件拷贝到lib目录下：

```bash
cd aicas-master/libs/LINUX64
cp libarcsoft_face.so $JAVA_HOME/bin
cp libarcsoft_face_engine.so $JAVA_HOME/bin
cp libarcsoft_face_engine_jni.so $JAVA_HOME/bin
```

- 更新lib库

```bash
sudo ldconfig
```

- 安装JDK：

```bash
sudo apt install openjdk-8-jdk
```

- 在源代码根目录`aicas-master`中编译源代码：

```bash
./gradlew build
```

- 编译结束之后在`./build/libs`下找到`aicas-1.0.jar`

```bash
java -jar aicas-1.0.jar
```

- 配置防火墙，放行8080端口。

## Web端

- 使用Docker或者直接安装NGINX：

```bash
sudo apt install nginx
```

- 将`aicas_web-master`中代码放入NGINX的html目录下，一般是`/var/www/html`，或者修改nginx.conf更改为自定义目录

- 启动NGINX：

```bash
nginx
```

- 配置防火墙，放行80端口。

## Android端

- 安装Android集成开发环境Android Studio

- 通过Android Studio打开项目文件夹`FaceAttendance-master`

- 点击菜单栏中Build-Build Apk，得到apk文件

- 将apk文件安装到手机上即可