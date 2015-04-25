TweetSentiment
==============
Qiurui Jin  
Tianlong Li
## Overview
TweetSentiment is an app that shows the coordinates of Tweets and the heatmap of them. It feeds user using both data from database and real-time streamed data, with sentiment analysis of collected tweets.

Demo: [TweetSentiment](http://tweetsentiment15.elasticbeanstalk.com/)

## Usage

#### General

A tweet timeline window will keep feeding user with latest tweets streamed from Twitter regardless of modes.

#### Normal Mode
1. You can view clustered tweets as scatter points or heat map.
2. You can click each marker in scatter view to see embedded tweets, and profit.
3. Slide the time range to specify your desired time interval of tweets.

#### Real Time Mode
1. Click the real time mode button to enter.
2. Real time tweets will pop up all over the world!
3. Like scatter view, you can click each marker in the scatter view to see embedded tweets.

#### Sentiment
1. Average sentiment will be calculated and displayed with corresponding colors.
2. Real time trends of streamed sentiment will be visualized in real time mode.


## Design
#### Tweets Collection
Twitter4J library is used to collect streamed tweets from Twitter Streaming API.

#### Tweets Database
RDS is serving as the database for storing up to 100 MB of tweets.

#### Message Queue and Worker
Amazon SNS and SQS are used to serve the message queue and Aamazon Beanstalk's default worker tier is used to initiate worker to interact with message queue. Worker logic can be found in [**TweetSentimentWorker**](https://github.com/MonkeyLeeT/TweetSentimentWorker).

#### Dependencies
Google Map API and GSON library, Bootstrap, jQuery, D3.js, Websocket.

#### Issues
1. Tomcat 7+ provides built-in library for websocket, which will break the websocket-api.jar if used together. Just use Tomcat's websocket api and modify the calling style, since most online examples are based on websocket-api.jar lib.
2. To make websocket work, you need to modify the security group rule of the instance running this app as well as the security group rule of the load balancer of this instance, in addition to the listener rule of load balancer to route outside request on load balancer port to inside port of the instance. Refer to load balancer document for better understanding. 