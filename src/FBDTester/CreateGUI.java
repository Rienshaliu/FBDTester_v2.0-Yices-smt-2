package FBDTester;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import PLC_related.drawPanel;

public class CreateGUI extends Frame implements ActionListener {
	static JFrame window;
	static Container content;
	static JButton loadButton;
	static JButton openButton;
	static JTextField filePath;
	static JTextField panelSizeX;
	static JTextField panelSizeY;
	static JTextArea console;
	static JRadioButton BCTestCheck;
	static JRadioButton ICCTestCheck;
	static JRadioButton CCCTestCheck;
	static JRadioButton RTestCheck;
	static ButtonGroup btnGroup;
	static JCheckBox displayTrue;
	static JButton findDPath;
	static JPanel panel_right;
	static XML_load xml_load;
	static boolean firstload = true;
	static JButton testOpenButton;
	static JButton setSizeButton;
	static JButton smtBasedGeneration;
	static JButton randomTestGeneration;
	static JButton automationButton;
	static JTextField testFilePath;
	static JTextField DPathField;
	static JPanel leftPanel;
	static String console_text = "";
	static JTextField LogFilePath;
	static JButton logButton;
	static JTextField libraryPath;
	static JButton libraryButton;
	static JTextField libraryCalcPath;
	static JButton libraryCalcButton;
	static JLabel xmlStatus;
	static JTextField font;
	static JCheckBox bold;
	static JCheckBox italic;
	static JScrollPane jsp3;

	public static void console_flush() {
		console.setText(console_text);
		int pos = console.getText().length();
		console.setCaretPosition(pos);
		console.requestFocus();
	}

	public static void console_print(String s) {
		console_text = console_text + s;
	}

	public static void console_println(String s) {
		console_text = console_text + s + "\n";
	}

	FileDialog fd;

