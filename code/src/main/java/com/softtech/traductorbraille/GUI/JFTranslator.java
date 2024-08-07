package com.softtech.traductorbraille.GUI;

import com.softtech.traductorbraille.translator.Spanish;
import com.softtech.traductorbraille.translator.Braille;
import com.softtech.traductorbraille.translator.TranslatorManager;
import com.softtech.traductorbraille.format.TextFormatter;
import com.softtech.traductorbraille.voice.VoiceService;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

/**
 *
 * JFTranslator es la interfaz gráfica que permite al usuario realizar
 * traducciones de español-braille y braille-español.
 *
 * @since 1.0
 * @version 2.0
 * @author SoftTech
 */
public class JFTranslator extends javax.swing.JFrame {

    private TranslatorManager translator = new TranslatorManager();

    private TextFormatter textFormat = new TextFormatter();

    private boolean flag = true;
    private VoiceService voiceListener;

    private int x;
    private int y;
    private Color selectedColor = Color.BLACK;

    private BrailleCell currentBrailleCell;
    private BrailleCell additionalBrailleCell;

    private boolean isUpperCaseMode = false;
    private boolean isNumberMode = false;
    private String totalBrailleTranslation = new String();

    private static final int[] BRAILLE_INDEX_MAPPING = {0, 2, 4, 1, 3, 5};
    private static final int[] KEY_EVENT_MAPPING = {
        KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD3,
        KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6
    };
    private boolean firstTime = true;

    /**
     * Creates new form JFTranslatorGUI
     */
    public JFTranslator() {
        initComponents();
        configureWindow();
    }

