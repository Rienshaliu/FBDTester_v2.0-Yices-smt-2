package FBDTester;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.lang.Math;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import PLC_related.*;
import RandomPool.Item;
import Structure.TestCase;
import plcopen.inf.model.IPOU;
import plcopen.inf.model.IVariable;
import plcopen.inf.type.IConnection;
import plcopen.inf.type.IDataType;
import plcopen.inf.type.IPosition;
import plcopen.inf.type.IVariableList;
import plcopen.inf.type.group.elementary.IElementaryType;
import plcopen.inf.type.group.fbd.IBlock;
import plcopen.inf.type.group.fbd.IInVariable;
import plcopen.inf.type.group.fbd.IInVariableInBlock;
import plcopen.inf.type.group.fbd.IOutVariable;
import plcopen.inf.type.group.fbd.IOutVariableInBlock;
import plcopen.model.ProjectImpl;
import plcopen.type.body.FBD;
import plcopen.xml.PLCModel;

/**
 * @author donghwan-lab
 *
 */
public class XML_load {
	// * Added by donghwan
	private static int testSuiteID = 1;
	private static int iteration = 1; // Iteration index for random play.
	private static float coverageLevel = 0;
	private static int testSuiteSize;
	private static int iterationCount;
	// * Declaration end

	static String model = "";
	static boolean BC, ICC, CCC;

	static drawPanel panel_draw; // Draw panel for displaying diagram.
	static boolean silence = true; // silence flag.
	static int SCAN_TIME = 50;
	// static String LOG_PATH = "C:\\FBDT\\log_def.txt";
	static BufferedWriter writer;
	static BufferedWriter yicesWriter;

	static int colorcount = 0;

