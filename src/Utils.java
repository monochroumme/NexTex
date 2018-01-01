import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Random;

/**
 * Created by nadir on 13.04.2017.
 */
public class Utils {
    static boolean isEmpty(String string) {
        if (string.isEmpty())
            return true;
        else {
            for (int i = 0; i < string.length(); i++) {
                if(string.charAt(i) != ' ')
                    return false;
            }
            return true;
        }
    }

    static boolean containsChars(String container, String[] chars){
        for(String c : chars){
            if(container.contains(c))
                return true;
        }
        return false;
    }

    static boolean containsOnly(String[] container, String only) {
        return container.length == 1 && container[0].equalsIgnoreCase(only);
    }

    static String getRandomRGBColorString(){
        Random random = new Random();
        Color color = new Color(random.nextInt(205) + 50,random.nextInt(205) + 50,random.nextInt(205) + 50);
        color.brighter();
        return "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
    }

    static String getNickname(String nickname){ // This method checks if the name is full if it's not then the method searches for full version of it from list of users
        for(int i = 1; i < Main.chatGraphics.listOfUsers.getModel().getSize(); i++){ // Or if the name is full it just tells if it is in the list of users | i = 1 to omit the admin
            if (nickname.endsWith(">>") && Main.chatGraphics.listOfUsers.getModel().getElementAt(i).substring(Main.chatGraphics.listOfUsers.getModel().getElementAt(i).indexOf(
                    "size=") + 7).startsWith(nickname.substring(0, nickname.indexOf(">>")))
                    || nickname.startsWith("<") && nickname.endsWith(">") &&
                    Main.chatGraphics.listOfUsers.getModel().getElementAt(i).substring(Main.chatGraphics.listOfUsers.getModel().getElementAt(i).indexOf(
                    "size=") + 7, Main.chatGraphics.listOfUsers.getModel().getElementAt(i).indexOf("</")).contains(nickname.substring(1, nickname.indexOf(">")))) {
                return Main.chatGraphics.listOfUsers.getModel().getElementAt(i).substring(Main.chatGraphics.listOfUsers.getModel().getElementAt(i).indexOf("size=") + 7,
                        Main.chatGraphics.listOfUsers.getModel().getElementAt(i).indexOf("</"));
            } else if (Main.chatGraphics.listOfUsers.getModel().getElementAt(i).substring(Main.chatGraphics.listOfUsers.getModel().getElementAt(i).indexOf("size=") + 7,
                    Main.chatGraphics.listOfUsers.getModel().getElementAt(i).indexOf("</")).equals(nickname)) {
                return nickname;
            }
        }
        return "";
    }

    static String getIP(){
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            return null;
        }
        finally {
            if (socket != null)
                socket.close();
        }
    }

    static String getSearchIP(){
        try{
            return Utils.getIP().substring(0, Utils.getIP().lastIndexOf('.') + 1);
        } catch (Exception e){
            return "";
        }
    }
}

class NoSelectionModel extends DefaultListSelectionModel {

    @Override
    public void setAnchorSelectionIndex(final int anchorIndex) {}

    @Override
    public void setLeadAnchorNotificationEnabled(final boolean flag) {}

    @Override
    public void setLeadSelectionIndex(final int leadIndex) {}

    @Override
    public void setSelectionInterval(final int index0, final int index1) { }
}

class SmartScroller implements AdjustmentListener
{
    public final static int HORIZONTAL = 0;
    public final static int VERTICAL = 1;

    public final static int START = 0;
    public final static int END = 1;

    private int viewportPosition;

    private JScrollBar scrollBar;
    private boolean adjustScrollBar = true;

    private int previousValue = -1;
    private int previousMaximum = -1;

    public SmartScroller(JScrollPane scrollPane)
    {
        this(scrollPane, VERTICAL, END);
    }

    public SmartScroller(JScrollPane scrollPane, int scrollDirection, int viewportPosition)
    {
        if (scrollDirection != HORIZONTAL
                &&  scrollDirection != VERTICAL)
            throw new IllegalArgumentException("invalid scroll direction specified");

        if (viewportPosition != START
                &&  viewportPosition != END)
            throw new IllegalArgumentException("invalid viewport position specified");

        this.viewportPosition = viewportPosition;

        if (scrollDirection == HORIZONTAL)
            scrollBar = scrollPane.getHorizontalScrollBar();
        else
            scrollBar = scrollPane.getVerticalScrollBar();

        scrollBar.addAdjustmentListener( this );

        Component view = scrollPane.getViewport().getView();

        if (view instanceof JTextComponent)
        {
            JTextComponent textComponent = (JTextComponent)view;
            DefaultCaret caret = (DefaultCaret)textComponent.getCaret();
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }

    @Override
    public void adjustmentValueChanged(final AdjustmentEvent e)
    {
        SwingUtilities.invokeLater(() -> checkScrollBar(e));
    }

    private void checkScrollBar(AdjustmentEvent e)
    {
        JScrollBar scrollBar = (JScrollBar)e.getSource();
        BoundedRangeModel listModel = scrollBar.getModel();
        int value = listModel.getValue();
        int extent = listModel.getExtent();
        int maximum = listModel.getMaximum();

        boolean valueChanged = previousValue != value;
        boolean maximumChanged = previousMaximum != maximum;

        if (valueChanged && !maximumChanged)
        {
            if (viewportPosition == START)
                adjustScrollBar = value != 0;
            else
                adjustScrollBar = value + extent >= maximum;
        }

        if (adjustScrollBar && viewportPosition == END)
        {
            scrollBar.removeAdjustmentListener( this );
            value = maximum - extent;
            scrollBar.setValue( value );
            scrollBar.addAdjustmentListener( this );
        }

        if (adjustScrollBar && viewportPosition == START)
        {
            scrollBar.removeAdjustmentListener( this );
            value = value + maximum - previousMaximum;
            scrollBar.setValue( value );
            scrollBar.addAdjustmentListener( this );
        }

        previousValue = value;
        previousMaximum = maximum;
    }
}
