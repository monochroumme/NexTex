package core;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by nadir on 12.03.2017.
 **/

public class Graphics extends JFrame {
    private final String TITLE = "NexTex";
    private final int FRM_WIDTH = 755;
    private final int FRM_HEIGHT = 490;

    private JPanel panelMain;
    private JPanel panelChat;
    private JPanel panelInput;
    private JButton sendBut;
    public JTextArea msgOutputTA;
    public JTextField msgInputTF;
    public JList listOfUsers;

    private Color chatColor = new Color(30, 30, 30);
    private Color secondaryColor = new Color(65, 65, 65);
    private Color selectionColor = new Color(30, 100, 255);

    public Graphics() {
        draw();
        setVisible(true);
        handleInserts();
    }

    public void draw() {
        // Window presets
        setTitle(TITLE);
        setSize(FRM_WIDTH, FRM_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // TODO отправлять серверу инфу о выходе при закрытии проги
        setLocationRelativeTo(null);
        UIManager.put("ToolTip.background", new ColorUIResource(secondaryColor));
        UIManager.put("ToolTip.foreground", new ColorUIResource(Color.white));

        // Main panel for holding other panels
        panelMain = new JPanel();
        panelMain.setVisible(false);
        panelMain.setForeground(Color.white);
        panelMain.setSize(FRM_WIDTH, FRM_HEIGHT);
        panelMain.setBackground(chatColor);
        getContentPane().add(panelMain);

        //Panel for holding chat and list of users
        panelChat = new JPanel();
        panelChat.setVisible(false);
        panelChat.setBackground(chatColor);
        panelChat.setPreferredSize(new Dimension(800, 415));

        //Panel for holding send button and textField
        panelInput = new JPanel();
        panelInput.setVisible(false);
        panelInput.setBackground(chatColor);
        panelInput.setPreferredSize(new Dimension(800, 35));

        // Components' presets
        msgOutputTA = new JTextArea(25, 49); // 27, 67
        //msgOutputTA.setPreferredSize(new Dimension(535, 408));
        msgOutputTA.setText("/help - помощь\n");
        msgOutputTA.setBackground(chatColor);
        msgOutputTA.setForeground(Color.white);
        msgOutputTA.setLineWrap(true);
        msgOutputTA.setWrapStyleWord(true);
        msgOutputTA.setEditable(false);
        msgOutputTA.setSelectionColor(selectionColor);
        msgOutputTA.setSelectedTextColor(Color.white);
        JScrollPane msgOutputTASP = new JScrollPane(msgOutputTA);
        msgOutputTASP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        msgOutputTASP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        msgOutputTASP.setAutoscrolls(true);

        msgInputTF = new JTextField(62);
        msgInputTF.setBackground(secondaryColor);
        msgInputTF.setForeground(Color.white);
        msgInputTF.setToolTipText("Напишите сообщение или команду и нажмите → или ENTER");
        msgInputTF.setCaretColor(Color.white);
        msgInputTF.setSelectionColor(selectionColor);
        msgInputTF.setSelectedTextColor(Color.white);


        sendBut = new JButton("➥"); // TODO Change the font of button to make this arrow look nice
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

        // Adding components to panels
        panelMain.add(panelChat, BorderLayout.CENTER);
        panelMain.add(panelInput, BorderLayout.SOUTH);

        panelChat.add(msgOutputTASP, BorderLayout.CENTER);
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
        if (!msgInputTF.getText().isEmpty() && !ChatCommands.isCommand(msgInputTF.getText())) { // TODO add if connectedToServer
            if (!MainHandler.selfClient.nickname.equals("")) {
                if (!isEmpty(msgInputTF.getText(), true))
                    log(msgInputTF.getText(), true, true);
            } else
                log("Вы не можете отправлять сообщения пока у вас нету ника. Поставьте его написав:\n /set nickname <ВАШ_НИК> (без <> и без пробелов)", false, false);
        }
    }

    public void log(String message, boolean toServer, boolean withOwnNick) {
        if (toServer && withOwnNick) { // TODO
            String msgToSend = MainHandler.selfClient.nickname + ": " + message + "\n";
            msgOutputTA.append(msgToSend);
            // TODO send it to server
        }
        else {
            msgOutputTA.append(message + "\n");
        }

        msgInputTF.setText("");
        msgOutputTA.setCaretPosition(msgOutputTA.getDocument().getLength());
    }

    private boolean isEmpty(String string, boolean rude) {
        if (string.isEmpty())
            return true;
        else {
            for (int i = 0; i < string.length(); i++) {
                if (string.charAt(i) == ' ' && i == string.length() - 1 && rude) {
                    log("Низя", false, false);
                }
                else if (string.charAt(i) == ' ')
                    continue;
                else return false;
            }
            return true;
        }
    }

    public void clearChat(){
        msgOutputTA.setText("");
        msgInputTF.setText("");
    }
}
