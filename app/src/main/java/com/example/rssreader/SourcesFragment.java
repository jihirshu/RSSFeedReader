package com.example.rssreader;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SourcesFragment extends Fragment {

    private ListView listView;
    Button addButton;


    public static boolean removeFlag = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sources, container, false);
        listView = (ListView) view.findViewById(R.id.sourcesList);
        addButton = (Button) view.findViewById(R.id.btnAddSources);
        final List<Map<String, Object>> list = getData();
        final SourceListAdapter adapter = new SourceListAdapter(getActivity(), list);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    removeUserSource(MainActivity.usersourcenames.get(i), MainActivity.usersourcelinks.get(i));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                list.remove(i);
                adapter.notifyDataSetChanged();
                Toast.makeText(getActivity(),"Source removed", Toast.LENGTH_LONG).show();
                return true;
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SourceListActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    public List <Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for(int i = 0; i < MainActivity.usersourcenames.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("sourceName", MainActivity.usersourcenames.get(i));
            list.add(map);
        }
        return list;
    }

    DatabaseReference mRootref = FirebaseDatabase.getInstance().getReference();
    DatabaseReference reader = mRootref.child("Reader");
    DatabaseReference sourcedb = reader.child("Sources");
    DatabaseReference userRss = reader.child("UserRSS");


    public void removeUserSource(String name, String link) throws ExecutionException, InterruptedException {
        removeFlag = false;
        String exec_result = new ProcessInBackground().execute(name, link).get();
    }

    public void checkLinkValidity(final String name, final String link)
    {


        userRss.child(MainActivity.user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child(name).getValue().equals(link))
                {
                    removeFlag = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public class ProcessInBackground extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(final String... strings)
        {

            userRss.child(MainActivity.user).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.child(strings[0]).getValue().equals(strings[1]))
                    {
                        removeFlag = true;
                        userRss.child(MainActivity.user).child(strings[0].toUpperCase()).removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return "executed";
        }
    }





}