	static void console_flush() {
		// Flushes console buffer into GUI Console.
		// DO NOT always flush after console_print,
		// since it requires a lot of time.
		try {
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		CreateGUI.console_flush();
	}

	static void console_print(String str) {
		// Saves str into console buffer if silence flag is false.
		try {
			writer.write(str);
			if (!silence)
				CreateGUI.console_print(str);
			System.out.print(str);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void console_println(String str) {
		// Saves str into console buffer if silence flag is false.

		try {
			writer.write(str + "\r\n");
			if (!silence)
				CreateGUI.console_println(str);
			System.out.println(str);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	static void drawChar(char[] data, int offset, int length, int x, int y) {
		panel_draw.g2d.drawChars(data, offset, length, x, y);
		extendPanelSize(x + 10 * length, y + 20);
	}

	static void drawConnection(Element nextelem, IConnection conn, DPath path, int dx, int dy) {
		if (path == null)
			panel_draw.g2d.setColor(Color.blue);
		else
			panel_draw.g2d.setColor(Color.green);
		long firstid = conn.getRefLocalID();
		long secondid = nextelem.LocalID;

		Element prevelem = getElementById(conn.getRefLocalID());
		IPosition before = null;

		// float hue = (float) (Math.random() * 360) / 360;
		float hue = (float) 0.65;
		float saturation = (float) (0.5 + (Math.random() * 0.5));
		float brightness = (float) (0.0 + (Math.random() * 1.0));
		panel_draw.g2d.setColor(Color.getHSBColor(hue, saturation, brightness));
		for (IPosition current : conn.getPositions()) {
			if (before != null) {
				if (before.getX() == current.getX() || before.getY() == current.getY()) {
					drawLine(before.getX() + dx, before.getY() + dy, current.getX() + dx, current.getY() + dy);
				} else {
					panel_draw.g2d.drawRect(before.getX() - 1 + dx, before.getY() - 31 + dy, 2, 2);
					drawLine(before.getX() + dx, before.getY() + dy, before.getX() + dx, before.getY() - 30 + dy);
				}
			} else {
				panel_draw.g2d.drawRect(current.getX() - 1 + dx, current.getY() - 1 + dy, 2, 2);
			}
			before = current;
		}
		if (before != null) {
			drawLine(before.getX() + dx, before.getY() + dy, before.getX() - 80 + dx, before.getY() + dy);
			panel_draw.g2d.drawRect(before.getX() - 81 + dx, before.getY() - 1 + dy, 2, 2);
		} else {
			CreateGUI.console_println("Error: No graphical information detected in Connection between " + prevelem.LocalID + " and "
					+ nextelem.LocalID);
			if (prevelem.type == Element.INVAR) {
				// int x;
				// for (x = 0; x < nextelem.block.getInVariables().size(); x++)
				// {
				// if
				// (nextelem.block.getInVariables().get(x).getFormalParameter().equals())
				// {
				// break;
				// }
				// }
				drawLine(prevelem.invar.getPosition().getX(), prevelem.invar.getPosition().getY(), nextelem.block.getPosition().getX(),
						nextelem.block.getPosition().getY() + 1 * nextelem.block.getSize().getHeight() / 2);
			} else if (nextelem.type == Element.OUTVAR) {
				drawLine(prevelem.block.getPosition().getX() + prevelem.block.getSize().getWidth(), prevelem.block.getPosition().getY()
						+ prevelem.block.getSize().getHeight() / 2, nextelem.outvar.getPosition().getX(), nextelem.outvar.getPosition()
						.getY());
			} else {
				drawLine(prevelem.block.getPosition().getX() + prevelem.block.getSize().getWidth(), prevelem.block.getPosition().getY()
						+ prevelem.block.getSize().getHeight() / 2, nextelem.block.getPosition().getX(), nextelem.block.getPosition()
						.getY() + 1 * nextelem.block.getSize().getHeight() / 2);
			}
			CreateGUI.console_flush();
		}
		panel_draw.g2d.setColor(Color.black);
	}

	static void drawLine(int x1, int y1, int x2, int y2) {
		panel_draw.g2d.drawLine(x1, y1, x2, y2);
		extendPanelSize(x1, y1);
		extendPanelSize(x2, y2);
	}

	static void drawPicture(int dpath_id) {
		DPath datapath = null;

		if (dpath_id == -1) {
			Color before = panel_draw.g2d.getColor();
			panel_draw.g2d.setColor(Color.white);
			panel_draw.g2d.fillRect(0, 0, 10000, 20000);
			// 10000 이상에서 깨지던 버그 수정. 2011-10-09, by donghwan.
			panel_draw.g2d.setColor(before);
		}
		for (DPath path : DPaths) {
			if (path.DPathID == dpath_id) {
				datapath = path;
				break;
			}
		}
		if (dpath_id != -1 && datapath == null)
			return;

		IPOU POU = PLCProject.getPOUs().get(indexOfTopModule);
		FBD ld = (FBD) POU.getBody();

		for (IOutVariable outVar : ld.getOutVariables()) {
			String chars = "(" + outVar.getLocalID() + ")" + outVar.getExpression();
			if (dpath_id != -1 && outVar.getLocalID() == datapath.endElem.LocalID) {
				drawChar(chars.toCharArray(), 0, chars.length(), outVar.getPosition().getX() - 1, outVar.getPosition().getY() - 3);
				drawChar(chars.toCharArray(), 0, chars.length(), outVar.getPosition().getX(), outVar.getPosition().getY() - 3);
				panel_draw.g2d.setColor(Color.black);
			}
			if (dpath_id == -1) {
				drawChar(chars.toCharArray(), 0, chars.length(), outVar.getPosition().getX(), outVar.getPosition().getY() - 3);
			}
		}
		if (dpath_id == -1) {
			console_println("========BLOCKS========");
		}
		for (IBlock block : ld.getBlocks()) {
			boolean focus = false;
			if (dpath_id != -1) {
				for (Connection c : datapath.connections) {
					if (c.start == block.getLocalID() || c.end == block.getLocalID()) {
						focus = true;
						break;
					}
				}
			}
			String chars = "(" + block.getLocalID() + ")" + block.getTypeName();

			if (focus) {
				float hue = (float) 0.17;
				float saturation = (float) 0.2;
				float brightness = (float) 1.0;
				panel_draw.g2d.setColor(Color.getHSBColor(hue, saturation, brightness));
				panel_draw.g2d.fillRect(block.getPosition().getX(), block.getPosition().getY(), block.getSize().getWidth(), block.getSize()
						.getHeight() + 25);
			}
			panel_draw.g2d.setColor(Color.RED);
			if (focus) {
				drawRect(block.getPosition().getX() - 1, block.getPosition().getY() - 1, block.getSize().getWidth(), block.getSize()
						.getHeight() + 25);
				drawRect(block.getPosition().getX(), block.getPosition().getY() - 1, block.getSize().getWidth(), block.getSize()
						.getHeight() + 25);
				drawRect(block.getPosition().getX() - 1, block.getPosition().getY(), block.getSize().getWidth(), block.getSize()
						.getHeight() + 25);
			}
			drawRect(block.getPosition().getX(), block.getPosition().getY(), block.getSize().getWidth(), block.getSize().getHeight() + 25);
			panel_draw.g2d.setColor(Color.black);
			if (focus) {
				drawChar(chars.toCharArray(), 0, chars.length(), block.getPosition().getX() + 3, block.getPosition().getY() + 11);
			}
			drawChar(chars.toCharArray(), 0, chars.length(), block.getPosition().getX() + 2, block.getPosition().getY() + 11);

			if (focus) {
				int count = 0;
				panel_draw.g2d.setColor(Color.black);
				for (IInVariableInBlock in : block.getInVariables()) {
					String s = in.getFormalParameter();
					drawChar(s.toCharArray(), 0, s.length(), block.getPosition().getX() + 4, block.getPosition().getY() + 30 + count * 30
							+ 6);
					count++;
				}
				count = 0;
				for (IOutVariableInBlock out : block.getOutVariables()) {
					String s = out.getFormalParameter();
					drawChar(s.toCharArray(), 0, s.length(), block.getPosition().getX() + block.getSize().getWidth() + 4, block
							.getPosition().getY() + 30 + count * 30 + 10);
					count++;
				}
				panel_draw.g2d.setColor(Color.black);
			} else {
				panel_draw.g2d.setColor(Color.black);
			}
			int count = 0;
			for (IInVariableInBlock in : block.getInVariables()) {
				String s = in.getFormalParameter();
				drawChar(s.toCharArray(), 0, s.length(), block.getPosition().getX() + 3, block.getPosition().getY() + 30 + count * 30 + 6);
				count++;
			}
			count = 0;
			for (IOutVariableInBlock out : block.getOutVariables()) {
				String s = out.getFormalParameter();
				drawChar(s.toCharArray(), 0, s.length(), block.getPosition().getX() + block.getSize().getWidth() + 3, block.getPosition()
						.getY() + 30 + count * 30 + 10);
				count++;
			}
		}
		for (IBlock block : blocks) {
			Element nextelem = getElementById(block.getLocalID());
			for (IInVariableInBlock inVar : block.getInVariables()) {
				for (IConnection conn : inVar.getConnectionPointIn().getConnections()) {
					boolean contains = false;
					long firstid = conn.getRefLocalID();
					long secondid = nextelem.LocalID;
					if (datapath != null) {
						for (Connection c : datapath.connections) {
							if (c.start == firstid && c.end == secondid) {
								contains = true;
								break;
							}
						}
					}
					drawConnection(nextelem, conn, datapath, 0, 0);
					if (contains) {
						drawConnection(nextelem, conn, datapath, -1, -1);
					}
				}
			}
		}
		for (IOutVariable outVariable : outputVariables) {
			Element nextelem = getElementById(outVariable.getLocalID());
			for (IConnection conn : outVariable.getConnectionPointIn().getConnections()) {
				boolean contains = false;
				long firstid = conn.getRefLocalID();
				long secondid = nextelem.LocalID;
				if (datapath != null) {
					for (Connection c : datapath.connections) {
						if (c.start == firstid && c.end == secondid) {
							contains = true;
							break;
						}
					}
				}
				drawConnection(nextelem, conn, datapath, 0, 0);
				if (contains) {
					drawConnection(nextelem, conn, datapath, -1, -1);
					drawConnection(nextelem, conn, datapath, 0, 1);
				}
			}
		}
		for (IInVariable inVar : ld.getInVariables()) {
			String chars = "(" + inVar.getLocalID() + ")" + inVar.getExpression();
			if (dpath_id != -1 && inVar.getLocalID() == datapath.startElem.LocalID) {
				drawChar(chars.toCharArray(), 0, chars.length(), inVar.getPosition().getX(), inVar.getPosition().getY() - 3);
				drawChar(chars.toCharArray(), 0, chars.length(), inVar.getPosition().getX() + 1, inVar.getPosition().getY() - 3);
				panel_draw.g2d.setColor(Color.black);
			}
			if (dpath_id == -1) {
				panel_draw.g2d.setColor(Color.black);
				drawChar(chars.toCharArray(), 0, chars.length(), inVar.getPosition().getX(), inVar.getPosition().getY() - 3);
			}
		}
		CreateGUI.window.repaint();
		panel_draw.repaint();
	}

	static void drawRect(int x, int y, int width, int height) {
		panel_draw.g2d.drawRect(x, y, width, height);
		extendPanelSize(x + width, y + height);
	}

	static void extendPanelSize(int x, int y) {
		Dimension Current = panel_draw.getPreferredSize();
		if (Current.width < x)
			Current.width = x;
		if (Current.height < y)
			Current.width = y;
		panel_draw.setPreferredSize(Current);
	}

	static Position findPosition(int type, Element prevelem, Element nextelem) {
		Element elem = (type == 0) ? prevelem : nextelem;
		Position pos = new Position();
		if (elem.type == elem.BLOCK) {
			pos.x = elem.block.getPosition().getX();
			pos.y = elem.block.getPosition().getY();
			if (type == 0) {
				pos.x = pos.x + elem.block.getSize().getWidth();
				pos.y = pos.y + elem.block.getSize().getHeight() / 2;
			} else {
				IBlock block = nextelem.block;
				int cnt = 0;
				int RefCount = 1;
				for (IInVariableInBlock inVar : block.getInVariables()) {
					for (IConnection conn : inVar.getConnectionPointIn().getConnections()) {
						cnt++;
						if (prevelem.LocalID == conn.getRefLocalID())
							RefCount = cnt;
					}
				}
				pos.y = pos.y + (elem.block.getSize().getHeight() / (cnt + 1) * RefCount);
			}
		} else if (elem.type == elem.INVAR) {
			pos.x = elem.invar.getPosition().getX();
			pos.y = elem.invar.getPosition().getY() - 5;
		} else {
			pos.x = elem.outvar.getPosition().getX();
			if (type == 1)
				pos.x -= 5;
			pos.y = elem.outvar.getPosition().getY() - 5;
		}
		return pos;
	}

	static String executeCommand(List<String> command, String workspaceFolder) {
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(new File(workspaceFolder));

		try {

			Process process;
			process = builder.start();

			InputStream is = process.getInputStream();
			InputStream es = process.getErrorStream();
			InputStreamReader isr = new InputStreamReader(is);
			InputStreamReader esr = new InputStreamReader(es);

			final BufferedReader br = new BufferedReader(isr);
			final BufferedReader ebr = new BufferedReader(esr);
			final StringBuffer result = new StringBuffer();
			final StringBuffer errResult = new StringBuffer();

			Thread outThread = new Thread(new Runnable() {
				@Override
				public void run() {
					String line;
					try {
						while ((line = br.readLine()) != null) {
							result.append(line + "\n");
							System.out.println(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(-1);
					}
				}
			});
			outThread.start();

			Thread errThread = new Thread(new Runnable() {
				@Override
				public void run() {
					String line;
					try {
						while ((line = ebr.readLine()) != null) {
							errResult.append(line + "\n");
							System.out.println(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(-1);
					}
				}

			});
			errThread.start();

			outThread.join();
			errThread.join();

			int exitVal = process.waitFor();

			br.close();
			ebr.close();
			isr.close();
			esr.close();
			is.close();
			es.close();

			process.destroy();

			if (exitVal != 0) {
				System.out.println(result);
				System.err.println("Execution Error!-1");
				System.err.println(errResult);
				//                System.exit(exitVal);
				return "unsatisfied assertion ids";
			}

			return result.toString();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Execution Error!-2");
			System.exit(-1);
		}

		return null;
	}

	static Element getElementById(long LocalID) {
		// Returns element that has Local ID as given LocalID.
		for (Element elem : elements) {
			if (elem.LocalID == LocalID)
				return elem;
		}
		return null;
	}

	static void getFunctionCalculationLibrary() {
		// Reads function calculation libary from FUNCTIONCALC.TXT
		// and stores into functionCalcLibs(List).
		try {
			BufferedReader in = new BufferedReader(new FileReader(CreateGUI.libraryCalcPath.getText()));
			String s;
			while (true) {
				s = acceptNonComment(in);
				if (s == null)
					break;
				StringTokenizer tok = new StringTokenizer(s, ":");
				String functioninfo = tok.nextToken();
				String outvar = tok.nextToken();
				outvar = outvar.trim();
				functionCalcLibrary fcl = new functionCalcLibrary();
				fcl.outvar = outvar;
				//why had character ")" been omitted?
				StringTokenizer tok2 = new StringTokenizer(functioninfo, "(, \t");
				fcl.functionName = tok2.nextToken();
				while (tok2.hasMoreTokens())
					fcl.invars.add(tok2.nextToken());
				s = acceptNonComment(in);
				//we don't need to insert space to Calc library at the very first time...
				tok = new StringTokenizer(s, " \t");
				String exp = "";
				while (tok.hasMoreTokens())
					exp += tok.nextToken();
				fcl.calculation = exp;
				functionCalcLibs.add(fcl);
			}
			s = "";
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}


	/**
	 * @author donghwan
	 * @param testPath 테스트 문서를 읽어서 input의 tpye을 정함
	 * 
	 * 비슷한 내용이 여러군데에서 쓰이고 있음.
	 * testdoc.txt 내용을 읽어서 데이터의 type을 결정해야 하기 때문에 필요함.
	 * 
	 */
	static void loadTestFile(String testDoc) {
		try {
			BufferedReader testFile = new BufferedReader(new FileReader(testDoc));

			List<Item> testCases = new ArrayList<Item>();
			List<Item> testCCases = new ArrayList<Item>();

			int mode = -1;
			String thisLine = "";
			while ((thisLine = testFile.readLine()) != null) {
				if (thisLine.trim().length() == 0)
					continue;
				if (thisLine.startsWith("//"))
					continue;
				if (thisLine.startsWith("###")) {
					if (thisLine.contains("constants")) {
						mode = 1;
					} else if (thisLine.contains("inputs")) {
						mode = 2;
					} else if (thisLine.contains("outputs")) {
						mode = 3;
					} else if (thisLine.contains("number of test cases")) {
						mode = 4;
					} else if (thisLine.contains("test cases")) {
						mode = 5;
					} else if (thisLine.contains("cTypes")){
						mode = 6;
					}
				} else {
					switch (mode) {
					case 1:
						String[] cNames = thisLine.split(", ");
						for (int i = 0; i < cNames.length; i++)
							testCCases.add(i, new Item(cNames[i]));
						break;
					case 2: // inputs
						String[] inputNames = thisLine.split(", ");
						for (int i = 0; i < inputNames.length; i++){
							System.out.println("input["+i+"]: "+inputNames[i]);
							testCases.add(i, new Item(inputNames[i]));
						}
						System.out.println("testCase: "+testCases.size());
						break;
					case 3:
						break;
					case 4:
						break;
					case 5: // test cases
						String[] testValues = thisLine.split("\t");
						System.out.println("testValues: "+testValues.length);
						if (testValues.length != testCases.size()) {
							System.err.println("ERROR: #input != #test-values");
							System.exit(-1);
						}
						for (int i = 0; i < testCases.size(); i++) {
							for (Element var : invars) {
								if (testCases.get(i).getName().equals(var.invar.getExpression())) {
									if (Character.isLetter(testValues[i].charAt(0))) {
										var.valueType = Element.BOOLEAN;
										testCases.get(i).setType(0);
									} else if (testCases.get(i).getName().contains("CNT")) {
										var.valueType = Element.INTEGER;
										testCases.get(i).setType(1);
									} else {
										var.valueType = Element.REAL;
										testCases.get(i).setType(2);
									}
									if (testCases.get(i).getType() == 0) {
										var.value = (testCases.get(i).getValue() == 0) ? "false" : "true";
									} else {
										var.value = testCases.get(i).getValue() + "";
									}
								}
							}
						}
						break;
					case 6:
						String[] testCValues = thisLine.split("\t");
						if (testCValues.length != testCCases.size()) {
							System.err.println("ERROR: #input != #test-values");
							System.exit(-1);
						}
						for (int i = 0; i < testCCases.size(); i++) {
							for (Element var : invars) {
								if (testCCases.get(i).getName().equals(var.invar.getExpression())) {
									if (Character.isLetter(testCValues[i].charAt(0))) {
										var.valueType = Element.BOOLEAN;
										testCCases.get(i).setType(0);
									} else if (testCCases.get(i).getName().contains("CNT")) {
										var.valueType = Element.INTEGER;
										testCCases.get(i).setType(1);
									} else {
										var.valueType = Element.REAL;
										testCCases.get(i).setType(2);
									}
									if (testCCases.get(i).getType() == 0) {
										var.value = (testCCases.get(i).getValue() == 0) ? "false" : "true";

									} else {
										var.value = testCCases.get(i).getValue() + "";
									}
								}
							}
						}
						break;
					} // end of switch
				} // end of else
			} // end of while
			testFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("loadTestFile(): FATAL ERROR, file not found");
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("loadTestFile(): FATAL ERROR, IOException");
			System.exit(-1);
		}
	}


	static void rewriteDPCWithCalculation(LogicStatement functionCond, long end) {
		// rewrites the "INPUT" variable into prior function block calculations.
		LogicRecursion(functionCond, end);
	}

	static void sortDataPaths() {
		Collections.sort(DPaths);
		if(DPaths.isEmpty())
			System.out.println("DPaths is empty");
		DPath prev = DPaths.get(0);
		console_println("===== [D-Paths] for " + prev.endElem.outvar.getExpression() + " =====");
		for (DPath path : DPaths) {
			String datapath_print = "p" + path.dpath_length + "_" + path.dpath_subindex + " [" + path.DPathID + "] : " + path.datapath_str;
			if (prev.endElem != path.endElem) {
				console_println("===== [D-Paths] for " + path.endElem.outvar.getExpression() + " =====");
			}
			console_println(datapath_print);
			prev = path;
		}
		console_println("====================\n");
		console_flush();
	}

	boolean focused_connection = false;

	private File file;
	static ProjectImpl PLCProject = null;
	private static int indexOfTopModule;
	private final static List<JCheckBox> outvarsCheckboxs = new ArrayList<JCheckBox>();
	private final static List<Element> elements = new ArrayList<Element>();
	private final static List<IInVariable> inputVariables = new ArrayList<IInVariable>();
	private final static List<IOutVariable> outputVariables = new ArrayList<IOutVariable>();
	private final static Set<Element> blockOutVars = new HashSet<Element>();
	private final static HashMap<String,LogicStatement> oneDepthFunctionCalcs = new HashMap<String, LogicStatement>();
	private final static List<IOutVariable> connectedOutputVars = new ArrayList<IOutVariable>();
	private final static Set<Connection> feedbackConnections = new HashSet<Connection>();
	public static int setIter=1;
	public static int defaultIter = 1;
	// unfolding iteration number eg. 4, 5, 6, 7, 8 cycles in a test sequence
	private static int maxIter = 5;
	private final static List<IBlock> blocks = new ArrayList<IBlock>();
	private final static List<DPCStore> functionDPCs = new ArrayList<DPCStore>();
	private final static List<FunctionVariable> functionBlockLocalVars = new ArrayList<FunctionVariable>();
	private final static List<FunctionVariable> functionBlockPreVars = new ArrayList<FunctionVariable>();
	private final static List<Connection> connections = new ArrayList<Connection>();
	public final static List<Element> invars = new ArrayList<Element>();
	public final static List<DPCMacro> DPCMacros = new ArrayList<DPCMacro>();
	private final static List<Element> outvars = new ArrayList<Element>();
	private final static List<functionCalcLibrary> functionCalcLibs = new ArrayList<functionCalcLibrary>();
	static Element dataPath[] = new Element[10000];
	static Connection DPConn[] = new Connection[10000];
	static int DPathCounter;
	private final static List<DPath> allPaths = new ArrayList<DPath>();
	private final static List<DPath> DPaths = new ArrayList<DPath>();
	private final static List<DPath> ICC_DPaths = new ArrayList<DPath>();
	private final static List<DPath> CCC_DPaths = new ArrayList<DPath>();
	static DPath currentDPath;
	public static List<DPCLibrary> DPCLibs = new ArrayList<DPCLibrary>();
	static int dpathCount = 0;
	static String prevDpathOutvar = "";
	static String testPath = "";
	static boolean rewriteDpcNoRecurse = false;
	static int[][] numOfEachTC = new int[100000][1000];

	static String acceptNonComment(BufferedReader in) {
		String s = "";
		while (true) {
			try {
				s = in.readLine();
				if (s == null)
					return null;
				if (s.length() == 0)
					continue;
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			String substr = s.substring(0, 2);
			if (!substr.equals("//")) {
				return s;
			}
		}
	}
	static int longestPathSize = 0;
	static void calculateDPC() {
		console_println("Calculating DPC for each D-Path...\n");
		LogicStatement[] logicStatements = new LogicStatement[10000];
		long macro_first = 0;
		long macro_second = 0;
		int macro_uniqueid = 0;
		int temp_iter=0;
		HashMap<Long, LogicStatement> tuningBlockOrderET = new HashMap<Long, LogicStatement>();
		HashMap<Long, LogicStatement> tuningBlockOrderQ = new HashMap<Long, LogicStatement>();
		/* DPath connections iteration start --------------------------------------------------------*/
		for (DPath path : allPaths) {
			Connection con;
			boolean first = true;
			boolean previouslyAdded = false;
			Connection prevcon = null;
			String functionName, fName;
			String invar;
			String outvar;
			String dpc_str = "";
			macro_first = path.DPathID;
			macro_second = 0;
			System.out.println("p" + path.dpath_length + "_" + path.dpath_subindex + " [" + path.DPathID + "] : " + path.datapath_str);
			if (longestPathSize < path.connections.size())
				longestPathSize = path.connections.size();
			/* connection size iteration start ---------------------------------------------------------------- */
			for (int i = path.connections.size() - 1; i >= 0; i--) {
				con = path.connections.get(i);
				if (prevcon == null) {
					prevcon = con;
					continue;
				}
				macro_first = con.start;
				macro_second = con.end;
				getElementById(con.end);
				fName = getElementById(con.end).block.getTypeName();
				if(fName.endsWith("_BOOL"))
					functionName = fName.substring(0, fName.length()-5);
				else if(fName.endsWith("_DINT"))
					functionName = fName.substring(0, fName.length()-5);
				else if(fName.endsWith("_REAL"))
					functionName = fName.substring(0, fName.length()-5);
				else
					functionName = fName;
				invar = con.endParam;
				outvar = prevcon.startParam;
				System.out.println(functionName + " : " + invar + " : " +outvar );
				DPCLibrary functionDPC = findDPCLibrary(functionName, invar, outvar);
				LogicStatement functionCond;
				LogicStatement functionCond_one_depth;
				
				if (functionDPC == null) {
					console_println("### Warning : " + functionName + "(" + invar + ")->" + outvar
							+ " not found in library. DPC Calculated as TRUE.");
					functionCond = new LogicStatement(LogicStatement.VARIABLE, "true");
					functionCond_one_depth = new LogicStatement(LogicStatement.VARIABLE, "true");
				} else {
					if (functionDPC.condition.toLowerCase().equals("true")) {
						functionCond = new LogicStatement(LogicStatement.VARIABLE, "true");
						functionCond_one_depth = new LogicStatement(LogicStatement.VARIABLE, "true");
					} else if (functionDPC.condition.toLowerCase().equals("false")) {
						functionCond = new LogicStatement(LogicStatement.VARIABLE, "false");
						functionCond_one_depth = new LogicStatement(LogicStatement.VARIABLE, "false");
					} else {
						functionCond = new LogicStatement(functionDPC.condition);
						functionCond_one_depth = new LogicStatement(functionDPC.condition);
					}
					System.out.println("functionCon: "+functionCond_one_depth);
				}

				if (functionCond.type == LogicStatement.VARIABLE) {
					LogicStatement varCond = functionCond;
					functionCond = new LogicStatement(LogicStatement.EMBRACE, varCond);
				}

				if (functionCond_one_depth.type == LogicStatement.VARIABLE) {
					LogicStatement varCond_one_depth = functionCond_one_depth;
					functionCond_one_depth = new LogicStatement(LogicStatement.EMBRACE, varCond_one_depth);
				}


				//for one-depth function condition connection rewriter
				rewriteDpcNoRecurse = true;
				rewriteDPCWithCalculation(functionCond_one_depth, con.end);
				rewriteDpcNoRecurse = false;

				/* To rewrite the input and output of Calc library into corresponding variable start --------------------- */
				functionCalcLibrary functionCL = findCalcLibrary(functionName, outvar);

				LogicStatement functionCalc;
				LogicStatement functionCalcforBlock;
				if (functionCL == null) {
					console_println("### Warning : " + functionName + ")->" + outvar
							+ " not found in library. Function Calculated as TRUE.");
					functionCalc = new LogicStatement(LogicStatement.VARIABLE, "true");
				} else {
					if(functionCL.calculation.equals("in_T")){
						functionCalcLibrary functionCLforB = findCalcLibrary(functionName,"in_T");
						functionCalcforBlock = new LogicStatement(functionCLforB.calculation);

						rewriteDpcNoRecurse = true;
						rewriteDPCWithCalculation(functionCalcforBlock, con.end);
						rewriteDpcNoRecurse = false;
						functionCalcforBlock.dpcl = functionDPC;
						functionCalcforBlock.blockId = con.end;
						functionCalcforBlock.blockOrder = (path.connections.size()-1)-i;
						if(functionDPC.outVar.equals("ET")){
							tuningBlockOrderET.put(con.end, functionCalcforBlock);
							if(tuningBlockOrderQ.containsKey(con.end)){
								LogicStatement tempLs = tuningBlockOrderQ.get(con.end);
								if(tempLs.blockOrder >= functionCalcforBlock.blockOrder) 
									functionCalcforBlock.blockOrder =  tempLs.blockOrder + 0.5;
							}
						}
						oneDepthFunctionCalcs.put(functionCalcforBlock.dpcl.functionName+functionCalcforBlock.blockId+"_"+functionCalcforBlock.dpcl.outVar.toLowerCase(), functionCalcforBlock);
					}
					else{
						functionCalc = new LogicStatement(functionCL.calculation);
						rewriteDpcNoRecurse = true;
						rewriteDPCWithCalculation(functionCalc, con.end);
						rewriteDpcNoRecurse = false;
						
						// detect the negated output edge of block
						for (IBlock block : blocks) {
							for (IOutVariableInBlock OutVar : block.getOutVariables()) {
								if (block.getLocalID() == con.end) {
									if (OutVar.isNegated()) {
										functionCalc = new LogicStatement(LogicStatement.NOT, functionCalc);
									}
								}
							}
						}

						functionCalc.dpcl = functionDPC;
						functionCalc.blockId = con.end;
						functionCalc.blockOrder = (path.connections.size()-1)-i;

						if(functionCL.functionName.equals("TON")||functionCL.functionName.equals("TOF")||functionCL.functionName.equals("TP")){
							tuningBlockOrderQ.put(con.end, functionCalc);
							if(tuningBlockOrderET.containsKey(con.end)){
								LogicStatement tempLs = tuningBlockOrderET.get(con.end);
								if(tempLs.blockOrder <= functionCalc.blockOrder) 
									tempLs.blockOrder = functionCalc.blockOrder + 0.5;
								oneDepthFunctionCalcs.put(tempLs.dpcl.functionName+tempLs.blockId+"_"+tempLs.dpcl.outVar.toLowerCase(),tempLs);
							}
						}

						if(oneDepthFunctionCalcs.containsKey(functionCalc.dpcl.functionName+functionCalc.blockId+"_"+functionCalc.dpcl.outVar.toLowerCase())){
							double preBlockOrder = oneDepthFunctionCalcs.get(functionCalc.dpcl.functionName+functionCalc.blockId+"_"+functionCalc.dpcl.outVar.toLowerCase()).blockOrder;
							if(preBlockOrder > functionCalc.blockOrder)
								functionCalc.blockOrder = preBlockOrder;
						}

						oneDepthFunctionCalcs.put(functionCalc.dpcl.functionName+functionCalc.blockId+"_"+functionCalc.dpcl.outVar.toLowerCase(), functionCalc);
					}
				}

				// ---------------------- To rewrite the input and output of Calc library into corresponding variable end */
				if (functionCond_one_depth.L1.type == LogicStatement.VARIABLE
						&& functionCond_one_depth.L1.variable.toLowerCase().equals("true")) {
					if (CreateGUI.displayTrue.isSelected()) {
						if (!first)
							dpc_str =  " (and" + dpc_str;
						boolean macro_found = false;
						String macro = "";
						for (DPCMacro m : DPCMacros) {
							if (m.DPC.equals(functionCond_one_depth.YicesString(0,true))) {
								macro = m.macroname;
								macro_found = true;
							}
						}
						if (!macro_found) {
							DPCMacro m = new DPCMacro();
							m.DPC = functionCond_one_depth.YicesString(0,true);
							macro = "C" + macro_first + "_" + macro_second + "_" + macro_uniqueid;
							macro_uniqueid++;
							m.macroname = macro;
							DPCMacros.add(m);
						}
						if (first)
							dpc_str = dpc_str + " " + macro;
						else
							dpc_str = dpc_str + " " + macro+ ")";
						previouslyAdded = true;
						if (first) {
							first = false;
						}
					}
				} else {
					if (!first)
						dpc_str = " (and" + dpc_str;

					boolean macro_found = false;
					String macro = "";
					for (DPCMacro m : DPCMacros) {
						if (m.DPC.equals(functionCond_one_depth.YicesString(0,true))) {	
							macro = m.macroname;
							macro_found = true;
						}
					}
					if (!macro_found) {
						DPCMacro m = new DPCMacro();
						m.DPC = functionCond_one_depth.YicesString(0,true);
						macro = "C" + macro_first + "_" + macro_second + "_" + macro_uniqueid;
						macro_uniqueid++;
						m.macroname = macro;
						DPCMacros.add(m);
					}
					if(first)
						dpc_str = dpc_str + " " + macro;
					else
						dpc_str = dpc_str + " " + macro + ")";
					if (first) {
						first = false;
					}
					previouslyAdded = true;
				}

				//for the full path of function condition connection rewriter
				rewriteDPCWithCalculation(functionCond, con.end);

				logicStatements[i] = functionCond;
				prevcon = con;
			}
			// ----------------------------------------------------------------connection size iteration end */

			if (path.connections.size() >= 3) {
				LogicStatement L = new LogicStatement(LogicStatement.AND, logicStatements[0], logicStatements[1]);
				for (int i = 2; i < path.connections.size() - 1; i++) {
					LogicStatement L_ = new LogicStatement(LogicStatement.AND, L, logicStatements[i]);
					L = L_;
				}
				path.DPC = L;
			} else {
				path.DPC = logicStatements[0];
			}
			if (dpc_str.equals(""))
				dpc_str = "true";
			path.dpc_str = dpc_str;
			for(DPath dpath : DPaths){
				if(path.datapath_str.equals(dpath.datapath_str)){
					dpath.dpc_str = dpc_str;
					dpath.DPC = path.DPC;
				}
			}
		}
		// ------------------------------------------------------------------DPath connections iteration end */



		console_println("====================================");
		console_println(" Macros ");
		console_println("====================================");
		Collections.sort(DPCMacros);
		for (DPCMacro m : DPCMacros) {
			console_println(m.macroname + " : " + m.DPC);
		}
		console_println("\n====================================");
		console_println(" DPCs");
		console_println("====================================");

		DPath prev = DPaths.get(0);
		console_println("===== [DPCs] for " + prev.endElem.outvar.getExpression() + " =====");
		for (DPath path : DPaths) {
			if (prev.endElem != path.endElem) {
				console_println("===== [DPCs] for " + path.endElem.outvar.getExpression() + " =====");
			}
			console_println("p" + path.dpath_length + "_" + path.dpath_subindex + " [" + path.DPathID + "] : " + path.dpc_str);
			prev = path;
		}
		console_println("\nDone.");
	}

	static void createCCCPath() {
		console_println("\nCalculating DPC For CCC Test...");
		int id = 0;
		for (DPath path : DPaths) {
			String startVal = "";
			boolean isPathAdded = false;
			boolean isSequenceInput =true;
			for(int i = 0 ; i<inputs.length; i++){
				if(path.startElem.invar.getExpression().equals(inouts[i])){
					isSequenceInput=false;
					break;
				}
			}
			if (/*isSequenceInput && */path.startElem.value != null && path.startElem.valueType == Element.BOOLEAN){
				DPath newTruePath = new DPath();
				DPath newFalsePath = new DPath();
				newTruePath.DPathID = path.DPathID;
				newFalsePath.DPathID = path.DPathID;
				LogicStatement trueIn = new LogicStatement(path.startElem.invar.getExpression());
				LogicStatement falseIn = new LogicStatement("~" + path.startElem.invar.getExpression());
				LogicStatement trueDPC = new LogicStatement(LogicStatement.AND, trueIn, path.DPC);
				LogicStatement falseDPC = new LogicStatement(LogicStatement.AND, falseIn, path.DPC);
				newTruePath.dpc_str = " (and " +path.startElem.invar.getExpression() + " p" + path.dpath_length + "_"
						+ path.dpath_subindex + "_" + path.dPathType+")";
				newFalsePath.dpc_str = " (and " + "(not " + path.startElem.invar.getExpression() + ") p" + path.dpath_length + "_"
						+ path.dpath_subindex + "_" + path.dPathType + ")";
				newTruePath.dpath_length = path.dpath_length;
				newTruePath.dpath_subindex = path.dpath_subindex;
				newFalsePath.dpath_length = path.dpath_length;
				newFalsePath.dpath_subindex = path.dpath_subindex;
				newTruePath.DPC = trueDPC;
				newFalsePath.DPC = falseDPC;
				newTruePath.endElem = path.endElem;
				newFalsePath.endElem = path.endElem;
				newTruePath.identifier = id*2;
				newFalsePath.identifier = id*2+1;
				CCC_DPaths.add(newTruePath);
				//if(!isSequenceInput)
					CCC_DPaths.add(newFalsePath);
				isPathAdded = true;
				id++;
			}
			//if(isSequenceInput)
			Connection prevcon = null;
			for (Connection conn : path.connections) {
				if (getElementById(conn.start).type != Element.BLOCK){
					prevcon = conn;
					continue;
				}
				IBlock block = getElementById(conn.start).block;
				String blockname = block.getTypeName();
				String function_name;
				if(blockname.endsWith("_BOOL"))
					function_name = blockname.substring(0, blockname.length()-5);
				else if(blockname.endsWith("_DINT"))
					function_name = blockname.substring(0, blockname.length()-5);
				else if(blockname.endsWith("_REAL"))
					function_name = blockname.substring(0, blockname.length()-5);
				else
					function_name = blockname;
				// DPCLibrary dpclib = findDPCLibraryByName(function_name);
				DPCLibrary dpclib = findDPCLibrary(function_name, prevcon.endParam, conn.startParam);
				
				if (dpclib == null) {
					dpclib = findDPCLibraryByName(function_name);
				}

				if (dpclib.boolOutput == false)
					continue;
				// CURRENT BLOCK IS BOOLEAN
				LogicStatement fcalcLogic = new LogicStatement(conn.endParam);
				LogicStatement embraceLogic = new LogicStatement(LogicStatement.EMBRACE, fcalcLogic);
				rewriteDPCWithCalculation(embraceLogic, conn.end);
				LogicStatement newTrueLogic = new LogicStatement(LogicStatement.AND, embraceLogic, path.DPC);
				LogicStatement intFalseLogic = new LogicStatement(LogicStatement.NOT, embraceLogic);
				LogicStatement newFalseLogic = new LogicStatement(LogicStatement.AND, intFalseLogic, path.DPC);
				DPath newTruePath = new DPath();
				DPath newFalsePath = new DPath();
				newTruePath.DPathID = path.DPathID;
				newTruePath.DPC = newTrueLogic;

				String function_name_to_add = function_name + conn.start + "_" + conn.startParam.toLowerCase();
				
				// boolean isSequenceOutput = true;
				
				// for (Connection c : connections) {
					// if (c.start == conn.start) {
						Element elem = getElementById(conn.end);
						if (elem.type == Element.OUTVAR)// {
							function_name_to_add = elem.outvar.getExpression();
							// for(int i = 0 ; i<inouts.length; i++){
								// if(function_name_to_add.equals(inouts[i])){
									// isSequenceOutput=false;
									// break;
								// }
							// }
							// break;
						// }
					// }
				// }

				newTruePath.dpc_str = " (and " + function_name_to_add + " p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType + ")";
				newFalsePath.DPathID = path.DPathID;
				newFalsePath.DPC = newFalseLogic;
				newFalsePath.dpc_str = " (and " + "(not " + function_name_to_add + ") p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType + ")";
				newTruePath.dpath_length = path.dpath_length;
				newTruePath.dpath_subindex = path.dpath_subindex;
				newFalsePath.dpath_length = path.dpath_length;
				newFalsePath.dpath_subindex = path.dpath_subindex;
				newTruePath.endElem = path.endElem;
				newFalsePath.endElem = path.endElem;
				newTruePath.identifier = id*2;
				newFalsePath.identifier = id*2+1;
				isPathAdded = true;
//				if(isSequenceOutput){
				CCC_DPaths.add(newTruePath);
				CCC_DPaths.add(newFalsePath);
//				}
				id++;
				prevcon = conn;
			}
			if (!isPathAdded) {
				//				CCC_DPaths.add(path);
			}
		}
		DPath prev = CCC_DPaths.get(0);
		console_println("===== [CCC TRs] for " + prev.endElem.outvar.getExpression() + " =====");
		for (DPath path : CCC_DPaths) {
			if (prev.endElem != path.endElem) {
				console_println("===== [CCC TRs] for " + path.endElem.outvar.getExpression() + " =====");
			}
			console_println("p" + path.dpath_length + "_" + path.dpath_subindex + " [" + path.DPathID + "] : " + path.dpc_str);
			prev = path;
		}
		console_println("Done.\n");
	}

	static void createICCPath() {
		console_println("\nCalculating DPC For ICC Test...");

		for (DPath path : DPaths) {
			boolean isSequenceInput =true;
			/*
			for(int i = 0 ; i<inputs.length; i++){
				if(path.startElem.invar.getExpression().equals(inouts[i])){
					isSequenceInput=false;
					break;
				}
			}
			*/
			if (isSequenceInput && path.startElem.value != null && path.startElem.valueType == Element.BOOLEAN){
				DPath newTruePath = new DPath();
				DPath newFalsePath = new DPath();
				newTruePath.DPathID = path.DPathID;
				newFalsePath.DPathID = path.DPathID;
				LogicStatement trueIn = new LogicStatement(path.startElem.invar.getExpression());
				LogicStatement falseIn = new LogicStatement("~" + path.startElem.invar.getExpression());
				LogicStatement trueDPC = new LogicStatement(LogicStatement.AND, trueIn, path.DPC);
				LogicStatement falseDPC = new LogicStatement(LogicStatement.AND, falseIn, path.DPC);
				newTruePath.dpc_str =  " (and " + path.startElem.invar.getExpression() + " p" + path.dpath_length + "_"
						+ path.dpath_subindex + "_" + path.dPathType + ")";
				newFalsePath.dpc_str = " (and "+"(not " + path.startElem.invar.getExpression() + ") p" + path.dpath_length + "_"
						+ path.dpath_subindex + "_" + path.dPathType + ")";
				newTruePath.dpath_length = path.dpath_length;
				newTruePath.dpath_subindex = path.dpath_subindex;
				newFalsePath.dpath_length = path.dpath_length;
				newFalsePath.dpath_subindex = path.dpath_subindex;
				newTruePath.DPC = trueDPC;
				newFalsePath.DPC = falseDPC;
				newTruePath.endElem = path.endElem;
				newFalsePath.endElem = path.endElem;
				newFalsePath.identifier = 1;
				ICC_DPaths.add(newTruePath);
				//if(!isSequenceInput)
					ICC_DPaths.add(newFalsePath);
			} else {
				//				ICC_DPaths.add(path);
			}
		}

		// boolean input이 없는 경우, ICC_DPaths.size() = 0 일 수 있다.
		if(ICC_DPaths.size() == 0) {
			console_println("ICC has no paths!");
		} else {
			DPath prev = ICC_DPaths.get(0);
			console_println("===== [ICC TRs] for " + prev.endElem.outvar.getExpression() + " =====");
			for (DPath path : ICC_DPaths) {
				if (prev.endElem != path.endElem) {
					console_println("===== [ICC TRs] for " + path.endElem.outvar.getExpression() + " =====");
				}
				console_println("p" + path.dpath_length + "_" + path.dpath_subindex + " [" + path.DPathID + "] : " + path.dpc_str);
				prev = path;
			}
			console_println("Done.\n");
		}
	}

	public static HashMap<Long, String> in_Ts = new HashMap<Long, String>();
	public static HashMap<Long, String> CVs = new HashMap<Long, String>();
	public static HashMap<Long, String> pre_INs = new HashMap<Long, String>();
	public static HashMap<Long, String> pre_CVs = new HashMap<Long, String>();
	public static HashMap<Long, String> pre_CLKs = new HashMap<Long, String>();
	public static HashMap<Long, String> pre_Q1s = new HashMap<Long, String>();

	static void determineLogicAndRecurse(LogicStatement parent, LogicStatement l, long end) {
		if (l == null)
			return;
		if (l.type != LogicStatement.VARIABLE) {
			determineLogicAndRecurse(l, l.L1, end);
			determineLogicAndRecurse(l, l.L2, end);
			determineLogicAndRecurse(l, l.L3, end);
			return;
		}
		if (l.variable == null) {
			return;
		}
		// Find the connected block.
		//
		// start end
		//    +-----------+    +---------+
		// A--+  outParam +----+ inParam |
		// B--+           +    |         |
		//    +-----------+    +---------+
		//
		String inParam = l.variable;
		long start = 0, next = 0;
		String outParam = null;
		boolean negated = false, nextNegated = false;

		if (inParam.equals("in_T")){
			for(Connection c : connections){
				if(c.start == end){
					next = c.end;
					for(Connection cc: connections){
						if(next == cc.end)
							nextNegated = cc.negated;
					}
					if(getElementById(next).type == Element.OUTVAR && c.startParam.equals("ET")){
						String nextName = getElementById(next).outvar.getExpression();
						//여기서 바꾸면 될듯
						in_Ts.put(next, nextName);
						LogicStatement newLogic = new LogicStatement(LogicStatement.VARIABLE, inParam+next);

						if (nextNegated) {
							LogicStatement priorLogic = newLogic;
							newLogic = new LogicStatement(LogicStatement.NOT, priorLogic);
						}
						if (parent.L1 == l)
							parent.L1 = newLogic;
						else if (parent.L2 == l)
							parent.L2 = newLogic;
						else if (parent.L3 == l)
							parent.L3 = newLogic;
						return;
					}
				}
			}
//		} else if (inParam.equals("CV")){
//			for(Connection c : connections){
//				if(c.start == end){
//					next = c.end;
//					for(Connection cc: connections){
//						if(next == cc.end)
//							nextNegated = cc.negated;
//					}
//					if(getElementById(next).type == Element.OUTVAR && c.startParam.equals("CV")){
//						String nextName = getElementById(next).outvar.getExpression();
//						//여기서 바꾸면 될듯
//						CVs.put(next, nextName); 
//						LogicStatement newLogic = new LogicStatement(LogicStatement.VARIABLE, inParam+next);
//
//						if (nextNegated) {
//							LogicStatement priorLogic = newLogic;
//							newLogic = new LogicStatement(LogicStatement.NOT, priorLogic);
//						}
//						if (parent.L1 == l)
//							parent.L1 = newLogic;
//						else if (parent.L2 == l)
//							parent.L2 = newLogic;
//						else if (parent.L3 == l)
//							parent.L3 = newLogic;
//						return;
//					}
//				}
//			}
		} else if(inParam.equals("pre_IN")){
			for(Connection c : connections){
				if(c.end == end){
					start = c.start;
					outParam = c.startParam;
					negated = c.negated;
					if(getElementById(c.start).type == Element.BLOCK){
						String startName = getElementById(start).block.getTypeName();
						String funName = "";
						//StringTokenizer tok = new StringTokenizer(startName, "_");
						//funName = tok.nextToken();
						if(startName.endsWith("_BOOL"))
							funName = startName.substring(0, startName.length()-5);
						else if(startName.endsWith("_DINT"))
							funName = startName.substring(0, startName.length()-5);
						else if(startName.endsWith("_REAL"))
							funName = startName.substring(0, startName.length()-5);
						else
							funName = startName;
						pre_INs.put(start, funName + start + "_" + outParam.toLowerCase());
						LogicStatement newLogic = new LogicStatement(LogicStatement.VARIABLE, inParam+start);

						if (negated) {
							LogicStatement priorLogic = newLogic;
							newLogic = new LogicStatement(LogicStatement.NOT, priorLogic);
						}
						if (parent.L1 == l)
							parent.L1 = newLogic;
						else if (parent.L2 == l)
							parent.L2 = newLogic;
						else if (parent.L3 == l)
							parent.L3 = newLogic;
						return;
					} 
				}
			}
		} else if(inParam.equals("pre_CLK")){
			for(Connection c : connections){
				if(c.end == end){
					start = c.start;
					outParam = c.startParam;
					negated = c.negated;
					if(getElementById(c.start).type == Element.BLOCK){
						String startName = getElementById(start).block.getTypeName();
						String funName = "";
						//StringTokenizer tok = new StringTokenizer(startName, "_");
						//funName = tok.nextToken();
						if(startName.endsWith("_BOOL"))
							funName = startName.substring(0, startName.length()-5);
						else if(startName.endsWith("_DINT"))
							funName = startName.substring(0, startName.length()-5);
						else if(startName.endsWith("_REAL"))
							funName = startName.substring(0, startName.length()-5);
						else
							funName = startName;
						pre_CLKs.put(start, funName + start + "_" + outParam.toLowerCase());
						LogicStatement newLogic = new LogicStatement(LogicStatement.VARIABLE, inParam+start);

						if (negated) {
							LogicStatement priorLogic = newLogic;
							newLogic = new LogicStatement(LogicStatement.NOT, priorLogic);
						}
						if (parent.L1 == l)
							parent.L1 = newLogic;
						else if (parent.L2 == l)
							parent.L2 = newLogic;
						else if (parent.L3 == l)
							parent.L3 = newLogic;
						return;
					}
				}
			}
		} else if(inParam.equals("pre_Q1")){
			for(Connection c : connections){
				if(c.start == end){
					start = c.start;
					outParam = c.startParam;
					negated = c.negated;
					if(getElementById(c.start).type == Element.BLOCK){
						String startName = getElementById(start).block.getTypeName();
						String funName = "";
						//StringTokenizer tok = new StringTokenizer(startName, "_");
						//funName = tok.nextToken();
						if(startName.endsWith("_BOOL"))
							funName = startName.substring(0, startName.length()-5);
						else if(startName.endsWith("_DINT"))
							funName = startName.substring(0, startName.length()-5);
						else if(startName.endsWith("_REAL"))
							funName = startName.substring(0, startName.length()-5);
						else
							funName = startName;
						if(getElementById(c.end).type == Element.OUTVAR)
							pre_Q1s.put(start, getElementById(c.end).outvar.getExpression());
						else
							pre_Q1s.put(start, funName + start + "_" + outParam.toLowerCase());
						LogicStatement newLogic = new LogicStatement(LogicStatement.VARIABLE, inParam+start);

						if (negated) {
							LogicStatement priorLogic = newLogic;
							newLogic = new LogicStatement(LogicStatement.NOT, priorLogic);
						}
						if (parent.L1 == l)
							parent.L1 = newLogic;
						else if (parent.L2 == l)
							parent.L2 = newLogic;
						else if (parent.L3 == l)
							parent.L3 = newLogic;
						return;
					}
				}
			}
		} else if(inParam.equals("pre_CV")){
			for(Connection c : connections){
				if(c.start == end && c.startParam.equals("CV")){
					start = c.start;
					outParam = c.startParam;
					negated = c.negated;
					if(getElementById(c.start).type == Element.BLOCK){
						String startName = getElementById(start).block.getTypeName();
						String funName = "";
						//StringTokenizer tok = new StringTokenizer(startName, "_");
						//funName = tok.nextToken();
						if(startName.endsWith("_BOOL"))
							funName = startName.substring(0, startName.length()-5);
						else if(startName.endsWith("_DINT"))
							funName = startName.substring(0, startName.length()-5);
						else if(startName.endsWith("_REAL"))
							funName = startName.substring(0, startName.length()-5);
						else
							funName = startName;
						if(getElementById(c.end).type == Element.OUTVAR){
							pre_CVs.put(start, getElementById(c.end).outvar.getExpression());
						}else
							pre_CVs.put(start, funName + start + "_" + outParam.toLowerCase());
						LogicStatement newLogic = new LogicStatement(LogicStatement.VARIABLE, inParam+start);

						if (negated) {
							LogicStatement priorLogic = newLogic;
							newLogic = new LogicStatement(LogicStatement.NOT, priorLogic);
						}
						if (parent.L1 == l)
							parent.L1 = newLogic;
						else if (parent.L2 == l)
							parent.L2 = newLogic;
						else if (parent.L3 == l)
							parent.L3 = newLogic;
						return;
					}
				}
			}
		}
		for (Connection c : connections) {
			if (c.end == end && c.endParam.equals(inParam)) {
				start = c.start;
				outParam = c.startParam;
				negated = c.negated;
				break;
			}
		}
		if (outParam == null)
			return;

		if (!rewriteDpcNoRecurse) {
			for (DPCStore store : functionDPCs) {
				if (store.variable.equals(inParam) && store.end_local_id == end) {
					LogicStatement newLogic = store.dpc;
					if (parent.L1 == l)
						parent.L1 = newLogic;
					else if (parent.L2 == l)
						parent.L2 = newLogic;
					else if (parent.L3 == l)
						parent.L3 = newLogic;
					return;
				}
			}
		}

		if (getElementById(start).type == Element.BLOCK) {
			String startName = getElementById(start).block.getTypeName();
			String funName = "";
			//StringTokenizer tok = new StringTokenizer(startName, "-");
			//funName = tok.nextToken();
			if(startName.endsWith("_BOOL"))
				funName = startName.substring(0, startName.length()-5);
			else if(startName.endsWith("_DINT"))
				funName = startName.substring(0, startName.length()-5);
			else if(startName.endsWith("_REAL"))
				funName = startName.substring(0, startName.length()-5);
			else
				funName = startName;
			boolean found = false;
			for (functionCalcLibrary FCL : functionCalcLibs) {
				if (FCL.functionName.equals(funName) && FCL.outvar.equals(outParam)) {
					LogicStatement newLogic;
					found = true;
					if (!rewriteDpcNoRecurse) {

						if (negated) {
							newLogic = new LogicStatement("(~" + FCL.calculation + ")");
						} else {
							newLogic = new LogicStatement(FCL.calculation);
						}
					} else {
						LogicStatement logicVar = new LogicStatement(LogicStatement.VARIABLE, funName +  start+ "_" + outParam.toLowerCase());
						if (negated) {
							newLogic = new LogicStatement(LogicStatement.NOT, logicVar);
						} else {
							newLogic = logicVar;
						}
					}
					if (parent.L1 == l)
						parent.L1 = newLogic;
					else if (parent.L2 == l)
						parent.L2 = newLogic;
					else if (parent.L3 == l)
						parent.L3 = newLogic;

					if (!rewriteDpcNoRecurse) {
						rewriteDPCWithCalculation(newLogic, start);

						DPCStore newStore = new DPCStore();
						newStore.dpc = newLogic;
						newStore.variable = inParam;
						newStore.end_local_id = end;

						functionDPCs.add(newStore);
					}

					return;
				}
			}
			if (!found) {
				System.err.println("No " + startName + " Function in Calculation Library!");
			}
		}else { // INVAR
			String startName = getElementById(start).invar.getExpression();
			char[] varstr_array = startName.toCharArray();
			boolean isalphabet = false;
			boolean isreal = false;
			LogicStatement newLogic;
			if(inParam.equals("pre_CLK") || inParam.equals("pre_Q1"))
				newLogic = new LogicStatement(LogicStatement.VARIABLE, startName+"_pre");
			else
				newLogic = new LogicStatement(LogicStatement.VARIABLE, startName);
			if (negated) {
				LogicStatement priorLogic = newLogic;
				newLogic = new LogicStatement(LogicStatement.NOT, priorLogic);
			}
			if (startName.toLowerCase().equals("true")) {
				newLogic = new LogicStatement(LogicStatement.VALUE, true);
			} else if (startName.toLowerCase().equals("false")) {
				newLogic = new LogicStatement(LogicStatement.VALUE, false);
			} else {
				for (int j = 0; j < startName.length(); j++) {
					if (Character.isLetter(varstr_array[j])) {
						isalphabet = true;
						break;
					}
					if (varstr_array[j] == '.') {
						isreal = true;
					}
					if (varstr_array[j] != '.' && !Character.isLetterOrDigit(varstr_array[j])) {
						isalphabet = true;
					}
				}
				if (!isalphabet) {
					if (isreal) {
						newLogic.type = LogicStatement.VALUE;
						newLogic.valueType = LogicStatement.REAL;
						newLogic.realValue = Double.parseDouble(startName);
					} else {
						newLogic.type = LogicStatement.VALUE;
						newLogic.valueType = LogicStatement.INTEGER;
						newLogic.intValue = Integer.parseInt(startName);
					}
				}
			}

			if (parent.L1 == l)
				parent.L1 = newLogic;
			else if (parent.L2 == l)
				parent.L2 = newLogic;
			else if (parent.L3 == l)
				parent.L3 = newLogic;

			if (!rewriteDpcNoRecurse) {
				DPCStore newStore = new DPCStore();
				newStore.dpc = newLogic;
				newStore.variable = inParam;
				newStore.end_local_id = end;

				functionDPCs.add(newStore);
			}

		}
	}

	static void findDataPaths() {
		console_println("\n\n\n=== Data Paths ===");
		currentDPath = new DPath();
		DPathCounter = 0;
		for (Element elem : outvars) {
			currentDPath.endElem = elem;
			for(Connection con : connections){
				if(elem.LocalID == con.end){
					if(con.startParam.equals("ET")){
						//isOutvar = false;
						DPathRecursion(elem, 0, false);
					}
					else{
						DPathRecursion(elem, 0, true);
						DPathRecursion(elem, 0, false);
					}
				}
			}
		}
		if (DPathCounter == 0
				/*|| (!CreateGUI.BCTestCheck.isSelected() && !CreateGUI.ICCTestCheck.isSelected() && !CreateGUI.CCCTestCheck.isSelected())*/) {
			System.err.println("[Error]D-Path doesn't created or none of tests are selected.");
		}
	}

	static void DPathRecursion(Element elem, int depth, boolean isDpath) {
		dataPath[depth] = elem;
		boolean isOutvar = true;
		boolean pathExist = false;
		for (Connection conn : connections) {
			if (conn.end == elem.LocalID) {
				pathExist = true;
				DPConn[depth] = conn;
				DPathRecursion(getElementById(conn.start), depth + 1, isDpath);
			}
		}
		if (!pathExist) {
			currentDPath.startElem = elem;
			for (int i = depth - 1; i >= 0; i--)
				currentDPath.connections.add(DPConn[i]);
			String datapath = "<";
			for (int i = depth; i >= 0; i--) {
				Element el = dataPath[i];
				if (i != 1)
					datapath += "(" + el.LocalID + ")";
				if (el.type == el.INVAR){
					datapath += el.invar.getExpression();
				}
				else if (el.type == el.OUTVAR){
					datapath += el.outvar.getExpression();
				}
				else {
					String blockname = el.block.getTypeName();
					//StringTokenizer tok = new StringTokenizer(blockname, "-");
					//blockname = tok.nextToken();
					String funName = "";
					if(blockname.endsWith("_BOOL"))
						funName = blockname.substring(0, blockname.length()-5);
					else if(blockname.endsWith("_DINT"))
						funName = blockname.substring(0, blockname.length()-5);
					else if(blockname.endsWith("_REAL"))
						funName = blockname.substring(0, blockname.length()-5);
					else
						funName = blockname;
					if (i != 1)
						datapath += funName + "_" + DPConn[i - 1].startParam.toLowerCase();
				}
				if (i != 0 && i != 1)
					datapath += ", ";


			}
			datapath += ">";
			currentDPath.DPathID = dpathCount++;
			currentDPath.datapath_str = datapath;
			currentDPath.dpath_length = depth;
			int datapath_subindex = 1;
			for (DPath prev_dpath : DPaths) {
				if (prev_dpath.dpath_length == depth)
					datapath_subindex++;
			}
			currentDPath.dpath_subindex = datapath_subindex;
			prevDpathOutvar = dataPath[0].outvar.getExpression();
			if(isDpath)
				DPaths.add(currentDPath);
			else
				allPaths.add(currentDPath);
			DPath prevDPath = currentDPath;
			currentDPath = new DPath();
			currentDPath.endElem = prevDPath.endElem;
			DPathCounter++;
		}
	}

	/* Get instance of functionCalcLibrary from List------------------------------------------*/
	static functionCalcLibrary findCalcLibrary(String functionName, String outvar){
		for (functionCalcLibrary fcl : functionCalcLibs) {
			if(fcl.functionName.equals(functionName) && fcl.outvar.equals(outvar))
				return fcl;
		}
		return null;
	}
	//------------------------------------------ Get instance of functionCalcLibrary from List */

	static DPCLibrary findDPCLibrary(String functionName, String invar, String outvar) {
		for (DPCLibrary dpclib : DPCLibs) {
			if (dpclib.functionName.equals(functionName) && dpclib.inVar.equals(invar) && dpclib.outVar.equals(outvar)) {
				return dpclib;
			}
		}
		return null;
	}

	static DPCLibrary findDPCLibraryByName(String functionName) {
		for (DPCLibrary dpclib : DPCLibs) {
			if (dpclib.functionName.equals(functionName))
				return dpclib;
		}
		return null;
	}

	static void getDPCLibrary() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(CreateGUI.libraryPath.getText()));
			String s;
			String fName;
			String fType;
			String functionName;
			String[] invar = new String[10000];
			String outvar;
			DPCLibrary dpc;
			int invars_cnt = 0;
			while (true) {
				s = acceptNonComment(in);
				if (s == null)
					break;
				dpc = null;
				StringTokenizer tok = new StringTokenizer(s, " ,()");
				fType = tok.nextToken();
				fName = tok.nextToken();
				StringTokenizer tok2 = new StringTokenizer(fName, ";");
				functionName = tok2.nextToken();
				if (tok2.hasMoreTokens()) {
					outvar = tok2.nextToken();
				}
				invars_cnt = 0;
				while (tok.hasMoreTokens()) {
					invar[invars_cnt++] = tok.nextToken();
				}
				for (int i = 0; i < invars_cnt; i++) {
					dpc = new DPCLibrary();
					dpc.functionName = functionName;
					if (fType.equals("BOOL")) {
						dpc.boolOutput = true;
					} else {
						dpc.boolOutput = false;
					}
					s = acceptNonComment(in);
					tok = new StringTokenizer(s, ":");
					String inoutvars = tok.nextToken();
					String cond = tok.nextToken();
					tok2 = new StringTokenizer(inoutvars, " ()\t,");
					tok2.nextToken();
					dpc.inVar = tok2.nextToken();
					dpc.outVar = tok2.nextToken();
					tok2 = new StringTokenizer(cond, " \t");
					String condition = "";
					while (tok2.hasMoreTokens()) {
						condition += tok2.nextToken();
					}
					dpc.condition = condition;
					DPCLibs.add(dpc);
				}
			}
			s = s;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static FunctionVariable GetFunctionBlockLocalVariable(String name, long block_id) {
		for (FunctionVariable var : functionBlockLocalVars) {
			if (var.name.equals(name) && var.block_id == block_id && var.type == FunctionVariable.IN) {
				return var;
			}
		}
		FunctionVariable var = new FunctionVariable(FunctionVariable.IN, name, block_id);
		functionBlockLocalVars.add(var);
		return var;
	}

	public static FunctionVariable GetFunctionBlockPreVariable(String name, long block_id) {
		for (FunctionVariable var : functionBlockPreVars) {
			if (var.name.equals(name) && var.block_id == block_id && var.type == FunctionVariable.IN) {
				return var;
			}
		}
		FunctionVariable var = new FunctionVariable(FunctionVariable.PRE, name, block_id);
		functionBlockPreVars.add(var);
		return var;
	}

	static void LogicRecursion(LogicStatement l, long end) {
		// IBlock block = getElem(end).block;
		determineLogicAndRecurse(l, l.L1, end);
		determineLogicAndRecurse(l, l.L2, end);
		determineLogicAndRecurse(l, l.L3, end);
	}

	void drawOutvarsCheckbox() {
		JPanel leftPanel = CreateGUI.leftPanel;
		for (Element elem : outvars) {
			JCheckBox check = new JCheckBox(elem.outvar.getExpression(), false);
			outvarsCheckboxs.add(check);
			leftPanel.add(check);
		}
		CreateGUI.window.repaint();
		panel_draw.repaint();
	}

	void getBasicInfo() {
		panel_draw.repaint();

		indexOfTopModule = 0;
		IPOU POU = PLCProject.getPOUs().get(indexOfTopModule);
		FBD ld = (FBD) POU.getBody();

		console_println("Project Name : " + PLCProject.getProjectName());
		console_println("Project Type : " + POU.getBodyType());


		console_println("========INVARS========"); // In Variables
		for (IInVariable inVar : ld.getInVariables()) {
			inVar.setInitialLocalID(inVar.getLocalID());

			Element elem = new Element(Element.INVAR, inVar.getLocalID());
			elem.invar = inVar;

			elements.add(elem);
			inputVariables.add(inVar);

			console_println(inVar.getLocalID() + " " + inVar.getExpression());
		}

		console_println("========OUTVARS========"); // Out Variables
		for (IOutVariable outVar : ld.getOutVariables()) {
			outVar.setInitialLocalID(outVar.getLocalID());

			Element elem = new Element(Element.OUTVAR, outVar.getLocalID());
			for(Element el: elements){
				if(el.type == Element.INVAR)
					if(el.invar.getExpression().equals(outVar.getExpression())){
						String prevOut = outVar.getExpression();
						outVar.setExpression(prevOut+"_out");
					}
			}

			elem.outvar = outVar;

			elements.add(elem);
			outputVariables.add(outVar);

			console_println(outVar.getLocalID() + " " + outVar.getExpression());
		}

		console_println("========BLOCKS========"); // Blocks
		for (IBlock block : ld.getBlocks()) {
			block.setInitialLocalID(block.getLocalID());

			Element elem = new Element(Element.BLOCK, block.getLocalID());
			elem.block = block;

			elements.add(elem);
			blocks.add(block);
			console_println(block.getLocalID() + " " + block.getTypeName());
		}

		console_println("==============CONNECTIONS===================");
		for (IBlock block : blocks) {
			// 먼저 [Invar | Block] <======> [Block] 형식의 Connection을 추출한다.
			Element nextelem = getElementById(block.getLocalID());
			for (IInVariableInBlock inVar : block.getInVariables()) {
				for (IConnection conn : inVar.getConnectionPointIn().getConnections()) {
					Element prevelem = getElementById(conn.getRefLocalID());
					if (prevelem == null)
						continue;
					prevelem.nextElement = nextelem;
					nextelem.prevElement = prevelem;
					// nextelem은 BLOCK 형식이고, prevelem은 invar 또는 block이다.
					// 무엇인지 알 수 없으므로 여기서 결정한다.
					Connection newCon = new Connection(prevelem.LocalID, (prevelem.type == Element.BLOCK) ? (conn.getFormalParam())
							: (prevelem.invar.getExpression()), nextelem.LocalID, inVar.getFormalParameter());
					if (inVar.isNegated()) { // Negation 처리.
						newCon.negated = true;
						console_print(" ~ ");
					}
					connections.add(newCon); // 추출된 연결 정보들은 connections 에 들어간다.
					prevelem.FormalParam = inVar.getFormalParameter();

					if(prevelem.FormalParam.equals("S") || prevelem.FormalParam.equals("S1") || prevelem.FormalParam.equals("R") || prevelem.FormalParam.equals("R1")|| prevelem.FormalParam.equals("CLK")){
						defaultIter = 2;
						setIter = defaultIter;
					}
					if (prevelem.type == Element.BLOCK) {
						console_println(prevelem.LocalID + prevelem.block.getTypeName() + " <-> " + nextelem.LocalID + " "
								+ nextelem.block.getTypeName());

						/*-----------block의 output의 타입을 알아내어 hashset에 넣어둔다. block output value define할때 필요함 */
						Element blockOutVar = new Element(Element.OUTVAR, prevelem.block.getLocalID());
						String prevBlockName = prevelem.block.getTypeName();
						//StringTokenizer blockNameTok = new StringTokenizer(prevBlockName, "_");
						String conprevBlockName; //= blockNameTok.nextToken();
						if(prevBlockName.endsWith("_BOOL"))
							conprevBlockName = prevBlockName.substring(0, prevBlockName.length()-5);
						else if(prevBlockName.endsWith("_DINT"))
							conprevBlockName = prevBlockName.substring(0, prevBlockName.length()-5);
						else if(prevBlockName.endsWith("_REAL"))
							conprevBlockName = prevBlockName.substring(0, prevBlockName.length()-5);
						else
							conprevBlockName = prevBlockName;
						for(DPCLibrary dpclib : DPCLibs){
							if(dpclib.functionName.equals(prevBlockName) || dpclib.functionName.equals(conprevBlockName)){
								blockOutVar.value = dpclib.functionName+prevelem.block.getLocalID()+"_"+dpclib.outVar.toLowerCase();
								if(dpclib.boolOutput)
									blockOutVar.valueType = Element.BOOLEAN;
								else
									blockOutVar.valueType = Element.INTEGER;
								blockOutVars.add(blockOutVar);
								break;
							}
						}
						//-------------------------------------------------------------------------------------*/

					} else if (prevelem.type == Element.INVAR) {
						console_println(prevelem.LocalID + " " + prevelem.invar.getExpression() + " <-> " + nextelem.LocalID + " "
								+ nextelem.block.getTypeName());
					}
				}
			}

		}
		for (IOutVariable outVariable : outputVariables) {
			// [Block] <====> [Outvar] 연결을 추출한다.
			// [Invar] <====> [Outvar] 라는 connection은 무시.
			Element nextelem = getElementById(outVariable.getLocalID());
			for (IConnection conn : outVariable.getConnectionPointIn().getConnections()) {

				Element prevelem = getElementById(conn.getRefLocalID());
				if (prevelem == null)
					continue;
				// nextelem : outvar
				// prevelem : block
				IBlock prevblock = prevelem.block;
				if(prevblock != null){
					Connection newCon = new Connection(prevelem.LocalID, conn.getFormalParam(), nextelem.LocalID, outVariable.getExpression());
					if (outVariable.isNegated()) {
						newCon.negated = true;
						console_print(" ~ ");
					}
					connections.add(newCon);
					console_println("Block " + prevblock.getTypeName() + " : ");
					console_println(conn.getFormalParam() + " / " + outVariable.getExpression());

					console_println(prevelem.LocalID + " " + prevelem.block.getTypeName() + " <-> " + nextelem.LocalID + " "
							+ nextelem.outvar.getExpression());
					prevelem.nextElement = nextelem;
					nextelem.prevElement = prevelem;
				}
			}
		}

		for (Element elem : elements) {
			if (elem.type == elem.BLOCK)
				continue;
			boolean existInput = false;
			boolean existOutput = false;
			for (Connection conn : connections) {
				if (conn.start == elem.LocalID)
					existInput = true;
				if (conn.end == elem.LocalID)
					existOutput = true;
				if (existInput && existOutput)
					break;
			}
			// 각각의 Element에 대해, element의 input만 있으면 outvar,
			// output만 있으면 invar로 간주하여 invars와 outvars에 삽입한다.
			if (existInput && !existOutput)
				invars.add(elem);
			if (!existInput && existOutput)
				outvars.add(elem);
		}
		console_println("====================================================");
		console_println("=================LOAD COMPLETE======================");
		console_println("====================================================");
		console_println("");
		console_println("=== Detected input variables ===");
		for (Element elem : invars) {
			console_println(" (" + elem.LocalID + ") " + elem.invar.getExpression());
		}
		console_println("=== Detected output variables ===");
		for (Element elem : outvars) {
			console_println(" (" + elem.LocalID + ") " + elem.outvar.getExpression());
		}

		drawPicture(-1);
	}

	void initialize() {
		for (JCheckBox check : outvarsCheckboxs) {
			CreateGUI.leftPanel.remove(check);
		}
		String s = CreateGUI.font.getText();
		StringTokenizer tok = new StringTokenizer(s, ",");
		String fontname = tok.nextToken();
		String fontsize = tok.nextToken();
		fontsize = fontsize.trim();
		int fontAttr;
		fontAttr = Font.PLAIN;
		if (CreateGUI.bold.isSelected())
			fontAttr = Font.BOLD;
		Font font = new Font(fontname, fontAttr, Integer.parseInt(fontsize));
		panel_draw.g2d.setFont(font);
		CreateGUI.console_text = "";
		CreateGUI.console_flush();
		CreateGUI.console_println("Initializing...");
		colorcount = 0;
		file = null;
		PLCProject = null;
		indexOfTopModule = 0;
		outvarsCheckboxs.clear();
		elements.clear();
		inputVariables.clear();
		outputVariables.clear();
		blocks.clear();
		functionDPCs.clear();
		functionBlockLocalVars.clear();
		functionBlockPreVars.clear();
		connections.clear();
		invars.clear();
		DPCMacros.clear();
		outvars.clear();
		functionCalcLibs.clear();
		DPathCounter = 0;
		DPaths.clear();
		ICC_DPaths.clear();
		CCC_DPaths.clear();
		DPCLibs.clear();
		dpathCount = 0;
		prevDpathOutvar = "";
		testPath = "";
		rewriteDpcNoRecurse = false;
	}

	/**
	 * XML 파일을 불러오는 함수.
	 * 
	 * @param filePath
	 */
	void loadXML(String filePath) {
		initialize();

		try {
			CreateGUI.console_println("Opening writer...");
			
			writer = new BufferedWriter(new FileWriter(".\\output\\"+CreateGUI.LogFilePath.getText()));
			Thread.sleep(5000);
			file = new File(filePath);
			CreateGUI.console_println("Creating PLC Object...");
			PLCProject = (ProjectImpl) PLCModel.readFromXML(file);

			CreateGUI.console_println("Loading library...");
			CreateGUI.console_println(" DPC library");
			getDPCLibrary();
			CreateGUI.console_println(" Function calculation library");
			getFunctionCalculationLibrary();

			CreateGUI.console_println("Reading XML...");
			getBasicInfo(); // Invar, outvar, block 정보 및 연결 정보들을 추출한다.
			CreateGUI.console_println("Creating Checkbox...");
			drawOutvarsCheckbox();

			CreateGUI.console_println("Load success.");
			CreateGUI.xmlStatus.setText("Load success");
			CreateGUI.xmlStatus.setForeground(Color.blue);
			console_flush();
		} catch (Exception e) {
			CreateGUI.console_println("Load failed.");
			CreateGUI.xmlStatus.setText("Load failed");
			CreateGUI.xmlStatus.setForeground(Color.red);
			CreateGUI.console_println("======EXCEPTION======");
			CreateGUI.console_println(e.toString());
			CreateGUI.console_println("=====================");
			e.printStackTrace();
			console_flush();
			System.exit(-1);
		}
	}

	/**
	 * 외부에서 호출되는 함수.
	 * 여러개의 randomSuites 생성.
	 * 
	 * @author donghwan
	 * @param maxRun RT을 반복할 횟수, RT-suite의 개수
	 * @param maxSize 하나의 테스트 스위트 사이즈의 최대값
	 * @param testDoc 변수 정보 등이 담긴 test document
	 * @param coverageGoal 달성하고자 하는 커버리지 레벨 목표치
	 * @param includeAll 도움이 되는 테스트 케이스만 포함할지, 모든 테스트 케이스를 포함할지 선택
	 * 

	 */
	static void generateRandomTestSuites(int maxRun, int maxSize, String testDoc, float coverageGoal) {
		String pre = "";
		boolean includeAll = false;
		if (CreateGUI.BCTestCheck.isSelected()) {
			pre += "BC_";
		}
		else if (CreateGUI.ICCTestCheck.isSelected()) {
			pre += "ICC_";
		}
		else if (CreateGUI.CCCTestCheck.isSelected()) {
			pre += "CCC_";
		}
		else if (CreateGUI.RTestCheck.isSelected()) {
			pre = "RT_";
			includeAll = true;
			coverageGoal = 2; // Cannot be achieved!
		}

		float coverage;
		String log = "No.\tCov.\tSize\tMaxRun\r\n";

		for (int i = 0; i < maxRun; i++) {
			coverage = generateRandomTestSuite(maxSize, testDoc, coverageGoal, includeAll);
			log += i + "\t" + coverage + "\t" + testSuiteSize + "\t" + iterationCount + "\r\n";
		}

		try {
			BufferedWriter logFile = new BufferedWriter(new FileWriter("output\\"+ pre + "R-Suite_coverage_levels.txt"));
			logFile.write(log.trim());
			logFile.close();
			Runtime.getRuntime().exec("C:\\Windows\\System32\\notepad.exe output\\"+ pre + "R-Suite_coverage_levels.txt");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		testSuiteID = 1;
	}

	/**
	 * 내부적으로 testCase 인스턴스의 random() 함수를 이용.
	 * 하나의 random test suite을 생성.
	 * 
	 * @author donghwan
	 * @param maxSize 테스트 스위트 사이즈의 최대값 (test case 개수)
	 * @param testDoc 변수 정보 등이 담긴 test document
	 * @param coverageGoal 달성하고자 하는 커버리지 레벨 목표치
	 * @param includeAll 도움이 되는 테스트 케이스만 포함할지, 모든 테스트 케이스를 포함할지 선택
	 * @return coverage level
	 * 
	 */
	private static float generateRandomTestSuite(int maxSize, String testDoc, float coverageGoal, boolean includeAll) {
		DPaths.clear();
		ICC_DPaths.clear();
		CCC_DPaths.clear();
		DPCMacros.clear();
		functionDPCs.clear();
		functionBlockLocalVars.clear();
		functionBlockPreVars.clear();

		findDataPaths();
		sortDataPaths();
		calculateDPC();

		CreateGUI.window.repaint();
		CreateGUI.window.setVisible(true);

		ArrayList<ArrayList<Item>> testSuite = new ArrayList<ArrayList<Item>>();
		ArrayList<Item> testCase;

		// 테스트 케이스 열어서
		// 필요한 변수들에 대해서 랜덤 테스트 케이스 생성하고
		try {
			// 변수의 타입을 결정하기 위해서 testDoc.txt 파일을 열어봐야만 함.
			// 이 부분에 대한 별도의 함수가 있음.
			// loadTestFile() 함수를 참조해서, 추후에 필요없으면 정리하던가 해야 함.
			BufferedReader testFile = new BufferedReader(new FileReader(testDoc));

			testCase = new ArrayList<Item>();
			List<Item> testCCases = new ArrayList<Item>();

			int mode = -1;
			String thisLine = "";
			while ((thisLine = testFile.readLine()) != null) {
				if (thisLine.trim().length() == 0)
					continue;
				if (thisLine.startsWith("//"))
					continue;
				if (thisLine.startsWith("###")) {
					if (thisLine.contains("constants")) {
						mode = 1;
					} else if (thisLine.contains("inputs")) {
						mode = 2;
					} else if (thisLine.contains("outputs")) {
						mode = 3;
					} else if (thisLine.contains("number of test cases")) {
						mode = 4;
					} else if (thisLine.contains("test cases")) {
						mode = 5;
					} else if (thisLine.contains("cTypes")){
						mode = 6;
					}
				} else {
					switch (mode) {
					case 1:
						String[] cNames = thisLine.split(", ");
						for (int i = 0; i < cNames.length; i++)
							testCCases.add(i, new Item(cNames[i]));
						break;
					case 2: // inputs
						String[] inputNames = thisLine.split(", ");
						for (int i = 0; i < inputNames.length; i++)
							testCase.add(i, new Item(inputNames[i]));
						break;
					case 3:
						break;
					case 4:
						break;
					case 5: // test cases
						String[] testValues = thisLine.split("\t");
						if (testValues.length != testCase.size()) {
							System.err.println("ERROR: #input != #test-values");
							System.exit(-1);
						}
						for (int i = 0; i < testCase.size(); i++) {
							for (Element var : invars) {
								if (testCase.get(i).getName().equals(var.invar.getExpression())) {
									if (Character.isLetter(testValues[i].charAt(0))) {
										var.valueType = var.BOOLEAN;
										testCase.get(i).setType(0);
									} else if (testCase.get(i).getName().contains("CNT")) {
										var.valueType = var.INTEGER;
										testCase.get(i).setType(1);
									} else {
										var.valueType = var.REAL;
										testCase.get(i).setType(2);
									}

									// 실제로 random 값이 생성되는 부분
									testCase.get(i).random();

									if (testCase.get(i).getType() == 0) {
										var.value = (testCase.get(i).getValue() == 0) ? "false" : "true";
									} else {
										var.value = testCase.get(i).getValue() + "";
									}
								}
							}
						}
						break;
					case 6:
						String[] testCValues = thisLine.split("\t");
						if (testCValues.length != testCCases.size()) {
							System.err.println("ERROR: #input != #test-Cvalues");
							System.exit(-1);
						}
						for (int i = 0; i < testCCases.size(); i++) {
							for (Element var : invars) {
								if (testCCases.get(i).getName().equals(var.invar.getExpression())) {
									if (Character.isLetter(testCValues[i].charAt(0))) {
										var.valueType = Element.BOOLEAN;
										testCCases.get(i).setType(0);
									} else if (testCCases.get(i).getName().contains("CNT")) {
										var.valueType = Element.INTEGER;
										testCCases.get(i).setType(1);
									} else {
										var.valueType = Element.REAL;
										testCCases.get(i).setType(2);
									}

									if (testCCases.get(i).getType() == 0) {
										var.value = (testCCases.get(i).getValue() == 0) ? "false" : "true";
									} else {
										var.value = testCCases.get(i).getValue() + "";
									}
								}
							}
						}
						break;
					} // end of switch
				} // end of else
			} // end of while

			testSuite.add(testCase); // !! IMPORTANT

			testFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("randomTestCaseGeneration: FATAL ERROR, file not found");
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("randomTestCaseGeneration: FATAL ERROR, IOException");
			System.exit(-1);
		}

		// * Test Documentation Load Complete
		// * Type 정보를 바탕으로 ICC, CCC path 생성
		if (CreateGUI.ICCTestCheck.isSelected())
			createICCPath();
		if (CreateGUI.CCCTestCheck.isSelected())
			createCCCPath();

		List<DPath> dPath = new ArrayList<DPath>();
		if (CreateGUI.BCTestCheck.isSelected())
			dPath.addAll(DPaths);
		if (CreateGUI.ICCTestCheck.isSelected())
			dPath.addAll(ICC_DPaths);
		if (CreateGUI.CCCTestCheck.isSelected())
			dPath.addAll(CCC_DPaths);

		float coverage = 0, newCoverage = 0;
		coverage = assessCoverageLevel(testSuite, dPath);
		System.out.println("1th generation: " + coverage);

		int iteration = 2;
		while (coverage < coverageGoal && iteration <= maxSize) {
			// * 새로운 random testCase 생성
			testCase = new ArrayList<Item>();
			for (Item item : testSuite.get(0)) {
				testCase.add(new Item(item.getName(), item.getType()));
			}

			testSuite.add(testCase);

			// * 업데이트된 testSuite을 바탕으로 coverage 측정
			newCoverage = assessCoverageLevel(testSuite, dPath);
			System.out.println(iteration + "th generation: " + newCoverage);

			// * 지난 coverage 보다 나아진 경우에만 testSuite에 포함
			//if (!includeAll && newCoverage <= coverage)
			//	testSuite.remove(testCase);

			coverage = newCoverage;
			iteration++;
		}

		String writerLog = "";
		for (ArrayList<Item> tc : testSuite) {
			String result = "";
			String value = "";
			for (Item item : tc) {
				if (item.getType() == 0) {
					value = (item.getValue() == 0) ? "false" : "true";
				} else {
					value = item.getValue() + "";
				}
				result += "(" + item.getName() + " " + value + ")";
			}
			writerLog += result + "\n";
		}
		try {
			String pre = "";
			if (CreateGUI.BCTestCheck.isSelected()) {
				pre += "BC_";
			}
			if (CreateGUI.ICCTestCheck.isSelected()) {
				pre += "ICC_";
			}
			if (CreateGUI.CCCTestCheck.isSelected()) {
				pre += "CCC_";
			}
			if (CreateGUI.RTestCheck.isSelected())
				pre = "RT_";

			BufferedWriter writer = new BufferedWriter(new FileWriter("output\\"+ pre + "R-Suite_"
					+ String.format("%04d", testSuiteID++) + ".txt"));
			writer.write(writerLog.trim());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		CreateGUI.console_println("Random Test Generation ... done.");
		console_flush();
		testSuiteSize = testSuite.size();
		iterationCount = iteration-1;
		return coverage;
	}

	/**
	 * TODO
	 * 이 함수와 countSatisfiedAsserts 함수가 유사한 일을 하는데 중복으로 구현됨.
	 * 추후에 이 함수를 제거하고 countSatisfiedAsserts 만을 사용하도록 할 필요가 있음.
	 * 
	 * @author donghwan
	 * @param testSuite Item으로 이루어진 testSet
	 * @param dPath
	 * @return coverage level
	 * 
	 */
	private static float assessCoverageLevel(ArrayList<ArrayList<Item>> testSuite, List<DPath> dPath) {
		boolean[] isCovered = new boolean[dPath.size()];
		if (dPath.size() == 0) return 0; // case: no criteria selected
		for (ArrayList<Item> testCase : testSuite) {
			for (int j = 0; j < testCase.size(); j++) {
				for (Element var : invars) {
					if (testCase.get(j).getName().equals(var.invar.getExpression())) {
						if (testCase.get(j).getType() == 0) {
							var.value = (testCase.get(j).getValue() == 0) ? "false" : "true";
						} else {
							var.value = testCase.get(j).getValue() + "";
						}
					}
				}
			}
			for(Element var: invars) {
				if(var.value != null && var.value.trim().equals(Integer.MIN_VALUE+"")) {
					System.err.println("Input value missing!: " + var.invar.getExpression());
					System.exit(-1);
				}
			}
			for (int i = 0; i < dPath.size(); i++) {
				if (dPath.get(i).DPC.calculate() == 1.0) {
					isCovered[i] = true;
				}
			}
		}

		int counter = 0;
		for (int i = 0; i < isCovered.length; i++)
			if (isCovered[i])
				counter++;

		return (float) counter / (float) isCovered.length;
	}

	/**
	 * 주어진 testSuites를 이용해서 dPath 중 몇개를 만족하는지 알려주는 함수.
	 * 
	 * @author donghwan
	 * @param testSuite
	 * @param dPath
	 * @return number of satisfied assertions
	 * 
	 */
	public static int countSatisfiedAsserts(ArrayList<TestCase> testSuite, List<DPath> dPath) {
		boolean[] isCovered = new boolean[dPath.size()];
		boolean log = false;

		for(TestCase testCase : testSuite) {
			for(String input : testCase.inputs) {
				for(Element var : invars) {
					if(input.equals(var.invar.getExpression())) {
						var.value = testCase.getValue(input);
					}
				}
			}
			for (int i = 0; i < isCovered.length; i++) {
				if(isCovered[i] == false)
					if (dPath.get(i).DPC.calculate() == 1.0)
						isCovered[i] = true;
			}
		}

		int counter = 0;
		for (int i = 0; i < isCovered.length; i++)
			if (isCovered[i] == true) {
				counter++;
				if(log) System.out.println(i + ": " + dPath.get(i).datapath_str);
			}

		if(log) System.out.println("Total Number of satisfied dPaths: " + counter);

		return counter;
	}

	/**
	 * @author donghwan
	 */
	private static void dPathsClear() {
		DPaths.clear();
		ICC_DPaths.clear();
		CCC_DPaths.clear();
	}

	/**
	 * Each [test requirement] is executed by Yices,
	 * so the actual feasible maximum coverage is calculated.
	 * Execution results are saved as an external file.
	 * @author donghwan
	 */
	void assessFeasibleMaxCoverage() {
		dPathsClear();

		loadTestFile(testPath);

		findDataPaths();
		sortDataPaths();
		calculateDPC();

		String pre = null;
		List<DPath> dPaths = new ArrayList<DPath>();

		if (CreateGUI.BCTestCheck.isSelected() || BC) {
			pre = "BC_";

			for(DPath dpath: DPaths) dpath.dPathType = 0;
			dPaths.addAll(DPaths);
		} else if (CreateGUI.ICCTestCheck.isSelected() || ICC) {
			pre = "ICC_";

			for(DPath dpath: DPaths) dpath.dPathType = 0;
			createICCPath();
			for(DPath dpath: ICC_DPaths) dpath.dPathType = 1;

			dPaths.addAll(DPaths);
			dPaths.addAll(ICC_DPaths);
		} else if(CreateGUI.CCCTestCheck.isSelected() || CCC) {
			pre = "CCC_";

			for(DPath dpath: DPaths) dpath.dPathType = 0;
			createICCPath();
			for(DPath dpath: ICC_DPaths) dpath.dPathType = 1;
			createCCCPath();
			for(DPath dpath: CCC_DPaths) dpath.dPathType = 2;

			dPaths.addAll(DPaths);
			dPaths.addAll(ICC_DPaths);
			dPaths.addAll(CCC_DPaths);
		} else {
			System.err.println("GenerateTestCase(): Fatal error");
			System.exit(-1);
		}

		//		debug();


		long start, end;
		start = System.currentTimeMillis();

		int counter = 0, totalPaths = dPaths.size();
		String assessCoverageLog = "Not Satisfied dPaths among [0 to "+(dPaths.size()-1)+"]\n";

		for(int id=0; id<totalPaths; id++) {
			yicesHeader(pre + "test" + id + ".ys", true, true);
			try {
				yicesWriter.write(";; Test requirements for " + pre + "\r\n");
				yicesWriter.write("(assert+ " + dPaths.get(id).DPC.YicesString(0,true) + " 8)\t; id: " + id + "\r\n");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("ERROR: assessMaxCoverage()");
				System.exit(-1);
			}
			yicesFooter(pre + "test" + id + ".ys");

			//* yices 파일을 실행시켜서 결과값을 가져옴
			String[] result = yicesExecuter(pre + "test" + id + ".ys");

			boolean isSatisfied = true;
			for(int i=0; i<result.length; i++) {
				String thisLine = result[i];
				if(thisLine.equals("")) continue;
				if(thisLine.contains("unsatisfied assertion ids")) {
					assessCoverageLog += id + "\n";
					isSatisfied = false;
				}
			}

			// 하드디스크 용량을 위해 yices 파일 삭제
			yicesRemover(pre + "test" + id + ".ys");

			if(!isSatisfied)
				counter++;

		}

		float maximumCoverage = (totalPaths - counter) / (float) totalPaths;
		System.out.println("Maximum Coverage: "+ String.format("%.3f", maximumCoverage));

		end = System.currentTimeMillis();



		// 모든 연산이 끝난 뒤, 실험 결과를 외부 파일에 저장함

		try {
			BufferedWriter covLogFile = new BufferedWriter(new FileWriter("output\\"+model+"\\"+pre+"MaxCoverage.txt"));
			covLogFile.write(assessCoverageLog);
			covLogFile.write("Unsat counter: " + counter + "\n");
			covLogFile.write("Maximum Coverage: "+ String.format("%.3f", maximumCoverage)+"\n");
			covLogFile.write("Time elapsed: "+( end - start )/1000.0);
			covLogFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * 외북에서 호출하는 함수.
	 * 내부적으로 generateTestSuite 함수를 호출한다.
	 * 기본적으로 TR의 분할을 위해 존재한다.
	 * 
	 * @author donghwan
	 * @param maxrun 테스트 스위트 개수 (반복 횟수)
	 * @param parameter 전체 TR 분할 파라미터 (check STVR paper)
	 * @throws IOException 

	 */
	static void generateTestSuites(int maxrun, int parameter) throws IOException {
		
		// 초기화
		dPathsClear();
		loadTestFile(testPath);
		findDataPaths();
		sortDataPaths();
		calculateDPC();
		loadProgramInfoFile();
		
		// 테스트 케이스 생성 준비
		String pre = null;
		List<DPath> dPath = new ArrayList<DPath>();

		if (CreateGUI.BCTestCheck.isSelected() || BC) {
			pre = "BC_";

			for(DPath dpath: DPaths) dpath.dPathType = 0;
			dPath.addAll(DPaths);
		} else if (CreateGUI.ICCTestCheck.isSelected() || ICC) {
			pre = "ICC_";

			for(DPath dpath: DPaths) dpath.dPathType = 0;
			createICCPath();
			for(DPath dpath: ICC_DPaths) dpath.dPathType = 1;
			System.out.println("ICC path is added");
			dPath.addAll(DPaths);
			dPath.addAll(ICC_DPaths);
		} else if(CreateGUI.CCCTestCheck.isSelected() || CCC) {
			pre = "CCC_";

			for(DPath dpath: DPaths) dpath.dPathType = 0;
			createICCPath();
			for(DPath dpath: ICC_DPaths) dpath.dPathType = 1;
			createCCCPath();
			for(DPath dpath: CCC_DPaths) dpath.dPathType = 2;

			dPath.addAll(DPaths);
			dPath.addAll(CCC_DPaths);
		} else {
			System.out.println("GenerateTestCase(): Fatal error");
			System.exit(-1);
		}

		//		debug(); // FIXME

		// 테스트 케이스 생성 시작
		long start, end;
		start = System.currentTimeMillis();

		ArrayList<TestCase> testSet = null;
		int size = dPath.size();
		String solutionLog = "Part\tNo.\tCov.\n";
		for (int i = 0; i < maxrun; i++) {
			try {
				//				Collections.shuffle(dPath);
				iteration = i+1;
				testSet = new ArrayList<TestCase>();
				boolean isFirstYicesHeader = true;
				yicesHeaderList.clear();
				for(int partNo = 1; partNo <= parameter; partNo++) {
					int testSetSize = testSet.size(); // 현재 testSet의 크기를 기억했다가 그만큼만 중복을 체크한다. 즉, 새로 만들어지는 newTCs 안에서의 중복은 없다고 가정한다.
					System.out.println("Test set size: "+testSetSize);
					
					ArrayList<TestCase> newTCs = generateTestSuite(pre, dPath, dPath.subList(size*(partNo-1)/parameter, size*partNo/parameter), partNo, isFirstYicesHeader);
					// isFirstYicesHeader = false;
					for(TestCase newTC: newTCs) {
						// 중복 제거 (같은 testCase가 이미 testSet에 있으면 추가하지 않는다.)
						boolean isExist = false;
						for(TestCase tc: testSet.subList(0, testSetSize)) {
							if(newTC.originalTC.equals(tc.originalTC)) {
								isExist = true;
								break;
							}
						}
						if(!isExist) testSet.add(newTC);
					}

					solutionLog += partNo+"\t"+(iteration)+"\t"+String.format("%.3f", coverageLevel)+"\n";
				}

				// * solution.txt 파일 출력
//				String solutionStr = "";
//				for(TestCase tc: testSet)
//					solutionStr += tc.toString() + "\n";
				solutionWriter(testSet, pre + "SMT_based_testSuite_" + String.format("%03d", iteration)+".txt");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("GenerateTestCase(): FATAL ERR");
				System.exit(-1);
			}
		}

		end = System.currentTimeMillis();

		try {
			BufferedWriter solutionLogFile = new BufferedWriter(new FileWriter("output\\"+model+"\\"+pre+"solutionCoverageLevels.txt"));
			solutionLogFile.write(solutionLog);
			solutionLogFile.write("Time elapsed: "+( end - start )/1000.0);
			solutionLogFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	/**
	 * 실제로 Yices를 실행해서 SMT-based TestSuite을 만드는 함수.
	 * @author donghwan
	 * @throws IOException
	 * 
	 */
	static ArrayList<String> yicesHeaderList = new ArrayList<String>();
	private static ArrayList<TestCase> generateTestSuite(String pre, List<DPath> allDPath, List<DPath> dPaths, int partNo, boolean isFirstYicesHeader) throws IOException { // isFirstYicesHeader 없애기
		ArrayList<TestCase> testSuite = new ArrayList<TestCase>();
		ArrayList<DPath> assertions = new ArrayList<DPath>();
		ArrayList<Integer> unsatYicesIDs = new ArrayList<Integer>();
		ArrayList<Integer> unsatAssertIDs = new ArrayList<Integer>();
		boolean isSolution = false;
		boolean coveredAsserts[] = null;

		int assertID = 0;
		int totalAsserts = 0;
		int counter = 1;


		int beforeUnsatSize = 0;
		String log = "";

		// 매 반복시마다 하나의 test case 생성
		for(int tempIter = 0 ; tempIter < maxIter ; tempIter++){
			boolean isFirstYices = true, isSatisfiedAll = false;
			yicesHeaderList.clear();
			assertID = 0;
			totalAsserts = 0;
			assertions.clear();
			while(true) {
				boolean isOutVar = false;
				long start = System.currentTimeMillis();
				yicesHeader(pre + "test" + partNo +"-"+ counter + ".ys", isFirstYicesHeader, isFirstYices);
				if(isFirstYicesHeader && isFirstYices) { // isFirstYices로 바꾸기
					
					for(int tempSetIter = setIter; tempSetIter >=0; tempSetIter--) {
						/* Writing feedback in-out variables starts --------------------------------------------------------------*/

						for(int i = 0; i<inputs.length; i++){
							boolean isFeedback = false;
							for(Connection feedbackCon : feedbackConnections){
								if(feedbackCon.startParam.equals(inputs[i])){
									String definition = "";
									if(tempSetIter != 0) {
										yicesWriter.write("(define " + feedbackCon.startParam + "_t" + tempSetIter + "::");
										definition += "(define " + feedbackCon.startParam + "_t" + tempSetIter + "::";
									} else {
										yicesWriter.write("(define " + feedbackCon.startParam + "::");
										definition += "(define " + feedbackCon.startParam + "::";
									}
									boolean isBool= true;
											if(getElementById(feedbackCon.start).valueType == Element.BOOLEAN)
												isBool = true;
											else
												isBool = false;
									if(isBool){
										yicesWriter.write("bool " + feedbackCon.endParam + "_t" + (tempSetIter+1) + ")\r\n");
										definition += "bool " + feedbackCon.endParam + "_t" + (tempSetIter+1) + ")\r\n";
									}else {
										yicesWriter.write("int " + feedbackCon.endParam + "_t" + (tempSetIter+1) + ")\r\n");
										definition += "int " + feedbackCon.endParam + "_t" + (tempSetIter+1) + ")\r\n";
									}
									isFeedback = true;
									yicesHeaderList.add(definition);
								}
							}

							if(!isFeedback){
								String definition = "";
								if(tempSetIter != 0) {
									yicesWriter.write("(define " + inputs[i] + "_t" + tempSetIter + "::");
									definition += "(define " + inputs[i] + "_t" + tempSetIter + "::";
								} else {
									yicesWriter.write("(define " + inputs[i] + "::");
									definition += "(define " + inputs[i] + "::";
								} 
								
								if(Character.isLetter(itypes[i].charAt(0))){
									yicesWriter.write("bool " //+ itypes[i] 
											+ ")\r\n");
									definition += "bool )\r\n";
								} else{
									StringTokenizer range = new StringTokenizer(itypes[i], "-");
									String from = range.nextToken();
									String to = null;
									if (range.hasMoreTokens()) {
										to = range.nextToken();
									}
									if (to == null){
										yicesWriter.write("int " //+ itypes[i] 
												+ ")\r\n");
										definition += "int )\r\n";
									}
									else {
										yicesWriter.write("(subrange "+ from + " " + to + "))\r\n");
										definition += "(subrange "+ from + " " + to + "))\r\n";
									}
								}
								yicesHeaderList.add(definition);
							}
						}
						// --------------------------------------------------------------- Writing feedback in-out variables ends */

						/* Writing function calculation definitions starts ------------------------------------------------------*/
						HashMap<String, String> outVarDefs = new HashMap<String, String>();
						for(double i = longestPathSize - 1; i >= 0 ; i=i-0.5)
							for(String key : oneDepthFunctionCalcs.keySet()){
								LogicStatement ls = oneDepthFunctionCalcs.get(key);
								Element lastBlockElem = getElementById(ls.blockId); 
								for (Connection con : connections){
									String outVarDef = ""; String id="";
									Element outvarElem = getElementById(con.end);
									if(getElementById(con.start) == lastBlockElem && ls.dpcl.outVar.equals(con.startParam)){
										id = String.valueOf(con.start)+con.startParam;

										if(outvarElem.type ==Element.OUTVAR){
											if(tempSetIter != 0)
												outVarDef += "(define " + outvarElem.outvar.getExpression() + "_t" + tempSetIter + "::";
											else
												outVarDef += "(define " + outvarElem.outvar.getExpression() + "::";
										}else{
											if(tempSetIter != 0){
												outVarDef += "(define "+ ls.dpcl.functionName + ls.blockId + "_" + ls.dpcl.outVar.toLowerCase() + "_t" + tempSetIter + "::";
											}else
												outVarDef += "(define "+ ls.dpcl.functionName + ls.blockId + "_" + ls.dpcl.outVar.toLowerCase() + "::";
										}
										if(ls.dpcl.boolOutput){
											String blockName = getElementById(ls.blockId).block.getTypeName();
											if(blockName.equals("TON") || blockName.equals("TOF") || blockName.equals("TP") || blockName.equals("CTU")||blockName.equals("CTD")||blockName.equals("CTUD"))
												outVarDef += "bool "+ls.YicesString(tempSetIter,true)+")\r\n";
											else
												outVarDef += "bool "+ls.YicesString(tempSetIter,false)+")\r\n";
										}else{
											String blockName = getElementById(ls.blockId).block.getTypeName();
											if(blockName.equals("TON") || blockName.equals("TOF") || blockName.equals("TP")|| blockName.equals("CTU")||blockName.equals("CTD")||blockName.equals("CTUD"))
												if(con.startParam.equals("ET"))
													outVarDef += "int "+ls.YicesString(tempSetIter,true)+")\r\n";
												else
													outVarDef += "bool "+ls.YicesString(tempSetIter,false)+")\r\n";
											else{
												// outVarDef += "int "+ls.YicesString(tempSetIter,false)+")\r\n";
												String str = "";
												str = ls.YicesString(tempSetIter,false);
												if(blockName.contains("SEL")) {
													if(str.contains("true")||str.contains("false"))
														outVarDef += "bool ";
													else
														outVarDef += "int ";
												}else {
													outVarDef += "int ";
												}
												outVarDef += str + ")\r\n";
											}
										}
									}
									if(id != "" && ls.blockOrder == i){
										if(!outVarDefs.containsValue(outVarDef)){
											yicesWriter.write(outVarDef);
											yicesHeaderList.add(outVarDef);
											outVarDefs.put(id,outVarDef);
										}
									}
								}
							}
						// -------------------------------------------------------- Writing function calculation definitions ends */

						yicesWriter.write(write);
						yicesHeaderList.add(write);
						yicesWriter.write("\r\n");
					}
					feedbackConnections.clear();
					
					yicesWriter.write("\r\n");
					yicesWriter.write(";; DPCs\r\n");

					yicesHeaderList.add("\r\n");
					yicesHeaderList.add(";; DPCs\r\n");
					
					for (DPCMacro m : DPCMacros) {
						yicesWriter.write("(define "+m.macroname+"::bool "+m.DPC+")\r\n");
						yicesHeaderList.add("(define "+m.macroname+"::bool "+m.DPC+")\r\n");
						//console_println(m.macroname + " : " + m.DPC);
					}
					yicesWriter.write("\r\n");
					yicesHeaderList.add("\r\n");
					
					for (DPath path : allDPath) {
						if (path.dPathType == 0) {
							yicesWriter.write("(define p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType + "::bool " + path.dpc_str + ")\r\n");
							yicesHeaderList.add("(define p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType + "::bool " + path.dpc_str + ")\r\n");
						} else if (path.dPathType == 1) {
							yicesWriter.write("(define p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType +"_" + path.identifier + "::bool " + path.dpc_str + ")\r\n");
							yicesHeaderList.add("(define p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType +"_" + path.identifier + "::bool " + path.dpc_str + ")\r\n");
						} else {
							yicesWriter.write("(define p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType +"_" + path.identifier + "::bool " + path.dpc_str + ")\r\n");
							yicesHeaderList.add("(define p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType +"_" + path.identifier + "::bool " + path.dpc_str + ")\r\n");
						//console_println("p" + path.dpath_length + "_" + path.dpath_subindex + " [" + path.DPathID + "] : " + path.dpc_str);
						}
					}
					
				} else {
					for(String definition: yicesHeaderList) {
						yicesWriter.write(definition);
					}
					yicesWriter.write("\r\n");
				}
				
				yicesWriter.write("\r\n");
				yicesWriter.write(";; Test requirements for " + pre + "\r\n");
				//yicesHeaderList.add("\r\n");
				//yicesHeaderList.add(";; Test requirements for " + pre + "\r\n");
				System.out.println("Rule 2 : " + pre);
				/* Writing DPCs and assert+ Starts --------------------------------------------------------------------------------------- */
				
				
				int size = dPaths.size();
				
				if(isFirstYices) {
					// * First run
					isFirstYices = false;	
					for (DPath path : dPaths/*.subList(size*(partNo-1), size*partNo)*/){
						assertions.add(path); assertID++;
						if(path.dPathType == 0)
							yicesWriter.write("(assert+ p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType +" 8)\r\n");
						else if(path.dPathType == 1)
							yicesWriter.write("(assert+ p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType + "_" + path.identifier + " 4)\r\n");
						else if(path.dPathType == 2)
							yicesWriter.write("(assert+ p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType + "_" + path.identifier + " 2)\r\n");
						else {
							System.err.println("ERROR: generateTestSuite(), weigth!");
							System.exit(-1);
						}
					}
					yicesWriter.write("\r\n");

					// ------------------------------------------------------------------------------------------Writing DPCs and assert+ Ends */

					yicesFooter(pre + "test" + partNo+"-"+ counter + ".ys");
					long end = System.currentTimeMillis();
					System.err.println("** yicesWriter: Time elapsed: " + ( end - start )/1000.0 + " (sec)");

					// * assert에 대한 covered 정보를 기록할 수 있도록 초기화
					totalAsserts = assertID;
					assert(totalAsserts == dPaths/*.subList(size*(partNo-1), size*partNo)*/.size());
					coveredAsserts = new boolean[totalAsserts+1];
					for(int i=1; i<=totalAsserts; i++) coveredAsserts[i] = false;

				} else {
					// * Second or more run
					for(int i=1; i<=totalAsserts; i++) {
						if(!coveredAsserts[i]) {
							DPath path = assertions.get(i-1);
							if(path.dPathType == 0)
								yicesWriter.write("(assert+ p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType + " 8)\r\n");
							else if(path.dPathType == 1)
								yicesWriter.write("(assert+ p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType + "_" + path.identifier + " 4)\r\n");
							else if(path.dPathType == 2)
								yicesWriter.write("(assert+ p" + path.dpath_length + "_" + path.dpath_subindex + "_" + path.dPathType + "_" + path.identifier + " 2)\r\n");
							else {
								System.err.println("ERROR: generateTestSuite(), weigth!");
								System.exit(-1);
							}
						}
					}
					yicesFooter(pre + "test" + partNo+"-"+ counter + ".ys");
				}

				// 매 yices 실행 전에 초기화
				unsatYicesIDs.clear();
				unsatAssertIDs.clear();
				isSolution = false;
				String thisLine = "";

				//* yices 파일을 실행시켜서 결과값을 가져옴
				String[] result = yicesExecuter(pre + "test" + partNo+"-"+ counter + ".ys");

				// 하드디스크 용량을 위해 yices 파일 삭제
				// 디버그를 위해서 yices 파일이 필요한 경우 아래 줄을 주석처리 하면 됨
//							yicesRemover(pre + "test" + partNo+"-"+ counter + ".ys");

				// Test Case를 읽어서 어떤 assertion이 만족되었는지 체크 
				log += "Round " + counter + "\r\n";
				log += "========================\r\n";
				String testCase = "";
				for(int i=0; i<result.length; i++) {
					thisLine = result[i];
					if(thisLine.contains("unknown")) {
						log += "unkonwn";
						for(int j = 1; j<=totalAsserts; j++)
							unsatYicesIDs.add(j);
						break;
					}
					if(thisLine.equals("")) continue;
					if(thisLine.contains("unsatisfied assertion ids")) {
						StringTokenizer tok = new StringTokenizer(thisLine, " ");
						while(tok.hasMoreTokens()) {
							String token = tok.nextToken();
							if(!token.matches("[1-9].*")) continue;
							int unsatYicesID = Integer.parseInt(token);
							unsatYicesIDs.add(unsatYicesID);

							int unsatAssertID = getUnsatAssertIDfromYicesID(coveredAsserts, unsatYicesID);
							unsatAssertIDs.add(unsatAssertID);
						}
						log += unsatYicesIDs.size() + " unsatisfied / " + totalAsserts + " assertions\r\n";
					} else if(thisLine.contains("(=")) {
						if(!isSolution) {
							isSolution = true;
						}
						log += thisLine + "\r\n";
						testCase += thisLine;
					}
				}
				TestCase tc = new TestCase(testCase);
				tc.partNo = partNo;
				tc.counter = counter;
				testSuite.add(tc);

				for(int i=1; i<=totalAsserts; i++)
					if(!unsatAssertIDs.contains(i))
						coveredAsserts[i] = true;

				log += "*[Not covered on this turn: ";
				for(Integer unsatAssertID: unsatAssertIDs) {
					log += unsatAssertID + " ";
				}
				log += "]\r\n========================\r\n\r\n";

				counter++;
				numOfEachTC[partNo][counter-2] = setIter; 

				// * while문 종료 조건
				
				if (unsatYicesIDs.size() == 0) {
					isSatisfiedAll=true;
					log += "STOP : No unsatisfied assertions\r\n\r\n";
					break;
				} else if (!isSolution || beforeUnsatSize == unsatYicesIDs.size()) {
					log += "STOP : No more satisfiable assertions\r\n\r\n";
					//testSuite.remove(tc);
					break;
				}
				beforeUnsatSize = unsatYicesIDs.size();
			}
			setIter++;
			if(isSatisfiedAll)
				break;
		} // * while문의 끝: 매 반복시 하나의 testCase 생성
		
		//Reset the value of setIter
		setIter = defaultIter;
		//* coverage level update
		//int satisfiedAsserts = countSatisfiedAsserts(testSuite, dPaths);
		coverageLevel = (totalAsserts-unsatYicesIDs.size()) / (float) totalAsserts;
		// * yices_log.txt 파일 출력		
		String iterationName = String.format("%03d", iteration) + "-" + partNo + ".txt";
		yicesLogWriter(log, totalAsserts-unsatYicesIDs.size(), totalAsserts, pre + "yices_log_" + iterationName);

		//* unsatID 만으로 원래 dPathID를 알 수 있도록 아래 코드 추가
		System.out.println("\nUnsatAssertID\tOriginalDPathID");
		for(Integer unsatAssertID: unsatAssertIDs) {
			System.out.println(unsatAssertID + "\t" + dPaths.get((unsatAssertID-1)).DPathID);
		}

		return testSuite;
	}


	/**
	 * yices 파일 시작부분에 공통적으로 들어가는 내용에 대한 출력
	 * @author donghwan
	 * @param fileName yices 파일 이름
	 */
	public static String inputs[];
	public static String itypes[];
	public static String constants[];
	public static String cTypes[];
	public static String outputs[];
	public static String otypes[];
	public static String inouts[];
	public static String scanCycle;
	public static int maxDelay = 0;

	private static void loadProgramInfoFile() throws IOException {
		BufferedReader testFile = new BufferedReader(new FileReader(testPath));
		String thisLine = "";

		while ((thisLine = testFile.readLine()) != null) {
			if(thisLine.startsWith("### inputs")) {
				thisLine = testFile.readLine();
				inputs = thisLine.split(", ");
			} else if(thisLine.startsWith("### test")) {
				thisLine = testFile.readLine();
				itypes = thisLine.split("\t");
			} else if(thisLine.startsWith("### scan")){
				thisLine = testFile.readLine();
				scanCycle = thisLine;
			} else if(thisLine.startsWith("### constants")){
				thisLine = testFile.readLine();
				constants = thisLine.split(", ");
			} else if(thisLine.startsWith("### cTypes")){
				thisLine = testFile.readLine();
				cTypes = thisLine.split("\t");
			} else if(thisLine.startsWith("### outputs")){
				thisLine = testFile.readLine();
				outputs = thisLine.split(", ");
			} else if(thisLine.startsWith("### oTypes")){
				thisLine = testFile.readLine();
				otypes = thisLine.split("\t");
			}
		}
		testFile.close();
		int inoutArrayNum=0;
		inouts = new String[inputs.length];
		for(int i = 0; i < inputs.length; i++)
			for(int j = 0 ; j < outputs.length; j++)
				if(inputs[i].equals(outputs[j])){
					inouts[inoutArrayNum]= inputs[i];
					inoutArrayNum++;
					outputs[j] = outputs[j]+"_out";
				}
	}
	private static void yicesHeader(String fileName, boolean yices1, boolean yices2) {
		try {
			yicesWriter = new BufferedWriter(new FileWriter("output\\"+model+"\\"+fileName));
			String write = ";; Environment setting\r\n" + "(set-evidence! true)\r\n" + "(set-verbosity! 1)\r\n";
			if (yices1 && yices2) {
			
			write += ";; Rule 1-1. Define constant and variables\r\n";
			//			System.out.println("Rule 1-1");

			if(!constants[0].equals(""))
				for(int i = 0 ; i < constants.length; i++){
					for(IInVariable inVariable : inputVariables){
						if(inVariable.getExpression().equals(constants[i])){
							for(Connection c : connections){
								if(c.start == inVariable.getLocalID()){
									if(c.endParam.equals("PT")){
										int delay = Integer.parseInt(cTypes[i]);
										int TONblockIter = delay / Integer.parseInt(scanCycle);
										if(setIter<TONblockIter)
											setIter = TONblockIter;
									}
									else if(c.endParam.equals("PV")){
										if(setIter<Math.abs(Integer.parseInt(cTypes[i])))
											setIter = Math.abs(Integer.parseInt(cTypes[i]));
									}
								}
							}
						}
					}
				}
			

			/* Writing scan cyle and constants Starts----------------------------- */
			write += ";; constant variables\r\n";
			write += "(define SCAN_TIME::int "+scanCycle+")\r\n";
			if(!constants[0].equals(""))
				for(int i=0; i<constants.length; i++) {
					write += "(define "+constants[i].trim()+"::";
					if(Character.isLetter(cTypes[i].charAt(0)))
						write += "bool " + cTypes[i] + ")\r\n";
					else
						write += "int " + cTypes[i] + ")\r\n";
				}
			write += "\r\n";
			// ---------------------------- Writing scan cycle and constants Ends */

			
			/*feedbackConnection starts--------------------------------------------*/
			for(int i = 0 ; i<inputs.length; i++)
				for(int j = 0; j<outputs.length; j++)
					if ((inputs[i]+"_out").equals(outputs[j])){
						Long preID=0L,nextID=0L;
						String preExpr="", nextExpr="";
						for(Element elem : elements){
							if(elem.type == Element.OUTVAR && elem.outvar.getExpression().equals(outputs[j])){
								nextID = elem.LocalID;
								nextExpr = elem.outvar.getExpression();
							}
							if(elem.type == Element.INVAR && elem.invar.getExpression().equals(inputs[i])){
								preID = elem.LocalID;
								preExpr = elem.invar.getExpression();
							}
						}
						Connection newCon = new Connection(preID,preExpr,nextID,nextExpr);
						feedbackConnections.add(newCon);
					}
			// --------------------------------------------feedbackConnection ends */

			for (Connection con : connections) {
				Element conIn = getElementById(con.start);
				Element conOut = getElementById(con.end);
				if (conIn.type == Element.INVAR) {
					//System.out.println("--------- connection ---------");
					//System.out.println(conIn.invar.getExpression());
					if (conOut.type == Element.BLOCK)
						if (conOut.block.getTypeName().equals("R_TRIG"))
							write += "(define " + conIn.invar.getExpression() + "_t" + (setIter+2) + "::bool false)\r\n";
							//System.out.println(conOut.block.getTypeName());
				}
				else if (conIn.type == Element.BLOCK) {
					//System.out.println("--------- connection ---------");
					//System.out.println(conIn.block.getTypeName());
					if (conOut.type == Element.OUTVAR) {
						if (conIn.block.getTypeName().equals("TON") || conIn.block.equals("TOF") || conIn.block.equals("TP") || 
							conIn.block.getTypeName().equals("CTU") || conIn.block.getTypeName().equals("CTD") || conIn.block.getTypeName().equals("CTUD")) {
							for (IOutVariable outVariable : outputVariables) {
								if (outVariable.getExpression().contains(conOut.outvar.getExpression())) {
									for (IConnection icon : outVariable.getConnectionPointIn().getConnections()) {
										if (icon.getFormalParam().equals("ET") || icon.getFormalParam().equals("CV"))
											write += "(define " + conOut.outvar.getExpression() + "_t" + (setIter+2) + "::int 0)\r\n";
									}
									break;
								}
							}
							//write += "(define " + conOut.outvar.getExpression() + "_t" + (setIter+2) + "::int 0)\r\n";
						}
						if (conIn.block.getTypeName().equals("SR") || conIn.block.getTypeName().equals("RS"))
							write += "(define " + conOut.outvar.getExpression() + "_t" + (setIter+2) + "::bool false)\r\n";
					}
				}
			}
			write += "\r\n";
			
			/* Writing inputs with subranges Starts ----------------------------- */
			for(int i=0; i<inputs.length; i++) {
				if(setIter != 0)
					write += "(define "+inputs[i].trim()+"_t"+(setIter+1)+"::";
				else
					write += "(define "+inputs[i].trim()+"::";
				if(Character.isLetter(itypes[i].charAt(0)))
					write += "bool " //+ itypes[i] 
					+ ")\r\n";
				else{
					StringTokenizer range = new StringTokenizer(itypes[i], "-");
					String from = range.nextToken();
					String to = null;
					if (range.hasMoreTokens()) {
						to = range.nextToken();
					}
					if(to == null)
						write += "int " //+ itypes[i] 
						+ ")\r\n";
					else{
						write += "(subrange "+ from + " " + to + "))\r\n"; 
					}
				}
			}
			// -------------------------------- Writing inputs with subranges Ends */

			/* Writing block's output variables Starts ---------------------------*/


			HashMap<String, String> outVarDefs = new HashMap<String, String>();
			for(double i = longestPathSize - 1 ; i >= 0 ; i=i-0.5)
				for(String key : oneDepthFunctionCalcs.keySet()){
					LogicStatement ls = oneDepthFunctionCalcs.get(key);
					Element lastBlockElem = getElementById(ls.blockId); 
					for (Connection con : connections){
						String outVarDef = ""; String id="";
						Element outvarElem = getElementById(con.end);
						if(getElementById(con.start) == lastBlockElem && ls.dpcl.outVar.equals(con.startParam)){
							id = String.valueOf(con.start)+con.startParam;
							if(outvarElem.type ==Element.OUTVAR){
								if(setIter != 0)
									outVarDef += "(define " + outvarElem.outvar.getExpression() + "_t" + (setIter+1) + "::";
								else
									outVarDef += "(define " + outvarElem.outvar.getExpression() + "::";
								if(ls.dpcl.boolOutput){
									if(setIter != 0)
										outVarDef += "bool "+ls.YicesString(setIter+1,false)+")\r\n";
									else
										outVarDef += "bool "+ls.YicesString()+")\r\n";
								}else{
									/*
									if(setIter != 0)
										outVarDef += "int "+ls.YicesString(setIter+1,false)+")\r\n";
									else
										outVarDef += "int "+ ls.YicesString()+")\r\n";
									*/
									String str = "";
									if(setIter != 0)
										str = ls.YicesString(setIter+1,false);
									else
										str = ls.YicesString();
									if(lastBlockElem.block.getTypeName().contains("SEL")) {
										if(str.contains("true")||str.contains("false"))
											outVarDef += "bool ";
										else
											outVarDef += "int ";
									}else {
										outVarDef += "int ";
									}
									outVarDef += str + ")\r\n";
								}
							}else{
								if(setIter != 0)
									outVarDef += "(define "+ ls.dpcl.functionName + ls.blockId + "_" + ls.dpcl.outVar.toLowerCase() + "_t" + (setIter+1) + "::";
								else
									outVarDef += "(define "+ ls.dpcl.functionName + ls.blockId + "_" + ls.dpcl.outVar.toLowerCase() + "::";
								if(ls.dpcl.boolOutput){
									if(setIter != 0)
										outVarDef += "bool "+ls.YicesString(setIter+1,false)+")\r\n";
									else
										outVarDef += "bool "+ls.YicesString()+")\r\n";
								}else{
									/*
									if(setIter != 0)
										outVarDef += "int "+ls.YicesString(setIter+1,false)+")\r\n";
									else
										outVarDef += "int "+ls.YicesString()+")\r\n";
									*/
									String str = "";
									if(setIter != 0)
										str = ls.YicesString(setIter+1,false);
									else
										str = ls.YicesString();
									if(ls.dpcl.functionName.contains("SEL")) {
										if(str.contains("true")||str.contains("false"))
											outVarDef += "bool ";
										else
											outVarDef += "int ";
									}else {
										outVarDef += "int ";
									}
									outVarDef += str + ")\r\n";
								}
							}
						}
						if(id != "" && ls.blockOrder == i){
							if(!outVarDefs.containsValue(outVarDef)){
								write += outVarDef;
								outVarDefs.put(id,outVarDef);
							}
						}
					}
				}
			
			// yicesWriter.write(";; Rule 1-2. Define DPCs for d-paths\r\n\r\n");
			//			System.out.println("Rule 1-2");
			
			// yicesWriter.write(";; Rule 2. Assert test requirements\r\n");
			
			}
			yicesHeaderList.add(write);
			yicesWriter.write(write + "\r\n");
			// yicesHeaderList.add(";; Rule 1-2. Define DPCs for d-paths\r\n\r\n");
			// yicesHeaderList.add(";; Rule 2. Assert test requirements\r\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("FATAL ERROR: yicesHeader()");
			System.exit(-1);
		}
	}

	/**
	 * yices 파일 아래부분에 공통적으로 들어가는 내용에 대한 출력
	 * @author donghwan
	 * @param fileName .ys file name
	 * 
	 */
	private static void yicesFooter(String fileName) {
		try {
			yicesWriter.write("\r\n");
			yicesWriter.write("\r\n;; Rule 3. Execute max-sat\r\n(max-sat)");
			yicesWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("FATAL ERROR: yicesFooter()");
			System.exit(-1);
		}
		System.err.println("Generating " + fileName + " ... Completed.");
	}

	/**
	 * executeCommand() 함수를 이용하여 yices 실행
	 * @author donghwan
	 * @param fileName yices 파일 이름
	 * 
	 */
	private static String[] yicesExecuter(String fileName) {
		//		long start = System.currentTimeMillis();
		ArrayList<String> commandList = new ArrayList<String>();
		commandList.add(".\\yices\\bin\\yices.exe");
		commandList.add("output\\"+model+"\\"+fileName);

		String resultStr = executeCommand(commandList, ".");
		String[] result = resultStr.split("\n");
		//		long end = System.currentTimeMillis();
		//		System.out.println("** yicesExecuter: Time elapsed: " + ( end - start )/1000.0 + " (sec)");
		return result;
	}

	/**
	 * @author donghwan
	 * @param fileName target removing file
	 */
	private static void yicesRemover(String fileName) {
		File file = new File("output\\"+model+"\\"+fileName);
		if(!file.delete()) {
			System.err.println("yicesRemover(): ERROR!");
			System.exit(-1);
		}
	}

	/**
	 * Debug purpose only.
	 * @author donghwan
	 *
	 */
	private static void debug() {
		System.out.println("BC\tICC\tCCC");
		System.out.printf("%d\t%d\t%d", DPaths.size(), DPaths.size()+ICC_DPaths.size(), DPaths.size()+ICC_DPaths.size()+CCC_DPaths.size());
		System.exit(0);
	}

	/**
	 * @author donghwan
	 * @param coveredAsserts 모든 어설트에 대해서 covered 정보를 갖는 비트벡터
	 * @param unsatYicesID yices에서 unsat 되었다고 알려준 ID
	 * @return 실제 unsat인 어설트의 ID
	 */
	private static int getUnsatAssertIDfromYicesID(boolean[] coveredAsserts, Integer unsatYicesID) {
		int index = 0;
		int length = coveredAsserts.length;
		for(int i=1; i<length; i++) {
			if(!coveredAsserts[i]) {
				index++;
				if(index == unsatYicesID) return i;
			}
		}

		return -1;
	}

	/**
	 * @author donghwan
	 * @param log 실행 라운드 및 달성된 assertion 관련 내용
	 * @param solutionStr yices 실행 결과로 나오는 솔루션
	 * @param sat 만족된 assert 개수
	 * @param max 전체 assert 개수
	 * @param fileName yices 로그 파일 이름
	 */
	private static void yicesLogWriter(String log,int sat, int max, String fileName) {
		double cov = (sat / (double) max) * 100.0;
		try {
			BufferedWriter yicesLog = new BufferedWriter(new FileWriter("output\\"+model+"\\"+fileName));
			yicesLog.write(log);
			yicesLog.write("\nTotal " + sat + " satisfied / " + max + " assertions : coverage " + String.format("%.3f", cov) + "%");
			yicesLog.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("yicesLogWriter(): FATAL ERR");
			System.exit(-1);
		}
		System.out.println(log);
	}

	/**
	 * @author donghwan + Jiyoung
	 * @param solutionStr 솔루션 로그 파일에 적을 내용
	 * @param fileName  솔루션 로그 파일 이름 (fileName.txt으로 저장)
	 * @param st (variable0 value0)(variable1 value1)...
	 * @param tc (variable0 value0
	 */
	private static void solutionWriter(ArrayList<TestCase> testSuite, String fileName) {
		// * solution.txt 파일 출력
		String[] sortedInput = new String[inputs.length];
		for(int i = 0; i<inputs.length; i++){
			sortedInput[i] = inputs[i];
		}
		Arrays.sort(sortedInput, String.CASE_INSENSITIVE_ORDER);
		String[][] tempArray = new String[3][inputs.length]; 
		for(int i = 0; i<inputs.length; i++){
			tempArray[1][i] =sortedInput[inputs.length-i-1];
			for(int j = 0; j<inputs.length; j++){
				if(inputs[j].equals(tempArray[1][i])){
					tempArray[0][i]= ""+j;
					tempArray[2][i] = itypes[Integer.parseInt(tempArray[0][i])];
					break;
				}
			}
		}
		
		
		try {
			/*constant writing start----------------------------------------------------------------------*/
			BufferedWriter solution = new BufferedWriter(new FileWriter("output\\"+model+"\\"+fileName));
			solution.write("### constants\r\n");
			//if(!constants[0].equals(""))
				for (int i = 0 ; i< constants.length; i++)
					solution.write(constants[i]+"\t"+cTypes[i]+"\r\n");
			solution.write("\r\n");
			//----------------------------------------------------------------------constant writing ends */
			
			/*input variable writing start---------------------------------------------------------------*/
			solution.write("### inputs\r\n");
			for (int i = 0; i< inputs.length-1; i++)
				solution.write(tempArray[1][i]+", ");
			solution.write(tempArray[1][inputs.length-1]+"\r\n\r\n");
			//-----------------------------------------------------------------input variable writing end*/
			
			/*output variable writing start----------------------------------------------------------------------*/
			solution.write("### outputs\r\n");
			String[] tempOutput = new String[outputs.length];
			for (int i =0; i< outputs.length; i++){
				tempOutput[i] = outputs[i];
				for(int j =0; j<inputs.length; j++)
					if(outputs[i].length()>4 && outputs[i].substring(0, outputs[i].length()-4).equals(inputs[j])){
						tempOutput[i] = outputs[i].substring(0,outputs[i].length()-4);
					}
			}
			for (int i = 0; i< tempOutput.length-1; i++)
				solution.write(tempOutput[i]+", ");
			solution.write(tempOutput[tempOutput.length-1]+"\r\n\r\n");
			//-------------------------------------------------------------------------output variable writing end*/
			
			/*test sequence writing start----------------------------------------------------*/
			solution.write("### number of test sequences\r\n"+testSuite.size()+"\r\n\r\n");
			solution.write("### test sequence\r\n");
			int tcNum = 1;
			for(TestCase testCase: testSuite){
				solution.write("TS"+tcNum+"\r\n");
//				StringTokenizer tc = new StringTokenizer(st.nextToken(),")");
				String[][] ts = new String[numOfEachTC[testCase.partNo][testCase.counter-1]+2][inputs.length];
				for(int i = 0; i< inputs.length; i++){
					if(Character.isLetter(tempArray[2][i].charAt(0)))
						for(int j = 0; j<numOfEachTC[testCase.partNo][testCase.counter-1]+2; j++)
							ts[j][i] = "false";
					else
						for(int j = 0; j<numOfEachTC[testCase.partNo][testCase.counter-1]+2; j++)
							ts[j][i] = "0";
				}
				for(String key : testCase.valueMap.keySet()){
					int row=0, col=0;
//					String token = tc.nextToken();
					for(int i = 0; i<inputs.length; i++){
						//This part has a bug
						if(key.startsWith(tempArray[1][i])){
							col = i;
//							StringTokenizer variableValue = new StringTokenizer(token,"( ");
//							//variable--> variable0
//							String variable = variableValue.nextToken();
//							//value--> value0
//							String value = variableValue.nextToken();
							int variableSize = tempArray[1][i].length();
//							System.out.println("var size: "+variableSize+ " varlength: "+variable.length());
							if (variableSize <key.length()){
								String cycleNum = key.substring(variableSize+2);
								row = Integer.parseInt(cycleNum);
							}
							else
								row = 0;
							System.out.println("row: "+row+" col: "+col);
//							System.out.println("leng: "+ts.length);
							ts[row][col] = testCase.valueMap.get(key);
							break;
						}
					}
				}
				System.out.println("end while");
				//---------------------------------------------------------test sequence writing end*/
				
				/*erasing in-out variable value start--------------------------------------------*/
				for (int i = 0; i<inputs.length; i++){
					for (int j = 0; j<tempOutput.length; j++){
						if(tempArray[1][i].equals(tempOutput[j])){
							for(int k = numOfEachTC[testCase.partNo][testCase.counter-1]; k>=0; k--)
								ts[k][i]="-";
							break;
						}
					}
				}
				//----------------------------------------------erasing in-out variable value end*/
				
				/*writing one test sequence start------------------------------------------------*/
				for(int i = numOfEachTC[testCase.partNo][testCase.counter-1]+1; i >= 0 ; i--){
					solution.write("_t"+i+"\r\n");
					for(int j = 0; j < inputs.length; j++){
						solution.write(ts[i][j]+"\t");
					}
					solution.write("\r\n");
				}
				tcNum++;
				solution.write("\r\n");
			}
			//---------------------------------------------------writing one test sequence end*/
			solution.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("solutionWriter(): FATAL ERR");
			System.exit(-1);
		}
	}


}