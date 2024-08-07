package com.softtech.traductorbraille.executable;

import com.softtech.traductorbraille.GUI.JFTranslator;

/**
 * La clase principal que inicia la aplicación.
 * 
 * @since 1.0
 * @version 1.0
 * @author SoftTech
 */
public class MainClass {

    /**
     * El método principal que sirve como punto de entrada a la aplicación.
     * 
     * @param args los argumentos de la línea de comandos. Actualmente no se utilizan.
     */
    public static void main(String[] args) {
        JFTranslator translator = new JFTranslator();
        translator.setVisible(true);
    }
}
