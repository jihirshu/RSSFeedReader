/**
 * Author : Jihirshu Narayan
 *
 * Description : This class contains essential functions related to a given rss feed link.
 */


package com.example.rssreader;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RSSObject
{
    public String sourceName;
    public String url;
    public String category;
    public static boolean validRSSLink=false;

    public RSSObject(String sourceName, String url, String category)
    {
        this.sourceName = sourceName;
        this.url = url;
        this.category = category;
    }
    public RSSObject()
    {

    }

    public RSSObject(String url)
    {
        this.url = url;
    }

    public boolean checkCorrectness()
    {
        return false;
    }


    /**
     * Author : Jihirshu Narayan
     *
     * Description : The execution of this function populates the static variables titles, links, dates, images and descriptions with
     *              relevant data depending on the sources present in the static variable usersourcelinks list
     */

    public void getUserData()
    {
        getuserSources(new FirebaseCallback() {
            @Override
            public void onCallback(List<String> usersourceNames, List<String> usersourceLinks) throws ExecutionException, InterruptedException {
                MainActivity.usersourcenames = usersourceNames;
                MainActivity.usersourcelinks = usersourceLinks;
                MainActivity.titles = new ArrayList<>();
                MainActivity.links = new ArrayList<>();
                MainActivity.dates = new HashMap<>();
                MainActivity.images = new HashMap<>();
                MainActivity.description = new HashMap<>();
                if (MainActivity.usersourcelinks.size()>0)
                {

                    for (int i=0;i<MainActivity.usersourcelinks.size();i++)
                    {
                        String str_result = new ProcessInBackGround().execute(MainActivity.usersourcelinks.get(i)).get();
                    }
                }

            }
        });
    }

    /**
     * Author : Jihirshu Narayan
     *
     * Description : The execution of this function populates the  static variables favouriteLinks and favouriteTitles from the corresponding
     *               data present in the user's favourites database.
     */

    public void getUserFavourites()
    {
        getUserFavourites(new FirebaseCallback() {
            @Override
            public void onCallback(List<String> favouriteLink, List<String> favouriteTitle) {
                MainActivity.favouriteLinks = favouriteLink;
                MainActivity.favouriteTitles = favouriteTitle;

            }
        });
    }

    /**
     * Author : Jihirshu Narayan
     *
     * Description : The execution of this function gets the app's complete list of rss feed source names, links and categories.
     */

    public void getSources()
    {
        getSources(new FirebaseCallback2() {
            @Override
            public void onCallback(List<String> rsssources, List<String> Categories, List<String> rssLinks)
            {
                MainActivity.appSourcesNames = rsssources;
                MainActivity.appSourcesCategories = Categories;
                MainActivity.appsourcesLinks = rssLinks;
            }
        });
    }

    /**
     * Author : Jihirshu Narayan
     *
     * Description : This function checks if a given RSS source feed already exists in the app's database or not and if not then creates a new node
     *              with the relevant information.
     */

    public String addRssSource(final String name, final String Link, final String Category) throws ExecutionException, InterruptedException {
        String result = "";
        validRSSLink = false;
        String execResult = new ValidityCheck().execute(Link).get();

        if (validRSSLink)
        {
            sourcedb.child(name.toUpperCase()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null)
                    {
                        sourcedb.child(name.toUpperCase()).setValue(new NewSource(Category, Link));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            result = "Valid Link, Added";
        }
        else
        {
            result = "Invalid Link";
        }
        return result;
    }


    /**
     * Author : Jihirshu Narayan
     *
     * Description : This function refreshes the user's news feed
     *
     */

    public void manualRefresh()
    {
        MainActivity.titles = new ArrayList<>();
        MainActivity.links = new ArrayList<>();
        MainActivity.dates = new HashMap<>();
        MainActivity.images = new HashMap<>();
        MainActivity.description = new HashMap<>();
        for (int i=0;i<MainActivity.usersourcelinks.size();i++)
        {
            try {
                String str_result = new ProcessInBackGround().execute(MainActivity.usersourcelinks.get(i)).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }





    public InputStream getInputStream(URL url)
    {
        try
        {
            return url.openConnection().getInputStream();
        }
        catch (IOException e)
        {
            validRSSLink = false;
            return null;
        }
    }

    /**
     * Author : Jihirshu Narayan
     *
     * Description : This class establishes a collection with the given rss source link, this class asynchronously works in the background and
     *              downloads the xml data and checks whether the data is in a valid format or not.
     *
     */

    public class ValidityCheck extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            Exception exception = null;
            String title="";
            try
            {
                URL Url = new URL(strings[0]);
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);

                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(getInputStream(Url), "UTF_8");

                boolean insideterm = false;

                int eventType = xpp.getEventType();

                while(eventType != XmlPullParser.END_DOCUMENT)
                {
                    if (eventType == XmlPullParser.START_TAG)
                    {
                        if (xpp.getName().equalsIgnoreCase("item"))
                        {
                            insideterm = true;
                            validRSSLink = true;
                        }
                        else if (xpp.getName().equalsIgnoreCase("title"))
                        {
                            if (insideterm)
                            {
                                title = xpp.nextText();
//                                MainActivity.titles.add(title);
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("link"))
                        {
                            if (insideterm)
                            {

//                                MainActivity.links.add(xpp.nextText());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("pubDate"))
                        {
                            if (insideterm)
                            {
//                                MainActivity.dates.put(title, xpp.nextText());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("image"))
                        {
                            if (insideterm)
                            {
//                                MainActivity.images.put(title, xpp.nextText());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("description"))
                        {
                            if (insideterm)
                            {
//                                MainActivity.description.put(title, xpp.nextText());
                            }
                        }
                    }
                    else if ((eventType == XmlPullParser.END_TAG) && (xpp.getName().equalsIgnoreCase("item")))
                    {
                        insideterm = false;
                    }

                    eventType = xpp.next();
                }



            }
            catch (MalformedURLException e)
            {
                exception = e;
            }
            catch (XmlPullParserException e)
            {
                exception = e;
            }
            catch (IOException e)
            {
                exception = e;
            }
            return "executed";
        }
    }

    /**
     * Author : Jihirshu Narayan
     *
     * Description : Given a rss feed link, this class asynchronously works in the background and establishes a connection with the link and downloads
     *              the xml data and populates the titles, dates, images, description and links.
     */

    public class ProcessInBackGround extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            Exception exception = null;
            String title="";
            try
            {
                URL Url = new URL(strings[0]);
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);

                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(getInputStream(Url), "UTF_8");

                boolean insideterm = false;

                int eventType = xpp.getEventType();

                while(eventType != XmlPullParser.END_DOCUMENT)
                {
                    if (eventType == XmlPullParser.START_TAG)
                    {
                        if (xpp.getName().equalsIgnoreCase("item"))
                        {
                            insideterm = true;
                            validRSSLink = true;
                        }
                        else if (xpp.getName().equalsIgnoreCase("title"))
                        {
                            if (insideterm)
                            {
                                title = xpp.nextText();
                                MainActivity.titles.add(title);
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("link"))
                        {
                            if (insideterm)
                            {

                                MainActivity.links.add(xpp.nextText());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("pubDate"))
                        {
                            if (insideterm)
                            {
                                MainActivity.dates.put(title, xpp.nextText());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("image"))
                        {
                            if (insideterm)
                            {
                                MainActivity.images.put(title, xpp.nextText());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("description"))
                        {
                            if (insideterm)
                            {
                                MainActivity.description.put(title, xpp.nextText());
                            }
                        }
                    }
                    else if ((eventType == XmlPullParser.END_TAG) && (xpp.getName().equalsIgnoreCase("item")))
                    {
                        insideterm = false;
                    }

                    eventType = xpp.next();
                }



            }
            catch (MalformedURLException e)
            {
                exception = e;
            }
            catch (XmlPullParserException e)
            {
                exception = e;
            }
            catch (IOException e)
            {
                exception = e;
            }
            return "executed";
        }
    }

    public interface FirebaseCallback
    {
        void onCallback(List<String> sourcenames, List<String> sourcelinks) throws ExecutionException, InterruptedException;
    }


    // Creating the required database instances



    DatabaseReference mRootref = FirebaseDatabase.getInstance().getReference();
    DatabaseReference reader = mRootref.child("Reader");
    DatabaseReference userrss = reader.child("UserRSS");
    DatabaseReference sourcedb = reader.child("Sources");
    DatabaseReference favourites = reader.child("Favourites");

    /**
     * Author : Jihirshu Narayan
     *
     * Description : This function is called whenever there is a change in the user's rss sources database. It updates the sourcename and sourcelink
     *              arraylist for the user.
     *
     */


    public void getuserSources(final FirebaseCallback firebaseCallback)
    {
        ValueEventListener event = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MainActivity.usersourcenames = new ArrayList<>();
                MainActivity.usersourcelinks = new ArrayList<>();
                if (dataSnapshot.getValue() != null)
                {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        MainActivity.usersourcenames.add(ds.getKey());
                        MainActivity.usersourcelinks.add(ds.getValue(String.class));
                    }
                }

                try {
                    firebaseCallback.onCallback(MainActivity.usersourcenames, MainActivity.usersourcelinks);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        userrss.child(MainActivity.user).addValueEventListener(event);
    }


    public interface FirebaseCallback2
    {
        void onCallback(List<String> rsssources, List<String> Categories, List<String> rssLinks);
    }


    /**
     * Author : Jihirshu Narayan
     *
     * Description : This function is called whenever there is a change in the apps rss sources database. It updates the sourcename and sourcelink
     *              arraylist for the entire app.
     *
     */

    public void getSources(final FirebaseCallback2 firebaseCallback2)
    {
        ValueEventListener event = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                MainActivity.appsourcesLinks = new ArrayList<>();
                MainActivity.appSourcesNames = new ArrayList<>();
                MainActivity.appSourcesCategories = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    MainActivity.appsourcesLinks.add(ds.child("Link").getValue(String.class));
                    MainActivity.appSourcesCategories.add(ds.child("Category").getValue(String.class));
                    MainActivity.appSourcesNames.add(ds.getKey());
                }

                firebaseCallback2.onCallback(MainActivity.appSourcesNames, MainActivity.appSourcesCategories, MainActivity.appsourcesLinks);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        };

        sourcedb.addValueEventListener(event);

    }

    /**
     * Author : Jihirshu Narayan
     *
     * Description : This function is called whenever there is a change in the user's favourites database. It updates the links, dates,
     *              description and titles arraylist for the user.
     *
     */


    public void getUserFavourites(final FirebaseCallback firebaseCallback)
    {
        ValueEventListener event = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                MainActivity.favouriteLinks = new ArrayList<>();
                MainActivity.favouriteTitles = new ArrayList<>();
                MainActivity.favouriteDescriptions = new ArrayList<>();
                MainActivity.favouriteDates = new ArrayList<>();
                if (dataSnapshot.getValue() != null)
                {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        MainActivity.favouriteLinks.add(ds.child("link").getValue(String.class));
                        MainActivity.favouriteTitles.add(ds.child("title").getValue(String.class));
                        MainActivity.favouriteDescriptions.add(ds.child("description").getValue(String.class));
                        MainActivity.favouriteDates.add(ds.child("date").getValue(String.class));
                    }
                }
                try {
                    firebaseCallback.onCallback(MainActivity.favouriteLinks, MainActivity.favouriteTitles);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        };

        favourites.child(MainActivity.user).addValueEventListener(event);

    }

}
