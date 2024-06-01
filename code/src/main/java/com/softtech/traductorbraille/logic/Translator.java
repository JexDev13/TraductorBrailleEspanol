package com.softtech.traductorbraille.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Isma2
 */
public class Translator {

    private static final Map<String, String> brailleMap = new HashMap<>();
    private static final Map<String, String> reverseBrailleMap = new HashMap<>();

    static {
        // Representación de las letras en Braille usando combinaciones de números del 1 al 6
        // Espacio
        brailleMap.put("", " ");
        // Alfabeto
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
        brailleMap.put("156", "u");
        brailleMap.put("1236", "v");
        brailleMap.put("2456", "w");
        brailleMap.put("1346", "x");
        brailleMap.put("13456", "y");
        brailleMap.put("1356", "z");
        brailleMap.put("12456", "ñ");
        // Vocales acentuadas
        brailleMap.put("12356", "á");
        brailleMap.put("2346", "é");
        brailleMap.put("34", "í");
        brailleMap.put("346", "ó");
        brailleMap.put("23456", "ú");
        brailleMap.put("1256", "ü");
        // Caracteres especiales
        brailleMap.put("3", ".");
        brailleMap.put("2", ",");
        brailleMap.put("23", ";");
        brailleMap.put("25", ":");
        brailleMap.put("36", "-");
        brailleMap.put("236", "\"");
        brailleMap.put("235", "!");
        brailleMap.put("26", "?");
        brailleMap.put("126", "(");
        brailleMap.put("345", ")");
        brailleMap.put("235", "+");
        brailleMap.put("236", "*");
        brailleMap.put("2356", "=");
        brailleMap.put("256", "/");
        brailleMap.put("3456", "#");
        // Números
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

        createUppercaseAlphabet();
        createReverseMap();
    }

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

    private static void createReverseMap() {
        for (Map.Entry<String, String> entry : brailleMap.entrySet()) {
            if (!entry.getKey().isEmpty()) {
                reverseBrailleMap.put(entry.getValue(), entry.getKey());
            }
        }
    }

    private char brailleCharFromSegment(String dots) {
        int baseUnicode = 0x2800;
        int brailleValue = 0;
        for (char dot : dots.toCharArray()) {
            switch (dot) {
                case '1' ->
                    brailleValue |= 0x01;
                case '2' ->
                    brailleValue |= 0x02;
                case '3' ->
                    brailleValue |= 0x04;
                case '4' ->
                    brailleValue |= 0x08;
                case '5' ->
                    brailleValue |= 0x10;
                case '6' ->
                    brailleValue |= 0x20;
                default ->
                    brailleValue = 0;
            }
        }
        return (char) (baseUnicode + brailleValue);
    }

    public String translateToSpanish(String brailleText) {
        StringBuilder translatedText = new StringBuilder();
        String[] cells = brailleText.split("  "); //Separar en palabras
        //Separar por letras y valores numericos 
        for (String cell : cells) {
            String[] words = cell.split(" ");
            for (int i = 0; i < words.length; i++) {
                String translatedChar;
                String auxiliary = words[i];
                if (words[i].equals("46") || words[i].equals("3456")) {
                    auxiliary = words[i] + " " + words[i + 1];
                    i++;
                }
                translatedChar = brailleMap.getOrDefault(auxiliary, "?");

                translatedText.append(translatedChar);
            }
            translatedText.append(" ");
        }
        return translatedText.toString();
    }

    private List<Character> brailleCharsFromDots(String dots) {
        List<Character> brailleChars = new ArrayList<>();
        String[] segments = dots.split(" ");
        for (String segment : segments) {
            if (!segment.isEmpty()) {
                brailleChars.add(brailleCharFromSegment(segment));
            }
        }
        return brailleChars;
    }

    public String translateToBraille(String spanishText) {
        StringBuilder brailleText = new StringBuilder();
        // Flag para saber si hay una secuencia de números
        boolean isNumber = false;
        for (char ch : spanishText.toCharArray()) {
            String brailleChar = reverseBrailleMap.getOrDefault(String.valueOf(ch), "");
            if (!brailleChar.isEmpty()) {
                List<Character> result = brailleCharsFromDots(brailleChar);
                // Verifica si es un dígito
                if (Character.isDigit(ch) || ch == '.' || ch == ',') {
                    if (isNumber && Character.isDigit(ch)) {
                        result.remove(0);
                    } else {
                        isNumber = true;
                    }
                } else {
                    isNumber = false;
                }
                addCharacterToText(result, brailleText);
            } else {
                brailleText.append("  ");
                isNumber = false;
            }
        }
        return brailleText.toString().trim();
    }

    private void addCharacterToText(List<Character> characters, StringBuilder text) {
        for (char element : characters) {
            text.append(element).append(" ");
        }
    }

    public String brailleToUnicode(String brailleText) {
        StringBuilder quadratsString = new StringBuilder();
        String[] words = brailleText.split("");
        for (String word : words) {
            List<Character> quadrats = brailleCharsFromDots(word);
            addCharacterToText(quadrats, quadratsString);
            quadratsString.append("  ");
        }

        return quadratsString.toString();
    }

}