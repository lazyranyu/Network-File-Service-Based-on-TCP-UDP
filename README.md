# Network-File-Service-Based-on-TCP-UDP
基于Java Socket TCP和UDP实现一个简易的网络文件服务程序，包含服务器端FileServer和客户端FileClient；
服务器端启动时需传递root目录参数，并校验该目录是否有效；
服务器启动后，开启TCP：2021端口，UDP：2020端口，其中，TCP连接负责与用户交互，UDP负责传送文件；
客户端启动后，连接指定服务器的TCP 2021端口，成功后，服务器端回复信息：“客户端IP地址:客户端端口号>连接成功”；
连接成功后，用户可通过客户端命令行执行以下命令：
	[1]	ls	服务器返回当前目录文件列表（<file/dir>	name	size）
	[2]	cd  <dir>	进入指定目录（需判断目录是否存在，并给出提示）
	[3]	get  <file>	通过UDP下载指定文件，保存到客户端当前目录下
	[4]	bye	断开连接，客户端运行完毕
服务器端支持多用户并发访问，不用考虑文件过大或UDP传输不可靠的问题。
