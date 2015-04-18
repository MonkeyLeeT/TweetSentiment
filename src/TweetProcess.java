import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import com.amazonaws.auth.PropertiesCredentials;
import com.google.gson.Gson;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * <p>This is a code example of Twitter4J Streaming API - sample method support.<br>
 * Usage: java twitter4j.examples.PrintSampleStream<br>
 * </p>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class TweetProcess {
	SQS sqs;
	String queueUrl;
	TwitterStream twitterStream;
	static Gson gson = new Gson();
	
	
    /**
     * Main entry of this application.
     * @param args
     * @throws IOException 
     */
	public TweetProcess() throws IOException {
    	Properties twitterKey = new Properties();
    	twitterKey.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("credentials.ini"));
    	ConfigurationBuilder cb = new ConfigurationBuilder();
         cb.setDebugEnabled(true)
           .setOAuthConsumerKey(twitterKey.getProperty("consumerKey"))
           .setOAuthConsumerSecret(twitterKey.getProperty("consumerSecret"))
           .setOAuthAccessToken(twitterKey.getProperty("token"))
           .setOAuthAccessTokenSecret(twitterKey.getProperty("tokenSecret"));
         
        PropertiesCredentials propertiesCredentials = new PropertiesCredentials(Thread.currentThread().getContextClassLoader().getResourceAsStream("credentials.ini"));
        sqs = new SQS(propertiesCredentials);
        queueUrl = "https://sqs.us-east-1.amazonaws.com/328246660824/TweetSentimentQueue";
        //System.out.println(queueUrl);
        
        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        StatusListener listener = new StatusListener() {
        	
        	public void handleTweet(String keyword, String id_str, String text, String user, String created_at, double latitude, double longitude){	
                System.out.println("Keyword:" + keyword + ", user:" + user + ", str_id:" + id_str);
        		Rds rds = Rds.getInstance();
        		if (!rds.isPasswordSet()) {
        			rds.setPassword(readPass());
        		}
                rds.insert(String.valueOf(id_str), keyword, user, text, String.valueOf(latitude), String.valueOf(longitude), created_at);
                Tweet tweet = new Tweet(id_str, created_at, text, user, longitude, latitude);
                //System.out.println(gson.toJson(tweet));
                sqs.sendMessage(queueUrl, gson.toJson(tweet));
        	}
        	
            @Override
            public void onStatus(Status status) {
            	if (status.getGeoLocation() != null){
            		String text=status.getText().toLowerCase();
            		String keyword=null;
            		
            		if (text.contains("food")) 
            			keyword="food";
            		else if (text.contains("game"))
            			keyword="game";
            		else if (text.contains("love"))
            			keyword="love";
            		else if (text.contains("sport"))
            			keyword="sport";
            		
            		if (keyword != null && status.getGeoLocation() != null)
            			handleTweet(keyword,String.valueOf(status.getId()), status.getText(), status.getUser().getScreenName(), status.getCreatedAt().toString(), status.getGeoLocation().getLatitude(), status.getGeoLocation().getLongitude());
            	}
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                //System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                //System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                //System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                //ex.printStackTrace();
            }
        };
        
        FilterQuery fq = new FilterQuery();
        String keys[] = {"food", "game", "sport", "love"};
        fq.track(keys);
        twitterStream.addListener(listener);
	    twitterStream.filter(fq); 
	}
    
	@SuppressWarnings("resource")
	private String readPass() {
		InputStream password = Thread.currentThread().getContextClassLoader().getResourceAsStream("credentials.ini");
        String pass = null;
        pass = new Scanner(password).next();
        return pass;
	}
	
}
