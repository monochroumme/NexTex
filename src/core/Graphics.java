package core;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Created by nadir on 12.03.2017.
 **/

public class Graphics extends JFrame {
    private final String TITLE = "NexTex";
    private final int FRM_WIDTH = 800;
    private final int FRM_HEIGHT = 490;

    private JPanel panelMain;
    private JPanel panelChat;
    private JPanel panelInput;
    private JButton sendBut;
    private JEditorPane msgOutputEP;
    private HTMLDocument doc;
    private HTMLEditorKit edit;
    public JTextField msgInputTF;
    public JList listOfUsers;

    private Color chatColor = new Color(30, 30, 30);
    private Color secondaryColor = new Color(65, 65, 65);
    private Color selectionColor = new Color(30, 100, 255);

    public Graphics() {
        draw();
        setVisible(true);
        handleInserts();

        // setting doc to edit output
        log("<html><font face='arial' color='green'>/help</font><font face='arial' color='white'> - помощь</font>\n</html>", false, false, false);
    }

    public void draw() {
        // Window presets
        setTitle(TITLE);
        setSize(FRM_WIDTH, FRM_HEIGHT);
        setMinimumSize(new Dimension(FRM_WIDTH, FRM_HEIGHT));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // TODO отправлять серверу инфу о выходе при закрытии проги
        setLocationRelativeTo(null);
        UIManager.put("ToolTip.background", new ColorUIResource(secondaryColor));
        UIManager.put("ToolTip.foreground", new ColorUIResource(Color.white));
        UIManager.put("ToolTip.border", new ColorUIResource(Color.black));

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
        msgOutputEP = new JTextPane(); // 27, 67
        msgOutputEP.setContentType( "text/html" );
        //msgOutputTA.setPreferredSize(new Dimension(535, 408));
        msgOutputEP.setText("");
        msgOutputEP.setBackground(chatColor);
        msgOutputEP.setForeground(Color.white);
        msgOutputEP.setAutoscrolls(true);
        //msgOutputTA.setWrapping(true);
        //msgOutputTA.setWrapStyleWord(true);
        msgOutputEP.setEditable(false);
        msgOutputEP.setSelectionColor(selectionColor);
        msgOutputEP.setSelectedTextColor(Color.white);
        JScrollPane msgOutputEPSP = new JScrollPane(msgOutputEP);
        msgOutputEPSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        msgOutputEPSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        msgOutputEPSP.setAutoscrolls(true);
        msgOutputEPSP.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        doc = (HTMLDocument) msgOutputEP.getDocument();
        edit = (HTMLEditorKit) msgOutputEP.getEditorKit();
        msgOutputEP.setDocument(doc);

        msgInputTF = new JTextField(62);
        msgInputTF.setBackground(secondaryColor);
        msgInputTF.setForeground(Color.white);
        msgInputTF.setToolTipText("Напишите сообщение или команду и нажмите → или ENTER");
        msgInputTF.setCaretColor(Color.white);
        msgInputTF.setSelectionColor(selectionColor);
        msgInputTF.setSelectedTextColor(Color.white);
        msgInputTF.setBorder(BorderFactory.createLineBorder(Color.black, 1));


        sendBut = new JButton("➥");
        sendBut.setBackground(secondaryColor);
        sendBut.setForeground(Color.white);
        sendBut.setFocusPainted(false);
        sendBut.setToolTipText("Отправить сообщение");

        listOfUsers = new JList();
        listOfUsers.setBackground(chatColor);
        listOfUsers.setForeground(Color.white);
        listOfUsers.setToolTipText("Список подключенных пользователей");
        listOfUsers.setSelectionBackground(selectionColor);
        listOfUsers.setSelectionForeground(Color.white);
        JScrollPane listOfUsersSP = new JScrollPane(listOfUsers);
        listOfUsersSP.setPreferredSize(new Dimension(190, 403));
        listOfUsersSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listOfUsersSP.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        // Adding components to panels
        panelMain.add(panelChat, BorderLayout.CENTER);
        panelMain.add(panelInput, BorderLayout.SOUTH);

        panelChat.add(msgOutputEPSP, BorderLayout.CENTER);
        panelChat.add(listOfUsersSP, BorderLayout.EAST);

        panelInput.add(msgInputTF, BorderLayout.CENTER);
        panelInput.add(sendBut, BorderLayout.EAST);

        // Enabling panels
        panelMain.setVisible(true);
        panelChat.setVisible(true);
        panelInput.setVisible(true);

        // Set input area to be focused on open
        addWindowListener( new WindowAdapter() {
            public void windowOpened( WindowEvent e ){
                msgInputTF.requestFocus();
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
    }

    private void sendButtonPressed(){
        if (!msgInputTF.getText().isEmpty() && !ChatCommands.isCommand(msgInputTF.getText())) {
            if (!MainHandler.selfClient.getNickname().equals("")) {
                if (!isEmpty(msgInputTF.getText(), true))
                    log(msgInputTF.getText(), true, true, true);
            } else
                log("<html><font face='verdana' color='red'>Вы не можете отправлять сообщения пока у вас нету ника.</font><font face='verdana' color='white'>" +
                        " Поставьте его написав:</font><font face='arial' color='green'>\n /set nickname \t&lt;</font><font face='arial' color='white'>ВАШ_НИК</font><font face='arial' color='green'>" +
                        "&gt;</font> <font face='verdana' color='white'>(без \t&lt;&gt; и без пробелов)</font></html>", false, false, false);
        }
    }

    public void log(String message, boolean toServer, boolean withOwnNick, boolean addHtml) {
        if (toServer && withOwnNick) { // TODO
            String msgToSend;
            if (!addHtml)
                msgToSend = MainHandler.selfClient.getNickname() + "<font face='arial' color='white'>: </font>" + message + "<br>";
            else msgToSend = "<html>" + MainHandler.selfClient.getNickname() + "<font face='arial' color='white'>: " + message + "</font><br></html>";
            try {
                edit.insertHTML(doc, doc.getLength(), msgToSend + "\n", 0, 0, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // TODO send it to server
        }
        else {
            try {
                edit.insertHTML(doc, doc.getLength(),  message + "\n", 0, 0, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        msgInputTF.setText("");
        msgOutputEP.setCaretPosition(msgOutputEP.getDocument().getLength()); // Autoscroll down
    }

    private boolean isEmpty(String string, boolean rude) {
        if (string.isEmpty())
            return true;
        else {
            for (int i = 0; i < string.length(); i++) {
                if (string.charAt(i) == ' ' && i == string.length() - 1 && rude) {
                    log("<html><font face='verdana' color='brown'>Низя</font></html>", false, false, false);
                }
                else if (string.charAt(i) == ' ')
                    continue;
                else return false;
            }
            return true;
        }
    }

    public void clearChat(){
        msgOutputEP.setText("");
        msgInputTF.setText("");
    }
}
