package com.softtech.traductorbraille.translator;

import java.util.HashMap;
import java.util.Map;

/**
 * Dictionary es un diccionario de equivalencias entre el abecedario español y las claves braille y viceversa.
 * 
 * @since 2.0
 * @version 3.0
 * @author SoftTech
 */
public class Dictionary {

    private static Dictionary instance;
    private static final Map<String, String> brailleMap = new HashMap<>();
    private static final Map<String, String> spanishMap = new HashMap<>();
    private static final Map<Character, Character> brailleMirrorMap = new HashMap<>();

    static {
        brailleMap.put("\t", "\t");
        brailleMap.put("  ", " ");
        brailleMap.put("\n", "\n");
        brailleMap.put("1", "a");
        brailleMap.put("12", "b");
        brailleMap.put("14", "c");
        brailleMap.put("145", "d");
        brailleMap.put("15", "e");
        brailleMap.put("124", "f");
        brailleMap.put("1245", "g");
        brailleMap.put("125", "h");
        brailleMap.put("24", "i");
        brailleMap.put("245", "j");
        brailleMap.put("13", "k");
        brailleMap.put("123", "l");
        brailleMap.put("134", "m");
        brailleMap.put("1345", "n");
        brailleMap.put("135", "o");
        brailleMap.put("1234", "p");
        brailleMap.put("12345", "q");
        brailleMap.put("1235", "r");
        brailleMap.put("234", "s");
        brailleMap.put("2345", "t");
        brailleMap.put("136", "u");
        brailleMap.put("1236", "v");
        brailleMap.put("2456", "w");
        brailleMap.put("1346", "x");
        brailleMap.put("13456", "y");
        brailleMap.put("1356", "z");
        brailleMap.put("12456", "ñ");
        brailleMap.put("12356", "á");
        brailleMap.put("2346", "é");
        brailleMap.put("34", "í");
        brailleMap.put("346", "ó");
        brailleMap.put("23456", "ú");
        brailleMap.put("1256", "ü");
        brailleMap.put("3", ".");
        brailleMap.put("2", ",");
        brailleMap.put("23", ";");
        brailleMap.put("25", ":");
        brailleMap.put("36", "_");
        brailleMap.put("63", "-");
        brailleMap.put("632", "\"");
        brailleMap.put("532", "!");
        brailleMap.put("325", "¡");
        brailleMap.put("26", "?");
        brailleMap.put("62", "¿");
        brailleMap.put("126", "(");
        brailleMap.put("345", ")");
        brailleMap.put("235", "+");
        brailleMap.put("236", "*");
        brailleMap.put("2356", "=");
        brailleMap.put("256", "/");
        brailleMap.put("3456", "#");
        brailleMap.put("1246", "$");
        brailleMap.put("146", "%");
        brailleMap.put("12346", "&");
        brailleMap.put("5", "@");
        brailleMap.put("3456 1", "1");
        brailleMap.put("3456 12", "2");
        brailleMap.put("3456 14", "3");
        brailleMap.put("3456 145", "4");
        brailleMap.put("3456 15", "5");
        brailleMap.put("3456 124", "6");
        brailleMap.put("3456 1245", "7");
        brailleMap.put("3456 125", "8");
        brailleMap.put("3456 24", "9");
        brailleMap.put("3456 245", "0");
        
        brailleMirrorMap.put('⠁', '⠈'); 
        brailleMirrorMap.put('⠃', '⠘');  
        brailleMirrorMap.put('⠉', '⠉'); 
        brailleMirrorMap.put('⠙', '⠋');  
        brailleMirrorMap.put('⠑', '⠊'); 
        brailleMirrorMap.put('⠋', '⠙');  
        brailleMirrorMap.put('⠦', '⠴');  
        brailleMirrorMap.put('⠊', '⠑'); 
        brailleMirrorMap.put('⠚', '⠓');
        brailleMirrorMap.put('⠓', '⠚');  
        brailleMirrorMap.put('⠅', '⠨');       
        brailleMirrorMap.put('⠇','⠸');              
        brailleMirrorMap.put('⠍', '⠩'); 
        brailleMirrorMap.put('⠝', '⠫');  
        brailleMirrorMap.put('⠕', '⠪');  
        brailleMirrorMap.put('⠏', '⠹'); 
        brailleMirrorMap.put('⠟','⠻');
        brailleMirrorMap.put('⠗', '⠺'); 
        brailleMirrorMap.put('⠎', '⠱'); 
        brailleMirrorMap.put('⠞','⠳');
        brailleMirrorMap.put('⠥', '⠬'); 
        brailleMirrorMap.put('⠧', '⠼'); 
        brailleMirrorMap.put('⠺', '⠗'); 
        brailleMirrorMap.put('⠭', '⠭'); 
        brailleMirrorMap.put('⠽','⠯');  
        brailleMirrorMap.put('⠵', '⠮'); 
        brailleMirrorMap.put('⠷','⠾');
        brailleMirrorMap.put('⠾','⠷');
        brailleMirrorMap.put('⠬', '⠥');
        brailleMirrorMap.put('⠮','⠵');
        brailleMirrorMap.put('⠌','⠡');     
        brailleMirrorMap.put('⠼','⠧');
        brailleMirrorMap.put('⠂', '⠐');
        brailleMirrorMap.put('⠦', '⠴'); 
        brailleMirrorMap.put('⠖', '⠲'); 
        brailleMirrorMap.put('⠲', '⠖'); 
        brailleMirrorMap.put('⠆', '⠰');        
        brailleMirrorMap.put('⠜','⠣'); 
        brailleMirrorMap.put('⠣','⠜'); 
        brailleMirrorMap.put('⠄','⠠'); 
        brailleMirrorMap.put('⠢','⠔'); 
        brailleMirrorMap.put('⠫', '⠝');
        brailleMirrorMap.put('⠩', '⠍');
        brailleMirrorMap.put('⠯', '⠽');
        brailleMirrorMap.put('⠐', '⠂'); 
        
        createUppercaseAlphabet();
        createReverseMap();
    }

