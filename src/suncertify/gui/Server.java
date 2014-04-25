package suncertify.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import suncertify.conn.DBConnection;

/**
 * The Server module. This is the GUI a user see when the application is started
 * in server mode
 *
 * @author Emmanuel
 */
public class Server extends JFrame {

    // Pre defined string to use in the client.
    // Used to make changes to control text easier
    private static final String START_BUTTON_TEXT = "Start Server";
    private static final String START_BUTTON_TOOLTIP
            = "Enter server configuration then click to start server";
    private static final String STOP_BUTTON_TEXT = "Stop Server";
    private static final String STOP_BUTTON_TOOLTIP
            = "Stops the server as soon as it is safe";
    private static final String EXIT_BUTTON_TEXT = "Exit";
    private static final String EXIT_BUTTON_TOOLTIP
            = "Stops the server as soon as it is safe and exits the application";

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.gui</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.gui");

    // configuration panel
    private final ConfigPanel configPanel;
    // control panel
    private final JPanel ctrlPanel;
    private final JButton startStopButton;
    private final JButton exitButton;
    // server start stop status
    private boolean running;

    /**
     * Instantiates the Server GUI and handles initial configurations.
     */
    public Server() {
        super("URLyBird Server");
        this.setDefaultCloseOperation(Server.EXIT_ON_CLOSE);
        this.setResizable(false);

        this.running = false;

        this.startStopButton = new JButton();
        this.exitButton = new JButton(EXIT_BUTTON_TEXT);

        configPanel = new ConfigPanel(Application.Mode.SERVER);
        this.add(configPanel, BorderLayout.NORTH);
        ctrlPanel = controlPanel();
        this.add(ctrlPanel, BorderLayout.SOUTH);

        this.pack();
        // Center on screen
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((d.getWidth() - this.getWidth()) / 2);
        int y = (int) ((d.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);
        this.setVisible(true);
    }

    /**
     * Configures the control panel. Adding the start, stop and exit buttons to
     * it and setting their actions.
     *
     * @return JPanel with the start, stop and exit server buttons already
     * configured.
     */
    private JPanel controlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));

        if (running) {
            startStopButton.setText(STOP_BUTTON_TEXT);
            startStopButton.setToolTipText(START_BUTTON_TOOLTIP);
        } else {
            startStopButton.setText(START_BUTTON_TEXT);
            startStopButton.setToolTipText(START_BUTTON_TOOLTIP);
        }
        startStopButton.addActionListener(new ServerAction());
        panel.add(startStopButton);

        exitButton.setToolTipText(EXIT_BUTTON_TOOLTIP);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        });
        panel.add(exitButton);

        return panel;
    }

    /**
     * Checks if the server is currently running or not.
     *
     * @return true if the server is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Class to handle the action of starting and stopping the server.
     */
    private class ServerAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            // start server
            synchronized (startStopButton) {
                String location = configPanel.getLocationFieldText();
                int port = Integer.parseInt(configPanel.getPortNumberText());
                if (running) {
                    try {
                        startStopButton.setEnabled(false);
                        configPanel.setAllFieldsEnabled(false);
                        DBConnection.unregister(port);
                        running = false;
                    } catch (RemoteException ex) {
                        Application.handleException("Unable to stop server, "
                                + "access to registry not granted", ex);
                    } catch (IllegalArgumentException ex) {
                        // should never happen with running server
                        Application.handleException(
                                "Illegal port number: " + port, ex);
                    } catch (NotBoundException ex) {
                        Application.handleException(
                                "Running server not found on port: " + port, ex);
                        running = false;
                    } finally {
                        if (running) {
                            log.warning("Server still running.");
                        } else {
                            startStopButton.setText(START_BUTTON_TEXT);
                            startStopButton.setToolTipText(START_BUTTON_TOOLTIP);
                            configPanel.setAllFieldsEnabled(true);
                        }
                        startStopButton.setEnabled(true);
                    }
                } else {
                    try {
                        startStopButton.setEnabled(false);
                        configPanel.setAllFieldsEnabled(false);
                        DBConnection.register(location, port);
                        running = true;
                    } catch (RemoteException ex) {
                        Application.handleException("Unable to start server, "
                                + "maybe port number already registered?", ex);
                    } catch (IllegalArgumentException ex) {
                        Application.handleException("Illegal port number", ex);
                    } finally {
                        if (running) {
                            startStopButton.setText(STOP_BUTTON_TEXT);
                            startStopButton.setToolTipText(STOP_BUTTON_TOOLTIP);
                            configPanel.save();
                        } else {
                            configPanel.setAllFieldsEnabled(true);
                        }
                        startStopButton.setEnabled(true);
                    }
                }
            }
        }

    }

}