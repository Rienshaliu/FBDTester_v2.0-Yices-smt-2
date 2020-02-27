package Structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

/** 
 * �븯�굹�쓽 TestCase瑜� ���옣�븯湲� �쐞�븳 援ъ“泥�.
 * �썝�옒 Yices瑜� �넻�븳 test case�뒗 �떎�쓬怨� 媛숈� �삎�깭�엫.
 * (= TRIP true)
 * (= PV_OUT 100)
 * ...
 * 
 * 媛� 蹂��닔�쓽 �씠由꾩� inputs�뿉 ���옣�븯怨�,
 * 蹂��닔-媛� �뙇�� valueMap�뿉 ���옣�븳�떎.
 * 
 * @author donghwan
 *
 */
public class TestCase {
	
	public int partNo = 0;
	public int counter = 0;
	public String originalTC;

	public ArrayList<String> inputs; // Test case inputs
	public HashMap<String, String> valueMap; // input-value pair

	// sample tc: (TRIP true)(PTRIP true)(RST_MCR_LATCH false)(PV_OUT 2)
	public TestCase(String tc) {
		originalTC = tc;
		inputs = new ArrayList<String>();
		valueMap = new HashMap<String, String>();

		String input = "", value = ""; // Temporal variables

		StringTokenizer token = new StringTokenizer(tc.trim(), "{}()= ");
		while (token.hasMoreTokens()) {
			input = token.nextToken().trim();
			value = token.nextToken().trim();

			// Test case data structure construction
			inputs.add(input);
			valueMap.put(input, value);
		}
	}
	
	public String getValue(String input) {
		return valueMap.get(input);
	}
	
	public String toString() {
		String ret = "";
		
		Collections.sort(inputs);
		for(String input: inputs)
			ret += "("+input + " " + valueMap.get(input) + ")";
		
		return ret.trim();
	}
}
