package com.softtech.traductorbraille.GUI;

import com.softtech.traductorbraille.logic.Translator;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

/**
 *
 * JFTranslatorGUI es la interfaz gráfica que permite al usuario realizar
 traducciones de español-braille y braille-español.
 *
 * @since 1.0
 * @version 2.0
 * @author SoftTech
 */
public class JFTranslatorGUI extends javax.swing.JFrame {

    private Translator translator = new Translator();
    
    private int x;
    private int y;
    private Color selectedColor = Color.BLACK;

    private BrailleCell currentBrailleCell;
    private BrailleCell additionalBrailleCell;

    private boolean isUpperCaseMode = false;
    private boolean isNumberMode = false;

    private static final int[] BRAILLE_INDEX_MAPPING = {0, 2, 4, 1, 3, 5};
    private static final int[] KEY_EVENT_MAPPING = {
        KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD3,
        KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6
    };

    /**
     * Creates new form JFTranslatorGUI
     */
    public JFTranslatorGUI() {
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
        this.jTLenSalida.setLineWrap(true);
        this.jSeparator3.setVisible(false);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyInput(e.getKeyCode());
            }
        });
        currentBrailleCell = new BrailleCell();
        braillePanel.add(currentBrailleCell);
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

        if (isSpanishToBraille) {
            title = "Brailingo - Traductor: Braille -> Español";
            brailleLabel = "Español";
            spanishLabel = "Braille";
            requestFocusInWindow();
        } else {
            title = "Brailingo - Traductor: Español -> Braille";
            brailleLabel = "Braille";
            spanishLabel = "Español";
        }
        this.jTALenEntrada.setEditable(!isSpanishToBraille);
        this.JPBrailleMenu.setVisible(isSpanishToBraille);
        this.jSeparator3.setVisible(isSpanishToBraille);
        this.jTATitulo.setText(title);
        this.jLBrailleEntrada.setText(brailleLabel);
        this.jLEspañolEntrada.setText(spanishLabel);
        this.jPLenSalida.setBorder(BorderFactory.createTitledBorder(brailleLabel));
        this.jPLenEntrada.setBorder(BorderFactory.createTitledBorder(spanishLabel));
    }

    /**
     * Cambia el modo de traducción entre Español a Braille y Braille a Español.
     */
    private void switchTranslationMode() {
        setTranslationMode(getTranslationMode());
        clearTextFields();
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
        } else if (keyCode == KeyEvent.VK_SPACE) {
            addSpace();
        }
    }

    /**
     * Agrega un espacio en la traducción Braille y los campos de texto correspondientes.
     */
    private void addSpace() {
        this.jTALenEntrada.append(" ");
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
     * Traduce la celda Braille actual a texto, usando la clase Translator y coloca los resultados en el formulario.
     * <p>
     * Este método realiza los siguientes pasos:
     * <ol>
     *     <li>Obtiene los puntos activados en la celda Braille actual y, si está presente, en una celda Braille adicional.</li>
     *     <li>Combina los textos Braille obtenidos de ambas celdas.</li>
     *     <li>Determina si el modo de número está activado y realiza la traducción correspondiente, ya sea traduciendo solo números o
     *         traduciendo normalmente.</li>
     *     <li>Actualiza la traducción total del Braille con el texto recién traducido.</li>
     *     <li>Muestra un mensaje de error si la combinación de puntos no existe en el diccionario de traducción.</li>
     *     <li>Convierte el texto Braille a Unicode y lo agrega al campo de texto Braille del formulario.</li>
     *     <li>Si hay una celda Braille adicional, limpia sus puntos activados.</li>
     *     <li>Limpia los puntos activados de la celda Braille actual y repinta el panel Braille.</li>
     *     <li>Actualiza el diseño del panel Braille si el modo de mayúsculas o el modo de número están activados.</li>
     * </ol>
     */
    private void translateCurrentBrailleCell() {
        StringBuilder cellText = new StringBuilder();
        StringBuilder targetCellText = new StringBuilder();

        for (int i = 0; i < BRAILLE_INDEX_MAPPING.length; i++) {
            if (currentBrailleCell.getPoint(BRAILLE_INDEX_MAPPING[i])) {
                cellText.append(i + 1);
            }
        }

        if (additionalBrailleCell != null) {
            for (int i = 0; i < BRAILLE_INDEX_MAPPING.length; i++) {
                if (additionalBrailleCell.getPoint(BRAILLE_INDEX_MAPPING[i])) {
                    targetCellText.append(i + 1);
                }
            }
        }

        String brailleText = cellText.toString();
        String targetBrailleText = targetCellText.toString();

        String combinedBrailleText = (targetBrailleText.isEmpty() ? "" : targetBrailleText + " ") + brailleText;
        String translation;

        if (isNumberMode) {
            // Si está en modo de número, solo traducir números
            translation = translator.translateToSpanishNumbersOnly(combinedBrailleText);
        } else {
            // Si no está en modo de número, traducir normalmente
            translation = translator.translateToSpanish(combinedBrailleText);
        }

        if (translation.equals("?")) {
            JOptionPane.showMessageDialog(this,
                    "La traducción para la combinación ingresada no existe en el diccionario.",
                    "Error de Traducción", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (additionalBrailleCell != null) {
            String targetBrailleUnicode = translator.brailleToUnicode(targetBrailleText);
            this.jTLenSalida.append(targetBrailleUnicode);
        }

        this.jTALenEntrada.append(translation);
        String brailleUnicode = translator.brailleToUnicode(brailleText);
        this.jTLenSalida.append(brailleUnicode);

        this.jTLenSalida.append(" ");

        if (additionalBrailleCell != null) {
            additionalBrailleCell.clearPoints();
        }

        currentBrailleCell.clearPoints();
        braillePanel.repaint();

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
            } else if (isNumberMode) {
                setAdditionalCellPoints(true, true, true, true);
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
     * Verifica si la traducción puede realizarse según los modos actuales y el estado de la celda.
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
            String spanishText = this.jTALenEntrada.getText();
            this.jTLenSalida.setText(translator.translateToBraille(spanishText));
        } else {
            if (canTranslate()) {
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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPArchivo = new javax.swing.JPanel();
        jBClose = new javax.swing.JButton();
        jBDispose = new javax.swing.JButton();
        jBExportar = new javax.swing.JButton();
        jBImportar = new javax.swing.JButton();
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
        jCheckBoxCursiva1 = new javax.swing.JCheckBox();
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jTALenEntrada = new javax.swing.JTextArea();
        jPLenSalida = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
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

        jBImportar.setBackground(new java.awt.Color(102, 102, 102));
        jBImportar.setForeground(new java.awt.Color(255, 255, 255));
        jBImportar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/import24.png"))); // NOI18N
        jBImportar.setText(" Importar");
        jBImportar.setBorderPainted(false);
        jBImportar.setFocusable(false);
        jBImportar.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jBImportar.setIconTextGap(2);
        jBImportar.setPreferredSize(new java.awt.Dimension(40, 40));
        jBImportar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

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
                .addComponent(jBImportar, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 296, Short.MAX_VALUE)
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
                    .addComponent(jBImportar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBImprimir, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBExportar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jBExportar.getAccessibleContext().setAccessibleName("");
        jBExportar.getAccessibleContext().setAccessibleDescription("");
        jBImportar.getAccessibleContext().setAccessibleName("110");
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

        jCheckBoxNegrita.setBackground(new java.awt.Color(255, 255, 255));
        jCheckBoxNegrita.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBoxNegrita.setText("Negrita");
        jCheckBoxNegrita.setContentAreaFilled(false);

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

        jCheckBoxCursiva1.setBackground(new java.awt.Color(255, 255, 255));
        jCheckBoxCursiva1.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBoxCursiva1.setText("TalkBack");
        jCheckBoxCursiva1.setContentAreaFilled(false);
        jCheckBoxCursiva1.setPreferredSize(new java.awt.Dimension(75, 24));

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
                            .addComponent(jCheckBoxCursiva1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                            .addComponent(jLTitulo3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxCursiva1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
        jCBMayusculas.setText("Mayúsculas(Ctrl-)");
        jCBMayusculas.setOpaque(true);
        jCBMayusculas.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCBMayusculasItemStateChanged(evt);
            }
        });

        jCBNumeros.setBackground(new java.awt.Color(153, 153, 153));
        jCBNumeros.setForeground(new java.awt.Color(255, 255, 255));
        jCBNumeros.setText("Números (Ctrl+)");
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

        jPLenEntrada.setBorder(javax.swing.BorderFactory.createTitledBorder("Español"));
        jPLenEntrada.setPreferredSize(new java.awt.Dimension(663, 115));

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTALenEntrada.setColumns(20);
        jTALenEntrada.setRows(5);
        jTALenEntrada.setWrapStyleWord(true);
        jTALenEntrada.setMinimumSize(new java.awt.Dimension(235, 85));
        jTALenEntrada.setPreferredSize(new java.awt.Dimension(235, 85));
        jTALenEntrada.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTALenEntradaFocusGained(evt);
            }
        });
        jScrollPane1.setViewportView(jTALenEntrada);

        javax.swing.GroupLayout jPLenEntradaLayout = new javax.swing.GroupLayout(jPLenEntrada);
        jPLenEntrada.setLayout(jPLenEntradaLayout);
        jPLenEntradaLayout.setHorizontalGroup(
            jPLenEntradaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPLenEntradaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPLenEntradaLayout.setVerticalGroup(
            jPLenEntradaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPLenEntradaLayout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        jPLenSalida.setBorder(javax.swing.BorderFactory.createTitledBorder("Braille"));
        jPLenSalida.setPreferredSize(new java.awt.Dimension(663, 115));

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTLenSalida.setEditable(false);
        jTLenSalida.setBackground(new java.awt.Color(255, 255, 255));
        jTLenSalida.setColumns(20);
        jTLenSalida.setRows(5);
        jTLenSalida.setMinimumSize(new java.awt.Dimension(235, 85));
        jTLenSalida.setPreferredSize(new java.awt.Dimension(235, 85));
        jScrollPane2.setViewportView(jTLenSalida);

        javax.swing.GroupLayout jPLenSalidaLayout = new javax.swing.GroupLayout(jPLenSalida);
        jPLenSalida.setLayout(jPLenSalidaLayout);
        jPLenSalidaLayout.setHorizontalGroup(
            jPLenSalidaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPLenSalidaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPLenSalidaLayout.setVerticalGroup(
            jPLenSalidaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPLenSalidaLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPTraduccionLayout = new javax.swing.GroupLayout(jPTraduccion);
        jPTraduccion.setLayout(jPTraduccionLayout);
        jPTraduccionLayout.setHorizontalGroup(
            jPTraduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPTraduccionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPLenEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPLenSalida, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        jPTraduccionLayout.setVerticalGroup(
            jPTraduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPTraduccionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPTraduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPLenSalida, javax.swing.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
                    .addComponent(jPLenEntrada, javax.swing.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE))
                .addContainerGap(15, Short.MAX_VALUE))
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
        Color color = JColorChooser.showDialog(this, "Seleccionar color de texto", selectedColor);
        if (color != null) {
            this.selectedColor = color;
            this.jLColor.setBackground(color);
        }
    }//GEN-LAST:event_jLColorMouseClicked

    private void jBBorrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBBorrarActionPerformed
        clearTextFields();
    }//GEN-LAST:event_jBBorrarActionPerformed

    private void jBIntercambioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBIntercambioActionPerformed
        switchTranslationMode();
    }//GEN-LAST:event_jBIntercambioActionPerformed

    private void jTALenEntradaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTALenEntradaFocusGained
        if (!getTranslationMode()) {
            requestFocusInWindow();
        }
    }//GEN-LAST:event_jTALenEntradaFocusGained

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
            updateBraillePanelLayout();
        }
    }//GEN-LAST:event_jCBNumerosItemStateChanged

    private void jPCuadratin2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPCuadratin2MouseClicked
        handleFocusGainedOnBraille();
    }//GEN-LAST:event_jPCuadratin2MouseClicked

    private void jBTraducirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBTraducirActionPerformed
        translateText();
    }//GEN-LAST:event_jBTraducirActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel JPBrailleMenu;
    private javax.swing.JPanel JPHerramientas;
    private javax.swing.JPanel braillePanel;
    private javax.swing.JButton jBBorrar;
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBDispose;
    private javax.swing.JButton jBExportar;
    private javax.swing.JButton jBImportar;
    private javax.swing.JButton jBImprimir;
    private javax.swing.JButton jBIntercambio;
    private javax.swing.JButton jBTraducir;
    private javax.swing.JCheckBox jCBMayusculas;
    private javax.swing.JCheckBox jCBNumeros;
    private javax.swing.JCheckBox jCheckBoxCursiva;
    private javax.swing.JCheckBox jCheckBoxCursiva1;
    private javax.swing.JCheckBox jCheckBoxNegrita;
    private javax.swing.JComboBox<String> jComboBoxTamañoLetra;
    private javax.swing.JLabel jLBrailleEntrada;
    private javax.swing.JLabel jLColor;
    private javax.swing.JLabel jLEspañolEntrada;
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
    private javax.swing.JPanel jPTraduccion;
    private javax.swing.JPanel jPTraductor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
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
