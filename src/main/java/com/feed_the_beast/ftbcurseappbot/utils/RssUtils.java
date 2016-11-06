package com.feed_the_beast.ftbcurseappbot.utils;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class RssUtils {
    @Nullable
    public static SyndFeed getFeed (String url) {
        try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(new URL(url)));
            return feed;
        } catch (FeedException | IOException e) {
            e.printStackTrace();
            //TODO implement this
        }
        return null;
    }

    public static List<SyndEntryImpl> getFeedAfter (SyndFeed feed, long time) {
        List<SyndEntryImpl> l = feed.getEntries();
        return l.stream().filter(line -> check(line, time))
                .collect(Collectors.toList());
    }

    public static boolean check (SyndEntryImpl l, long time) {
        if (l.getUpdatedDate() != null) {
            return l.getUpdatedDate().getTime() >= (time);
        } else {
            return l.getPublishedDate().getTime() >= (time);
        }
    }

    public static void main (String args[]) {
        SyndFeed feed = getFeed("http://status.aws.amazon.com/rss/all.rss");
        //feed.getEntries().forEach(System.out::println);
        System.out.println("---------------");
        Date d = new Date((2016 - 1900), 9, 6);
        System.out.println("date: " + d.getTime());
        List<SyndEntryImpl> l = getFeedAfter(feed, d.getTime());
        System.out.println("size: " + l.size());
        l.forEach(System.out::println);
    }
}
