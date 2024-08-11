package com.softtech.traductorbraille.voice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Servicio de voz para la transcripción de voz a texto y texto a voz.
 *
 * @since 2.0
 * @version 3.0
 * @author SoftTech
 */
public class VoiceService {

    private boolean listening = false;
    private boolean speaking = false;
    private Process process = null;
    private static final String PYTHON_COMMAND = "python";

    /**
     * Inicia la escucha de voz y transcribe el texto reconocido en el JTextArea
     * proporcionado.
     *
     * @param texto JTextArea donde se añadirá el texto reconocido.
     */
    public void startListening(JTextArea texto) {
        listening = true;
        executePythonScript("src\\main\\java\\com\\softtech\\traductorbraille\\python\\voiceToText.py", texto);
    }

    /**
     * Detiene la escucha de voz.
     */
    public void stopListening() {
        listening = false;
        if (process != null) {
            process.destroy();
        }
    }

    /**
     * Convierte el texto proporcionado a voz.
     *
     * @param texto El texto que se convertirá a voz.
     */
    public void speak(String texto) {
        stopSpeaking();  // Asegúrate de detener cualquier proceso anterior antes de iniciar uno nuevo.
        speaking = true;
        executePythonScript("src\\main\\java\\com\\softtech\\traductorbraille\\python\\textToVoice.py \"" + texto + "\"", null);
    }

    /**
     * Detiene la reproducción de voz.
     */
    public void stopSpeaking() {
        speaking = false;
        if (process != null) {
            process.destroy();
        }
    }

    public boolean isSpeaking() {
        return speaking;
    }

    /**
     * Ejecuta un script de Python y procesa la salida y errores.
     *
     * @param scriptPath Ruta completa del script de Python.
     * @param outputArea JTextArea donde se añadirá la salida del script (puede
     * ser null si no se necesita).
     */
    private void executePythonScript(String scriptPath, JTextArea outputArea) {
        try {
            String cmd = PYTHON_COMMAND + " " + scriptPath;

            // Ejecutar el comando
            process = Runtime.getRuntime().exec(cmd);

            try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                String s;
                if (outputArea != null) {
                    while (listening && (s = stdInput.readLine()) != null) {
                        final String recognizedText = s;  // Capturar el valor de s en una variable final
                        SwingUtilities.invokeLater(() -> outputArea.append(recognizedText + "\n"));
                    }
                }
            }
        } catch (IOException e) {
        }
    }
}
