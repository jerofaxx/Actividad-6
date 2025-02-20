import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class Parceros extends JFrame {
    private JTextField nameField, numberField;
    private DefaultListModel<String> contactListModel;
    private JList<String> contactList;
    private JButton addButton, updateButton, deleteButton;
    private ArrayList<String> contacts;
    
    public Parceros() {
        setTitle("Lista de Contactos");
        setLayout(null);

        JLabel nameLabel = new JLabel("Nombre:");
        nameLabel.setBounds(20, 20, 100, 25);
        nameField = new JTextField();
        nameField.setBounds(120, 20, 150, 25);
        
        JLabel numberLabel = new JLabel("Número:");
        numberLabel.setBounds(20, 50, 100, 25);
        numberField = new JTextField();
        numberField.setBounds(120, 50, 150, 25);
        numberField.addKeyListener(new KeyAdapter() {
            private Color originalColor = numberField.getForeground();
            private boolean showingError = false; // Para evitar que el mensaje se quede
        
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
        
                // Permitir borrar con Backspace o Delete
                if (c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) {
                    return;
                }
        
                // Si no es un número, mostrar advertencia
                if (!Character.isDigit(c)) {
                    e.consume();
                    if (!showingError) { // Evita que se repita el mensaje al teclear varias veces
                        numberField.setForeground(Color.LIGHT_GRAY);
                        numberField.setText("Ingrese un número válido");
                        showingError = true;
        
                        Timer timer = new Timer(1000, evt -> {
                            numberField.setText("");
                            numberField.setForeground(originalColor);
                            showingError = false;
                        });
                        timer.setRepeats(false);
                        timer.start();
                    }
                }
            }
        
            @Override
            public void keyReleased(KeyEvent e) {
                // Si el campo está vacío después de borrar, restablecer color
                if (numberField.getText().isEmpty()) {
                    numberField.setForeground(originalColor);
                    showingError = false;
                }
            }
        });
        
        addButton = new JButton("Añadir");
        addButton.setBounds(20, 90, 80, 25);
        addButton.setBackground(new Color(64, 224, 208));
        addButton.addActionListener(e -> {
            addFriend();
            contactList.clearSelection();
        });
        
        updateButton = new JButton("Editar");
        updateButton.setBounds(110, 90, 80, 25);
        updateButton.setBackground(new Color(255, 165, 0));
        updateButton.addActionListener(e -> {
            updateFriend();
            contactList.clearSelection();
        });

        deleteButton = new JButton("Eliminar");
        deleteButton.setBounds(200, 90, 90, 25);
        deleteButton.setBackground(new Color(255, 99, 71));
        deleteButton.addActionListener(e -> {
            deleteFriend();
            contactList.clearSelection();
        });

        contactListModel = new DefaultListModel<>();
        contactList = new JList<>(contactListModel);
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.addListSelectionListener(e -> loadSelectedContact());
        
        JScrollPane scrollPane = new JScrollPane(contactList);
        scrollPane.setBounds(20, 130, 270, 150);

        add(nameLabel);
        add(nameField);
        add(numberLabel);
        add(numberField);
        add(addButton);
        add(updateButton);
        add(deleteButton);
        add(scrollPane);
        
        setSize(320, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
        
        showFriend();
    }

    private void addFriend() {
        String name = nameField.getText().trim();
        String number = numberField.getText().trim();
        
        if (name.isEmpty() || number.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar todos los campos.");
            return;
        }
        
        for (String contact : contacts) {
            String[] data = contact.split("!");
            if (data[0].equals(name) || data[1].equals(number)) {
                JOptionPane.showMessageDialog(this, "El contacto ya existe.");
                return;
            }
        }
        
        String newContact = name + "!" + number;
        contacts.add(newContact);
        try (RandomAccessFile raf = new RandomAccessFile("Contactos.txt", "rw")) {
            raf.seek(raf.length());
            raf.writeBytes(newContact + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar el contacto.");
        }
        contactListModel.addElement(name + " - " + number);
        nameField.setText("");
        numberField.setText("");
    }

    private void updateFriend() {
        int selectedIndex = contactList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un contacto para editar.");
            return;
        }
        
        String name = nameField.getText().trim();
        String number = numberField.getText().trim();
        
        if (name.isEmpty() || number.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar todos los campos.");
            return;
        }
        
        for (int i = 0; i < contacts.size(); i++) {
            if (i != selectedIndex) {
                String[] data = contacts.get(i).split("!");
                if (data[0].equals(name) || data[1].equals(number)) {
                    JOptionPane.showMessageDialog(this, "El contacto ya existe.");
                    return;
                }
            }
        }
        
        contacts.set(selectedIndex, name + "!" + number);
        try (RandomAccessFile raf = new RandomAccessFile("Contactos.txt", "rw")) {
            raf.setLength(0);
            for (String contact : contacts) {
                raf.writeBytes(contact + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar el contacto.");
        }
        contactListModel.set(selectedIndex, name + " - " + number);
        nameField.setText("");
        numberField.setText("");
    }

    private void deleteFriend() {
        int selectedIndex = contactList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un contacto para eliminar.");
            return;
        }
        
        contacts.remove(selectedIndex);
        try (RandomAccessFile raf = new RandomAccessFile("Contactos.txt", "rw")) {
            raf.setLength(0);
            for (String contact : contacts) {
                raf.writeBytes(contact + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar el contacto.");
        }
        contactListModel.remove(selectedIndex);
        nameField.setText("");
        numberField.setText("");
    }

    private void loadSelectedContact() {
        int selectedIndex = contactList.getSelectedIndex();
        if (selectedIndex != -1) {
            String[] data = contacts.get(selectedIndex).split("!");
            nameField.setText(data[0]);
            numberField.setText(data[1]);
        }
    }

    private void showFriend() {
        contacts = new ArrayList<>();
        File file = new File("Contactos.txt");
        
        if (!file.exists()) return;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                contacts.add(line);
                String[] data = line.split("!");
                contactListModel.addElement(data[0] + " - " + data[1]);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los contactos.");
        }
    }

    public static void main(String[] args) {
        new Parceros();
    }
}