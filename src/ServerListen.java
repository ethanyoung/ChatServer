import java.sql.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

/*
 * 服务端的侦听类，主要用途是侦听用户的上下线
 */
public class ServerListen extends Thread {
	ServerSocket server;
	
	JComboBox combobox;
	JTextArea textarea;
	JTextField textfield;
	UserLinkList userLinkList;//用户链表
        Connection conn;
	
	Node client;
	ServerReceive recvThread;
	
	public boolean isStop;

	public ServerListen(ServerSocket server,JComboBox combobox,
		JTextArea textarea,JTextField textfield,UserLinkList userLinkList,Connection conn){

		this.server = server;
		this.combobox = combobox;
		this.textarea = textarea;
		this.textfield = textfield;
		this.userLinkList = userLinkList;
                this.conn = conn;
		
		isStop = false;
	}
	
        @Override
	public void run(){
		while(!isStop && !server.isClosed()){
			try{
				client = new Node();
				client.socket = server.accept();
				client.output = new ObjectOutputStream(client.socket.getOutputStream());
				client.output.flush();
				client.input  = new ObjectInputStream(client.socket.getInputStream());
				client.username = (String)client.input.readObject();
				//显示提示信息
                                    combobox.addItem(client.username);
                                    userLinkList.addUser(client);
                                    textarea.append("用户 " + client.username + " 上线" + "\n");
                                    textfield.setText("在线用户" + userLinkList.getCount() + "人\n");

                                    //启动一个收发信息的线程
                                    recvThread = new ServerReceive(textarea,textfield,
                                            combobox,client,userLinkList,conn);
                                    recvThread.start();
                                
			}
			catch(Exception e){
			}
		}
	}
}
