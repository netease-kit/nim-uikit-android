package com.netease.nim.uikit.contact.core.query;

import android.text.TextUtils;

public class TextComparator {
	public static final int compare(String a, String b) {
		if (a == b) {
			return 0;
		}
		
		if (a == null) {
			return -1;
		}
		
		if (b == null) {
			return 1;
		}
		
		for (int index = 0; index < a.length() && index < b.length(); index++) {
			int compare = compare(a.charAt(index), b.charAt(index));
			if (compare != 0) {
				return compare;
			}
		}
		
		return a.length() - b.length();
	}
	
	public static final int compareIgnoreCase(String a, String b) {
		if (a == b) {
			return 0;
		}
		
		if (a == null) {
			return -1;
		}
		
		if (b == null) {
			return 1;
		}
		
		for (int index = 0; index < a.length() && index < b.length(); index++) {
			int compare = compareIgnoreCase(a.charAt(index), b.charAt(index));
			if (compare != 0) {
				return compare;
			}
		}
		
		return a.length() - b.length();
	}
	
	public static final int compare(char a, char b) {
		if (a == b) {
			return 0;
		}
		
		{
			int ai = getAsciiIndex(a, false);
			int bi = getAsciiIndex(b, false);
			
			if (ai != bi) {
				if (ai == -1) {
					return 1;
				} else if (bi == -1) {
					return -1;
				} else {
					return ai - bi;	
				}			
			} else {
				if (ai != -1) {
					return 0;
				}
			}
		}

		{
			int ai = PinYin.getIndex(a);
			int bi = PinYin.getIndex(b);
			
			if (ai != bi) {
				if (ai == -1) {
					return 1;
				} else if (bi == -1) {
					return -1;
				} else {
					return ai - bi;	
				}			
			} 
		}
		
		return a - b;
	}
	
	public static final int compareIgnoreCase(char a, char b) {
		if (a == b) {
			return 0;
		}
		
		{
			int ai = getAsciiIndex(a, true);
			int bi = getAsciiIndex(b, true);
			
			if (ai != bi) {
				if (ai == -1) {
					return 1;
				} else if (bi == -1) {
					return -1;
				} else {
					return ai - bi;	
				}			
			} else {
				if (ai != -1) {
					return 0;
				}
			}
		}

		{
			int ai = PinYin.getIndex(a);
			int bi = PinYin.getIndex(b);
			
			if (ai != bi) {
				if (ai == -1) {
					return 1;
				} else if (bi == -1) {
					return -1;
				} else {
					return ai - bi;	
				}			
			}
		}
		
		return a - b;
	}

	public static final String getLeadingUp(String s) {
		if (TextUtils.isEmpty(s)) {
			return null;
		}
		
		char c = s.charAt(0);
		
		String leading = getAsciiLeadingUp(c);
		if (leading == null) {
			leading = PinYin.getLeadingUp(c);
		}
		
		return leading;
	}
	
	public static final String getLeadingLo(String s) {
		if (TextUtils.isEmpty(s)) {
			return null;
		}
		
		char c = s.charAt(0);
		
		String leading = getAsciiLeadingLo(c);
		if (leading == null) {
			leading = PinYin.getLeadingLo(c);
		}
		
		return leading;
	}
	
	private static int getAsciiIndex(char c, boolean ignoreCase) {
		// 0-9 0x0
		if (c >= 0x30 && c <= 0x39) {
			return c - 0x30 + 0;
		}
		
		// a-z 0xA
		if (c >= 0x61 && c <= 0x7A) {
			return c - 0x61 + 0xA;
		}

		// A-Z 0x24
		if (c >= 0x41 && c <= 0x5A) {
			return c - 0x41 + (ignoreCase ? 0xA : 0x24);
		}

		return -1;
	}
	
	private static final String[] leadingUp = new String[] { "A", "B", "C", "D", "E",
			"F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
			"S", "T", "U", "V", "W", "X", "Y", "Z"
	};
	
	private static final String[] leadingLo = new String[] { "a", "b", "c",
			"d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
			"q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
	
	private static String getAsciiLeadingUp(char c) {
		// a-z 0xA
		if (c >= 0x61 && c <= 0x7A) {
			return leadingUp[c - 0x61];
		}

		// A-Z 0x24
		if (c >= 0x41 && c <= 0x5A) {
			return leadingUp[c - 0x41];
		}

		return null;
	}
	
	private static String getAsciiLeadingLo(char c) {
		// a-z 0xA
		if (c >= 0x61 && c <= 0x7A) {
			return leadingLo[c - 0x61];
		}

		// A-Z 0x24
		if (c >= 0x41 && c <= 0x5A) {
			return leadingLo[c - 0x41];
		}

		return null;
	}
}
