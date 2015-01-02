import java.sql.*;
import javax.swing.*;
import java.text.SimpleDateFormat;

/*
 * 服务器收发消息的类
 */
public class ServerReceive extends Thread {
	JTextArea textarea;
	JTextField textfield;
	JComboBox combobox;
	Node client;
	UserLinkList userLinkList;//用户链表
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
		//向所有人发送用户的列表
		sendUserList();
		//该线程一直不停的监测，只要套接字未关闭
		while(!isStop && !client.socket.isClosed()){
			try{
				String type = (String)client.input.readObject();
				
				if(type.equalsIgnoreCase("聊天信息")){
					String toSomebody = (String)client.input.readObject();
					String message = (String)client.input.readObject();

                                        SimpleDateFormat now= new SimpleDateFormat("hh:mm:ss");
                                        String nowtime = now.format(new java.util.Date());
					String msg =  client.username
                                                        +" 对 "
                                                        +toSomebody
							+"  "
                                                        +nowtime
                                                        +"  : "
                                                        +message
							+ "\n";
					textarea.append(msg);

                                        // statement用来执行SQL语句
                                        Statement statement = conn.createStatement();
                                        // 要执行的SQL语句
                                        try{
                                            //获取当前时间
                                            now= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                            String date = now.format(new java.util.Date());
                                            // 首先使用ISO-8859-1字符集将name解码为字节序列并将结果存储新的字节数组中。
                                            // 然后使用GB2312字符集解码指定的字节数组
                                            // message = new String(message.getBytes("GB2312"),"ISO-8859-1");
                                            String sql = "insert into logs(text,date,user,receiver) values ("
                                                +"\'"+message+"\',"
                                                +"\'"+date+"\',"
                                                +"\'"+client.username+"\',"
                                                +"\'"+toSomebody+"\')";

                                            // 结果集
                                            statement.execute(sql);
                                        }catch(SQLException e) {
                                        e.printStackTrace();
                                        }

					if(toSomebody.equalsIgnoreCase("所有人")){
						sendToAll(msg);//向所有人发送消息
					}
					else{
						try{
                                                        //发给发送者
							client.output.writeObject("聊天信息");
							client.output.flush();
							client.output.writeObject(msg);
							client.output.flush();
						}
						catch (Exception e){
							//System.out.println("###"+e);
						}
						
						Node node = userLinkList.findUser(toSomebody);
						//发给接受者
						if(node != null){
							node.output.writeObject("聊天信息"); 
							node.output.flush();
							node.output.writeObject(msg);
							node.output.flush();
						}
					}
				}
				else if(type.equalsIgnoreCase("用户下线")){
					Node node = userLinkList.findUser(client.username);
					userLinkList.delUser(node);
					
					String msg = "用户 " + client.username + " 下线\n";
					int count = userLinkList.getCount();

					combobox.removeAllItems();
					combobox.addItem("所有人");
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
					textfield.setText("在线用户" + userLinkList.getCount() + "人\n");
					
					sendToAll(msg);//向所有人发送消息
					sendUserList();//重新发送用户列表,刷新
					
					break;
				}
                                else if (type.equalsIgnoreCase("发送文件")) {
                                        String toSomebody = (String)client.input.readObject();
                                        String abPath = (String)client.input.readObject();
                                        String flname = (String)client.input.readObject();
                                        String user = client.username;
                                        Node node = userLinkList.findUser(toSomebody);
                                        //发给接受者
                                        
                                        if(node != null){
                                            node.output.writeObject("发送文件");
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
                                        textarea.append(user+" 向 "+toSomebody+" 提出文件传输请求\n");
                                }
                                else if(type.equalsIgnoreCase("拒绝文件")) {
                                            String user = (String)client.input.readObject();
                                            String toSomebody = (String)client.input.readObject();
                                            String flname = (String)client.input.readObject();
                                            System.out.println("没有建立文件传输的连接");
                                            Node node = userLinkList.findUser(user);
                                            if(node != null){
                                                node.output.writeObject("拒绝文件");
                                                node.output.flush();
                                                node.output.writeObject(toSomebody);
                                                node.output.flush();
                                                node.output.writeObject(flname);
                                                node.output.flush();
                                            }
                                            textarea.append(toSomebody+" 拒绝了来自 "+user+" 的文件\n");
                                        }
                                 else if (type.equalsIgnoreCase("接收文件")) {
                                    String user = (String)client.input.readObject();
                                    String flname = (String)client.input.readObject();
                                    String abPath = (String)client.input.readObject();
                                    String toSomebody = (String)client.input.readObject();
                                    TransferServer tf = new TransferServer(abPath);
                                    tf.start();
                                    System.out.println("准备接收");
                                    Node node = userLinkList.findUser(toSomebody);
                                    node.output.writeObject("准备接收");
                                    node.output.flush();
                                    node.output.writeObject(user);
                                    node.output.flush();
                                    node.output.writeObject(flname);
                                    node.output.flush();
                                    textarea.append(user+" 对 "+toSomebody+" 传输文件: "+flname+"\n");
                                    node = userLinkList.findUser(user);
                                    node.output.writeObject("接收文件");
                                    node.output.flush();
                                    node.output.writeObject(toSomebody);
                                    node.output.flush();
                                    node.output.writeObject(flname);
                                    node.output.flush();
                                }
                                else if (type.equalsIgnoreCase("查看记录")) {
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
                                    client.output.writeObject("查看记录");
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
	 * 向所有人发送消息
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
				node.output.writeObject("聊天信息");
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
	 * 向所有人发送用户的列表
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
				node.output.writeObject("用户列表");
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