    /**
     * Crea un mapa de braille a español
     */
    private static void createUppercaseAlphabet() {
        Map<String, String> auxiliaryMap = new HashMap<>();
        for (Map.Entry<String, String> entry : brailleMap.entrySet()) {
            if (Character.isLetter(entry.getValue().charAt(0))) {
                String uppercaseValue = entry.getValue().toUpperCase();
                auxiliaryMap.put("46 " + entry.getKey(), uppercaseValue);
            }
        }
        brailleMap.putAll(auxiliaryMap);
    }

    /**
     * Crear un mapa de español a braille
     */
    private static void createReverseMap() {
        for (Map.Entry<String, String> entry : brailleMap.entrySet()) {
            if (!entry.getKey().isEmpty()) {
                spanishMap.put(entry.getValue(), entry.getKey());
            }
        }
    }



    public static Dictionary getInstance() {
        if (instance == null) {
            instance = new Dictionary();
        }
        return instance;
    }

    /**
     * obtener el caracter en braille
     *
     * @param ch
     * @return braille caracter
     */
    public String getBrailleValue(char ch) {
        return spanishMap.getOrDefault(String.valueOf(ch), "□");
    }

    /**
     * obtener el caracter en español
     *
     * @param key
     * @return español caracter
     */
    public String getSpanishValue(String key) {
        return brailleMap.getOrDefault(String.valueOf(key), "□");
    }

    /**
     * Verifica si el caracter braille existe en el diccionario
     *
     * @param code
     * @return boolean
     */
    public boolean isExistInBrailleMap(String code) {
        return !brailleMap.getOrDefault(code, "??").equals("??");
    }
    
            /**
     * transformar texto braille en su forma expejo
     * @param brailleText
     * @return braille en espejo
     */
    public String generateBrailleMirror(String brailleText) {
        String reversedText = new StringBuilder(brailleText).reverse().toString();
        StringBuilder mirroredText = new StringBuilder();
        for (char ch : reversedText.toCharArray()) {
            mirroredText.append(brailleMirrorMap.getOrDefault(ch, ch));
        }


        return mirroredText.toString();
    }

}
