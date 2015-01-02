import java.sql.*;
import javax.swing.*;
import java.text.SimpleDateFormat;

/*
 * �������շ���Ϣ����
 */
public class ServerReceive extends Thread {
	JTextArea textarea;
	JTextField textfield;
	JComboBox combobox;
	Node client;
	UserLinkList userLinkList;//�û�����
        Connection conn;
	
	public boolean isStop;
	
	public ServerReceive(JTextArea textarea,JTextField textfield,
		JComboBox combobox,Node client,UserLinkList userLinkList,Connection conn){

		this.textarea = textarea;
		this.textfield = textfield;
		this.client = client;
		this.userLinkList = userLinkList;
		this.combobox = combobox;
                this.conn = conn;
		
		isStop = false;
	}
	
        @Override
	public void run(){
		//�������˷����û����б�
		sendUserList();
		//���߳�һֱ��ͣ�ļ�⣬ֻҪ�׽���δ�ر�
		while(!isStop && !client.socket.isClosed()){
			try{
				String type = (String)client.input.readObject();
				
				if(type.equalsIgnoreCase("������Ϣ")){
					String toSomebody = (String)client.input.readObject();
					String message = (String)client.input.readObject();

                                        SimpleDateFormat now= new SimpleDateFormat("hh:mm:ss");
                                        String nowtime = now.format(new java.util.Date());
					String msg =  client.username
                                                        +" �� "
                                                        +toSomebody
							+"  "
                                                        +nowtime
                                                        +"  : "
                                                        +message
							+ "\n";
					textarea.append(msg);

                                        // statement����ִ��SQL���
                                        Statement statement = conn.createStatement();
                                        // Ҫִ�е�SQL���
                                        try{
                                            //��ȡ��ǰʱ��
                                            now= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                            String date = now.format(new java.util.Date());
                                            // ����ʹ��ISO-8859-1�ַ�����name����Ϊ�ֽ����в�������洢�µ��ֽ������С�
                                            // Ȼ��ʹ��GB2312�ַ�������ָ�����ֽ�����
                                            // message = new String(message.getBytes("GB2312"),"ISO-8859-1");
                                            String sql = "insert into logs(text,date,user,receiver) values ("
                                                +"\'"+message+"\',"
                                                +"\'"+date+"\',"
                                                +"\'"+client.username+"\',"
                                                +"\'"+toSomebody+"\')";

                                            // �����
                                            statement.execute(sql);
                                        }catch(SQLException e) {
                                        e.printStackTrace();
                                        }

					if(toSomebody.equalsIgnoreCase("������")){
						sendToAll(msg);//�������˷�����Ϣ
					}
					else{
						try{
                                                        //����������
							client.output.writeObject("������Ϣ");
							client.output.flush();
							client.output.writeObject(msg);
							client.output.flush();
						}
						catch (Exception e){
							//System.out.println("###"+e);
						}
						
						Node node = userLinkList.findUser(toSomebody);
						//����������
						if(node != null){
							node.output.writeObject("������Ϣ"); 
							node.output.flush();
							node.output.writeObject(msg);
							node.output.flush();
						}
					}
				}
				else if(type.equalsIgnoreCase("�û�����")){
					Node node = userLinkList.findUser(client.username);
					userLinkList.delUser(node);
					
					String msg = "�û� " + client.username + " ����\n";
					int count = userLinkList.getCount();

					combobox.removeAllItems();
					combobox.addItem("������");
					int i = 0;
					while(i < count){
						node = userLinkList.findUser(i);
						if(node == null) {
							i ++;
							continue;
						} 
			
						combobox.addItem(node.username);
						i++;
					}
					combobox.setSelectedIndex(0);

					textarea.append(msg);
					textfield.setText("�����û�" + userLinkList.getCount() + "��\n");
					
					sendToAll(msg);//�������˷�����Ϣ
					sendUserList();//���·����û��б�,ˢ��
					
					break;
				}
                                else if (type.equalsIgnoreCase("�����ļ�")) {
                                        String toSomebody = (String)client.input.readObject();
                                        String abPath = (String)client.input.readObject();
                                        String flname = (String)client.input.readObject();
                                        String user = client.username;
                                        Node node = userLinkList.findUser(toSomebody);
                                        //����������
                                        
                                        if(node != null){
                                            node.output.writeObject("�����ļ�");
                                            node.output.flush();
                                            node.output.writeObject(user);
                                            node.output.flush();
                                            node.output.writeObject(flname);
                                            node.output.flush();
                                            node.output.writeObject(abPath);
                                            node.output.flush();
                                            node.output.writeObject(toSomebody);
                                            node.output.flush();
                                        }
                                        textarea.append(user+" �� "+toSomebody+" ����ļ���������\n");
                                }
                                else if(type.equalsIgnoreCase("�ܾ��ļ�")) {
                                            String user = (String)client.input.readObject();
                                            String toSomebody = (String)client.input.readObject();
                                            String flname = (String)client.input.readObject();
                                            System.out.println("û�н����ļ����������");
                                            Node node = userLinkList.findUser(user);
                                            if(node != null){
                                                node.output.writeObject("�ܾ��ļ�");
                                                node.output.flush();
                                                node.output.writeObject(toSomebody);
                                                node.output.flush();
                                                node.output.writeObject(flname);
                                                node.output.flush();
                                            }
                                            textarea.append(toSomebody+" �ܾ������� "+user+" ���ļ�\n");
                                        }
                                 else if (type.equalsIgnoreCase("�����ļ�")) {
                                    String user = (String)client.input.readObject();
                                    String flname = (String)client.input.readObject();
                                    String abPath = (String)client.input.readObject();
                                    String toSomebody = (String)client.input.readObject();
                                    TransferServer tf = new TransferServer(abPath);
                                    tf.start();
                                    System.out.println("׼������");
                                    Node node = userLinkList.findUser(toSomebody);
                                    node.output.writeObject("׼������");
                                    node.output.flush();
                                    node.output.writeObject(user);
                                    node.output.flush();
                                    node.output.writeObject(flname);
                                    node.output.flush();
                                    textarea.append(user+" �� "+toSomebody+" �����ļ�: "+flname+"\n");
                                    node = userLinkList.findUser(user);
                                    node.output.writeObject("�����ļ�");
                                    node.output.flush();
                                    node.output.writeObject(toSomebody);
                                    node.output.flush();
                                    node.output.writeObject(flname);
                                    node.output.flush();
                                }
                                else if (type.equalsIgnoreCase("�鿴��¼")) {
                                    String username = (String)client.input.readObject();

                                    int row= 0 ;
                                    int i = 0 ;
                                    String [][] cn = null;
                                    try {
                                    String sql = "select * from logs where user="+"\'"+username+"\'"+
                                            " or receiver="+"\'"+username+"\'"+"order by date";
                                    Statement statement = conn.createStatement();
                                    ResultSet rs = statement.executeQuery(sql);
                                    if(rs.last()){
                                        row = rs.getRow();
                                    }
                                    if(row == 0){
                                        cn = new String[1][4];
                                        rs.first();
                                        rs.previous();
                                        while(rs.next()){
                                            cn[i][0] = rs.getString("");
                                            cn[i][1] = rs.getString("");
                                            cn[i][2] = rs.getString("");
                                            cn[i][3] = rs.getString("");
                                            i++;
                                        }
                                    }
                                    else{
                                        cn = new String[row][4];
                                        rs.first();
                                        rs.previous();
                                        while(rs.next()){
                                            cn[i][0] = rs.getString("date");
                                            cn[i][1] = rs.getString("text");
                                            cn[i][2] = rs.getString("user");
                                            cn[i][3] = rs.getString("receiver");
                                            i++;
                                        }
                                    }
                                    client.output.writeObject("�鿴��¼");
                                    client.output.flush();
                                    client.output.writeObject(cn);
                                    client.output.flush();
                                    rs.close();
                                    } catch (Exception e) {
                                    }

                                }
			}
			catch (Exception e){
				//System.out.println(e);
			}
		}
	}
	
	/*
	 * �������˷�����Ϣ
	 */
	public void sendToAll(String msg){
		int count = userLinkList.getCount();
		
		int i = 0;
		while(i < count){
			Node node = userLinkList.findUser(i);
			if(node == null) {
				i ++;
				continue;
			}
			
			try{
				node.output.writeObject("������Ϣ");
				node.output.flush();
				node.output.writeObject(msg);
				node.output.flush();
			}
			catch (Exception e){
				//System.out.println(e);
			}
			
			i++;
		}
	}
	
	/*
	 * �������˷����û����б�
	 */
	public void sendUserList(){
		String userlist = "";
		int count = userLinkList.getCount();

		int i = 0;
		while(i < count){
			Node node = userLinkList.findUser(i);
			if(node == null) {
				i ++;
				continue;
			}
			userlist += node.username;
			userlist += '\n';
			i++;
		}
		
		i = 0;
		while(i < count){
			Node node = userLinkList.findUser(i);
			if(node == null) {
				i ++;
				continue;
			}
			try{
				node.output.writeObject("�û��б�");
				node.output.flush();
				node.output.writeObject(userlist);
				node.output.flush();
			}
			catch (Exception e){
				//System.out.println(e);
			}
			i++;
		}
	}
}
