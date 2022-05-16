
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;

import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Controller<T> implements Initializable {

	ListItems allItemLists = new ListItems();
	Economics economy = new Economics();
	// selectedList wird beim Tabwechsel geändert, die entsprechenden Items werden
	// geladen
	Items[] selectedList = allItemLists.getEisItems();
	//OrderList[] orderedList = 

	int listcounter = 0;
	ArrayList<String> al = new ArrayList<String>();
	List<String> myArray = new ArrayList<String>();
	//ArrayList<String> OrderList = new ArrayList<String>();
	//String date1 = new Date().toString();
	
	ObservableList<Items> itemsObsList = FXCollections.observableArrayList(); // ????
	ObservableList<String> OrderedList = FXCollections.observableArrayList();
	
	@FXML
	TableView<Items> itemTable = new TableView<Items>();
	@FXML 
	TableColumn<Items, Integer> count = new TableColumn<Items, Integer>();
	@FXML
	TableColumn<Items, String> nameCol = new TableColumn<Items, String>();
	
	@FXML
	//ListView<T> myList = ((Collectors) OrderedList).toList().toBlocking().single();
	private ListView<String> myListView;
	//ListView<String> myListView = new ListView<>();
	ObservableList<String> myList;

	@FXML
	private Button abkassierenButton;
	@FXML
	private Button plusButton;
	@FXML
	private Button minusButton;
	@FXML
	private TextField summeTextField;
	@FXML
	private TextField gegebenTextField = new TextField();

	private Alert alert = new Alert(AlertType.NONE);

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		alert.setAlertType(AlertType.WARNING);
		// DisableProperty der +/- Buttons daran binden, ob etwas in der Tabelle
		// ausgewählt ist!
		minusButton.disableProperty().bind(Bindings.isNull(itemTable.getSelectionModel().selectedItemProperty()));
		plusButton.disableProperty().bind(Bindings.isNull(itemTable.getSelectionModel().selectedItemProperty()));
		// Festlegen, dass Neue Zellen bzw Rows aus den Properties der Items gebildet
		// werden
		count.setCellValueFactory(new PropertyValueFactory<Items, Integer>("anzahl"));
		nameCol.setCellValueFactory(new PropertyValueFactory<Items, String>("name"));


		itemTable.setItems(itemsObsList); //////////////////////////////// ??
		myListView.setItems(OrderedList);
		

	}

	// ActionHandler für verschiedene Arten von Buttons. "onAction" in GUI.fxml
	// festgelegt
	// Generell: Auswahl der Funktion anhand von Text im Button
	public void itemAction(ActionEvent e) {

		Button src = (Button) e.getSource();
		Items sel = null;

		// Hole zum Buttontext gehörendes Item aus der selectedList
		for (int i = 0; 0 <= selectedList.length; i++) {
			sel = selectedList[i];
			// getText() function ??
			if (src.getText().equals(sel.getName())) {

				if (!itemsObsList.contains(sel)) {

					selectedList[i].addItem();
					itemsObsList.add(selectedList[i]);
					itemTable.scrollTo(itemsObsList.size());

				} else {

					sel.addItem();
					// Manuelles setzen des Items an Index i
					// -wird anscheinend zum aktualisieren der View gebraucht.
					// ObservableList wird nur beim adden neuer Elemente berücksichtigt, nicht bei
					// Änderung von Werten innerhalb der Items in der ObservableList!
					// -Lazy Evaluation trotzdem gewährleistet, da nur bei Klicks ausgeführt
					itemTable.getItems().set(itemsObsList.indexOf(sel), sel);
 
				}

				break;
			}

		}

		economy.changeSum(sel.getPreis());
		summeTextField.setText("Summe:" + economy.getSum() + "€");

	}

	public void functionAction(ActionEvent e) throws IOException {

		Button src = (Button) e.getSource();

		Items focusItem = itemTable.getSelectionModel().getSelectedItem();
		// Dieser Index wird wieder zur manuellen Aktualisierung der TableView benötigt
		int indexOfFocus = itemTable.getSelectionModel().getFocusedIndex();

		switch (src.getText()) {
		case "+":

			plus(focusItem, indexOfFocus);
			summeTextField.setText("Summe:" + economy.getSum() + "€");
			break;

		case "-":

			minus(focusItem, indexOfFocus);
			summeTextField.setText("Summe:" + economy.getSum() + "€");
			break;

		case "Abkassieren":

			abkassieren();
			saveInDB();

			break;

		case "r�ckgeld":

			r�ckgeld();

			break;

		case "OK":

			reset();
			summeTextField.setText("Summe:" + economy.getSum() + "�");
			break;
		// Button for totalForDay
		/*
		 * case "totalForDay": economy.totalForDay(); economy.resetTotal(); break;
		 */
		default:
			break;

		}

	}

	public void tabAction(Event e) {

		Tab src = (Tab) e.getSource();

		switch (src.getText()) {

		case "Eis":

			selectedList = allItemLists.getEisItems();
			break;

		case "Shake":

			selectedList = allItemLists.getShakeItems();
			break;

		default:

			break;

		}

	}

	private void plus(Items focusedItem, int focusindex) {

		focusedItem.addItem();
		itemTable.getItems().set(focusindex, focusedItem);
		economy.changeSum(focusedItem.getPreis());

	}

	private void minus(Items focusedItem, int focusindex) {
		focusedItem.decItem();

		if (focusedItem.getAnzahl() <= 0) {

			itemsObsList.remove(focusedItem);

		} else {

			itemTable.getItems().set(focusindex, focusedItem);
		}

		economy.changeSum(-focusedItem.getPreis());

	}

	// Beim drücken auf abkassieren/rückgeld/ok wird anhand des Textwechsels der
	// Zustand des Programms gewechselt
	// Ändere den Zustand bei Fehleingaben nicht!

	private void abkassieren() {
		
		showListInGUI();
		gegebenTextField.setVisible(true);
		gegebenTextField.setDisable(false);
		summeTextField.setDisable(true);
		abkassierenButton.setText("r�ckgeld");

	}

	public void showListInGUI() {
		StringBuilder sb = new StringBuilder();
		String s2 = "";
		for (Items i : itemsObsList) {
			String s1 = (String) (i.getAnzahl() + "x" + i.getName() + "");
				s2 = s2 + " " + s1;
		
		}
		OrderedList.add(s2);
		System.out.println(s2);
		Iterator<String> iterator = OrderedList.iterator();
		while (iterator.hasNext()) {
			String element = iterator.next();
			//System.out.print(element);
		}
//		for (int i =0; i<myList.size(); i++){
//		       myArray.add(myList.get(i));
//		    }
//		// Test by printing out to the screen or a text field
//		    System.out.println(myList.get(0));
//		    //myTextField.setText(myList.get(0));
	}

	public void saveInDB() throws IOException {

//		for (Items i : itemsObsList) {
//			String s = (String) (i.getAnzahl() + "x" + i.getName() + " ");

			Date date = new Date();

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

			// File file = new File("TagesAbrechnung" + dateFormat.format(date) + ".txt") ;
			File file = new File("Tagesbrechnung.txt");
			//al.add(s);
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			
			for (String i : myListView.getItems()) {
				System.out.println(i);
				out.write(i);
				out.newLine();
			}
			//for (Items i : myListView) {
//				String s = (String) (i.getAnzahl() + "x" + i.getName() + " ");
			
//			Iterator<String> iterator = ((ListView<String>) myListView).iterator();
//
//			while (iterator.hasNext()) {
//				String element = iterator.next();
//				//System.out.print(element);
			
//			}

			out.close();
			// String data = String.format("%s: %s", date, al);
			// System.out.println(economy.totalDay());

		
		// writing to existing file
		// FileWriter writer = new FileWriter("TagesAbrechnung.txt", true);
		// writer.append("tresx");


	}

	private void r�ckgeld() {

		Double gegebenerWert = 0.0;
		if (gegebenTextField.getText().length() <= 1) {

			alert.setContentText("Nichts eingegeben!");
			alert.show();
			gegebenTextField.setText("Geben:"); //feld

		} else {
			// Hole hier den zurückgegebenen Geldwert aus dem TextField
			String gegebenText = (String) gegebenTextField.getText().subSequence(10,
					gegebenTextField.getText().length());

			try {

				gegebenerWert = Double.parseDouble(gegebenText);
				double r�ckgeld = economy.getChange(gegebenerWert);

				if (r�ckgeld >= 0) {

					abkassierenButton.setText("OK");
					gegebenTextField.setText("R�ckgeld: " + r�ckgeld);

				} else {

					alert.setContentText("Zu wenig r�ckgeld!");
					alert.show();
					gegebenTextField.setText("Gegeben:  ");

				}

			} catch (Exception ex) {

				alert.setContentText("Text eingegeben! Nur Zahlen erlaubt!");
				alert.show();
				gegebenTextField.setText("Gegeben:  ");

			}

		}

		System.out.println(economy.getTotalDay());

	}

	private void reset() {

		Iterator<Items> iter = itemsObsList.iterator();

		// Bei allen Items in der ObservableList die Anzahl zurücksetzen
		while (iter.hasNext()) {

			Items i = (Items) iter.next();
			i.clear();

		}


		abkassierenButton.setText("Abkassieren");
		economy.resetSum();
		itemsObsList.clear();
		summeTextField.setDisable(false);
		gegebenTextField.setDisable(true);
		gegebenTextField.setVisible(false);
		gegebenTextField.setText("Gegeben:  ");

	}

}
