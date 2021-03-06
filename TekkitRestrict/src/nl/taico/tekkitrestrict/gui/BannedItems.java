/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.taico.tekkitrestrict.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import nl.taico.tekkitrestrict.gui.lib.JSuggestField;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import javax.swing.table.DefaultTableModel;

import nl.taico.tekkitrestrict.NameProcessor;

/**
 *
 * @author Taico
 */
@SuppressWarnings("serial")
public class BannedItems extends javax.swing.JFrame {
	
	private int lastid = 0;
	/**
	 * Creates new form BannedItems
	 */
	public BannedItems() {
		initComponents();


	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	private void initComponents() {
		hiddenField1 = new JTextField();
		jPanel1 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTable1 = new javax.swing.JTable();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		msgField = new JTextField();
		btnAdd = new JButton();
		btnChange = new javax.swing.JButton();
		btnRemove = new javax.swing.JButton();

		suggestField = new JSuggestField(this, NameProcessor.getAllItems());

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

		jTable1.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][] {
						//{0, null, null},
				},
				new String[] {"ID", "Item/Block", "Message"}) {
			@SuppressWarnings("rawtypes")
			Class[] types = new Class[] {java.lang.Integer.class, java.lang.String.class, java.lang.String.class};
			boolean[] canEdit = new boolean[] {false, false, true};

			@SuppressWarnings("rawtypes")
			public Class getColumnClass(int columnIndex) {
				return types[columnIndex];
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		});
		jScrollPane1.setViewportView(jTable1);
		jTable1.getColumnModel().getColumn(0).setMinWidth(50);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(50);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(50);
		jTable1.getColumnModel().getColumn(1).setCellEditor(null);
		suggestField.setText("");

		jLabel1.setText("Item:");

		jLabel2.setText("Message:");

		msgField.setText("");

		hiddenField1.setText("0");

		jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable1.getSelectionModel().addListSelectionListener(new RowListener());
		
		btnAdd.setText("Add");
		btnAdd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnAddActionPerformed();
			}
		});
		
		btnChange.setText("Change");
		btnChange.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnChangeActionPerformed(evt);
			}
		});

		btnRemove.setText("Remove");
		btnRemove.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnRemoveActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(
				jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel2Layout.createSequentialGroup()
										.addComponent(btnChange, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(btnRemove)
										.addComponent(btnAdd)
										.addGap(0, 197, Short.MAX_VALUE))
										.addGroup(jPanel2Layout.createSequentialGroup()
												.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(jLabel1)
														.addComponent(jLabel2))
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																.addComponent(msgField)
																.addComponent(suggestField))))
																.addContainerGap())
				);
		jPanel2Layout.setVerticalGroup(
				jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(suggestField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel1))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(msgField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabel2))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(btnChange)
												.addComponent(btnRemove)
												.addComponent(btnAdd))
												.addContainerGap(38, Short.MAX_VALUE))
				);

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(
				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(30, 30, 30)
						.addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(338, Short.MAX_VALUE))
				);
		jPanel1Layout.setVerticalGroup(
				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addContainerGap(149, Short.MAX_VALUE))
				);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(187, Short.MAX_VALUE))
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap())
				);

		pack();
	}
	
	private void btnAddActionPerformed(){
		if (!suggestField.getSuggestData().contains(suggestField.getText())){
			if (suggestField.suggestions.isEmpty()) return;
			suggestField.setText(suggestField.suggestions.get(0));
		}
		DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
		model.addRow(new Object[]{lastid, suggestField.getText(), msgField.getText()});
		lastid++;
	}
	
	private void btnChangeActionPerformed(java.awt.event.ActionEvent evt) {
		if (!suggestField.getSuggestData().contains(suggestField.getText())){
			if (suggestField.suggestions.isEmpty()) return;
			suggestField.setText(suggestField.suggestions.get(0));
		}
		int row = Integer.parseInt(hiddenField1.getText());
		if (jTable1.getModel().getRowCount() <= row) return;
		jTable1.getModel().setValueAt(suggestField.getText(), row, 1);
		jTable1.getModel().setValueAt(msgField.getText(), row, 2);
	}                                      

	private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {
		int row = Integer.parseInt(hiddenField1.getText());
		if (jTable1.getModel().getRowCount() <= row) return;
		((DefaultTableModel)jTable1.getModel()).removeRow(row);
	} 

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(BannedItems.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(BannedItems.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(BannedItems.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(BannedItems.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new BannedItems().setVisible(true);
			}
		});
	}
	// Variables declaration - do not modify                     
	private JPanel jPanel1;
	private JScrollPane jScrollPane1;
	private JTable jTable1;
	private JSuggestField suggestField;

	private JButton btnAdd;
	private javax.swing.JButton btnChange;
	private javax.swing.JButton btnRemove;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JTextField msgField;
	private JTextField hiddenField1;
	// End of variables declaration            
	public void rowEvent(){
		int r = jTable1.getSelectedRow();
		hiddenField1.setText(""+r);
		suggestField.setText((String) jTable1.getModel().getValueAt(r, 1));
		msgField.setText((String) jTable1.getModel().getValueAt(r, 2));
	}
	private class RowListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			rowEvent();
		}
	}
}
