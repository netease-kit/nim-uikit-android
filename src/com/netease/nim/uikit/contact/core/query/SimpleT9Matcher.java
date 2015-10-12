package com.netease.nim.uikit.contact.core.query;

public class SimpleT9Matcher {

    private static final char[] LATIN_LETTERS_TO_DIGITS = {
        '2', '2', '2', // A,B,C -> 2
        '3', '3', '3', // D,E,F -> 3
        '4', '4', '4', // G,H,I -> 4
        '5', '5', '5', // J,K,L -> 5
        '6', '6', '6', // M,N,O -> 6
        '7', '7', '7', '7', // P,Q,R,S -> 7
        '8', '8', '8', // T,U,V -> 8
        '9', '9', '9', '9' // W,X,Y,Z -> 9
    };

    private static boolean isAlphaNumberString(String name) {
        return name.matches("[0-9a-zA-Z]+");
    }

    private static char getDialpadNumericCharacter(char ch) {
        if(ch >= 'A' && ch <= 'Z'){
            ch = (char) (ch + ('a' - 'A'));
        }
        if (ch >= 'a' && ch <= 'z') {
            return LATIN_LETTERS_TO_DIGITS[ch - 'a'];
        }
        return ch;
    }

    /**
     * 将仅包含数字和字母的字符串转化为数字串(对应T9)
     * @param name
     * @return
     */
    private static String alphaNumberStringToNumbericString(String name){
        if(isAlphaNumberString(name)){
            final int length = name.length();
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < length ; index++){
                builder.append(getDialpadNumericCharacter(name.charAt(index)));
            }
            return builder.toString();
        }
        return "";
    }

    public static boolean hit(String text,String query){
        return alphaNumberStringToNumbericString(text).contains(query);
    }

}
