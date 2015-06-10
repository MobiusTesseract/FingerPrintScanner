package gr.uom.softeng2015.team28;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;

public class SerialConnectFrame extends JFrame {

	private String selectedPort = "NULL";
	private SerialPort arduinoPort = new SerialPort(selectedPort);

	private JPanel portPanel;
	private JLabel portSelectPrompt;
	private JComboBox<String> portList;	//portList.addActionListener(this);
	private JButton refreshButton;
	private JButton connectButton;
	private JButton disconnectButton;

	private JPanel terminalPanel;
	private JScrollPane terminalScroll;
	private JTextArea terminalText;
	//private JList portList;
	//private ArrayList<> ports;

	public SerialConnectFrame() {

		// ----------------------------------------
		portPanel = new JPanel();
		portPanel.setLayout(new BoxLayout(portPanel, BoxLayout.X_AXIS));
		portPanel.add(Box.createHorizontalGlue());
		portPanel.add(portSelectPrompt = new JLabel("Select Arduino Port:"));
		portPanel.add(Box.createHorizontalStrut(5));
		portPanel.add(portList = new JComboBox<String>());
		portPanel.add(Box.createHorizontalStrut(5));
		portPanel.add(refreshButton = new JButton("Refresh"));
		portPanel.add(Box.createHorizontalStrut(5));
		portPanel.add(connectButton = new JButton("Connect"));
		portPanel.add(Box.createHorizontalStrut(5));
		portPanel.add(disconnectButton = new JButton("Disconnect"));
		portPanel.add(Box.createHorizontalStrut(5));
		// ----------------------------------------
		terminalPanel = new JPanel();
		terminalPanel.setLayout(new BorderLayout());
		terminalPanel.add(portPanel, BorderLayout.NORTH);
		terminalPanel.add(terminalScroll = new JScrollPane(terminalText = new JTextArea()), BorderLayout.CENTER);
		// ----------------------------------------

		portList.setMaximumSize(portList.getMinimumSize());
		terminalText.setEditable(false);

		this.setContentPane(terminalPanel);

		refreshAvailablePorts();

		ButtonListener listener = new ButtonListener();
		refreshButton.addActionListener(listener);
		connectButton.addActionListener(listener);
		disconnectButton.addActionListener(listener);

		this.setVisible(true);
		this.setSize(600, 200);
		this.setLocationRelativeTo(null);
		this.setTitle("Port Selection");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


	private void refreshAvailablePorts() {
		portList.removeAllItems();
		String[] portNames = SerialPortList.getPortNames();
		System.out.println("Available COM ports:");
		for(String portName: portNames) {
			portList.addItem(portName);
			System.out.println(portName);
		}
		portList.setSelectedIndex(portList.getItemCount() - 1);
	}                                            

	private void connectToArduino() {

		byte sensorFound = -1;
		arduinoPort = new SerialPort(selectedPort);

		try {
			arduinoPort.openPort();	//Open serial port
			arduinoPort.setParams(SerialPort.BAUDRATE_9600, 
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);	//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
			sensorFound = arduinoPort.readBytes(1, 10000)[0];
		} catch (SerialPortException serialEx) {
			System.out.println(serialEx);
			terminalText.append("Error opening port.\n");
		} catch (SerialPortTimeoutException timeoutEx) {
			System.out.println(timeoutEx);
			terminalText.append("Connection timeout. Is the Arduino connected to " + selectedPort + " and running properly?\n");
		}

		terminalText.append("Connected to Arduino!\n");
		if(sensorFound == '0') {
			terminalText.append("No fingerprint sensor found!\n");
		}
		else if(sensorFound == '1') {
			terminalText.append("Connected to Fingerprint sensor!\n");
		}
		else {
			terminalText.append("Uknown error!\n");
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					arduinoPort.closePort();
					System.out.println("Shutdown hook: Disconnected.\n");
				} catch (SerialPortException serialEx) {
					System.out.println("Shutdown hook: Port not opened.\n");
				}
			}
		});
	}

	private void disconnectFromArduino() {

		try {
			arduinoPort.writeBytes("1003".getBytes());	//Test writing data to port
			arduinoPort.closePort();
			terminalText.append("Disconnected.\n");
		} catch (SerialPortException serialEx) {
			terminalText.append("Port not opened.\n");
		}
	}

	class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			if(e.getSource() == refreshButton) {
				refreshAvailablePorts();
			}
			else if(e.getSource() == connectButton) {
				terminalText.append("Connecting to " + (String)portList.getSelectedItem() + "...\n");
				SerialPort serialPort = new SerialPort((String)portList.getSelectedItem());
				try {
					serialPort.openPort();	//Open serial port
					serialPort.setParams(SerialPort.BAUDRATE_9600, 
							SerialPort.DATABITS_8,
							SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);	//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
					Thread.sleep(1500);
					serialPort.writeBytes("1003".getBytes());	//Write data to port
					Thread.sleep(1500);
					serialPort.closePort();	//Close serial port
				} catch (SerialPortException | InterruptedException ex) {
					System.out.println(ex);
				}
			}

		}
	}

}