    /**
     * Configura la ventana de la aplicación.
     */
    private void configureWindow() {
        this.setLocationRelativeTo(this);
        setTranslationMode(false);
        this.JPBrailleMenu.setVisible(false);
        this.jTALenEntrada.setLineWrap(true);
        this.jTALenEntrada.setWrapStyleWord(true);
        this.jTLenSalida.setLineWrap(true);
        this.jTLenSalida.setWrapStyleWord(true);
        this.jSeparator3.setVisible(false);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyInput(e.getKeyCode());
            }
        });
        currentBrailleCell = new BrailleCell();
        braillePanel.add(currentBrailleCell);
        voiceListener = new VoiceService();
    }

    /**
     * Limpia los campos de texto para Braille y texto en español.
     */
    private void clearTextFields() {
        this.jTALenEntrada.setText("");
        this.jTLenSalida.setText("");
    }

    /**
     * Obtiene el modo de traducción actual (Español a Braille o Braille a
     * Español).
     *
     * @return true si el modo es Español a Braille, false en caso contrario
     */
    private boolean getTranslationMode() {
        return this.jLEspañolEntrada.getText().equalsIgnoreCase("Español")
                && this.jLBrailleEntrada.getText().equalsIgnoreCase("Braille");
    }

    /**
     * Establece el modo de traducción y actualiza los componentes de la
     * interfaz de usuario en consecuencia.
     *
     * @param isSpanishToBraille true si el modo es Español a Braille, false en
     * caso contrario
     */
    private void setTranslationMode(boolean isSpanishToBraille) {
        String title;
        String brailleLabel;
        String spanishLabel;

        textFormat.setTranslationMode(isSpanishToBraille);

        if (isSpanishToBraille) {
            title = "Brailingo - Traductor: Braille -> Español";
            brailleLabel = "Español";
            spanishLabel = "Braille";
        } else {
            title = "Brailingo - Traductor: Español -> Braille";
            brailleLabel = "Braille";
            spanishLabel = "Español";
        }
        this.jPSpanishEntrance.setVisible(!isSpanishToBraille);
        this.jPSpanishOut.setVisible(isSpanishToBraille);
        this.jTALenEntrada.setEditable(!isSpanishToBraille);
        this.JPBrailleMenu.setVisible(isSpanishToBraille);
        this.jSeparator3.setVisible(isSpanishToBraille);
        this.jTATitulo.setText(title);
        this.jLBrailleEntrada.setText(brailleLabel);
        this.jLEspañolEntrada.setText(spanishLabel);
        this.jLLenEntrada.setText(spanishLabel);
        this.jLLenSalida.setText(brailleLabel);

        textFormat.applyConditionalFormatting(jTALenEntrada, jTLenSalida);
        resetFormattingOptions();
    }

    /**
     * Cambia el modo de traducción entre Español a Braille y Braille a Español.
     */
    private void switchTranslationMode() {
        setTranslationMode(getTranslationMode());
        clearTextFields();
        textFormat.applyConditionalFormatting(jTALenEntrada, jTLenSalida);
    }

    /**
     * Restablece las opciones de formato a sus valores predeterminados.
     *
     * Este método restablece las opciones de tamaño de letra, negrita y cursiva
     * en los componentes de interfaz correspondientes.
     */
    private void resetFormattingOptions() {
        jComboBoxTamañoLetra.setSelectedIndex(0);
        jCheckBoxCursiva.setSelected(false);
        jCheckBoxNegrita.setSelected(false);
    }

    /**
     * Maneja la entrada del teclado para la entrada de puntos Braille.
     *
     * @param keyCode el código de la tecla presionada
     */
    private void handleKeyInput(int keyCode) {
        int index = findIndexForKeyCode(keyCode);
        if (index != -1) {
            boolean currentState = currentBrailleCell.getPoint(index);
            currentBrailleCell.setPoint(index, !currentState);
            braillePanel.repaint();
        } else if (keyCode == KeyEvent.VK_ENTER) {
            translateText();
            totalBrailleTranslation += " ";
        } else if (keyCode == KeyEvent.VK_SPACE) {
            addSpace();
        } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
            deleteLastCharacter();
        }
    }

    /**
     * Borra el último carácter escrito en los campos de texto de entrada y
     * salida.
     */
    private void deleteLastCharacter() {
        String entradaText = this.jTALenEntrada.getText();
        String salidaText = this.jTLenSalida.getText();

        if (entradaText.length() > 0) {
            this.jTALenEntrada.setText(entradaText.substring(0, entradaText.length() - 2));
        }

        if (salidaText.length() > 0) {
            this.jTLenSalida.setText(salidaText.substring(0, salidaText.length() - 1));
        }

        currentBrailleCell.clearPoints();
        braillePanel.repaint();
    }

    /**
     * Verifica el último carácter de un texto y deselecciona el checkbox de
     * números si el último carácter no es un dígito, un punto o una coma.
     *
     * @param text el texto a verificar
     */
    public void checkLastCharacterAndUnselect(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        char lastChar = text.charAt(text.length() - 1);
        if (!Character.isDigit(lastChar) && lastChar != '.' && lastChar != ',') {
            jCBNumeros.setSelected(false);
        }
    }

    /**
     * Agrega un espacio en la traducción Braille y los campos de texto
     * correspondientes.
     */
    private void addSpace() {
        totalBrailleTranslation += "  ";
        this.jTALenEntrada.append("  ");
        this.jTLenSalida.append("⠀");
        currentBrailleCell.clearPoints();
        braillePanel.repaint();
    }

    /**
     * Encuentra el índice para el punto Braille correspondiente basado en el
     * código de tecla.
     *
     * @param keyCode el código de la tecla presionada
     * @return el índice del punto Braille
     */
    private int findIndexForKeyCode(int keyCode) {
        for (int i = 0; i < KEY_EVENT_MAPPING.length; i++) {
            if (keyCode == KEY_EVENT_MAPPING[i]) {
                return BRAILLE_INDEX_MAPPING[i];
            }
        }
        return -1;
    }

    /**
     * Traduce la celda Braille actual a texto, usando la clase Translator y
     * coloca los resultados en el formulario.
     */
    private void translateCurrentBrailleCell() {
        String cellText = getBrailleCellText(currentBrailleCell);
        String targetCellText = additionalBrailleCell != null ? getBrailleCellText(additionalBrailleCell) : "";

        String combinedBrailleText = combineBrailleTexts(targetCellText, cellText);
        totalBrailleTranslation += combinedBrailleText;
        String translation = translator.translate(totalBrailleTranslation);

        //TODO: CORREGIR ESTO LOS ERRORES SE CONTROLAN EN LÓGICA
        if (translation.equals("?")) {
            showTranslationError();
            return;
        }

        updateFormFields(targetCellText, cellText, translation);
        clearBrailleCells();
        updateBraillePanelIfNeeded();
        checkLastCharacterAndUnselect(translation);
    }

    /**
     * Obtiene los puntos activados en la celda Braille.
     *
     * @param brailleCell La celda Braille de la cual obtener los puntos
     * activados.
     * @return Texto Braille basado en los puntos activados.
     */
    private String getBrailleCellText(BrailleCell brailleCell) {
        StringBuilder cellText = new StringBuilder();

        for (int i = 0; i < BRAILLE_INDEX_MAPPING.length; i++) {
            if (brailleCell.getPoint(BRAILLE_INDEX_MAPPING[i])) {
                cellText.append(i + 1);
            }
        }

        return cellText.toString();
    }

    /**
     * Actualiza los campos del formulario con la traducción y el texto Braille
     * en Unicode.
     *
     * @param targetCellText Texto de la celda Braille adicional.
     * @param cellText Texto de la celda Braille actual.
     * @param translation Texto traducido.
     */
    private void updateFormFields(String targetCellText, String cellText, String translation) {
        this.clearTextFields();
        Braille brailleTranslator = new Braille();

        this.jTLenSalida.append(translation);
        String brailleUnicode = brailleTranslator.brailleToQuadratin(totalBrailleTranslation);
        this.jTALenEntrada.append(brailleUnicode);
        this.jTALenEntrada.append(" ");
    }

    /**
     * Combina los textos Braille obtenidos de dos celdas.
     *
     * @param targetCellText Texto de la celda Braille adicional.
     * @param cellText Texto de la celda Braille actual.
     * @return Texto Braille combinado.
     */
    private String combineBrailleTexts(String targetCellText, String cellText) {
        return (targetCellText.isEmpty() ? "" : targetCellText + " ") + cellText;
    }

    /**
     * Muestra un mensaje de error si la combinación de puntos no existe en el
     * diccionario de traducción.
     */
    private void showTranslationError() {
        JOptionPane.showMessageDialog(this,
                "La traducción para la combinación ingresada no existe en el diccionario.",
                "Error de Traducción", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Limpia los puntos activados de las celdas Braille y repinta el panel
     * Braille.
     */
    private void clearBrailleCells() {
        if (additionalBrailleCell != null) {
            additionalBrailleCell.clearPoints();
        }

        currentBrailleCell.clearPoints();
        braillePanel.repaint();
    }

    /**
     * Actualiza el diseño del panel Braille si el modo de mayúsculas o el modo
     * de número están activados.
     */
    private void updateBraillePanelIfNeeded() {
        if (isUpperCaseMode || isNumberMode) {
            updateBraillePanelLayout();
        }
    }

    /**
     * Obtiene el texto de la celda objetivo (mayúsculas o números).
     *
     * @param cell la celda Braille objetivo
     * @return el texto de la celda objetivo
     */
    private String getTargetCellText(BrailleCell cell) {
        StringBuilder targetText = new StringBuilder();
        for (int i = 0; i < BRAILLE_INDEX_MAPPING.length; i++) {
            if (cell.getPoint(BRAILLE_INDEX_MAPPING[i])) {
                targetText.append(i + 1);
            }
        }
        return targetText.toString();
    }

    /**
     * Actualiza el diseño del panel de Braille.
     */
    private void updateBraillePanelLayout() {
        int columns = isUpperCaseMode || isNumberMode ? 2 : 1;
        int panelWidth = isUpperCaseMode || isNumberMode ? 108 : 54;

        braillePanel.setLayout(new GridLayout(1, columns));

        if (columns == 1 && braillePanel.getComponentCount() > 1) {
            braillePanel.remove(0);
            additionalBrailleCell = null;
        } else if (columns == 2 && braillePanel.getComponentCount() == 1) {
            additionalBrailleCell = new BrailleCell();
            braillePanel.add(additionalBrailleCell, 0);
        }

        braillePanel.setPreferredSize(new Dimension(panelWidth, braillePanel.getHeight()));
        braillePanel.revalidate();

        configureAdditionalBrailleCell();
    }

    /**
     * Configura la celda Braille adicional según el modo activo.
     */
    private void configureAdditionalBrailleCell() {
        if (additionalBrailleCell != null) {
            if (isUpperCaseMode) {
                setAdditionalCellPoints(true, true, false, false);
            } else if (isNumberMode && firstTime) {
                setAdditionalCellPoints(true, true, true, true);
                firstTime = false;
            }
        }
    }

    /**
     * Configura los puntos de la celda Braille adicional.
     *
     * @param p1 estado del punto 1
     * @param p2 estado del punto 2
     * @param p3 estado del punto 3
     * @param p4 estado del punto 4
     */
    private void setAdditionalCellPoints(boolean p1, boolean p2, boolean p3, boolean p4) {
        additionalBrailleCell.setPoint(1, p1);
        additionalBrailleCell.setPoint(5, p2);
        additionalBrailleCell.setPoint(4, p3);
        additionalBrailleCell.setPoint(3, p4);
    }

    /**
     * Activa/desactiva el modo mayúsculas.
     */
    private void upperCaseSelect() {
        isUpperCaseMode = !isUpperCaseMode;
        isNumberMode = false;
        firstTime = true;
        updateBraillePanelLayout();
        requestFocusInWindow();
    }

    /**
     * Activa/desactiva el modo números.
     */
    private void numberCaseSelect() {
        isNumberMode = !isNumberMode;
        isUpperCaseMode = false;
        updateBraillePanelLayout();
        requestFocusInWindow();
    }

    /**
     * Verifica si la traducción puede realizarse según los modos actuales y el
     * estado de la celda.
     *
     * @return true si la traducción puede continuar, false en caso contrario
     */
    private boolean canTranslate() {
        return !(isUpperCaseMode || isNumberMode) || !isBrailleCellEmpty(currentBrailleCell);
    }

    /**
     * Verifica si la celda Braille dada está vacía.
     *
     * @param cell la celda Braille a verificar
     * @return true si la celda está vacía, false en caso contrario
     */
    private boolean isBrailleCellEmpty(BrailleCell cell) {
        for (int i = 0; i < 6; i++) {
            if (cell.getPoint(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Traduce el texto según el modo de traducción actual.
     */
    private void translateText() {
        if (getTranslationMode()) {
            translator.setLanguage(new Braille());
            String spanishText = this.jTALenEntrada.getText();
            this.jTLenSalida.setText(translator.translate(spanishText));
        } else {
            if (canTranslate()) {
                translator.setLanguage(new Spanish());
                translateCurrentBrailleCell();
                currentBrailleCell.clearPoints();
                if (additionalBrailleCell != null) {
                    additionalBrailleCell.clearPoints();
                }
                updateBraillePanelLayout();
            } else {
                JOptionPane.showMessageDialog(this,
                        "El modo de mayúscula o número está activado, pero la celda de braille actual está vacía.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            handleFocusGainedOnBraille();
        }
    }

    /**
     * Maneja el foco ganado en el área de texto Braille.
     */
    private void handleFocusGainedOnBraille() {
        if (!getTranslationMode()) {
            requestFocusInWindow();
        }
    }

    /**
     * Utiliza el servicio de voz para reproducir el texto especificado en voz
     * alta.
     *
     * @param talkback El texto que se desea reproducir en voz alta.
     */
    private void speaker(String talkback) {
        voiceListener.speak(talkback);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPArchivo = new javax.swing.JPanel();
        jBClose = new javax.swing.JButton();
        jBDispose = new javax.swing.JButton();
        jBExportar = new javax.swing.JButton();
        jBImprimir = new javax.swing.JButton();
        jTATitulo = new javax.swing.JTextArea();
        jPMenu = new javax.swing.JPanel();
        jPTraductor = new javax.swing.JPanel();
        jLTitulo2 = new javax.swing.JLabel();
        jLEspañolEntrada = new javax.swing.JLabel();
        jBIntercambio = new javax.swing.JButton();
        jLBrailleEntrada = new javax.swing.JLabel();
        jTextArea1 = new javax.swing.JTextArea();
        jSeparator1 = new javax.swing.JSeparator();
        jPEdicion = new javax.swing.JPanel();
        jLTamFuente = new javax.swing.JLabel();
        jComboBoxTamañoLetra = new javax.swing.JComboBox<>();
        jLTamFuente1 = new javax.swing.JLabel();
        jLColor = new javax.swing.JLabel();
        jCheckBoxCursiva = new javax.swing.JCheckBox();
        jCheckBoxNegrita = new javax.swing.JCheckBox();
        jLTitulo1 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        JPHerramientas = new javax.swing.JPanel();
        jLTitulo3 = new javax.swing.JLabel();
        jBTraducir = new javax.swing.JButton();
        jBBorrar = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        JPBrailleMenu = new javax.swing.JPanel();
        jTextArea3 = new javax.swing.JTextArea();
        jCBMayusculas = new javax.swing.JCheckBox();
        jCBNumeros = new javax.swing.JCheckBox();
        jLTitulo4 = new javax.swing.JLabel();
        jPCuadratin2 = new javax.swing.JPanel();
        braillePanel = new javax.swing.JPanel();
        jPTraduccion = new javax.swing.JPanel();
        jPLenEntrada = new javax.swing.JPanel();
        jLLenEntrada = new javax.swing.JLabel();
        jPSpanishEntrance = new javax.swing.JPanel();
        jBMic = new javax.swing.JButton();
        jBSpeakerIn = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTALenEntrada = new javax.swing.JTextArea();
        jPLenSalida = new javax.swing.JPanel();
        jLLenSalida = new javax.swing.JLabel();
        jPSpanishOut = new javax.swing.JPanel();
        jBSpeakerOut = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTLenSalida = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Traductor");
        setBackground(new java.awt.Color(102, 102, 102));
        setUndecorated(true);
        setResizable(false);
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPArchivo.setBackground(new java.awt.Color(102, 102, 102));
        jPArchivo.setPreferredSize(new java.awt.Dimension(1341, 35));

        jBClose.setBackground(new java.awt.Color(102, 102, 102));
        jBClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/close32.png"))); // NOI18N
        jBClose.setBorderPainted(false);
        jBClose.setFocusable(false);
        jBClose.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBClose.setPreferredSize(new java.awt.Dimension(40, 40));
        jBClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBCloseActionPerformed(evt);
            }
        });

        jBDispose.setBackground(new java.awt.Color(102, 102, 102));
        jBDispose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/dispose32.png"))); // NOI18N
        jBDispose.setBorderPainted(false);
        jBDispose.setFocusable(false);
        jBDispose.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBDispose.setPreferredSize(new java.awt.Dimension(40, 40));
        jBDispose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBDisposeActionPerformed(evt);
            }
        });

        jBExportar.setBackground(new java.awt.Color(102, 102, 102));
        jBExportar.setForeground(new java.awt.Color(255, 255, 255));
        jBExportar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/export24.png"))); // NOI18N
        jBExportar.setText(" Exportar");
        jBExportar.setBorderPainted(false);
        jBExportar.setFocusable(false);
        jBExportar.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jBExportar.setIconTextGap(2);
        jBExportar.setPreferredSize(new java.awt.Dimension(40, 40));
        jBExportar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBExportar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBExportarActionPerformed(evt);
            }
        });

        jBImprimir.setBackground(new java.awt.Color(102, 102, 102));
        jBImprimir.setForeground(new java.awt.Color(255, 255, 255));
        jBImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/print24.png"))); // NOI18N
        jBImprimir.setText(" Imprimir");
        jBImprimir.setBorderPainted(false);
        jBImprimir.setFocusable(false);
        jBImprimir.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jBImprimir.setIconTextGap(2);
        jBImprimir.setPreferredSize(new java.awt.Dimension(40, 40));
        jBImprimir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBImprimirActionPerformed(evt);
            }
        });

        jTATitulo.setEditable(false);
        jTATitulo.setBackground(new java.awt.Color(204, 204, 204));
        jTATitulo.setColumns(20);
        jTATitulo.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jTATitulo.setForeground(new java.awt.Color(204, 204, 204));
        jTATitulo.setRows(5);
        jTATitulo.setText("Brailingo - Traductor: Braille -> Español");
        jTATitulo.setWrapStyleWord(true);
        jTATitulo.setAutoscrolls(false);
        jTATitulo.setBorder(null);
        jTATitulo.setFocusable(false);
        jTATitulo.setOpaque(false);

        javax.swing.GroupLayout jPArchivoLayout = new javax.swing.GroupLayout(jPArchivo);
        jPArchivo.setLayout(jPArchivoLayout);
        jPArchivoLayout.setHorizontalGroup(
            jPArchivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPArchivoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBExportar, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 417, Short.MAX_VALUE)
                .addComponent(jTATitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(224, 224, 224)
                .addComponent(jBDispose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jBClose, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPArchivoLayout.setVerticalGroup(
            jPArchivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPArchivoLayout.createSequentialGroup()
                .addGroup(jPArchivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPArchivoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jTATitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addComponent(jBClose, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBDispose, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBImprimir, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBExportar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jBExportar.getAccessibleContext().setAccessibleName("");
        jBExportar.getAccessibleContext().setAccessibleDescription("");
        jBImprimir.getAccessibleContext().setAccessibleName("");

        getContentPane().add(jPArchivo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1350, 40));

        jPMenu.setBackground(new java.awt.Color(153, 153, 153));

        jPTraductor.setBackground(new java.awt.Color(153, 153, 153));

        jLTitulo2.setForeground(new java.awt.Color(255, 255, 255));
        jLTitulo2.setText("            Seleccionar idioma");
        jLTitulo2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLEspañolEntrada.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLEspañolEntrada.setForeground(new java.awt.Color(255, 255, 255));
        jLEspañolEntrada.setText("Español");
        jLEspañolEntrada.setPreferredSize(new java.awt.Dimension(62, 22));

        jBIntercambio.setBackground(new java.awt.Color(242, 242, 242));
        jBIntercambio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/to32.png"))); // NOI18N
        jBIntercambio.setBorder(null);
        jBIntercambio.setBorderPainted(false);
        jBIntercambio.setContentAreaFilled(false);
        jBIntercambio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBIntercambio.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/img/switch32.png"))); // NOI18N
        jBIntercambio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBIntercambio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBIntercambioActionPerformed(evt);
            }
        });

        jLBrailleEntrada.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLBrailleEntrada.setForeground(new java.awt.Color(255, 255, 255));
        jLBrailleEntrada.setText("Braille");

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setForeground(new java.awt.Color(204, 204, 204));
        jTextArea1.setRows(5);
        jTextArea1.setText("Traducciendo de ");
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setAutoscrolls(false);
        jTextArea1.setBorder(null);
        jTextArea1.setFocusable(false);
        jTextArea1.setOpaque(false);

        javax.swing.GroupLayout jPTraductorLayout = new javax.swing.GroupLayout(jPTraductor);
        jPTraductor.setLayout(jPTraductorLayout);
        jPTraductorLayout.setHorizontalGroup(
            jPTraductorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPTraductorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPTraductorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLTitulo2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPTraductorLayout.createSequentialGroup()
                        .addComponent(jTextArea1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPTraductorLayout.createSequentialGroup()
                        .addComponent(jLEspañolEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jBIntercambio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLBrailleEntrada, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE))))
        );
        jPTraductorLayout.setVerticalGroup(
            jPTraductorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPTraductorLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTextArea1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPTraductorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLBrailleEntrada, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLEspañolEntrada, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBIntercambio))
                .addGap(24, 24, 24)
                .addComponent(jLTitulo2)
                .addContainerGap())
        );

        jSeparator1.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator1.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jPEdicion.setBackground(new java.awt.Color(153, 153, 153));

        jLTamFuente.setBackground(new java.awt.Color(255, 255, 255));
        jLTamFuente.setForeground(new java.awt.Color(255, 255, 255));
        jLTamFuente.setText("Tamaño Fuente:");

        jComboBoxTamañoLetra.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30" }));
        jComboBoxTamañoLetra.setFocusable(false);
        jComboBoxTamañoLetra.setOpaque(true);
        jComboBoxTamañoLetra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTamañoLetraActionPerformed(evt);
            }
        });

        jLTamFuente1.setBackground(new java.awt.Color(255, 255, 255));
        jLTamFuente1.setForeground(new java.awt.Color(255, 255, 255));
        jLTamFuente1.setText("Color de texto:");

        jLColor.setBackground(new java.awt.Color(51, 51, 51));
        jLColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        jLColor.setOpaque(true);
        jLColor.setPreferredSize(new java.awt.Dimension(32, 23));
        jLColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLColorMouseClicked(evt);
            }
        });

        jCheckBoxCursiva.setBackground(new java.awt.Color(255, 255, 255));
        jCheckBoxCursiva.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBoxCursiva.setText("Cursiva");
        jCheckBoxCursiva.setContentAreaFilled(false);
        jCheckBoxCursiva.setPreferredSize(new java.awt.Dimension(75, 20));
        jCheckBoxCursiva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxCursivaActionPerformed(evt);
            }
        });

        jCheckBoxNegrita.setBackground(new java.awt.Color(255, 255, 255));
        jCheckBoxNegrita.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBoxNegrita.setText("Negrita");
        jCheckBoxNegrita.setContentAreaFilled(false);
        jCheckBoxNegrita.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNegritaActionPerformed(evt);
            }
        });

        jLTitulo1.setForeground(new java.awt.Color(255, 255, 255));
        jLTitulo1.setText("                      Edición");
        jLTitulo1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPEdicionLayout = new javax.swing.GroupLayout(jPEdicion);
        jPEdicion.setLayout(jPEdicionLayout);
        jPEdicionLayout.setHorizontalGroup(
            jPEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPEdicionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLTitulo1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPEdicionLayout.createSequentialGroup()
                        .addGroup(jPEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jCheckBoxCursiva, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLTamFuente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLTamFuente1, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBoxTamañoLetra, 0, 0, Short.MAX_VALUE)
                            .addComponent(jCheckBoxNegrita, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPEdicionLayout.setVerticalGroup(
            jPEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPEdicionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jComboBoxTamañoLetra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLTamFuente, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLTamFuente1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxNegrita)
                    .addComponent(jCheckBoxCursiva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLTitulo1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        JPHerramientas.setBackground(new java.awt.Color(153, 153, 153));

        jLTitulo3.setForeground(new java.awt.Color(255, 255, 255));
        jLTitulo3.setText("        Herramientas");
        jLTitulo3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jBTraducir.setBackground(new java.awt.Color(204, 204, 204));
        jBTraducir.setText("Traducir");
        jBTraducir.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        jBTraducir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBTraducirActionPerformed(evt);
            }
        });

        jBBorrar.setBackground(new java.awt.Color(204, 204, 204));
        jBBorrar.setText("Borrar");
        jBBorrar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        jBBorrar.setPreferredSize(new java.awt.Dimension(34, 24));
        jBBorrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBBorrarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout JPHerramientasLayout = new javax.swing.GroupLayout(JPHerramientas);
        JPHerramientas.setLayout(JPHerramientasLayout);
        JPHerramientasLayout.setHorizontalGroup(
            JPHerramientasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPHerramientasLayout.createSequentialGroup()
                .addGroup(JPHerramientasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBTraducir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(JPHerramientasLayout.createSequentialGroup()
                        .addGroup(JPHerramientasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jBBorrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLTitulo3, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        JPHerramientasLayout.setVerticalGroup(
            JPHerramientasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, JPHerramientasLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jBTraducir, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBBorrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLTitulo3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSeparator3.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator3.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        JPBrailleMenu.setBackground(new java.awt.Color(153, 153, 153));

        jTextArea3.setEditable(false);
        jTextArea3.setColumns(20);
        jTextArea3.setForeground(new java.awt.Color(204, 204, 204));
        jTextArea3.setRows(5);
        jTextArea3.setText("Atajos");
        jTextArea3.setWrapStyleWord(true);
        jTextArea3.setAutoscrolls(false);
        jTextArea3.setBorder(null);
        jTextArea3.setFocusable(false);
        jTextArea3.setOpaque(false);

        jCBMayusculas.setBackground(new java.awt.Color(153, 153, 153));
        jCBMayusculas.setForeground(new java.awt.Color(255, 255, 255));
        jCBMayusculas.setText("Mayúsculas");
        jCBMayusculas.setOpaque(true);
        jCBMayusculas.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCBMayusculasItemStateChanged(evt);
            }
        });

        jCBNumeros.setBackground(new java.awt.Color(153, 153, 153));
        jCBNumeros.setForeground(new java.awt.Color(255, 255, 255));
        jCBNumeros.setText("Números ");
        jCBNumeros.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCBNumerosItemStateChanged(evt);
            }
        });

        jLTitulo4.setForeground(new java.awt.Color(255, 255, 255));
        jLTitulo4.setText("           Cuadratín");
        jLTitulo4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jPCuadratin2.setBackground(new java.awt.Color(153, 153, 153));
        jPCuadratin2.setPreferredSize(new java.awt.Dimension(66, 88));
        jPCuadratin2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPCuadratin2MouseClicked(evt);
            }
        });

        braillePanel.setBackground(new java.awt.Color(153, 153, 153));
        braillePanel.setOpaque(false);
        braillePanel.setPreferredSize(new java.awt.Dimension(54, 88));

        javax.swing.GroupLayout braillePanelLayout = new javax.swing.GroupLayout(braillePanel);
        braillePanel.setLayout(braillePanelLayout);
        braillePanelLayout.setHorizontalGroup(
            braillePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 54, Short.MAX_VALUE)
        );
        braillePanelLayout.setVerticalGroup(
            braillePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 88, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPCuadratin2Layout = new javax.swing.GroupLayout(jPCuadratin2);
        jPCuadratin2.setLayout(jPCuadratin2Layout);
        jPCuadratin2Layout.setHorizontalGroup(
            jPCuadratin2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPCuadratin2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(braillePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(123, Short.MAX_VALUE))
        );
        jPCuadratin2Layout.setVerticalGroup(
            jPCuadratin2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPCuadratin2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(braillePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout JPBrailleMenuLayout = new javax.swing.GroupLayout(JPBrailleMenu);
        JPBrailleMenu.setLayout(JPBrailleMenuLayout);
        JPBrailleMenuLayout.setHorizontalGroup(
            JPBrailleMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPBrailleMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(JPBrailleMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCBMayusculas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextArea3, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                    .addComponent(jCBNumeros, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                    .addComponent(jLTitulo4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPCuadratin2, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 6, Short.MAX_VALUE))
        );
        JPBrailleMenuLayout.setVerticalGroup(
            JPBrailleMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPBrailleMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(JPBrailleMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(JPBrailleMenuLayout.createSequentialGroup()
                        .addComponent(jTextArea3, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCBMayusculas, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCBNumeros, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLTitulo4, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPCuadratin2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPMenuLayout = new javax.swing.GroupLayout(jPMenu);
        jPMenu.setLayout(jPMenuLayout);
        jPMenuLayout.setHorizontalGroup(
            jPMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPMenuLayout.createSequentialGroup()
                .addComponent(jPTraductor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPEdicion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JPHerramientas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JPBrailleMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(463, Short.MAX_VALUE))
        );
        jPMenuLayout.setVerticalGroup(
            jPMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPMenuLayout.createSequentialGroup()
                .addGroup(jPMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(JPBrailleMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPTraductor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPEdicion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(JPHerramientas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(117, 117, 117))
            .addGroup(jPMenuLayout.createSequentialGroup()
                .addGroup(jPMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPMenuLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPMenuLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPMenuLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPMenu, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 1350, 120));

        jPLenEntrada.setPreferredSize(new java.awt.Dimension(663, 115));

        jLLenEntrada.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLLenEntrada.setText("Español");
        jLLenEntrada.setPreferredSize(new java.awt.Dimension(545, 60));

        jPSpanishEntrance.setOpaque(false);
        jPSpanishEntrance.setPreferredSize(new java.awt.Dimension(90, 50));

        jBMic.setBackground(new java.awt.Color(204, 204, 204));
        jBMic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/mic32.png"))); // NOI18N
        jBMic.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        jBMic.setPreferredSize(new java.awt.Dimension(32, 32));
        jBMic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBMicActionPerformed(evt);
            }
        });

        jBSpeakerIn.setBackground(new java.awt.Color(204, 204, 204));
        jBSpeakerIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/speaker32.png"))); // NOI18N
        jBSpeakerIn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        jBSpeakerIn.setPreferredSize(new java.awt.Dimension(32, 32));
        jBSpeakerIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBSpeakerInActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPSpanishEntranceLayout = new javax.swing.GroupLayout(jPSpanishEntrance);
        jPSpanishEntrance.setLayout(jPSpanishEntranceLayout);
        jPSpanishEntranceLayout.setHorizontalGroup(
            jPSpanishEntranceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPSpanishEntranceLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jBSpeakerIn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBMic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPSpanishEntranceLayout.setVerticalGroup(
            jPSpanishEntranceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPSpanishEntranceLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPSpanishEntranceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBMic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBSpeakerIn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTALenEntrada.setColumns(20);
        jTALenEntrada.setRows(5);
        jTALenEntrada.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTALenEntradaKeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(jTALenEntrada);

        javax.swing.GroupLayout jPLenEntradaLayout = new javax.swing.GroupLayout(jPLenEntrada);
        jPLenEntrada.setLayout(jPLenEntradaLayout);
        jPLenEntradaLayout.setHorizontalGroup(
            jPLenEntradaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPLenEntradaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPLenEntradaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(jPLenEntradaLayout.createSequentialGroup()
                        .addComponent(jLLenEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPSpanishEntrance, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPLenEntradaLayout.setVerticalGroup(
            jPLenEntradaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPLenEntradaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPLenEntradaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPSpanishEntrance, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                    .addComponent(jLLenEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPLenSalida.setPreferredSize(new java.awt.Dimension(663, 115));

        jLLenSalida.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLLenSalida.setText("Braille");
        jLLenSalida.setPreferredSize(new java.awt.Dimension(545, 60));

        jPSpanishOut.setOpaque(false);
        jPSpanishOut.setPreferredSize(new java.awt.Dimension(90, 50));

        jBSpeakerOut.setBackground(new java.awt.Color(204, 204, 204));
        jBSpeakerOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/speaker32.png"))); // NOI18N
        jBSpeakerOut.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        jBSpeakerOut.setPreferredSize(new java.awt.Dimension(32, 32));
        jBSpeakerOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBSpeakerOutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPSpanishOutLayout = new javax.swing.GroupLayout(jPSpanishOut);
        jPSpanishOut.setLayout(jPSpanishOutLayout);
        jPSpanishOutLayout.setHorizontalGroup(
            jPSpanishOutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPSpanishOutLayout.createSequentialGroup()
                .addContainerGap(53, Short.MAX_VALUE)
                .addComponent(jBSpeakerOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );
        jPSpanishOutLayout.setVerticalGroup(
            jPSpanishOutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPSpanishOutLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jBSpeakerOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jTLenSalida.setEditable(false);
        jTLenSalida.setBackground(new java.awt.Color(255, 255, 255));
        jTLenSalida.setColumns(20);
        jTLenSalida.setRows(5);
        jScrollPane1.setViewportView(jTLenSalida);

        javax.swing.GroupLayout jPLenSalidaLayout = new javax.swing.GroupLayout(jPLenSalida);
        jPLenSalida.setLayout(jPLenSalidaLayout);
        jPLenSalidaLayout.setHorizontalGroup(
            jPLenSalidaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPLenSalidaLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLLenSalida, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPSpanishOut, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPLenSalidaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPLenSalidaLayout.setVerticalGroup(
            jPLenSalidaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPLenSalidaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPLenSalidaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLLenSalida, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPSpanishOut, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPTraduccionLayout = new javax.swing.GroupLayout(jPTraduccion);
        jPTraduccion.setLayout(jPTraduccionLayout);
        jPTraduccionLayout.setHorizontalGroup(
            jPTraduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPTraduccionLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jPLenEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPLenSalida, javax.swing.GroupLayout.PREFERRED_SIZE, 657, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPTraduccionLayout.setVerticalGroup(
            jPTraduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPTraduccionLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPTraduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPLenEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, 620, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPLenSalida, javax.swing.GroupLayout.PREFERRED_SIZE, 620, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        getContentPane().add(jPTraduccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 160, 1350, 640));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        int posX = evt.getXOnScreen();
        int posY = evt.getYOnScreen();
        this.setLocation(posX - this.x, posY - this.y);
    }//GEN-LAST:event_formMouseDragged

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        this.x = evt.getX();
        this.y = evt.getY();
    }//GEN-LAST:event_formMousePressed

    private void jBCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBCloseActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jBCloseActionPerformed

    private void jBDisposeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBDisposeActionPerformed
        this.setState(ICONIFIED);
    }//GEN-LAST:event_jBDisposeActionPerformed

    private void jLColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLColorMouseClicked
        Color color = JColorChooser.showDialog(this, "Seleccionar color de texto", textFormat.getSelectedColor());
        if (color != null) {
            textFormat.setSelectedColor(color);
            this.jLColor.setBackground(color);
            textFormat.applyConditionalFormatting(jTALenEntrada, jTLenSalida);
        }
    }//GEN-LAST:event_jLColorMouseClicked

    private void jBBorrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBBorrarActionPerformed
        clearTextFields();
        totalBrailleTranslation = "";
    }//GEN-LAST:event_jBBorrarActionPerformed

    private void jBIntercambioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBIntercambioActionPerformed
        switchTranslationMode();
        resetFormattingOptions();
    }//GEN-LAST:event_jBIntercambioActionPerformed

    private void jCBMayusculasItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCBMayusculasItemStateChanged
        requestFocusInWindow();
        if (jCBMayusculas.isSelected()) {
            upperCaseSelect();
            this.jCBNumeros.setSelected(false);
        } else {
            isUpperCaseMode = false;
            updateBraillePanelLayout();
        }
    }//GEN-LAST:event_jCBMayusculasItemStateChanged

    private void jCBNumerosItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCBNumerosItemStateChanged
        requestFocusInWindow();
        if (jCBNumeros.isSelected()) {
            numberCaseSelect();
            this.jCBMayusculas.setSelected(false);
        } else {
            isNumberMode = false;
            firstTime = true;
            updateBraillePanelLayout();
        }
    }//GEN-LAST:event_jCBNumerosItemStateChanged

    private void jPCuadratin2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPCuadratin2MouseClicked
        handleFocusGainedOnBraille();
    }//GEN-LAST:event_jPCuadratin2MouseClicked

    private void jBTraducirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBTraducirActionPerformed
        translateText();
    }//GEN-LAST:event_jBTraducirActionPerformed

    private void jBSpeakerInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBSpeakerInActionPerformed
        String talkback = this.jTALenEntrada.getText();
        speaker(talkback);
    }//GEN-LAST:event_jBSpeakerInActionPerformed

    private void jBSpeakerOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBSpeakerOutActionPerformed
        String talkback = this.jTLenSalida.getText();
        speaker(talkback);
    }//GEN-LAST:event_jBSpeakerOutActionPerformed

    private void jBMicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBMicActionPerformed
        if (flag) {
            this.jBMic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/mic32.png")));
            //Inicia el reconocimiento de voz
            Thread voiceThread = new Thread(() -> voiceListener.startListening(this.jTALenEntrada));
            voiceThread.start();
            flag = false;
        } else {
            this.jBMic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/nomic32.png")));
            //Detiene el reconocimiento de voz
            if (voiceListener != null) {
                voiceListener.stopListening();
            }
            flag = true;
        }

    }//GEN-LAST:event_jBMicActionPerformed

    private void jBImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBImprimirActionPerformed
        // TODO add your handling code here:
        if (jTLenSalida.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Error al imprimir: " + "No existe texto traducido para imprimir", "Error de Impresión", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean isBraille = "Braille".equals(jLBrailleEntrada.getText());
        JFPreview exportFrame = new JFPreview(jTLenSalida.getText(), textFormat.getFontSize(), textFormat.getSelectedColor(), isBraille);
        exportFrame.setVisible(true);
    }//GEN-LAST:event_jBImprimirActionPerformed

    private void jBExportarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBExportarActionPerformed
        JFExport exportFrame = new JFExport(jTLenSalida);
        exportFrame.setLocationRelativeTo(null);
        exportFrame.setVisible(true);
    }//GEN-LAST:event_jBExportarActionPerformed

    private void jCheckBoxCursivaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxCursivaActionPerformed
        if (textFormat.isSpanishToBraille()) {
            textFormat.setCursivaSalida(jCheckBoxCursiva.isSelected());
        } else {
            textFormat.setCursivaEntrada(jCheckBoxCursiva.isSelected());
        }
        textFormat.applyConditionalFormatting(jTALenEntrada, jTLenSalida);
    }//GEN-LAST:event_jCheckBoxCursivaActionPerformed

    private void jCheckBoxNegritaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNegritaActionPerformed
        if (textFormat.isSpanishToBraille()) {
            textFormat.setNegritaSalida(jCheckBoxNegrita.isSelected());
        } else {
            textFormat.setNegritaEntrada(jCheckBoxNegrita.isSelected());
        }
        textFormat.applyConditionalFormatting(jTALenEntrada, jTLenSalida);
    }//GEN-LAST:event_jCheckBoxNegritaActionPerformed

    private void jComboBoxTamañoLetraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTamañoLetraActionPerformed
        int fontSize = Integer.parseInt((String) jComboBoxTamañoLetra.getSelectedItem());
        textFormat.setFontSize(fontSize);
        textFormat.applyConditionalFormatting(jTALenEntrada, jTLenSalida);
    }//GEN-LAST:event_jComboBoxTamañoLetraActionPerformed

    private void jTALenEntradaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTALenEntradaKeyReleased
        translateText();
    }//GEN-LAST:event_jTALenEntradaKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel JPBrailleMenu;
    private javax.swing.JPanel JPHerramientas;
    private javax.swing.JPanel braillePanel;
    private javax.swing.JButton jBBorrar;
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBDispose;
    private javax.swing.JButton jBExportar;
    private javax.swing.JButton jBImprimir;
    private javax.swing.JButton jBIntercambio;
    private javax.swing.JButton jBMic;
    private javax.swing.JButton jBSpeakerIn;
    private javax.swing.JButton jBSpeakerOut;
    private javax.swing.JButton jBTraducir;
    private javax.swing.JCheckBox jCBMayusculas;
    private javax.swing.JCheckBox jCBNumeros;
    private javax.swing.JCheckBox jCheckBoxCursiva;
    private javax.swing.JCheckBox jCheckBoxNegrita;
    private javax.swing.JComboBox<String> jComboBoxTamañoLetra;
    private javax.swing.JLabel jLBrailleEntrada;
    private javax.swing.JLabel jLColor;
    private javax.swing.JLabel jLEspañolEntrada;
    private javax.swing.JLabel jLLenEntrada;
    private javax.swing.JLabel jLLenSalida;
    private javax.swing.JLabel jLTamFuente;
    private javax.swing.JLabel jLTamFuente1;
    private javax.swing.JLabel jLTitulo1;
    private javax.swing.JLabel jLTitulo2;
    private javax.swing.JLabel jLTitulo3;
    private javax.swing.JLabel jLTitulo4;
    private javax.swing.JPanel jPArchivo;
    private javax.swing.JPanel jPCuadratin2;
    private javax.swing.JPanel jPEdicion;
    private javax.swing.JPanel jPLenEntrada;
    private javax.swing.JPanel jPLenSalida;
    private javax.swing.JPanel jPMenu;
    private javax.swing.JPanel jPSpanishEntrance;
    private javax.swing.JPanel jPSpanishOut;
    private javax.swing.JPanel jPTraduccion;
    private javax.swing.JPanel jPTraductor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTextArea jTALenEntrada;
    private javax.swing.JTextArea jTATitulo;
    private javax.swing.JTextArea jTLenSalida;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea3;
    // End of variables declaration//GEN-END:variables
}
