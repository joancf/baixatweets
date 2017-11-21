/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baixatweets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import twitter4j.PagableResponseList;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;

import java.net.URL;
import java.time.Instant;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
/**
 *
 * @author joan
 */
public class FXMLvista_generalController implements Initializable {
   private static int MAX_FOLLOWERS=100;
   public static String propsFile;
    final public static Properties PROPERTIES = new Properties();
    public static ConfigurationBuilder cb;
    public static Twitter twitter;
    private MyTask processor;
    private ProcessStats stat;
    
    @FXML
    private Label label;
    @FXML
    private Button triaFitxer;
    @FXML
    private CheckBox usuaris;
    @FXML
    private Button creaFitxer;
    @FXML
    private Label instruccions;
    @FXML
    private CheckBox XarxaUsuaris;
    @FXML
    private TextField query;
    @FXML
    private Button processa;
    @FXML
    private CheckBox paraules;
    @FXML
    private CheckBox Tsenser;
    @FXML
    private CheckBox continua;
    @FXML
    private CheckBox cancela;
    @FXML
    private Label label2;
    @FXML
    private Label llenguaL;
    @FXML
    private TextField llengua;
    @FXML
    private Spinner<Integer> nRows;
     @FXML
    private Label lblProcessant;
    @FXML
    private TextField nrows2;
    @FXML
    private CheckBox follow;
    @FXML
    private Button continuaCancelat;

    @FXML
    private void continuaCancelat(ActionEvent event) {
        // reload the data
        try {
        stat=stat.restore_status();
        } catch (Exception e){
                       Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error en el fitxer  confConf ");
                alert.setContentText("problemes en obrir el fitxer de la última sessió" + e.getMessage());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(680, 100);
                alert.showAndWait();
                continuaCancelat.setDisable(true);
                return;
        }
        // reconnect twitter
         File file=new File(stat.confFile);
         if (!read_conf(file)) {
             // disable the button because there is no config file
             continuaCancelat.setDisable(true);
             return;
         }
        // setup the environment (buttons)
        continuaCancelat.setDisable(true);
        triaFitxer.setDisable(true);
        creaFitxer.setDisable(true);
   
        processa.setDisable(true);
        lblProcessant.setDisable(false);
        lblProcessant.setVisible(true);
        cancela.setVisible(true);
        cancela.setDisable(false);  
        stat.isCancelled = false;
        lblProcessant.setWrapText(true);
        lblProcessant.setTextAlignment(TextAlignment.JUSTIFY);
        XarxaUsuaris.setSelected(stat.XarxaUsuarisVal );
        follow.setSelected(stat.followVal);
        query.setText(stat.queryText);
        // TODO nRows.valueFactoryProperty().setValue(stat.nRowVal);
        processor = new Continue();
        cancela.setSelected(false);
        lblProcessant.textProperty().bind(processor.messageProperty());   
        // call the process...
        new Thread(processor).start();
        processa.setDisable(true);// 
    }

   
  
  
 
    



