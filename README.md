# Cheetah
连接代理服务器客户端

使用说明：
windows系统双击运行windows.bat，或者双击运行Cheetah.jar
linux或者mac系统运行linux_mac_start.sh，需要配置可执行权限（chmod 744 linux_mac_start.sh ）
点击界面连接图标开始连接服务，连接成功标题会显示listener本机地址，浏览器则配置该地址和端口就可以代理访问


配置文件说明:
config.cfg 配置连接参数，数据加密类型，拦截黑名单
interceptTable.dat 拦截黑名单文件，配置规则见该文件
private.key 和 public.key 是一套密钥，开启RSA才使用
proxyTable.dat 代理文件，配置该文件域名强制走代理或者不走代理
