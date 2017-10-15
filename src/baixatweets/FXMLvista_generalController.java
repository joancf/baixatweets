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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;

import twitter4j.IDs;
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
import javafx.scene.control.TextField;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author joan
 */
public class FXMLvista_generalController implements Initializable {

    private HashMap<Long, Status> tweets;
    private HashMap<String, Set<Long>> words;
    private HashMap<String, User> users;
    private HashMap<Long, String> userMap;
    private HashMap<String, HashMap<String, Integer>> userMent;
    private HashMap<Long, LinkedList<String>> tweetMent;
    private HashMap<String, LinkedList<String>> userFollows; // from user screenName to user screenName
    private HashMap<String, LinkedList<Long>> retweets; // from user to tweet
    private long maxID;
    public static String propsFile;
    final public static Properties PROPERTIES = new Properties();
    public static ConfigurationBuilder cb;
    public static Twitter twitter;
    private long llegits;
    private Task2 processor;

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
    private String queryText;
    private boolean XarxaUsuarisVal;
    private boolean followVal;
    private Integer nRowVal;
    private boolean isCancelled;
    @FXML
    private TextField nrows2;
    @FXML
    private CheckBox follow;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        nRows.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                nRows.increment(0); // won't change value, but will commit editor
            }
        });
    }

    @FXML
    private void handleTriaFitxer(ActionEvent event) {
        int limit = 0;
        int secs = 0;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("obre fitxer de configuració");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("conf", "*.conf")
        );
        InputStream input;

        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            try {
                input = new FileInputStream(file);
                propsFile = file.getAbsolutePath();
                PROPERTIES.load(input);
            } catch (IOException ex) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error en el fitxer  confConf ");
                alert.setContentText("problemes en obrir el fitxer de configruacio  ");
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(680, 100);
                alert.showAndWait();
                return;
            }
            cb = new ConfigurationBuilder();
            cb.setOAuthConsumerKey(PROPERTIES.getProperty("cmkey"));
            cb.setOAuthConsumerSecret(PROPERTIES.getProperty("cmSecret"));
            cb.setOAuthAccessToken(PROPERTIES.getProperty("token"));
            cb.setOAuthAccessTokenSecret(PROPERTIES.getProperty("tokenSecret"));

            try {
                twitter = new TwitterFactory(cb.build()).getInstance();
                limit = twitter.getRateLimitStatus().get("/search/tweets").getLimit();
             } catch (TwitterException te) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Twitter error");
                alert.setContentText("Error de connexió amb rate limit " + limit
                        + "reset en: " + secs + "sense connexió comprova el fitxer ");
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(680, 100);
                alert.showAndWait();
                return;
            } catch (Exception te) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Twitter error");
                alert.setContentText("Error de connexió  " + te.getMessage());

                alert.showAndWait();

                return;
            }
        } else {
            return;
        }

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
        cancela.setDisable(false);  isCancelled = false;
        lblProcessant.setWrapText(true);
        lblProcessant.setTextAlignment(TextAlignment.JUSTIFY);
        XarxaUsuarisVal = XarxaUsuaris.isSelected();
        followVal = follow.isSelected();
        nRowVal = nRows.getValue();

        //  copy the values to local variables
        queryText = query.getText();
        processor = new Task2();
        cancela.setSelected(false);
        lblProcessant.textProperty().bind(processor.messageProperty());

        new Thread(processor).start();
        processa.setDisable(true);
    }

    
    public class Task2 extends Task {

        public void changeMessage(String m) {
            updateMessage(m);
        }

        @Override
        protected Object call() throws Exception {
            try{
            llegits = 0;
            maxID=0;
            updateMessage("processant");
            users = new HashMap<>();
            retweets = new HashMap<>();
            userMap = new HashMap<>();
            userFollows = new HashMap<>();
            userMent = new HashMap<>();
            tweetMent = new HashMap<>();
            tweets = new HashMap<>();
            words = new HashMap<>();

            if (XarxaUsuarisVal) {
                 User user;
                try {
                    user = twitter.showUser(queryText.replaceAll("^@", ""));
                    if (user != null) {
                   }                   
                    consulta_usuaris(user, nRowVal, 0);
                } catch (TwitterException e1) {
                    e1.printStackTrace();
                }
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
                    SaveUsers(file);
                 });
               
                // ((Node)(event.getSource())).getScene().getWindow().hide();

            } else {
                int limit =  twitter.getRateLimitStatus().get("/search/tweets").getRemaining();
                int reset =  twitter.getRateLimitStatus().get("/search/tweets").getSecondsUntilReset();
               	while (limit==0 && reset >0 && !isCancelled){
                    try {
                        processor.changeMessage(" quota esgotada"
                                + " segons fins a resset:" + reset);
                        Thread.sleep(2000);
                        reset = twitter.getRateLimitStatus().get("/search/tweets").getSecondsUntilReset();
                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FXMLvista_generalController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
              
                
                Query query1 = new Query(queryText);
                if (!llengua.getText().contains("totes")) {
                    query1.setLang(llengua.getText());
                }
                 query1.resultType(Query.RECENT);
                //	If maxID is -1, then this is our first call and we do not want to tell Twitter what the maximum
                //	tweet id is we want to retrieve.  But if it is not -1, then it represents the lowest tweet ID
                //	we've seen, so we want to start at it-1 (if we start at maxID, we would see the lowest tweet
                //	a second time...
                if (continua.isSelected()) {
                    maxID = new Long(PROPERTIES.getProperty("lastID"));
                    if (maxID > 0) {
                        query1.setMaxId(maxID - 1);
                    }
                }
                query1.setCount(nRowVal);
                try {
                    saveResults(query1);
                    if (followVal){
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
                    SaveTweets(file);
                    // save the propoerties with the lastID
                    PROPERTIES.setProperty("lastID", (new Long(maxID)).toString());
                });
                FileOutputStream fileOutputStream;
                try {
                    fileOutputStream = new FileOutputStream(new File(propsFile));
                    PROPERTIES.store(fileOutputStream, "final with lastId " + maxID + "saved");
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
    ;

        private void getFollowing() {
            try{
             Set<String> usersL = users.keySet();
             Set<User> usersD= new HashSet<>();
            for (String usuari :usersL){
                usersD.add(users.get(usuari));
            }

           // For each user find to whom is following take no more than 100 people
            // extract the set because new users will be appended during opperation
             for (User usuari :usersD){
                consulta_following (usuari,100);
                if (isCancelled) break;
            }
            } catch (Exception e){
                e.printStackTrace();
            }
        
        }
  

    }
        	


      private boolean consulta_usuaris(User user, int selection, int level) {
        // 
        // if allready a user then skip it
        ArrayList<User> followers = new ArrayList<>();

        if (users.get(user.getScreenName()) != null) {
            return false;
        }
        users.put(user.getScreenName(), user);
        userMap.put(user.getId(), user.getScreenName());
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
                    while (waiting > 0) {
                        processor.changeMessage( "followers : "  + llegits + " waiting  " + waiting);

                        Thread.sleep(2000);
                        waiting -= 2;
                        if (isCancelled) {
                            segueix = false;
                            break;
                        }
                    }
                    if (!segueix) {
                        break;
                    }
                }
                PagableResponseList<User> usersResponse = twitter.getFollowersList(user.getId(), cursor);
                cursor = usersResponse.getNextCursor();
                System.out.println(user.getScreenName() + " " + count + "size() of iteration:" + +usersResponse.size());
                // iterate the followers and get the followores of them ....
                followers.addAll(usersResponse);
                llegits += usersResponse.size();
                count += usersResponse.size();
                processor.changeMessage( "llegint seguidors " + llegits + " processant "+ user.getScreenName() );

            } while (cursor > 0 && count < selection && segueix);

        } catch (TwitterException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LinkedList<String> followsL= new LinkedList<>();
       for (User follower : followers) {
            followed.put(follower.getScreenName(), 1);
            userMap.put(follower.getId(), follower.getScreenName());
            followsL.add(follower.getScreenName());
       }
         userFollows.put (user.getScreenName(),followsL); 
        if (!segueix) {
            return false;
        }
        for (User follower : followers) {
            segueix = consulta_followers(follower, selection);
            if (!segueix || isCancelled ) {
                return false;
            }
        }
        return true;
    }

    private boolean consulta_followers(User user, int selection) {
        ArrayList<User> followers = new ArrayList<>();
        if (users.get(user.getScreenName()) == null) {
           users.put(user.getScreenName(), user);
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
                    if (waiting>0) waiting++; // add a second for security
                    while (waiting > 0) {
                        processor.changeMessage("consulta veins " + user.getScreenName() + " (" + llegits + "de " + user.getFollowersCount() +" waiting  " + waiting);
                        Thread.sleep(2000);
                        waiting -= 2;
                        if (isCancelled) {
                            segueix = false;
                            break;
                        }
                    }
                    if (!segueix) {
                        break;
                    }
                }
                PagableResponseList<User> idsData = twitter.getFollowersList(user.getId(), cursor);
                cursor = idsData.getNextCursor();
                 processor.changeMessage("consulta veins " + user.getScreenName()  + " " + count + " de " + user.getFollowersCount());
                // iterate the followers and get the followores of them ....
                llegits +=  idsData.size();
                count +=  idsData.size();
                followers.addAll( idsData);

            } while (cursor > 0 && count < selection && segueix);
          for (User friend : followers) {
         // if user does not exist add it           
            if (!this.users.containsKey(friend.getScreenName())) {
                users.put(friend.getScreenName(),friend);
            }
            // add relationship between user and friend
            if (userFollows.containsKey(user.getScreenName())){
                userFollows.get(user.getScreenName()).add(friend.getScreenName());
            } else {
                
               LinkedList<String> friendsL= new LinkedList<>();
               friendsL.add(friend.getScreenName());
               userFollows.put (user.getScreenName(),friendsL); 
            }
           
          }
        } catch (TwitterException | InterruptedException e) {
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
                    if (waiting>0) 
                        waiting++; // add a second for security
                    while (waiting > 0) {
                        processor.changeMessage("consulta veins " +usuari + " (" + llegits + ") waiting  " + waiting);
                        Thread.sleep(2000);
                        waiting -= 2;
                        if (isCancelled) {
                            segueix = false;
                            break;
                        }
                    }
                    if (!segueix) {
                        break;
                    }
                }
                PagableResponseList<User> friendsData = twitter.getFriendsList(user.getId(), cursor);
                cursor = friendsData.getNextCursor();
                friends.addAll(friendsData);
                llegits += friendsData.size();
                count += friendsData.size();  
                processor.changeMessage("consulta veins " + usuari  + " " + count + " de " + friendsCount +".");
                // iterate the followers and get the followores of them ....

   
            } while (cursor > 0 && count < limit && segueix);

          for (User friend : friends) {
         // if user does not exist add it           
            if (!this.users.containsKey(friend.getScreenName())) {
                users.put(friend.getScreenName(),friend);
            }
            // add relationship between user and friend
            if (userFollows.containsKey(usuari)){
                userFollows.get(usuari).add(friend.getScreenName());
            } else {
                
               LinkedList<String> friendsL= new LinkedList<>();
               friendsL.add(friend.getScreenName());
               userFollows.put (usuari,friendsL); 
            }
           
          }
          } catch (Exception e) {
            e.printStackTrace();
        }

        return segueix;
    }
    
    protected void SaveUsers(File selected) {

        FileWriter f0;
        try {
            f0 = new FileWriter(selected);
        } catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error en fitxer ");
                alert.setContentText("Error en obrir el fitxer  " + e.getMessage());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(680, 100);
                alert.showAndWait();
            });

            e.printStackTrace();
            return;
        }
        try {
            String newLine = System.getProperty("line.separator");
            // Write all the tweets
            // write the headings....
            f0.write("nodedef>name VARCHAR,label VARCHAR,type VARCHAR, typeInt INT, lat DOUBLE,lng DOUBLE,followers INT, "
                    + " favorites INT,lang VARCHAR,text VARCHAR" + newLine);
            // write the nodess....

            for (String id : users.keySet()) {
                User user = users.get(id);
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

            for (String user : userFollows.keySet()) {
                LinkedList<String> list = userFollows.get(user);
                for (String m : list) {
                    String line = "@" + user + ",@" + m + ",1,true,follow," ;
                    f0.write(line + newLine);
                }
            }

        } catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error en fitxer ");
                alert.setContentText("Error en escriure el fitxer  " + e.getMessage());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(680, 100);
                alert.showAndWait();
            });
            e.printStackTrace();
        }
        try {
            f0.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void SaveTweets(File selected) {

        FileWriter f0;
        try {
            f0 = new FileWriter(selected);
        } catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error en fitxer ");
                alert.setContentText("Error en obrir el fitxer  " + e.getMessage());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(680, 100);
                alert.showAndWait();
            });
            e.printStackTrace();
            return;
        }
        try {
            String newLine = System.getProperty("line.separator");
            // Write all the tweets
            // write the headings....
            f0.write("nodedef>name VARCHAR,label VARCHAR,type VARCHAR, typeInt INT, lat DOUBLE,lng DOUBLE,retweets INT, "
                    + " favorites INT,lang VARCHAR,text VARCHAR" + newLine);
            // write the nodess....
            for (Long id : tweets.keySet()) {
                Status tweet = tweets.get(id);
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
                        line += StringEscapeUtils.escapeCsv(tweet.getText().replaceAll("(\\r|\\n)", " "));
                    }
                } else {
                    line = id + "," + id + ",tweet,0,,,,,,";
                }

                f0.write(line + newLine);

            }

            // write users 	
            for (String id : users.keySet()) {
                User user = users.get(id);
                String line ;
                if (user != null) {
                    line = "@" + id + ",@" + id + ",user,2,,," + user.getFollowersCount() + "," + user.getFriendsCount() + "," + user.getLang() + ",";
                } else {

                    line = "@" + id + ",@" + id + ",user,2,,,,,,";
                }
                f0.write(line + newLine);

            }
            // write words 	
            for (String id : words.keySet()) {
                String line = id + "," + id + ",word,1,,,,,,";
                f0.write(line + newLine);

            }
            // write the edges...
            f0.write("edgedef>node1 VARCHAR,node2 VARCHAR, weight DOUBLE,directed BOOLEAN, label VARCHAR, weight DOUBLE" + newLine);

            for (Long id : tweets.keySet()) {
                Status tweet = tweets.get(id);
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

            for (String word : words.keySet()) {
                Set<Long> list = words.get(word);
                for (long t : list) {
                    String line = t + "," + word + ",1.0,true,word,1.0";
                    f0.write(line + newLine);
                }
            }
            /*
		for (String user: userMent.keySet()){
			HashMap<String,Integer> list = userMent.get(user);
		    for (String m : list.keySet()){
	    		String line= "@"+user+",@"+m+",1.0,true,mention,"+list.get(m) ;			    
	    		 f0.write(line + newLine);		
		    }
		}
             */
            for (Long tweet : tweetMent.keySet()) {
                LinkedList<String> list = tweetMent.get(tweet);
                for (String m : list) {
                    String line = tweet + ",@" + m + ",1.0,true,mention,1.0";
                    f0.write(line + newLine);
                }
            }
           for (String user : userFollows.keySet()) {
                LinkedList<String> list = userFollows.get(user);
                for (String m : list) {
                    String line = "@" + user + ",@" + m + ",1.0,true,follows,1.0";
                    f0.write(line + newLine);
                }
            }

            for (String user : retweets.keySet()) {
                LinkedList<Long> list = retweets.get(user);
                for (Long t : list) {
                    String line = "@" + user + ", " + t + ",1.0,true,retweet,1.0" ;
                    f0.write(line + newLine);
                }
            }

        } catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error en fitxer ");
                alert.setContentText("Error en escriure el fitxer  " + e.getMessage());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(680, 100);
                alert.showAndWait();
            });
            e.printStackTrace();
        }
        try {
            f0.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void saveResults(Query query) throws TwitterException {
        QueryResult r;
        RateLimitStatus serachTweetsRateLimit;
        int LIMIT = nRowVal;

        do {
            r = twitter.search(query);
            ArrayList<Status> ts = (ArrayList<Status>) r.getTweets();
            for (int i = 0; i < ts.size() && llegits < LIMIT; i++) {
                Status tweet = ts.get(i);
                if (maxID < 1 || tweet.getId() < maxID) {
                    if (tweet.getId() == 0) {
                        System.out.println("error on tweet id == 0");
                    }
                    maxID = tweet.getId();
                }
                llegits++;
                 addTweet(tweet);
            } //
            serachTweetsRateLimit = r.getRateLimitStatus();
            if (serachTweetsRateLimit.getRemaining() < 1) {
                final String txt = "rate limit esgotat " + serachTweetsRateLimit.getRemaining()
                        + " reset en: " + serachTweetsRateLimit.getSecondsUntilReset() + "segons, \n espera o si fas cancel grabarà el que ha fet fins ara";
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Twitter error2");
                    alert.setContentText(txt);
                    alert.setResizable(true);
                    alert.getDialogPane().setPrefSize(680, 300);
                    alert.showAndWait();
                });
                int reset = r.getRateLimitStatus().getSecondsUntilReset();
               	while (reset >0 && !isCancelled){
                    try {
                        processor.changeMessage("guardats: "  + llegits + " quota esgotada"
                                + " segons fins a resset:" + reset);
                        Thread.sleep(2000);
                        reset = twitter.getRateLimitStatus().get("/search/tweets").getSecondsUntilReset();
                     } catch (InterruptedException ex) {
                        Logger.getLogger(FXMLvista_generalController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                processor.changeMessage("guardats: "  + llegits + " quota disponible:" + serachTweetsRateLimit.getRemaining() * 100
                        + " segons fins a resset:" + serachTweetsRateLimit.getSecondsUntilReset());

            }
        } while ((query = r.nextQuery()) != null && llegits < LIMIT && ! isCancelled);
        
        // find the original tweets of the retweets nd reply
        // TODO it needs to be checked as duplicate tweets appear.
        /* 
		HashMap<Long,Status> extraTweets=new  HashMap<Long,Status>();

		processat.setText(count +"("+llegits+") expansió ids");
		
		for (Long id : tweets.keySet()) {
		    Status  tweet=  tweets.get(id);
		    if (tweet==null && extraTweets.get(id)==null){ 
		    	// try to get the tweet
		    		Status newTweet=twitter.showStatus(id);
		    	    if (newTweet  != null) {
		    	    	extraTweets.put(id,newTweet);
		    	       if (usuaris.isSelected() ) putUsers(newTweet);
		    	    }
		     } else	if (tweet.getRetweetedStatus()!=null){
		    		long rId=tweet.getRetweetedStatus().getId();
		    		if (extraTweets.get(rId)==null && tweets.get(rId)==null){
		    			Status newTweet=twitter.showStatus(rId);
		    			if (newTweet  != null){ 
		    				extraTweets.put(id,newTweet);
			    	       if (usuaris.isSelected() ) putUsers(newTweet);
		    			}
		    		}
		     } else	if (tweet.getInReplyToStatusId()==0){
		    		long rId=tweet.getInReplyToStatusId();
		    		if (extraTweets.get(rId)==null && tweets.get(rId)==null){
		    			Status newTweet=twitter.showStatus(rId);
		    			if (newTweet  != null) {
		    				extraTweets.put(id,newTweet);
		    				if (usuaris.isSelected() ) putUsers(newTweet);
		    			}
			    	}
		     }	    		
		}
        tweets.putAll(extraTweets);
         */
    }

    private void addTweet(Status tweet) throws TwitterException {
         boolean skip=false;
         if (tweet.isRetweet()){ // is a retweet
                 long rtId= tweet.getRetweetedStatus().getId();
                 String uId = tweet.getUser().getScreenName();
                User user = tweet.getUser();
                 if (tweets.containsKey(rtId)) {
                    skip=true;
                 } else {
                     tweet = tweet.getRetweetedStatus();
                  // add retweet from author to tweet 
                    if (usuaris.isSelected()) {
                        if (retweets.containsKey(uId)){
                            retweets.get(uId).add(rtId);
                         } else { 
                             LinkedList<Long> rts= new LinkedList<>();
                              rts.add(rtId) ;                              
                             retweets.put(uId, rts);
                              users.put(uId, user);
                         }
                     }
                 }  
             }
             if (!skip) {
             tweets.put(tweet.getId(), tweet);
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

        users.put(id, tweet.getUser());
        HashMap<String, Integer> ments;
        LinkedList<String> ments2 = new LinkedList<>();
        if (!userMent.containsKey(id)) {
            ments = new HashMap<>();
        } else {
            ments = userMent.get(id);
        }
        String reply = tweet.getInReplyToScreenName();
        if (reply != null) {
            if (!users.containsKey(reply)) {
                // shall we look for user info?
                User user = twitter.showUser(reply);
                users.put(reply, user);
                // it was null
                //users.put(reply,null);
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
            if (!users.containsKey(reply)) {
                User user = twitter.showUser(reply);
                users.put(reply, user);
                //users.put(reply,null);
            }
            if (!ments.containsKey(reply)) {
                ments.put(reply, 1);
            } else {
                int tmp = ments.get(reply) + 1;
                ments.put(reply, tmp);
            }
        }
        userMent.put(id, ments);
        tweetMent.put(tweet.getId(), ments2);
    }

    private void putWords(long id, String text) {
        // splits the text into words (using regex)
        // for each word longer than 3 letters it stores
        // it can use a filter to remove some stop words...
        text = text.toLowerCase();
        String[] tokens = text.split("[-\\].\\s,)(\\[]");
        Pattern p = Pattern.compile("[#]?[a-z]{4,}");
        Matcher m = p.matcher("");

        for (String token : tokens) {
            if (m.reset(token).matches()) {
                Set<Long> s;
                if (words.containsKey(token)) {
                    s = words.get(token);
                } else {
                    s = new HashSet<>();
                }
                s.add(id);
                words.put(token, s);
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
            isCancelled = true;
        } else {
            isCancelled = false;
        }
    }

}