	public CreateGUI() {
		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(1280, 960);

		// window.setResizable(false);
		window.setTitle("FBDTester");
		window.setVisible(true);

		content = window.getContentPane();
		content.setLayout(new BorderLayout());

		filePath = new JTextField(20);

		openButton = new JButton("Open...");
		openButton.addActionListener(this);
		openButton.setActionCommand("openXML");

		loadButton = new JButton("Load XML");
		loadButton.addActionListener(this);
		loadButton.setActionCommand("loadXML");
		loadButton.setEnabled(false);

		testFilePath = new JTextField(20);
		DPathField = new JTextField(3);

		testOpenButton = new JButton("Open...");
		testOpenButton.addActionListener(this);
		testOpenButton.setActionCommand("testOpenXML");

		smtBasedGeneration = new JButton("SMT-based Test Generation");
		smtBasedGeneration.addActionListener(this);
		smtBasedGeneration.setActionCommand("smt-based");
		smtBasedGeneration.setEnabled(false);
		
		randomTestGeneration = new JButton("Random Test Generation");
		randomTestGeneration.addActionListener(this);
		randomTestGeneration.setActionCommand("random");
		randomTestGeneration.setEnabled(false);

		automationButton = new JButton("SMT-based automation");
		automationButton.addActionListener(this);
		automationButton.setActionCommand("automation");
		automationButton.setEnabled(true);

		findDPath = new JButton("Find D-Path");
		findDPath.addActionListener(this);
		findDPath.setActionCommand("findDPath");
		findDPath.setEnabled(false);

		panelSizeX = new JTextField(3);
		panelSizeY = new JTextField(3);
		panelSizeX.setText("1536");
		panelSizeY.setText("4096");
		setSizeButton = new JButton("Set");
		setSizeButton.addActionListener(this);
		setSizeButton.setActionCommand("setcanvassize");
		setSizeButton.setEnabled(false);

		xmlStatus = new JLabel("");

		JPanel panel_XML = new JPanel();
		panel_XML.setPreferredSize(new Dimension(1270, 40));
		panel_XML.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		panel_XML.add(new JLabel("    "));
		panel_XML.add(new JLabel("XML file : "));
		panel_XML.add(filePath);
		panel_XML.add(openButton);
		panel_XML.add(xmlStatus);
		panel_XML.add(new JLabel("    "));
		// panel_XML.add(loadButton);
		panel_XML.add(new JLabel("Test file : "));
		panel_XML.add(testFilePath);
		panel_XML.add(testOpenButton);

		panel_XML.add(new JLabel("    "));
		panel_XML.add(new JLabel("D-Path Finder : "));
		panel_XML.add(DPathField);
		panel_XML.add(findDPath);
		panel_XML.add(new JLabel("    "));

		leftPanel = new JPanel();
		leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		leftPanel.setPreferredSize(new Dimension(260, 600));
		leftPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		// console = new JTextArea(40, 20);
		BCTestCheck = new JRadioButton("Basic Coverage", false);
		ICCTestCheck = new JRadioButton("Input Condition Coverage", false);
		CCCTestCheck = new JRadioButton("Complex Condition Coverage", false);
		RTestCheck = new JRadioButton("(No coverage)", false);
		btnGroup = new ButtonGroup();
		btnGroup.add(BCTestCheck);
		btnGroup.add(ICCTestCheck);
		btnGroup.add(CCCTestCheck);
		btnGroup.add(RTestCheck);
		displayTrue = new JCheckBox("Display TRUE in test requirements", false);
		leftPanel.add(new JLabel("   ===== Test Coverage Criteria =====   "));
		leftPanel.add(BCTestCheck);
		leftPanel.add(ICCTestCheck);
		leftPanel.add(CCCTestCheck);
		leftPanel.add(RTestCheck);
		leftPanel.add(new JLabel("                                                            "));
		leftPanel.add(new JLabel("                 ===== Options =====                        "));
		leftPanel.add(displayTrue);
		leftPanel.add(new JLabel("       "));

		LogFilePath = new JTextField(9);
		LogFilePath.setText("Execution_log.txt");
		leftPanel.add(new JLabel("Log file:"));
		leftPanel.add(LogFilePath);
		logButton = new JButton("Select...");
		logButton.addActionListener(this);
		logButton.setActionCommand("selectlogfile");
		logButton.setEnabled(true);
		leftPanel.add(logButton);
		leftPanel.add(new JLabel("FC Library : "));
		libraryPath = new JTextField(7);
		libraryPath.setText("lib\\FC library_updated_20180103.txt");
		leftPanel.add(libraryPath);
		libraryButton = new JButton("Open...");
		libraryButton.addActionListener(this);
		libraryButton.setActionCommand("openlibrary");
		leftPanel.add(libraryButton);
		leftPanel.add(new JLabel("    "));

		leftPanel.add(new JLabel("Calc Library : "));
		libraryCalcPath = new JTextField(6);
		libraryCalcPath.setText("lib\\Calc library_updated_20120105.txt");
		leftPanel.add(libraryCalcPath);
		libraryCalcButton = new JButton("Open...");
		libraryCalcButton.addActionListener(this);
		libraryCalcButton.setActionCommand("opencalclibrary");
		leftPanel.add(libraryCalcButton);
		leftPanel.add(new JLabel("Canvas size : "));
		leftPanel.add(panelSizeX);
		leftPanel.add(new JLabel("x"));
		leftPanel.add(panelSizeY);
		leftPanel.add(setSizeButton);
		leftPanel.add(new JLabel("Font : "));
		font = new JTextField(10);
		font.setText("Tahoma,10");
		leftPanel.add(font);
		bold = new JCheckBox("Bold", false);
		leftPanel.add(bold);

		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		tempPanel.setPreferredSize(new Dimension(250, 100));
		leftPanel.add(new JLabel("                                                            "));
		tempPanel.add(smtBasedGeneration);
		tempPanel.add(randomTestGeneration);
		tempPanel.add(automationButton);
		leftPanel.add(tempPanel);
		JLabel title = new JLabel("          ===== Output Variables =====          ");
		leftPanel.add(title);
		JScrollPane jsp = new JScrollPane(leftPanel);

		panel_right = new JPanel();
		panel_right.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		panel_right.setPreferredSize(new Dimension(450, 225));

		content.add("North", panel_XML);
		content.add("West", jsp);

		XML_load.panel_draw = new drawPanel();
		XML_load.panel_draw.setPreferredSize(new Dimension(1536, 4096));
		JScrollPane jsp2 = new JScrollPane(XML_load.panel_draw);

		content.add("Center", jsp2);

		console = new JTextArea(13, 112);
		console.setFont(new Font("Courier New", Font.PLAIN, 13));
		jsp3 = new JScrollPane(console);
		panel_right.add(jsp3);
		content.add("South", panel_right);

		window.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println("Resize : width " + window.getWidth() + ", height " + window.getHeight());
				jsp3.setPreferredSize(new Dimension(window.getWidth() - 30, 211));
				jsp3.setSize(new Dimension(window.getWidth() - 30, 211));
				window.repaint();
				window.setVisible(true);
			}
		});

		XML_load.panel_draw.init(1536, 4096);
		XML_load.panel_draw.g2d.setColor(Color.black);

		jsp3.setPreferredSize(new Dimension(window.getWidth() - 30, 211));

		window.repaint();
		window.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		if (cmd.equals("openXML")) {
			try {
				open(0);
				if (!filePath.getText().equals("")) {
					xml_load = new XML_load();
					firstload = false;
					setSizeButton.setEnabled(true);
					
					xml_load.loadXML(filePath.getText());
					
					automationButton.setEnabled(false);
					
				}
				window.repaint();
				window.setVisible(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (cmd.equals("testOpenXML")) {
			try {
				open(1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (cmd.equals("openlibrary")) {
			try {
				open(2);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (cmd.equals("opencalclibrary")) {
			try {
				open(3);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (cmd.equals("selectlogfile")) {
			try {
				FileDialog save = new FileDialog(this, "Save Log File", FileDialog.SAVE);
				save.setVisible(true);
				String filepath = "";
				if (save.getDirectory() != null && save.getFile() != null) {
					filepath = save.getDirectory() + save.getFile();
					openButton.setEnabled(true);
					testOpenButton.setEnabled(true);
				} else {
					filepath = "";
					openButton.setEnabled(false);
					testOpenButton.setEnabled(false);
				}
				LogFilePath.setText(filepath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (cmd.equals("findDPath")) {
			xml_load.silence = true;
			xml_load.drawPicture(-1);
			xml_load.drawPicture(Integer.parseInt(DPathField.getText()));
			xml_load.silence = false;
			
		} else if (cmd.equals("setcanvassize")) {
			int x = Integer.parseInt(panelSizeX.getText());
			int y = Integer.parseInt(panelSizeY.getText());
			XML_load.panel_draw.init(x, y);
			XML_load.panel_draw.setPreferredSize(new Dimension(x, y));
			XML_load.silence = true;
			XML_load.drawPicture(-1);
			XML_load.silence = false;
			XML_load.panel_draw.invalidate();
			window.setVisible(true);
		} else if (cmd.equals("smt-based")) {
			// Button [SMT-based Test Generation]
			
			findDPath.setEnabled(true);
			xml_load.testPath = testFilePath.getText();
			try {
				xml_load.generateTestSuites(1, 1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			console_println("Finished!");
			console_flush();
		} else if(cmd.equals("random")) {
			// Button [Random Test Generation]
			
			findDPath.setEnabled(true);
			xml_load.testPath = testFilePath.getText();
			xml_load.generateRandomTestSuites(1, 100, xml_load.testPath, 1);
			console_println("Finished!");
			console_flush();
		} else if (cmd.equals("automation")) {
			
			// TODO
			// [automation] is the advanced feature.
			// Programmer should fix this code for their purpose.
			
			console_println("Aumation is not defined!");
			
			xml_load = new XML_load();
			try {
//				automation("CTU_TON");
//				automation("R_TRIG_SR");
//				automation("TON");
				automation("FFTD");
//				automation("FRTD");
//				automation("HB");
//				automation("VFTD");
//				automation("VRTD");
//				automation("MFTD");
//				automation("WholeTest");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.exit(0);
			
		} 
	}
	
	@SuppressWarnings("static-access")
	private void automation(String model) throws IOException {
		xml_load.loadXML("automation\\"+model+".xml");
		xml_load.model = model;
		xml_load.testPath = "automation\\"+model+"_test_doc.txt";
		
		xml_load.BC = true;
		xml_load.ICC = false;
		xml_load.CCC = false;
		xml_load.generateTestSuites(1, 1);
//		xml_load.assessFeasibleMaxCoverage();
		xml_load.BC = false;
		xml_load.ICC = true;
		xml_load.CCC = false;
		xml_load.generateTestSuites(1, 1);
//		xml_load.assessFeasibleMaxCoverage();
		xml_load.BC = false;
		xml_load.ICC = false;
		xml_load.CCC = true;
		xml_load.generateTestSuites(1, 1);
//		xml_load.assessFeasibleMaxCoverage();
	}

	public void open(int type) throws IOException {
		if (type == 0) {
			fd = new FileDialog(this, "Open XML File", FileDialog.LOAD);
			fd.setFile("*.xml");
		} else if (type == 1) {
			fd = new FileDialog(this, "Open Test Case", FileDialog.LOAD);
			fd.setFile("*.txt");
		} else if (type == 2) {
			fd = new FileDialog(this, "Open FC Library", FileDialog.LOAD);
			fd.setFile("*.txt");
		} else if (type == 3) {
			fd = new FileDialog(this, "Open Calculation Library", FileDialog.LOAD);
			fd.setFile("*.txt");
		}
		fd.setVisible(true);
		String filepath = "";
		if (fd.getDirectory() != null && fd.getFile() != null) {
			filepath = fd.getDirectory() + fd.getFile();
		} else
			filepath = "";
		if (type == 0) {
			filePath.setText(filepath);
		} else if (type == 1) {
			testFilePath.setText(filepath);
			console_println("Test file selected.");
			console_flush();
		} else if (type == 2) {
			libraryPath.setText(filepath);
		} else if (type == 3) {
			libraryCalcPath.setText(filepath);
		}
		if (!(filePath.getText().equals("") || testFilePath.getText().equals("") || libraryPath.getText().equals("")
				|| libraryCalcPath.getText().equals("") || LogFilePath.getText().equals(""))) {
			smtBasedGeneration.setEnabled(true);
			randomTestGeneration.setEnabled(true);
			automationButton.setEnabled(true);
		}	
	}
}