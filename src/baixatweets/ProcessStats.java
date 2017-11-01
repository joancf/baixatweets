/*
 * Copyright (C) 2017 joan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package baixatweets;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import twitter4j.Status;
import twitter4j.User;

/**
 *
 * @author joan
 */
public class ProcessStats implements Serializable {
    
       public String savedStatus; 
       public String confFile; 
       public String current;
       public HashMap<Long, Status> tweets;
       public HashMap<String, Set<Long>> words;
       public HashMap<String, User> users;
       public HashMap<Long, String> userMap;
       public HashMap<String, HashMap<String, Integer>> userMent;
       public HashMap<Long, HashSet<String>> tweetMent;
       public HashMap<String, LinkedList<String>> userFollows; // from user screenName to user screenName
       public HashMap<String, HashSet<Long>> retweets; // from user to tweet
       public ArrayList<User> followers;
       public long maxID;  
       public int llegits;
       public String queryText;
       public boolean XarxaUsuarisVal;
       public boolean followVal;
       public Integer nRowVal;
       public boolean isCancelled;
       
       public void saveStauts(String status) {
         // Serialize the original class object
        try {
            this.savedStatus=status;
            String homeDir = System.getProperty("user.home"); 
            FileOutputStream fo = new FileOutputStream(homeDir +"/btwts.tmp");
            ObjectOutputStream so = new ObjectOutputStream(fo);
            so.writeObject(this);
            so.flush();
            so.close();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
           // System.exit(1);
        }
        return;
        }

       public ProcessStats restore_status() throws Exception {
  
        // Deserialize in to new class object
            String homeDir = System.getProperty("user.home");
            FileInputStream fi = new FileInputStream(homeDir +"/btwts.tmp");
            ObjectInputStream si = new ObjectInputStream(fi);
            ProcessStats  stat = (ProcessStats) si.readObject();
            si.close();
            return stat;
    }  

}