    @Override
    public void initialize(URL url, ResourceBundle rb) {
        nRows.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                nRows.increment(0); // won't change value, but will commit editor
            }
        });
        stat =  new ProcessStats();
    }

    @FXML
    private void handleTriaFitxer(ActionEvent event) {


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("obre fitxer de configuració");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("conf", "*.conf")
        );
 
        File file = fileChooser.showOpenDialog(new Stage());
        if (!read_conf(file)) return;

        triaFitxer.setDisable(true);
        creaFitxer.setDisable(true);
        continuaCancelat.setDisable(true);
    
        instruccions.setDisable(false);
        XarxaUsuaris.setDisable(false);
        query.setDisable(false);
        usuaris.setDisable(false);
        paraules.setDisable(false);
        Tsenser.setDisable(false);
        continua.setDisable(false);
        follow.setDisable(false);
        llengua.setDisable(false);
        llenguaL.setDisable(false);
        processa.setDisable(false);
        nRows.setDisable(false);
        lblProcessant.textProperty().set("");
    }

    @FXML
    private void handleCreaFitxer(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML_confFile.fxml"));
            Parent root1 = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Credencials de twitter");
            stage.setScene(new Scene(root1));

            FXML_confFileController controller
                    = loader.<FXML_confFileController>getController();
            controller.initData(this);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        continuaCancelat.setDisable(true);
        triaFitxer.setDisable(true);
        creaFitxer.setDisable(true);
        instruccions.setDisable(false);
        XarxaUsuaris.setDisable(false);
        query.setDisable(false);
        usuaris.setDisable(false);
        paraules.setDisable(false);
        Tsenser.setDisable(false);
        continua.setDisable(false);
        follow.setDisable(false);
        llengua.setDisable(false);
        llenguaL.setDisable(false);
        processa.setDisable(false);
        nRows.setDisable(false);

    }

    @FXML
    private void processa(ActionEvent event) {

        processa.setDisable(true);
        lblProcessant.setDisable(false);
        lblProcessant.setVisible(true);
        cancela.setVisible(true);
        cancela.setDisable(false);  
        stat.isCancelled = false;
        lblProcessant.setWrapText(true);
        lblProcessant.setTextAlignment(TextAlignment.JUSTIFY);
        stat.XarxaUsuarisVal = XarxaUsuaris.isSelected();
        stat.followVal = follow.isSelected();
        stat.nRowVal = nRows.getValue();

        stat.users = new HashMap<>();
        stat.retweets = new HashMap<>();
        stat.userMap = new HashMap<>();
        stat.userFollows = new HashMap<>();
        stat.userMent = new HashMap<>();
        stat.tweetMent = new HashMap<>();
        stat.tweets = new HashMap<>();
        stat.words = new HashMap<>();
        stat.llegits = 0;
        stat.maxID=0;
        
        //  copy the values to local variables
        stat.queryText = query.getText();
        processor = new Task2();
        cancela.setSelected(false);
        lblProcessant.textProperty().bind(processor.messageProperty());

        new Thread(processor).start();
        processa.setDisable(true);
    }

    private boolean read_conf(File file) {
        InputStream input;
       if (file != null) {
            try {
                input = new FileInputStream(file);
                propsFile = file.getAbsolutePath();
                stat.confFile=propsFile;
                PROPERTIES.load(input);
            } catch (IOException ex) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error en el fitxer  confConf ");
                alert.setContentText("problemes en obrir el fitxer de configruacio  ");
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(680, 100);
                alert.showAndWait();
                return false;
            }
            cb = new ConfigurationBuilder();
            cb.setOAuthConsumerKey(PROPERTIES.getProperty("cmkey"));
            cb.setOAuthConsumerSecret(PROPERTIES.getProperty("cmSecret"));
            cb.setOAuthAccessToken(PROPERTIES.getProperty("token"));
            cb.setOAuthAccessTokenSecret(PROPERTIES.getProperty("tokenSecret"));

            try {
                twitter = new TwitterFactory(cb.build()).getInstance();
             } catch (Exception te) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Twitter error");
                alert.setContentText("Error de connexió  " + te.getMessage());

                alert.showAndWait();

                return false;
            }
        } else {
            return false;
        }
       return true;
    }

   
        
    public class Task2 extends MyTask {
        @Override
        public void changeMessage(String m) {
            updateMessage(m);
        }

        @Override
        protected Object call() throws Exception {
            try{

            updateMessage("processant");


            if (stat.XarxaUsuarisVal) {
                 User user;
                try {
                    user = twitter.showUser(stat.queryText.replaceAll("^@", ""));
                    if (user != null) {
                   }                   
                    consulta_usuaris(user, stat.nRowVal, 0);
                } catch (TwitterException e1) {
                    e1.printStackTrace();
                }

                SaveUsers();
                 // ((Node)(event.getSource())).getScene().getWindow().hide();

            } else {
                int limit =  twitter.getRateLimitStatus().get("/search/tweets").getRemaining();
                int reset =  twitter.getRateLimitStatus().get("/search/tweets").getSecondsUntilReset();
               	while (limit==0 && reset >0 && !stat.isCancelled){
                    try {
                        processor.changeMessage(" quota esgotada"
                                + " segons fins a resset:" + reset);
                        Thread.sleep(2000);
                        reset = twitter.getRateLimitStatus().get("/search/tweets").getSecondsUntilReset();
                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FXMLvista_generalController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
              
                
                Query query1 = new Query(stat.queryText);
                if (!llengua.getText().contains("totes")) {
                    query1.setLang(llengua.getText());
                }
                 query1.resultType(Query.RECENT);
                //	If stat.maxID is -1, then this is our first call and we do not want to tell Twitter what the maximum
                //	tweet id is we want to retrieve.  But if it is not -1, then it represents the lowest tweet ID
                //	we've seen, so we want to start at it-1 (if we start at stat.maxID, we would see the lowest tweet
                //	a second time...
                if (continua.isSelected()) {
                    stat.maxID = new Long(PROPERTIES.getProperty("lastID"));
                    if (stat.maxID > 0) {
                        query1.setMaxId(stat.maxID - 1);
                    }
                }
                query1.setCount(stat.nRowVal);
                try {
                    saveResults(query1);
                    if (stat.followVal){
                        getFollowing();
                   }
                } catch (TwitterException te) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error de connexió");
                        alert.setContentText("Error de lectura segurament has passat el limit: " + te.getErrorMessage());
                        alert.setResizable(true);
                        alert.getDialogPane().setPrefSize(680, 100);
                        alert.showAndWait();
                    });
                    te.printStackTrace();
                    updateMessage("acabat amb error: " + te.getErrorMessage());

                }
                SaveTweets();
  
                FileOutputStream fileOutputStream;
                try {
                    fileOutputStream = new FileOutputStream(new File(propsFile));
                    PROPERTIES.store(fileOutputStream, "final with lastId " + stat.maxID + "saved");
                    fileOutputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();

                }
            }
            } catch (Exception e){
                e.printStackTrace();
            } 
           processa.setDisable(false);
            return true;
        }
 
    }
      private void getFollowing() {
            try{
             Set<String> usersL = stat.users.keySet();
             Set<User> usersD= new HashSet<>();
            for (String usuari :usersL){
                usersD.add(stat.users.get(usuari));
            }

           // For each user find to whom is following take no more than 100 people
            // extract the set because new stat.users will be appended during opperation
             for (User usuari : usersD){
                stat.current=usuari.getScreenName(); 
                consulta_following (usuari,MAX_FOLLOWERS);
                if (stat.isCancelled){
                  stat.saveStauts("stat.following"); 
                break;
                }
            }
            } catch (Exception e){
                e.printStackTrace();
            }
        
        }
     private void getFollowing_cont() {
            try{
             Set<String> usersL = stat.users.keySet();
             Set<User> usersD= new HashSet<>();
            for (String usuari :usersL){
                usersD.add(stat.users.get(usuari));
            }

           // For each user find to whom is following take no more than 100 people
            // extract the set because new stat.users will be appended during opperation
            boolean skip=true;
 
  
             for (User usuari : usersD){
                 if (skip){
                     if (stat.current.equals(usuari.getScreenName())) skip=false;
                     else continue;
                 }
                 stat.current=usuari.getScreenName(); 
                consulta_following (usuari,MAX_FOLLOWERS);
                if (stat.isCancelled) {
                stat.saveStauts("stat.following");                     
                break;
                }
            }
            } catch (Exception e){
                e.printStackTrace();
            }
        
        }
     
     
      public abstract class MyTask extends Task {
          abstract void changeMessage(String m);  
      } 
      
      public class Continue extends MyTask {

        @Override
        public void changeMessage(String m) {
            updateMessage(m);
        }

        @Override
        protected Object call() throws Exception {
  
            updateMessage("continue");


            if (stat.XarxaUsuarisVal) {
                 boolean skip=true;
                 for (User follower : stat.followers) {
                 if (skip){
                     if (stat.current.equals(follower.getScreenName())) skip=false;
                     else continue;
                 }
               stat.current=follower.getScreenName(); 
               consulta_followers(follower, MAX_FOLLOWERS);
                if(stat.isCancelled){
                    stat.saveStauts("stat.followers");
                    break;
                 }
            }     
            SaveUsers();
          
        } else if (stat.followVal){
             
                getFollowing_cont();
                 
           

                SaveTweets();

                FileOutputStream fileOutputStream;
                try {
                    fileOutputStream = new FileOutputStream(new File(propsFile));
                    PROPERTIES.store(fileOutputStream, "final with lastId " + stat.maxID + "saved");
                    fileOutputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();

                }
            }
           processa.setDisable(false);
  
          return true;
        }
 }

      private boolean consulta_usuaris(User user, int selection, int level) {
        // 
        // if allready a user then skip it
        stat.followers = new ArrayList<>();

        if (stat.users.get(user.getScreenName()) != null) {
            return false;
        }
        stat.users.put(user.getScreenName(), user);
        stat.userMap.put(user.getId(), user.getScreenName());
        System.out.println("adding user " + user.getScreenName());
        if (level > 1) {
            return true;
        }
        boolean segueix = true;
        HashMap<String, Integer> followed = new HashMap<>();
        long cursor = -1;
        int count = 0;
        try {
            do {
                Map<String, RateLimitStatus> limits = twitter.getRateLimitStatus();
                RateLimitStatus rateLimit = limits.get("/followers/list");
                if (rateLimit.getRemaining() < 1) {
                    //if (MessageDialog.openConfirm(shlTwitterControl,"Error de twitter","rate limit esgotat" + rateLimit.getRemaining()+
                    //    		" reset en: " + rateLimit.getSecondsUntilReset() +"segons, \n si fas OK esperarà / false acabarà")){
                    int waiting = rateLimit.getSecondsUntilReset();
                    long s = Instant.now().getEpochSecond()+waiting;
                    while (s > Instant.now().getEpochSecond()) {
                        processor.changeMessage( "stat.followers : "  + stat.llegits + " waiting  " + waiting);
                        try {
                           Thread.sleep(2000);
                       } catch (InterruptedException ie) {
                           //Don't worry about it.
                       } 
                         waiting -= 2;
                         if (stat.isCancelled) {
                            segueix = false;
                            break;
                        }
                    }
  
                    }
                if (!segueix) {
                        processor.changeMessage( "cancel-lat continuarà baixant stat.followers dels stat.followers però no seguidors del usuari principal " );
                        break;
                    }
                 PagableResponseList<User>  usersResponse = twitter.getFollowersList(user.getId(), cursor);
                cursor =  usersResponse.getNextCursor();
                System.out.println(user.getScreenName() + " " + count + "size() of iteration:" +  usersResponse.size());
                // iterate the stat.followers and get the followores of them ....
                stat.followers.addAll(usersResponse);
                stat.llegits += usersResponse.size();
                count += usersResponse.size();
                processor.changeMessage( "llegint seguidors " + stat.llegits + " processant "+ user.getScreenName() );

            } while (cursor > 0 && count < selection && segueix);

        } catch (TwitterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LinkedList<String> followsL= new LinkedList<>();
       for (User follower : stat.followers) {
            followed.put(follower.getScreenName(), 1);
            stat.userMap.put(follower.getId(), follower.getScreenName());
            followsL.add(follower.getScreenName());
       }
         stat.userFollows.put (user.getScreenName(),followsL); 
        if (!segueix) { 
            stat.current=stat.followers.get(0).getScreenName(); 
            stat.saveStauts("stat.followers");
            return false;
       }
        for (User follower : stat.followers) {
          stat.current=follower.getScreenName(); 
          segueix = consulta_followers(follower, selection);
            if(stat.isCancelled){
                 stat.saveStauts("stat.followers");
                return false;
            }
        }
        return true;
    }

    private boolean consulta_followers(User user, int selection) {
        ArrayList<User> followersL = new ArrayList<>();
        if (stat.users.get(user.getScreenName()) == null) {
           stat.users.put(user.getScreenName(), user);
        }
        boolean segueix = true;
        long cursor = -1;
        int count = 0;
        try {
            do {
                Map<String, RateLimitStatus> limits = twitter.getRateLimitStatus();
                RateLimitStatus rateLimit = limits.get("/followers/list");
                if (rateLimit.getRemaining() < 1) {
                    int waiting = rateLimit.getSecondsUntilReset();
                    if (waiting>0){
                    stat.saveStauts("stat.followers");
                    waiting++; // add a second for security
                    long s = Instant.now().getEpochSecond()+waiting;
                    while (s > Instant.now().getEpochSecond()){
                        processor.changeMessage("consulta seguidors " + user.getScreenName() + " amb  "+  user.getFollowersCount()+" seguidors ( total seguidors llegits: " + stat.llegits  +"). Programa aturat durant: " + waiting+ " segons");
                        try {
                           Thread.sleep(2000);
                       } catch (InterruptedException ie) {
                           //Don't worry about it.
                       } 
                       waiting -= 2;
                        if (stat.isCancelled) {
                            segueix = false;
                            break;
                        }
                    }
                    }
                    if (!segueix) {
                        break;
                    }
                       try {
                           Thread.sleep(10000); // dona temps després d'una pausa  a que es reconecti la màquina
                       } catch (InterruptedException ie) {//Don't worry about it.
                       } 
                }
                PagableResponseList<User> idsData = twitter.getFollowersList(user.getId(), cursor);
                cursor = idsData.getNextCursor();
                 processor.changeMessage("consulta veins " + user.getScreenName()  + " " + count + " de " + user.getFollowersCount());
                // iterate the followersL and get the followores of them ....
                stat.llegits +=  idsData.size();
                count +=  idsData.size();
                followersL.addAll( idsData);

            } while (cursor > 0 && count < selection && segueix);
          for (User friend : followersL) {
         // if user does not exist add it           
            if (!this.stat.users.containsKey(friend.getScreenName())) {
                stat.users.put(friend.getScreenName(),friend);
            }
            // add relationship between user and friend
            if (stat.userFollows.containsKey(user.getScreenName())){
                stat.userFollows.get(user.getScreenName()).add(friend.getScreenName());
            } else {
                
               LinkedList<String> friendsL= new LinkedList<>();
               friendsL.add(friend.getScreenName());
               stat.userFollows.put (user.getScreenName(),friendsL); 
            }
           
          }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return segueix;
    }

    private boolean consulta_following(User user,int limit) {
        // 
        // if allready a user then skip it
        ArrayList<User> friends = new ArrayList<>();
       
        String usuari= user.getScreenName();
        int friendsCount= user.getFriendsCount();
        boolean segueix = true;
        HashMap<String, Integer> following = new HashMap<>();
        long cursor = -1;
        int count = 0;
        try {
            do {
                Map<String, RateLimitStatus> limits = twitter.getRateLimitStatus();
                RateLimitStatus rateLimit = limits.get("/friends/list");
                if (rateLimit.getRemaining() < 1) {
                    //if (MessageDialog.openConfirm(shlTwitterControl,"Error de twitter","rate limit esgotat" + rateLimit.getRemaining()+
                    //    		" reset en: " + rateLimit.getSecondsUntilReset() +"segons, \n si fas OK esperarà / false acabarà")){
                    int waiting = rateLimit.getSecondsUntilReset();
                    if (waiting>0) {
                        waiting++; // add a second for security
                        long s = Instant.now().getEpochSecond()+waiting;
                        // save status
                        stat.saveStauts("stat.following"); 
                        while (s > Instant.now().getEpochSecond()) {
                            processor.changeMessage("consulta veins " +usuari + " (" + stat.llegits + ") waiting  " + waiting);
                            try {
                               Thread.sleep(2000);
                           } catch (InterruptedException ie) {
                               //Don't worry about it.
                           } 
                             waiting -= 2;
                            if (stat.isCancelled) {
                                segueix = false;
                                break;
                            }
                        }
                    }
                    if (!segueix) {
                        break;
                    }
                        try {
                          Thread.sleep(10000);
                      } catch (InterruptedException ie) {
                          //Don't worry about it.
                      } 
               }
                PagableResponseList<User> friendsData = twitter.getFriendsList(user.getId(), cursor);
                cursor = friendsData.getNextCursor();
                friends.addAll(friendsData);
                stat.llegits += friendsData.size();
                count += friendsData.size();  
                processor.changeMessage("consulta veins " + usuari  + " " + count + " de " + friendsCount +".");
                // iterate the stat.followers and get the followores of them ....

   
            } while (cursor > 0 && count < limit && segueix);

          for (User friend : friends) {
         // if user does not exist add it           
            if (!this.stat.users.containsKey(friend.getScreenName())) {
                stat.users.put(friend.getScreenName(),friend);
            }
            // add relationship between user and friend
            if (stat.userFollows.containsKey(usuari)){
                stat.userFollows.get(usuari).add(friend.getScreenName());
            } else {
                
               LinkedList<String> friendsL= new LinkedList<>();
               friendsL.add(friend.getScreenName());
               stat.userFollows.put (usuari,friendsL); 
            }
           
          }
          } catch (TwitterException  e) {
            e.printStackTrace();
        }

        return segueix;
    }
    
    protected void SaveUsers() {
        Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("guarda fitxer");
        alert.setContentText("Data processada, indica el fitxer on guardar");
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(680, 100);
        alert.showAndWait();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Fitxer on guardar la xarxa");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("gephi", "*.gdf")
        );
        final FileOutputStream fileOutputStream;

        File file = fileChooser.showSaveDialog(new Stage());
        if (!file.getName().endsWith(".gdf")) {
            file = new File(file.getAbsolutePath() + ".gdf");
        } 
        FileWriter f0;
        try {
            f0 = new FileWriter(file);
        } catch (IOException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error en fitxer ");
                alert.setContentText("Error en obrir el fitxer  " + e.getMessage());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(680, 100);
                alert.showAndWait();

            e.printStackTrace();
            return;
        }
        try {
            String newLine = System.getProperty("line.separator");
            // Write all the stat.tweets
            // write the headings....
            f0.write("nodedef>name VARCHAR,label VARCHAR,type VARCHAR, typeInt INT, lat DOUBLE,lng DOUBLE,stat.followers INT, "
                    + " favorites INT,lang VARCHAR,text VARCHAR" + newLine);
            // write the nodess....

            for (String id : stat.users.keySet()) {
                User user = stat.users.get(id);
                String line ;
                if (user != null) {
                    line = "@" + id + ",@" + id + ",user,2,,," + user.getFollowersCount() + "," + user.isVerified() + "," + user.getLang() + ",";
                } else {

                    line = "@" + id + ",@" + id + ",user,2,,,,,,";
                }
                f0.write(line + newLine);

            }
            // write the edges...
            f0.write("edgedef>node1 VARCHAR,node2 VARCHAR, weight DOUBLE,directed BOOLEAN, label VARCHAR, weight DOUBLE" + newLine);

            for (String user : stat.userFollows.keySet()) {
                LinkedList<String> list = stat.userFollows.get(user);
                for (String m : list) {
                    String line = "@" + user + ",@" + m + ",1,true,follow," ;
                    f0.write(line + newLine);
                }
            }

        } catch (IOException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error en fitxer ");
                alert.setContentText("Error en escriure el fitxer  " + e.getMessage());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(680, 100);
                alert.showAndWait();
            
            e.printStackTrace();
        }
        try {
            f0.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(stat.isCancelled){   
                System.exit(1);
         }

        });
    }

    protected void SaveTweets() {
        Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("guarda fitxer");
        alert.setContentText("Data processada, indica el fitxer on guardar");
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(680, 100);
        alert.showAndWait();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Fitxer on guardar la xarxa");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("gdf", "*.gdf")
        );

        File file = fileChooser.showSaveDialog(new Stage());
        if (!file.getName().endsWith(".gdf")) {
            file = new File(file.getAbsolutePath() + ".gdf");
        }
       
        // save the propoerties with the lastID
        PROPERTIES.setProperty("lastID", (new Long(stat.maxID)).toString());
        
        FileWriter f0;
        try {
            f0 = new FileWriter(file);
        } catch (IOException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error en fitxer ");
                alert.setContentText("Error en obrir el fitxer  " + e.getMessage());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(680, 100);
                alert.showAndWait();
                e.printStackTrace();
                return;
        }
        try {
            String newLine = System.getProperty("line.separator");
            // Write all the stat.tweets
            // write the headings....
            f0.write("nodedef>name VARCHAR,label VARCHAR,type VARCHAR, typeInt INT, lat DOUBLE,lng DOUBLE,stat.retweets INT, "
                    + " favorites INT,lang VARCHAR,text VARCHAR" + newLine);
            // write the nodess....
            for (Long id : stat.tweets.keySet()) {
                Status tweet = stat.tweets.get(id);
                String line ;
                if (tweet != null) {
                    String lat = "";
                    String lng = "";
                    if (tweet.getGeoLocation() != null) {
                        lat = "" + tweet.getGeoLocation().getLatitude();
                        lng = "" + tweet.getGeoLocation().getLongitude();
                    }
                    line = tweet.getId() + "," + tweet.getId() + ",tweet,0," + lat
                            + "," + lng + "," + tweet.getRetweetCount() + "," + tweet.getFavoriteCount() + ","
                            + tweet.getLang() + ",";
                    if (Tsenser.isSelected()) {
                        line += StringEscapeUtils.escapeCsv(tweet.getText().replaceAll("(\\r|\\n|\")", " "));
                    }
                } else {
                    line = id + "," + id + ",tweet,0,,,,,,";
                }

                f0.write(line + newLine);

            }

            // write stat.users 	
            for (String id : stat.users.keySet()) {
                User user = stat.users.get(id);
                String line ;
                if (user != null) {
                    line = "@" + id + ",@" + id + ",user,2,,," + user.getFollowersCount() + "," + user.getFriendsCount() + "," + user.getLang() + ",";
                } else {

                    line = "@" + id + ",@" + id + ",user,2,,,,,,";
                }
                f0.write(line + newLine);

            }
            // write stat.words 	
            for (String id : stat.words.keySet()) {
                String line = id + "," + id + ",word,1,,,,,,";
                f0.write(line + newLine);

            }
            // write the edges...
            f0.write("edgedef>node1 VARCHAR,node2 VARCHAR, weight DOUBLE,directed BOOLEAN, label VARCHAR, weight DOUBLE" + newLine);

            for (Long id : stat.tweets.keySet()) {
                Status tweet = stat.tweets.get(id);
                if (tweet != null) {

                    String line = "@" + tweet.getUser().getScreenName() + "," + id + ",1.0,true,author,1.0";
                    f0.write(line + newLine);

                    if (tweet.getRetweetedStatus() != null) {
                        line = id + "," + tweet.getRetweetedStatus().getId() + ",1.0,true,retweet,1.0";
                        f0.write(line + newLine);
                    }
                    if (tweet.getInReplyToStatusId() == 0) {
                        line = id + "," + tweet.getInReplyToStatusId() + ",1.0,true,reply,1.0";
                        f0.write(line + newLine);
                    }
                }
            }

            for (String word : stat.words.keySet()) {
                Set<Long> list = stat.words.get(word);
                for (long t : list) {
                    String line = t + "," + word + ",1.0,true,word,1.0";
                    f0.write(line + newLine);
                }
            }
            /*
		for (String user: stat.userMent.keySet()){
			HashMap<String,Integer> list = stat.userMent.get(user);
		    for (String m : list.keySet()){
	    		String line= "@"+user+",@"+m+",1.0,true,mention,"+list.get(m) ;			    
	    		 f0.write(line + newLine);		
		    }
		}
             */
            for (Long tweet : stat.tweetMent.keySet()) {
                Set<String> list = stat.tweetMent.get(tweet);
                for (String m : list) {
                    // avoid stat.users metioning themselves
                    if (null !=stat.tweets.get(tweet) && stat.tweets.get(tweet).getUser().getScreenName().equals(m)) continue;
                    String line = tweet + ",@" + m + ",1.0,true,mention,1.0";
                    f0.write(line + newLine);
                }
            }
           for (String user : stat.userFollows.keySet()) {
                LinkedList<String> list = stat.userFollows.get(user);
                for (String m : list) {
                    String line = "@" + user + ",@" + m + ",1.0,true,follows,1.0";
                    f0.write(line + newLine);
                }
            }

            for (String user : stat.retweets.keySet()) {
                HashSet<Long> list = stat.retweets.get(user);
                for (Long t : list) {
                     if (stat.tweets.get(t) !=null && stat.tweets.get(t).getUser().getScreenName().equals(user)) continue;
                     String line = "@" + user + ", " + t + ",1.0,true,retweet,1.0" ;
                    f0.write(line + newLine);
                }
            }

        } catch (IOException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error en fitxer ");
                alert.setContentText("Error en escriure el fitxer  " + e.getMessage());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(680, 100);
                alert.showAndWait();
                 e.printStackTrace();
        }
        try {
            f0.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(stat.isCancelled){   
                System.exit(1);
         }

        });
    }

    protected void saveResults(Query query) throws TwitterException {
        QueryResult r;
        RateLimitStatus serachTweetsRateLimit;
        int LIMIT = stat.nRowVal;

        do {
            r = twitter.search(query);
            ArrayList<Status> ts = (ArrayList<Status>) r.getTweets();
            for (int i = 0; i < ts.size() && stat.llegits < LIMIT; i++) {
                Status tweet = ts.get(i);
                if (stat.maxID < 1 || tweet.getId() < stat.maxID) {
                    if (tweet.getId() == 0) {
                        System.out.println("error on tweet id == 0");
                    }
                    stat.maxID = tweet.getId();
                }
                stat.llegits++;
                 addTweet(tweet);
            } //
            serachTweetsRateLimit = r.getRateLimitStatus();
            if (serachTweetsRateLimit.getRemaining() < 1) {
                final String txt = "rate limit esgotat " + serachTweetsRateLimit.getRemaining()
                        + " reset en: " + serachTweetsRateLimit.getSecondsUntilReset() + "segons, \n espera o si fas atura grabarà el que ha fet fins ara";
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Twitter error2");
                    alert.setContentText(txt);
                    alert.setResizable(true);
                    alert.getDialogPane().setPrefSize(680, 300);
                    alert.showAndWait();
                });
                int reset = r.getRateLimitStatus().getSecondsUntilReset();
                reset+=2;
               	while (reset >0 && !stat.isCancelled){
                        processor.changeMessage("guardats: "  + stat.llegits + " quota esgotada"
                                + " segons fins a resset:" + reset);
                       try {
                           Thread.sleep(2000);
                       } catch (InterruptedException ie) {
                           //Don't worry about it.
                       } 
                      reset-=2;
                 }
            } else {
                processor.changeMessage("guardats: "  + stat.llegits + " quota disponible:" + serachTweetsRateLimit.getRemaining() * 100
                        + " segons fins a resset:" + serachTweetsRateLimit.getSecondsUntilReset());

            }
        } while ((query = r.nextQuery()) != null && stat.llegits < LIMIT && ! stat.isCancelled);

    }

    private void addTweet(Status tweet) throws TwitterException {
         boolean skip=false;
         if (tweet.isRetweet()){ // is a retweet
                 long rtId= tweet.getRetweetedStatus().getId();
                 String uId = tweet.getUser().getScreenName();
                User user = tweet.getUser();
                 if (stat.tweets.containsKey(rtId)) {
                    skip=true;
                 } else {
                     tweet = tweet.getRetweetedStatus();
                 }
                 // add retweet from author to tweet 
                  if (usuaris.isSelected()) {
                        if (stat.retweets.containsKey(uId)){
                            stat.retweets.get(uId).add(rtId);
                         } else { 
                             HashSet<Long> rts= new HashSet<>();
                              rts.add(rtId) ;                              
                             stat.retweets.put(uId, rts);
                              stat.users.put(uId, user);
                         }
                     }
              }
             if (!skip) {
             stat.tweets.put(tweet.getId(), tweet);
             if (paraules.isSelected()) {
                 putWords(tweet.getId(), tweet.getText());
             }
             if (usuaris.isSelected()) {
                 putUsers(tweet);
             }
             }
    }

    
    private void putUsers(Status tweet) throws TwitterException {

        String id = tweet.getUser().getScreenName();

        stat.users.put(id, tweet.getUser());
        HashMap<String, Integer> ments;
        HashSet<String> ments2 = new HashSet<>();
        if (!stat.userMent.containsKey(id)) {
            ments = new HashMap<>();
        } else {
            ments = stat.userMent.get(id);
        }
        String reply = tweet.getInReplyToScreenName();
        if (reply != null) {
            
            if (!stat.users.containsKey(reply)) try {
                // shall we look for user info?
                User user = twitter.showUser("@"+reply);
                stat.users.put(reply, user);
                // it was null
                //users.put(reply,null);
            } catch (Exception e){
                System.out.println("exception....!!"+reply);
                e.printStackTrace();
            }
            ments2.add(reply);
            if (!ments.containsKey(reply)) {
                ments.put(reply, 1);

            } else {
                int mentions = ments.get(reply) + 1;
                ments.put(reply, mentions);
            }
        }
        UserMentionEntity[] mentions = tweet.getUserMentionEntities();
        for (UserMentionEntity mention : mentions) {
            reply = mention.getScreenName();
            ments2.add(reply);
            try{
            if (!stat.users.containsKey(reply)) {
                User user = twitter.showUser("@"+reply);
                stat.users.put(reply, user);
                //System.out.println(reply+"reply correct \n ");
            }
           if (!ments.containsKey(reply)) {
                ments.put(reply, 1);
            } else {
                int tmp = ments.get(reply) + 1;
                ments.put(reply, tmp);
            }
            } catch (Exception e){
                System.out.println("exception....finding metion reply!!"+reply);
                e.printStackTrace();
            }
 
        }
        stat.userMent.put(id, ments);
        stat.tweetMent.put(tweet.getId(), ments2);
    }

    private void putWords(long id, String text) {
        // splits the text into stat.words (using regex)
        // for each word longer than 3 letters it stores
        // it can use a filter to remove some stop stat.words...
        text = text.toLowerCase();
        String[] tokens = text.split("[-\\].\\s,)(\\[]");
        Pattern p = Pattern.compile("[#]?[a-z]{4,}");
        Matcher m = p.matcher("");

        for (String token : tokens) {
            token=StringUtils.stripAccents(token.toLowerCase());
            if (m.reset(token).matches()) {
                Set<Long> s;
                if (stat.words.containsKey(token)) {
                    s = stat.words.get(token);
                } else {
                    s = new HashSet<>();
                }
                s.add(id);
                stat.words.put(token, s);
            }
        }
    }

    @FXML
    private void xarxaUsers(ActionEvent event) {
        if (XarxaUsuaris.isSelected()) {
            usuaris.setDisable(true);
            paraules.setDisable(true);
            Tsenser.setDisable(true);
            continua.setDisable(true);
            follow.setDisable(true);
            llengua.setDisable(true);
        } else {
            usuaris.setDisable(false);
            paraules.setDisable(false);
            Tsenser.setDisable(false);
            continua.setDisable(false);
            follow.setDisable(false);
            llengua.setDisable(false);

        }

    }

    @FXML
    private void cancel_Running(ActionEvent event) {
        if (cancela.isSelected()) {
            processor.cancel();
            stat.isCancelled = true;
        } else {
            stat.isCancelled = false;
        }
    }

}
