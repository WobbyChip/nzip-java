//package proj3;

import javax.swing.*;

import compression.lz77.LZ77Encoder;
import compression.deflate.Deflate;
import compression.huffman.HuffmanEncoder;

import java.awt.*;
import java.awt.event.ItemEvent;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;


public class GUI {
    private final JFrame mainWindow;
    private final JPanel fileSelect;
    private final JPanel fileOperation;
    private final JPanel fileDetails;
    
    private JLabel fileCompressRatio;
    private JButton selectFileButton;
    private JButton fileOperationButton;
    private JComboBox<String> compressionDropdown;
    private JTextField filePath;
    private JTextField fileNewName;
    private JProgressBar fileCompressProg;
    
    private String fileName;
    private String newFileExt;
    private String[] archiveExtensions = {".huff",".lzss",".nzip"};
    private Boolean toCompress = true;

    public GUI() {
        mainWindow = new JFrame("Ni.zip");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setLayout(new GridLayout(3,1));
        mainWindow.setLocationRelativeTo(null);
        
        // Section the mainWindow into JPanels so that we have a bit more control over the layout.
        fileSelect = new JPanel();
        fileOperation = new JPanel();
        fileDetails = new JPanel();
        
        // Set the grid layout so that the single button takes up the entire panel.
        // and set a border so that we have some breathing room around the edges.
        fileOperation.setLayout(new GridLayout(2,1));
        fileOperation.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        fileDetails.setLayout(new GridLayout(2,1));
        fileDetails.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        //////////////////////////
        // File Selection JPanel
        
        // MAKE SURE THESE ALIGN WITH WITH THE archiveExtensions ARRAY
        String[] choices = { "Huffman", "LZ77", "ni.zip" };
        compressionDropdown = new JComboBox<>(choices);
        compressionDropdown.addItemListener(e -> {
        	if (e.getStateChange() == ItemEvent.SELECTED) {
        		newFileExt = archiveExtensions[compressionDropdown.getSelectedIndex()];
        		if (!fileNewName.getText().equals("New File Name")) fileNewName.setText(fileNewName.getText().substring(0, fileNewName.getText().indexOf("."))+newFileExt);
        	}
        });
        
        filePath = new JTextField(16);
        filePath.setEnabled(false);
        filePath.setDisabledTextColor(Color.BLACK);
        filePath.setBackground(mainWindow.getBackground());

        selectFileButton = new JButton("Select File");
        selectFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            int returnValue = fileChooser.showOpenDialog(mainWindow);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
            	fileCompressProg.setValue(0);
            	fileCompressRatio.setText("");
                File selectedFile = fileChooser.getSelectedFile();
                filePath.setText(selectedFile.getAbsolutePath());
                
                fileName = selectedFile.getName();
                String fileExt = fileName.substring(fileName.indexOf("."));
                if (Arrays.asList(archiveExtensions).contains(fileExt)) {
                	String[] temp_choices = {"Decompress"};
					DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(temp_choices);
					compressionDropdown.setModel(model);
					compressionDropdown.setEnabled(false);
					newFileExt = fileExt;
					
					// Change button name to reflect the change.
					fileOperationButton.setText("Decompress");
					
					// Recalculate window size so that elements aren't out of bounds
					mainWindow.pack();
					toCompress = false;
                } else {
                	// We set these anyway in case the user selects an archive and then selects a regular file instead.
                	DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(choices);
					compressionDropdown.setModel(model);
					compressionDropdown.setEnabled(true);
                	fileOperationButton.setText("Compress");
                	
                	newFileExt = archiveExtensions[compressionDropdown.getSelectedIndex()];
            		fileNewName.setText(fileName+newFileExt);
            		fileNewName.setForeground(Color.BLACK);
                	
					// Recalculate window size so that elements aren't out of bounds
					mainWindow.pack();
                	toCompress = true;
                }
            }
        });
        fileSelect.add(compressionDropdown);
        fileSelect.add(filePath);
        fileSelect.add(selectFileButton);
        
        //////////////////////////
        // File Operation JPanel
        
        fileNewName = new JTextField("New File Name");
        fileNewName.setEnabled(false);
        fileNewName.setDisabledTextColor(Color.GRAY);
        //fileNewName.setMargin(new Insets(0, 0, 10, 0));
        fileOperationButton = new JButton("Compress");
        fileOperationButton.addActionListener(e -> {

        	new Thread() {
        		public void run() {
        			String resultFile = filePath.getText().substring(0,filePath.getText().indexOf(fileName))+"\\"+fileNewName.getText();
        			fileOperationButton.setEnabled(false);

                	// I have to initialize these 2 arrays otherwise the IDE threatens to find where I live.
                	byte[] data = null;
                	byte[] final_data = null;

                	try {
                        data = Files.readAllBytes(Paths.get(filePath.getText()));
                	} catch (IOException ex) {
        	            System.out.println(ex.getMessage());
        			}

                    Consumer<Float> callback = progress -> {
                        fileCompressProg.setValue(Math.round(progress));
                        fileCompressProg.setString(String.format(Locale.US, "%.2f%%", progress));
                    };

                	if (newFileExt.equals(".nzip")) {
            			if (toCompress) final_data = Deflate.compress(data, callback);
            			else final_data = Deflate.decompress(data, callback);
            		} else if (newFileExt.equals(".lzss")) {
                        if (toCompress) final_data = LZ77Encoder.compress(data, callback);
                        else final_data = LZ77Encoder.decompress(data, callback);
            		} else if (newFileExt.equals(".huff")) {
                        if (toCompress) final_data = HuffmanEncoder.compress(data, callback);
                        else final_data = HuffmanEncoder.decompress(data, callback);
            		} else {
            			System.out.println("My Brother in Christ what the fuck.");
            		}

            		if (toCompress) fileCompressRatio.setText("Ratio: " + ((float) data.length/final_data.length));

            		try {
            			Files.write(Paths.get(resultFile), final_data);
            			fileCompressProg.setValue(100);
            			fileOperationButton.setEnabled(true);
            		} catch (IOException ex) {
        	            System.out.println(ex.getMessage());
            		}
        		}
        	}.start();
        	
        });

        fileOperation.add(fileNewName);
        //fileOperation.add(new JLabel(""));
        fileOperation.add(fileOperationButton);
        
        
		//////////////////////////
		// Compression Info
        
        fileCompressProg = new JProgressBar(0,100);
        fileCompressProg.setStringPainted(true);
        
        fileCompressRatio = new JLabel("");
        
        fileDetails.add(fileCompressProg);
        fileDetails.add(fileCompressRatio);        
        
        
		//////////////////////////
		// Adding all Panels to Main Frame and aligning them to be center aligned.

        mainWindow.add(fileSelect, BorderLayout.CENTER);
        mainWindow.add(fileOperation, BorderLayout.CENTER);
        mainWindow.add(fileDetails, BorderLayout.CENTER);
        
        mainWindow.pack();
        mainWindow.setVisible(true);
    }

    public static void main(String[] args) {
        new GUI();
    }
}