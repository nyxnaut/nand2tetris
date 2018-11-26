import java.util.HashMap;

public class SymbolTable {
	private static final HashMap<String, Integer> table  = new HashMap<String, Integer>();

	static {
		table.put("SP", 0x0);
		table.put("LCL", 0x1);
		table.put("ARG", 0x2);
		table.put("THIS", 0x3);
		table.put("THAT", 0x4);
		table.put("R0", 0x0);
		table.put("R1", 0x1);
		table.put("R2", 0x2);
		table.put("R3", 0x3);
		table.put("R4", 0x4);
		table.put("R5", 0x5);
		table.put("R6", 0x6);
		table.put("R7", 0x7);
		table.put("R8", 0x8);
		table.put("R9", 0x9);
		table.put("R10", 0xA);
		table.put("R11", 0xB);
		table.put("R12", 0xC);
		table.put("R13", 0xD);
		table.put("R14", 0xE);
		table.put("R15", 0xF);
		table.put("SCREEN", 0x4000);
		table.put("KBD", 0x6000);
	}

	public static final void addEntry(String symbol, int address) {
		table.putIfAbsent(symbol, address);
	}

	public static final boolean contains(String symbol) {
		return table.containsKey(symbol);
	}
	public static final int getAddress(String symbol) {
		return table.get(symbol);
	}
}
