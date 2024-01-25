import java.io.*;
import java.net.*;

/**
 * 客户端代码
 *
 * @author : [2021302747]
 * @version : [v1.0]
 * @createTime : [2023/10/9 13:37]
 */
public class FileClient {
    private static final int TCP_PORT = 2021;
    private static final int UDP_PORT = 2020;
    private static final int BUFFER_SIZE = 1024;
    private static String serverIP;
    private static final String SAVE_PATH = "C:/Users/Lenovo/Desktop/分布式实验/receive/";

    public static void main(String[] args) throws SocketException {
        if (args.length < 1)
        {
            System.out.println("Usage: java FileClient <serverIP>");
            return;
        }
//        serverIP = ".";
        serverIP = args[0];
        //DatagramSocket udpSocket = new DatagramSocket();
        try (Socket socket = new Socket(serverIP, TCP_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            DatagramSocket udpSocket = new DatagramSocket();
            //InetAddress serverAddress = InetAddress.getByName(serverIP);

            byte[] buffer = new byte[BUFFER_SIZE];
            //DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, UDP_PORT);



            System.out.println("连接服务器：" + serverIP);
            //writer.println(udpclientport);
            String response;
            while ((response = reader.readLine()) != null) {
                System.out.println(response);
                if (response.equals("服务已连接")) {
                    break;
                }
            }
            BufferedReader commandReader = new BufferedReader(new InputStreamReader(System.in));
            String command;
            while (true) {
                do {
                    command = commandReader.readLine();
                }while (command.equals(" "));
                writer.println(command);
                if (command.equals("bye")) {
                    break;
                }  else if (command.startsWith("get")) {
                    int udpclientport = udpSocket.getLocalPort();
                    String[] tokens = command.split(" ");
                    if (tokens.length == 2) {
                        String fileName = tokens[1];
                        String ok ;
                        ok = reader.readLine();
                        if (ok.startsWith("文件"))
                        {
                            System.out.println(ok);
                        }else if (ok.startsWith("OK")) {

                            System.out.println(ok);
                            writer.println("UDP:"+udpclientport);
                            ok = reader.readLine();
                            System.out.println(ok);
                            receiveFile(fileName,udpSocket);
                        }

                    }
                }
                    String serverResponse;
                    do {
                        serverResponse = reader.readLine();
                        System.out.println(serverResponse);
                    } while (!serverResponse.endsWith("%"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveFile(String fileName,DatagramSocket udpSocket) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(SAVE_PATH + fileName)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length);

            while (true) {
                udpSocket.receive(packet);
                if (packet.getLength() == 0) {
                    break;
                }
                fileOutputStream.write(packet.getData(), 0, packet.getLength());
            }

            System.out.println("文件接收成功：" + SAVE_PATH + fileName+"\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}