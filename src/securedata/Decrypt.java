package securedata;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.*;
import java.util.Properties;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.swing.*;

public class Decrypt extends JFrame {

    private static final long serialVersionUID = 1L;
    private JButton btChoose = new JButton();
    private JProgressBar pbCryptProgress = new JProgressBar();
    private JPasswordField pfPassword = new JPasswordField();
    private JLabel lbAlgorithm = new JLabel();
    private JLabel lbSource = new JLabel();
    private JTextField tfSourceFile = new JTextField();
    private JLabel lbPassword = new JLabel();
    private JComboBox cmbAlgorithm = new JComboBox();
    private JComboBox cmbCompressionLevel = new JComboBox();
    private JButton btDecrypt = new JButton();    
    private JFileChooser fchooser = new JFileChooser();
    private Properties prop = new Properties();
    private long read = 0;
    

    public Decrypt() {
        super();
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void decrypt(final File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                decrypt(files[i]);
            }
        } else {
            new Thread() {
                public void run() {
                    try {
                        String kind = (String) cmbAlgorithm.getSelectedItem(); // Which algorithm?
                        int index = kind.indexOf("(");
                        kind = kind.substring(0, index);

                        Cipher c = Cipher.getInstance(kind);
                        Key k = new SecretKeySpec(
                                new String(pfPassword.getPassword()).getBytes(), kind);

                        c.init(Cipher.DECRYPT_MODE, k);

                        String filename = f.getCanonicalPath();

                        if (filename.endsWith(prop.getProperty(kind))) {
                            filename = filename.substring(
                                    0, filename.length()
                                    - prop.getProperty(kind).length());    // -suffix
                        } else {
                            displayError("Error: Wrong file chosen",
                                    "Ending of file and chosen algorithm do not match! Filename must end with: " + prop.getProperty(kind));
                            return;
                        }

                        FileInputStream fis =
                                new FileInputStream(f.getCanonicalPath());

                        FileOutputStream fos = new FileOutputStream(filename);
                        CipherInputStream cis = new CipherInputStream(fis, c);
                        byte[] buffer = new byte[0xFFFF];
                        final long size = f.length();

                        pbCryptProgress.setMaximum((int) size);

                        for (int len; (len = cis.read(buffer)) != -1;) {
                            fos.write(buffer, 0, len);
                            read += len;
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    pbCryptProgress.setValue((int) read);
                                    pbCryptProgress.repaint();
                                }
                            });    // Set Progress
                        }

                        cis.close();
                        fos.flush();
                        fos.close();
                        fis.close();
                        pbCryptProgress.setMaximum(100);
                        pbCryptProgress.setValue(0);
                        JOptionPane.showMessageDialog(rootPane, "File has been successfully decrypted.");

                        read = 0;
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
            }.start();
        }
    }

    

    
    private void btChoose_actionPerformed(ActionEvent e) {
        try {
            if (fchooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                String path = fchooser.getSelectedFile().getCanonicalPath();
                tfSourceFile.setText(path);
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    

    private void btDecrypt_actionPerformed(ActionEvent e) {
        String path = tfSourceFile.getText();
        if (!path.equals("")) { // File chosen?
            File file = new File(path);
            if (file.exists()) { // Does file exist?
                decrypt(new File(path));

            } else {
                displayError("Error: file does not exist",
                        "The file you have chosen does not exist: " + path);
            }
        }
    }

    private void jbInit() throws Exception {
        this.setTitle("Encryption and Description");
        this.setSize(new Dimension(500, 250));
        this.setResizable(true);

        // setup components

        btChoose.setText("Choose File");
        btChoose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btChoose_actionPerformed(e);
            }
        });

        


        btDecrypt.setText("Decrypt");
        btDecrypt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btDecrypt_actionPerformed(e);
            }
        });

        lbSource.setText("Source:");
        lbSource.setVerticalAlignment(JLabel.CENTER);
        lbSource.setHorizontalAlignment(JLabel.RIGHT);

        lbAlgorithm.setText("Algorithm:");
        lbAlgorithm.setVerticalAlignment(JLabel.CENTER);
        lbAlgorithm.setHorizontalAlignment(JLabel.RIGHT);

        lbPassword.setText("Password:");
        lbPassword.setVerticalAlignment(JLabel.CENTER);
        lbPassword.setHorizontalAlignment(JLabel.RIGHT);

        cmbCompressionLevel.setEnabled(false);
        prop.setProperty("DES", ".des");
        prop.setProperty("AES", ".aes");


        cmbAlgorithm.addItem("DES(8)");
        cmbAlgorithm.addItem("AES(16)");


        for (int i = 0; i < 10; i++) {
            cmbCompressionLevel.addItem(new Integer(i));
        }
        fchooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        // setup boxed layouts with panels

        JPanel jpInput = new JPanel();
        JPanel jpCompression = new JPanel();
        JPanel jpCryptButtons = new JPanel();
        JPanel jpProgress = new JPanel();

        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().add(jpInput, new GridBagConstraints(
                0, 0, 1, 1, 1.0, 3.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(8, 8, 8, 8), 0, 0));
        this.getContentPane().add(jpCompression, new GridBagConstraints(
                0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        this.getContentPane().add(jpCryptButtons, new GridBagConstraints(
                0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(8, 8, 0, 8), 0, 0));
        this.getContentPane().add(jpProgress, new GridBagConstraints(
                0, 3, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(8, 8, 8, 8), 0, 0));

        // add components to the panels

        jpInput.setLayout(new GridBagLayout());
        jpInput.add(lbSource, new GridBagConstraints(
                0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 8), 0, 0));
        jpInput.add(tfSourceFile, new GridBagConstraints(
                1, 0, 1, 1, 3.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 8), 0, 0));
        jpInput.add(btChoose, new GridBagConstraints(
                2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        jpInput.add(lbAlgorithm, new GridBagConstraints(
                0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(8, 0, 0, 8), 0, 0));
        jpInput.add(cmbAlgorithm, new GridBagConstraints(
                1, 1, 1, 1, 3.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(8, 0, 0, 8), 0, 0));
        jpInput.add(lbPassword, new GridBagConstraints(
                0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(8, 0, 0, 8), 0, 0));
        jpInput.add(pfPassword, new GridBagConstraints(
                1, 2, 1, 1, 3.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(8, 0, 0, 8), 0, 0));



        GridLayout cryptButtonsGrid = new GridLayout(1, 2);
        cryptButtonsGrid.setHgap(8);
        jpCryptButtons.setLayout(cryptButtonsGrid);        
        jpCryptButtons.add(btDecrypt);
        jpProgress.setLayout(new GridLayout(1, 1));
        jpProgress.add(pbCryptProgress);
    }

    private void displayError(String title, String text) {
        JOptionPane.showMessageDialog(this, text, title, JOptionPane.ERROR_MESSAGE);
    }
}
