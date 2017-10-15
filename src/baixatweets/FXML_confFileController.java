/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baixatweets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * FXML Controller class
 *
 * @author joan
 */
public class FXML_confFileController implements Initializable {

    @FXML
    private TextField ConsumerKey;
    @FXML
    private TextField ConsumerSecret;
    @FXML
    private TextField AccessToken;
    @FXML
    private TextField AccessTokenSecret;
    @FXML
    private Button guardarConf;
    private FXML_confFileController fileUploader;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    

    @FXML
    private void ck(ActionEvent event) {

    }

    @FXML
    private void cs(ActionEvent event) {

   }

    @FXML
    private void at(ActionEvent event) {
    }

    @FXML
    private void as(ActionEvent event) {
 
  }

    @FXML
    private void guarda(ActionEvent event) {
         if (( ConsumerKey.getText().isEmpty() 
            || ConsumerSecret.getText().isEmpty()
            ||AccessToken.getText().isEmpty()
            || AccessTokenSecret.getText().isEmpty()))
         {
        Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("cal posar els valors de les claus ");
                            alert.setContentText("Cal que el ConsumerKey, ConsumerSecret, AccessToken i AccessTokenScret estiguin definits");
                            alert.setResizable(true);
                            alert.getDialogPane().setPrefSize(680, 100);
                            alert.showAndWait();
                            return;
         }
    		        FXMLvista_generalController.PROPERTIES.setProperty("cmkey",ConsumerKey.getText() );
		        FXMLvista_generalController.PROPERTIES.setProperty("cmSecret",ConsumerSecret.getText() );
		        FXMLvista_generalController.PROPERTIES.setProperty("token",AccessToken.getText() );
		        FXMLvista_generalController.PROPERTIES.setProperty("tokenSecret",AccessTokenSecret.getText() );
		        FXMLvista_generalController.cb = new ConfigurationBuilder();
		        FXMLvista_generalController.cb.setOAuthConsumerKey(FXMLvista_generalController.PROPERTIES.getProperty("cmkey"));
		        FXMLvista_generalController.cb.setOAuthConsumerSecret(FXMLvista_generalController.PROPERTIES.getProperty("cmSecret"));
		        FXMLvista_generalController.cb.setOAuthAccessToken(FXMLvista_generalController.PROPERTIES.getProperty("token"));
		        FXMLvista_generalController.cb.setOAuthAccessTokenSecret(FXMLvista_generalController.PROPERTIES.getProperty("tokenSecret"));
		        FXMLvista_generalController.twitter = new TwitterFactory(FXMLvista_generalController.cb.build()).getInstance();
		        Query query = new Query("test");
		        query.setCount(1);
		         try {
		        int count=0;
		        QueryResult r;		         
		        r = FXMLvista_generalController.twitter.search(query);
		         }
		         catch (TwitterException te) {
                          Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Twitter error");
                            alert.setContentText("Error de connexió  "+ te.getMessage());
                            alert.setResizable(true);
                            alert.getDialogPane().setPrefSize(680, 100);                             
                            alert.showAndWait();
                            return;
                         }
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("obre fitxer de configuració");
                        fileChooser.getExtensionFilters().addAll(
                                  new FileChooser.ExtensionFilter("conf", "*.conf")
                              ); 
                        final FileOutputStream fileOutputStream;
   
                        File file = fileChooser.showSaveDialog(new Stage());                        
                        if (!file.getName().endsWith(".gdf")) file= new File(file.getAbsolutePath()+".conf");
 
              
		        FXMLvista_generalController.propsFile =file.getAbsolutePath();
		        FXMLvista_generalController.PROPERTIES.setProperty("lastID","-1" );
		        try {
		            fileOutputStream = new FileOutputStream(file);
		        } catch (FileNotFoundException ex) {
		            throw new RuntimeException(ex);
		        }
		        try {
		        	FXMLvista_generalController.PROPERTIES.store(fileOutputStream, "twitter actions saved");
		        } catch(IOException ex) {
		            throw new RuntimeException(ex);
		        } finally {
		            try {
		                fileOutputStream.close();
		            } catch (IOException ex) {
		                throw new RuntimeException(ex);
		            }
		        }
                           Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Gravat fitxer de configuració ");
                            alert.setContentText("Configuració provada i gravada a   "+ file.getAbsolutePath());
                            alert.setResizable(true);
                            alert.getDialogPane().setPrefSize(680, 100);
 
                            alert.showAndWait();
 		        ((Node)(event.getSource())).getScene().getWindow().hide();

    
    
    }

    void initData(FXMLvista_generalController parent) {
        fileUploader=this;
    }
    
}
