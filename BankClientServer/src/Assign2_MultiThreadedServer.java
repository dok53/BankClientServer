import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.awt.*;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class Assign2_MultiThreadedServer implements Runnable{
	Socket socket;
	private static JTextArea jta;
	private JFrame frm;
	private static InetAddress inetAddress;
	private boolean isRegistered;
	private int id;


	public Assign2_MultiThreadedServer(Socket socket){
		this.socket = socket;
	}
	public Assign2_MultiThreadedServer(){
		jta = new JTextArea();
		DefaultCaret caret = (DefaultCaret)jta.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		jta.setEditable(false);
		// Place text area on the frame
		frm = new JFrame();
		frm.setLayout(new BorderLayout());
		frm.add(new JScrollPane(jta), BorderLayout.CENTER);

		frm.setTitle("Server");
		frm.setSize(500, 300);
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.setVisible(true);
	}


	public static void main(String args[]) throws Exception {
		new Assign2_MultiThreadedServer();
		ServerSocket ssock;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			ssock = new ServerSocket(1234);
			inetAddress = ssock.getInetAddress();
			jta.append("Server started on " + new Date() + '\n');
			while (true) {
				Socket sock = ssock.accept();
				inetAddress = sock.getInetAddress();
				new Thread(new Assign2_MultiThreadedServer(sock)).start();
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "JVMBind on address" + "\n" + "Check is port is in use!!!");
			e.printStackTrace();
		}
	}

	public void mainMethod(){
		try{
			// Create data input and output streams
			DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
			DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
			while (true) {
				// Receive info from the client
				id = inputFromClient.readInt();
				double interest = inputFromClient.readDouble();
				double noOfYear = inputFromClient.readDouble();
				double amount = inputFromClient.readDouble();
				searchDatabase();
				if(isRegistered){
					double months = noOfYear * 12;
					// Compute loan
					double interestRate = interest;
					double monthlyRepayments = (amount * interest / 1200) /
							(1 - Math.pow(1 / (1 + interest / 1200), months));
					double finalAmount = monthlyRepayments * 12 * noOfYear;
					DecimalFormat df = new DecimalFormat("#.##");
					monthlyRepayments = Double.valueOf(df.format(monthlyRepayments));
					finalAmount = Double.valueOf(df.format(finalAmount));

					// Send info back to the client
					outputToClient.writeDouble(interestRate);
					outputToClient.writeDouble(finalAmount);
					outputToClient.writeDouble(monthlyRepayments);
					//Send message to client
					outputToClient.writeUTF("yes");


					jta.append("Interest received from client: " + interest + '\n');
					jta.append("Amount received from client: " + amount + '\n');
					jta.append("No Of Years received from client: " + noOfYear + '\n');
					jta.append("\n");
					jta.append("Interest Rate per euro : " + interestRate + '\n');
					jta.append("Number of years : " + noOfYear + '\n');
					jta.append("Monthly payment is : " + monthlyRepayments + '\n');
					jta.append("Final amount is : " + finalAmount + '\n');
					jta.append("\n");
				}
				else{
					//client expects data to prevent hanging
					outputToClient.writeDouble(0);
					outputToClient.writeDouble(0);
					outputToClient.writeDouble(0);
					outputToClient.writeUTF("no");
				}
			}
		}
		catch(IOException ex) {
			System.err.println(ex);
		}
	}

	
	public void searchDatabase()
	{
		String ipAdd = inetAddress.getHostAddress().toString();
		Connection con = null;
		PreparedStatement selectStatement = null;
		ResultSet result = null;
		try {
			con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/bankDatabase", "root", "");
			selectStatement = con.prepareStatement("SELECT * FROM registeredapplicants WHERE IPAddress = ('"+ipAdd+"') AND regId = ('"+id+"')" );
			result = selectStatement.executeQuery();
			if (result.next()){
				isRegistered = true;
				jta.append("Client ID = " + id + "\n" +
				"IP Address found " + inetAddress.getHostName() + "\n" + 
				"Hostname found, Welcome back " + InetAddress.getLocalHost().getHostName() + "\n" );
				jta.append("\n");
			}else{
				isRegistered = false;
				jta.append("Client ID = " + id + " IP Address: " + inetAddress.getHostAddress() + " not found" + "\n");
				jta.append("\n");
			}

		}catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Problem filling table (getData).","Missing info",2);
			e.printStackTrace();
		}
		finally{
			try{
				result.close();
			}catch(SQLException e){}
			try{
				selectStatement.close();
			}catch(SQLException e){}
			try{
				con.close();
			}catch(SQLException e){}
		}
	}



	public void run(){
		mainMethod();
	}
}

