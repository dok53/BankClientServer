import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.awt.event.*;

import javax.swing.*;

public class Assign2_Client extends Thread {

	// IO streams
	private DataOutputStream toServer;
	private DataInputStream fromServer;
	private JTextField interestRateField;
	private JTextField numberOfYearsField;
	private JTextField loanAmountField;
	private JTextArea jta;
	private JTextField idField;

	public static void main(String[] args) {
		new Assign2_Client();
	}

	public Assign2_Client() {

		JFrame frm = new JFrame();
		frm.setResizable(false);
		frm.setTitle("Client");
		frm.setSize(410, 545);
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.getContentPane().setLayout(null);

		JLabel lblInterest = new JLabel("Annual Interest Rate");
		lblInterest.setBounds(76, 99, 137, 16);
		frm.getContentPane().add(lblInterest);

		JLabel lblAmount = new JLabel("Number Of Years");
		lblAmount.setBounds(76, 158, 107, 16);
		frm.getContentPane().add(lblAmount);

		JLabel lblRepay = new JLabel("Loan Amount");
		lblRepay.setBounds(76, 218, 107, 16);
		frm.getContentPane().add(lblRepay);

		interestRateField = new JTextField();
		interestRateField.setBounds(214, 93, 116, 22);
		frm.getContentPane().add(interestRateField);
		interestRateField.setColumns(10);

		numberOfYearsField = new JTextField();
		numberOfYearsField.setBounds(214, 152, 116, 22);
		frm.getContentPane().add(numberOfYearsField);
		numberOfYearsField.setColumns(10);

		loanAmountField = new JTextField();
		loanAmountField.setBounds(214, 212, 116, 22);
		frm.getContentPane().add(loanAmountField);
		loanAmountField.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 312, 380, 186);
		frm.getContentPane().add(scrollPane);

		jta = new JTextArea();
		jta.setLineWrap(true);
		jta.setEditable(false);
		scrollPane.setViewportView(jta);

		JButton btnSubmit = new JButton("Submit");
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				submit();
			}
		});
		btnSubmit.setBounds(154, 274, 97, 25);
		frm.getContentPane().add(btnSubmit);
		
		idField = new JTextField();
		idField.setBounds(213, 40, 116, 22);
		frm.getContentPane().add(idField);
		idField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("User Id (1-5)");
		lblNewLabel.setBounds(76, 43, 125, 16);
		frm.getContentPane().add(lblNewLabel);
		frm.setVisible(true);

		try {
			// Create a socket to connect to the server
			@SuppressWarnings("resource")
			Socket socket = new Socket("localhost", 1234);

			// Create an input stream to receive data from the server
			fromServer = new DataInputStream(socket.getInputStream());

			// Create an output stream to send data to the server
			toServer = new DataOutputStream(socket.getOutputStream());
		}
		catch (IOException ex) {
			jta.append(ex.toString() + '\n');
		}
	}
	public void submit(){
		try {
			// Get the info from the fields
			double interest = Double.parseDouble(interestRateField.getText().trim());
			double noOfYear = Double.parseDouble(numberOfYearsField.getText().trim());
			double loanAmount = Double.parseDouble(loanAmountField.getText().trim());
			int id = Integer.parseInt(idField.getText().trim());

			// Send data to the server
			toServer.writeInt(id);
			toServer.writeDouble(interest);
			toServer.writeDouble(noOfYear);
			toServer.writeDouble(loanAmount);
			toServer.flush();

			// Get data from the server
			double interestRate = fromServer.readDouble();
			double finalLoanAmount = fromServer.readDouble();
			double monthlyRepayments = fromServer.readDouble();
			DecimalFormat df = new DecimalFormat("#.##");
			monthlyRepayments = Double.valueOf(df.format(monthlyRepayments));
			String warning = fromServer.readUTF();
			

			if (warning.equalsIgnoreCase("no")){
				jta.append("Sorry only registered users allowed to submit data" + "\n");
			}else{
				// Display to the text area
				jta.append("Interest rate is " + interest + "\n");
				jta.append("Number of years is " + noOfYear + "\n");
				jta.append("Loan amount is " + loanAmount + "\n");
				jta.append("\n");
				jta.append("Interest is " + interestRate + '\n');
				jta.append("Monthly repayment is " + monthlyRepayments + '\n');
				jta.append("Final loan amount is " + finalLoanAmount + '\n');
				jta.append("\n");
			}
		}
		catch (IOException ex) {
			System.err.println(ex);
		}

	}
}