import java.sql.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

/*
 * ����˵������࣬��Ҫ��;�������û���������
 */
public class ServerListen extends Thread {
	ServerSocket server;
	
	JComboBox combobox;
	JTextArea textarea;
	JTextField textfield;
	UserLinkList userLinkList;//�û�����
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
				//��ʾ��ʾ��Ϣ
                                    combobox.addItem(client.username);
                                    userLinkList.addUser(client);
                                    textarea.append("�û� " + client.username + " ����" + "\n");
                                    textfield.setText("�����û�" + userLinkList.getCount() + "��\n");

                                    //����һ���շ���Ϣ���߳�
                                    recvThread = new ServerReceive(textarea,textfield,
                                            combobox,client,userLinkList,conn);
                                    recvThread.start();
                                
			}
			catch(Exception e){
			}
		}
	}
}
