import java.awt.*;
import javax.swing.*;
import java.awt.event.*;


/**
 * �������öԻ������
 */
public class Help extends JDialog {

	JPanel titlePanel = new JPanel();
	JPanel contentPanel = new JPanel();
	JPanel closePanel = new JPanel();

	JButton close = new JButton();
	JLabel title = new JLabel("�����ҷ����");
	JTextArea help = new JTextArea(); 

	Color bg = new Color(255,255,255);

	public Help(JFrame frame) {
		super(frame, true);
		try {
			jbInit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//��������λ�ã�ʹ�Ի������
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation( (int) (screenSize.width - 400) / 2 + 25,
						(int) (screenSize.height - 320) / 2);
		this.setResizable(false);
	}

	private void jbInit() throws Exception {
		this.setSize(new Dimension(300, 200));
		this.setTitle("����");
		
		titlePanel.setBackground(bg);
		contentPanel.setBackground(bg);
		closePanel.setBackground(bg);
		
		help.setText("�������ķ���ˣ��������ö˿ڡ�\n\n" +
                        "�����������Է���ϵͳ��Ϣ���û�");
		help.setEditable(false);

		titlePanel.add(new Label("              "));
		titlePanel.add(title);
		titlePanel.add(new Label("              "));

		contentPanel.add(help);

		closePanel.add(new Label("              "));
		closePanel.add(close);
		closePanel.add(new Label("              "));

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(titlePanel, BorderLayout.NORTH);
		contentPane.add(contentPanel, BorderLayout.CENTER);
		contentPane.add(closePanel, BorderLayout.SOUTH);

		close.setText("�ر�");
		//�¼�����
		close.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			}
		);
	}
}