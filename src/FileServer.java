import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

/**
 * 服务端代码
 *
 * @author : 2021302747
 * @version : [v1.0]
 * @createTime : [2023/10/8 15:08]
 */
public class FileServer {
    private static final int TCP_PORT = 2021;
    private static final int UDP_PORT = 2020;
    private static File root;//根目录
    private static File currentDirectory;//当前目录
    private  static final int BUFFER_SIZE = 1024;//缓冲区大小

    public static void main(String[] args) {
        if (args.length < 1)
        {
            System.out.println("Usage: java FileServer <root_directory>");
            return;
        }

//        String rootDirectory = "C:\\Users\\Lenovo\\Desktop\\分布式实验\\do";
        String rootDirectory = args[0];
        root = new File(rootDirectory);
        if (!root.isDirectory()) {
            System.out.println("请传入正确的参数");
            return;
        }
        currentDirectory = root;

        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT);
            DatagramSocket udpseverSocket = new DatagramSocket(UDP_PORT)) {
            System.out.println("服务器准备完毕，等待客户端.....");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                int clientPort = clientSocket.getPort();
                Thread clientThread = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                         PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                        writer.println("Client connected: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientPort);
                        writer.println("服务已连接");
                        int udpCP = 0;
                        String command;
                        while (true) {
                            do {
                                command = reader.readLine();
                            }while (command.equals(" ")||command.endsWith("\n"));
                            if (command.equals("ls")) {
                                String list;
                                list =listFiles();
                                writer.write(list);
                                System.out.println(list);
                            } else if (command.startsWith("cd")) {
                                String directory = command.substring(2).trim();
                                String directoryOutput;
                                directoryOutput = changeDirectory(directory);
                                writer.println(directoryOutput);
                            } else if (command.startsWith("get")) {
                                String[] tokens = command.split(" ");
                                if (tokens.length == 2) {
                                    String fileName = tokens[1];
                                    File file = new File(currentDirectory, fileName);
                                    if (file.isFile()&&file.exists()) {
                                        long fileSize = file.length();
                                        String fileInfo = "OK\t" + file.getAbsolutePath() + "\t" + fileSize;
                                        writer.println(fileInfo);
                                        String udpac;
                                        udpac = reader.readLine();
                                        if (udpac.startsWith("UDP:"))
                                        {
                                            udpCP = Integer.parseInt(udpac.substring(4).trim());
                                            System.out.println(udpCP);
                                        }
                                        writer.println("开始传输文件："+fileName+"\n");
                                        sendFile(fileName,clientSocket.getInetAddress(),udpCP);
                                    } else {
                                        writer.println("文件不存在或不是普通文件\n");
                                    }
                                  }else {
                                    writer.println("无效命令");
                                }
                              }else if (command.equals("bye")) {
                                break;
                            } else {
                                writer.println("无效命令");
                            }
                            writer.println("%");
                        }
                        writer.close();
                        reader.close();
                        clientSocket.close();
                        System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //处理ls命令，返回当前目录下的文件列表
    private static String listFiles() {
        File[] files = currentDirectory.listFiles();
        StringBuilder output = new StringBuilder();
        if (files != null) {
            for (File file : files) {
                String fileType = file.isDirectory() ? "dir" : "file";
                String fileName = file.getName();
                long fileSize = file.length();
                output.append("<").append(fileType).append(">").append("   ").append(fileName).append("    ").append(fileSize).append("\n");
            }
        }
        return output.toString();
    }

   //处理cd命令，改变当前目录
    private static String changeDirectory(String directory) {
        StringBuilder output = new StringBuilder();
        if (directory.equals("..")) {
            if (!currentDirectory.equals(root)) {
                File parentDirectory = currentDirectory.getParentFile();
                if (parentDirectory != null) {
                    currentDirectory = parentDirectory;
                    output.append("Changed directory to: ").append(currentDirectory.getAbsolutePath());
                    //writer.println("Changed directory to: " + currentDirectory.getAbsolutePath()+"\n");
                } else {
                    output.append("已经是根目录");
                    //writer.println("已经是根目录\n");
                }
            } else {
                output.append("已经是根目录");
                //writer.println("已经是根目录\n");
            }
        } else if(directory.matches(".*[./]{3,}.*")){//利用正则表达式排除cd...;cd../..;
            output.append("找不到该目录\n");
        } else{
                File newDirectory = new File(currentDirectory, directory);
                if (newDirectory.isDirectory()&&newDirectory.exists()) {
                    currentDirectory = newDirectory;
                    output.append("Changed directory to: ").append(currentDirectory.getAbsolutePath());
                } else {
                    output.append("找不到该目录\n");
                    //writer.println("找不到该目录\n");
                }
            }
        return output.toString();
    }
    // 发送文件给客户端
    private static void sendFile(String fileName, InetAddress clientudpAddress,int udpclientport) {
        File file = new File(currentDirectory, fileName);
            try (FileInputStream fileInputStream = new FileInputStream(file);
            DatagramSocket udpseverSocket = new DatagramSocket()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                // 分次读取文件内容并发送给客户端
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    DatagramPacket packet = new DatagramPacket(buffer, bytesRead, clientudpAddress, udpclientport);
                    udpseverSocket.send(packet);
                    TimeUnit.MICROSECONDS.sleep(1);
                }
                // 发送空的DatagramPacket表示文件传输结束
                DatagramPacket endPacket = new DatagramPacket(new byte[0], 0, clientudpAddress, udpclientport);
                udpseverSocket.send(endPacket);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
    }

}
