import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nadir on 02.04.2017.
 */
public class ChatGraphics extends JFrame {
    private final String TITLE = "NexTex";
    private final int FRM_WIDTH = 800;
    private final int FRM_HEIGHT = 490;

    private JPanel panelMain;
    private JPanel panelChat;
    private JPanel panelInput;
    private JButton sendBut;
    JEditorPane msgOutputEP;
    private HTMLDocument doc;
    private HTMLEditorKit edit;
    JTextField msgInputTF;
    private JList<String> listOfUsers;
    private DefaultListModel<String> listOfUsersModel;
    boolean autoscroll = true;


    private Color chatColor = new Color(30, 30, 30);
    private Color secondaryColor = new Color(65, 65, 65);
    private Color selectionColor = new Color(30, 100, 255);

    private boolean waitingForNickname = true;

    ChatGraphics() {
        draw();
        setVisible(true);
        handleInserts();
        log("<html><font face='arial' color='green'>/help</font><font face='arial' color='white'> - помощь</font><br><font face='arial' color='yellow'>Введите ваш ник:</font></html>");
    }

    private void draw() {
        // Window presets
        setTitle(TITLE);
        setSize(FRM_WIDTH, FRM_HEIGHT);
        setMinimumSize(new Dimension(FRM_WIDTH, FRM_HEIGHT));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        UIManager.put("ToolTip.background", new ColorUIResource(secondaryColor));
        UIManager.put("ToolTip.foreground", new ColorUIResource(Color.white));
        UIManager.put("ToolTip.border", new ColorUIResource(Color.black));

        //Icon
        String imagePath = "res/NTLogo2m.png";
        InputStream imgStream = ChatGraphics.class.getResourceAsStream(imagePath);
        BufferedImage icon = null;
        try {
            icon = ImageIO.read(imgStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setIconImage(icon);

        // Main panel for holding other panels
        panelMain = new JPanel(new BorderLayout());
        panelMain.setVisible(false);
        panelMain.setForeground(Color.white);
        panelMain.setSize(FRM_WIDTH, FRM_HEIGHT);
        panelMain.setBackground(chatColor);
        getContentPane().add(panelMain);

        //Panel for holding chat and list of users
        panelChat = new JPanel(new BorderLayout());
        panelChat.setVisible(false);
        panelChat.setBackground(chatColor);
        panelChat.setPreferredSize(new Dimension(800, 0));

        //Panel for holding send button and textField
        panelInput = new JPanel(new BorderLayout());
        panelInput.setVisible(false);
        panelInput.setBackground(chatColor);
        panelInput.setPreferredSize(new Dimension(800, 20));

        // Components' presets
        msgOutputEP = new JTextPane();
        msgOutputEP.setContentType( "text/html" );
        msgOutputEP.setText("");
        msgOutputEP.setBackground(chatColor);
        msgOutputEP.setForeground(Color.white);
        msgOutputEP.setAutoscrolls(true);
        msgOutputEP.setEditable(false);
        msgOutputEP.setSelectionColor(selectionColor);
        msgOutputEP.setSelectedTextColor(Color.white);
        JScrollPane msgOutputEPSP = new JScrollPane(msgOutputEP);
        msgOutputEPSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        msgOutputEPSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        msgOutputEPSP.setAutoscrolls(false);
        msgOutputEPSP.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        msgOutputEPSP.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        msgOutputEPSP.getVerticalScrollBar().setUI(new BasicScrollBarUI()
        {
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0, 0));
                jbutton.setMinimumSize(new Dimension(0, 0));
                jbutton.setMaximumSize(new Dimension(0, 0));
                return jbutton;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D graphics2D = (Graphics2D) g.create();
                graphics2D.setColor(secondaryColor);
                graphics2D.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
                graphics2D.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D graphics2D = (Graphics2D) g.create();
                graphics2D.setColor(new Color(36, 36, 36));
                graphics2D.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
                graphics2D.dispose();
            }
        });
        doc = (HTMLDocument) msgOutputEP.getDocument();
        edit = (HTMLEditorKit) msgOutputEP.getEditorKit();
        msgOutputEP.setDocument(doc);
        ((DefaultCaret) msgOutputEP.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        msgInputTF = new JTextField(62);
        msgInputTF.setBackground(secondaryColor);
        msgInputTF.setForeground(Color.white);
        msgInputTF.setToolTipText("Напишите сообщение или команду и нажмите ➥ или ENTER");
        msgInputTF.setCaretColor(Color.white);
        msgInputTF.setSelectionColor(selectionColor);
        msgInputTF.setSelectedTextColor(Color.white);
        msgInputTF.setBorder(BorderFactory.createLineBorder(Color.black, 1));


        sendBut = new JButton("➥");
        sendBut.setBackground(secondaryColor);
        sendBut.setForeground(Color.white);
        sendBut.setFocusPainted(false);
        sendBut.setToolTipText("Отправить сообщение");

        listOfUsersModel = new DefaultListModel<>();
        listOfUsers = new JList<>(listOfUsersModel);
        listOfUsers.setSelectionModel(new NoSelectionModel());
        listOfUsers.setBackground(chatColor);
        listOfUsers.setForeground(Color.white);
        listOfUsers.setToolTipText("Список подключенных пользователей");
        listOfUsers.setSelectionBackground(selectionColor);
        listOfUsers.setSelectionForeground(Color.white);
        listOfUsers.setDragEnabled(false);
        listOfUsers.setLayoutOrientation(JList.VERTICAL);
        listOfUsers.setAutoscrolls(true);
        listOfUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listOfUsersSP = new JScrollPane(listOfUsers);
        listOfUsersSP.setPreferredSize(new Dimension(190, 403));
        listOfUsersSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listOfUsersSP.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        listOfUsersSP.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        listOfUsersSP.getVerticalScrollBar().setUI(new BasicScrollBarUI()
        {
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0, 0));
                jbutton.setMinimumSize(new Dimension(0, 0));
                jbutton.setMaximumSize(new Dimension(0, 0));
                return jbutton;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D graphics2D = (Graphics2D) g.create();
                graphics2D.setColor(secondaryColor);
                graphics2D.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
                graphics2D.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D graphics2D = (Graphics2D) g.create();
                graphics2D.setColor(new Color(36, 36, 36));
                graphics2D.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
                graphics2D.dispose();
            }
        });

        // Adding components to panels
        panelMain.add(panelChat, BorderLayout.CENTER);
        panelMain.add(panelInput, BorderLayout.SOUTH);

        panelChat.add(msgOutputEPSP, BorderLayout.CENTER);
        panelChat.add(listOfUsersSP, BorderLayout.EAST);

        panelInput.add(msgInputTF, BorderLayout.CENTER);
        panelInput.add(sendBut, BorderLayout.EAST);

        // Enabling panels
        panelChat.setVisible(true);
        panelInput.setVisible(true);
        panelMain.setVisible(true);

        // Set input area to be focused at startup
        addWindowListener( new WindowAdapter() {
            public void windowOpened( WindowEvent e ){
                msgInputTF.requestFocus();
            }
        });

        // Catch on exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(Main.server != null && Main.ownServer && Main.server.working) {
                    Main.server.stop();
                }
                else {
                    Main.selfClient.disconnect();
                }
                super.windowClosing(e);
            }
        });
    }

    private void handleInserts() {
        sendBut.addActionListener(al -> sendButtonPressed());

        msgInputTF.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER)
                    sendButtonPressed();
            }
        });

        msgOutputEP.addHyperlinkListener(hl -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(hl.getEventType())) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(hl.getURL().toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendButtonPressed(){
        if(waitingForNickname){
            if(!Utils.isEmpty(msgInputTF.getText()) && !Utils.containsChars(msgInputTF.getText(), new String[] {"<", ">", ":", ";"}) && msgInputTF.getText().length() <= 16) {
                Main.selfClient.setNickname(msgInputTF.getText());
                waitingForNickname = false;
                log("<html><font face='arial' color='yellow'>Ваш ник теперь " + Main.selfClient.getNickname() + "</font></html>");
                Main.selfClient.automaticallyConnect();
            }
            else {
                log("<html><font face='arial' color='red'>Некорректный ник, введите заново:</font></html>");
            }
        }
        else if (!Utils.isEmpty(msgInputTF.getText()) && !ChatCommands.isCommand(msgInputTF.getText())) {
            Main.selfClient.sendMessage(msgInputTF.getText());
        }
        msgInputTF.setText(""); // Clear input line
    }

    void log(String message) {
        try {
            edit.insertHTML(doc, doc.getLength(), message, 0, 0, null);
            if(autoscroll)
                msgOutputEP.setCaretPosition(msgOutputEP.getDocument().getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void changeList(String data) {
        String[] elements = data.split(":");
        listOfUsersModel.removeAllElements();
        if(elements.length > 1){
            for (int i = 1; i < elements.length; i++) {
                listOfUsersModel.addElement(elements[i]);
            }
        }
    }

    void clearList(){
        listOfUsersModel.clear();
    }

    void clearChat() {
        msgOutputEP.setText("");
    }
}
