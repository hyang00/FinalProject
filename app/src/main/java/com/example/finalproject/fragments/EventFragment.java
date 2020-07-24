package com.example.finalproject.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.finalproject.Common;
import com.example.finalproject.DatabaseClient;
import com.example.finalproject.EndlessRecyclerViewScrollListener;
import com.example.finalproject.R;
import com.example.finalproject.adapters.EventsAdapter;
import com.example.finalproject.models.Event;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EventFragment extends Fragment {

    private static final String TAG = "Event Fragment";

    // fragment initialization parameter (determine whether standard event feed, hosting event feed, attending feed)
    private static final String ARG_EVENT_TYPE = "Event Type";

    private RecyclerView rvEvents;
    protected EventsAdapter adapter;
    protected List<Event> allEvents;
    protected SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;
    protected String eventType;
    private TextView tvDefaultMessage;
    private CollapsingToolbarLayout collapsingToolbar;

    public EventFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param eventType Parameter 1.
     * @return A new instance of fragment UserPostFragment.
     */
    public static EventFragment newInstance(String eventType) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_TYPE, eventType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventType = getArguments().getString(ARG_EVENT_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvEvents = view.findViewById(R.id.rvEvents);

        tvDefaultMessage = view.findViewById(R.id.tvDefaultMessage);
        collapsingToolbar = view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Community Garden");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        allEvents = new ArrayList<>();
        adapter = new EventsAdapter(getContext(), allEvents, eventType);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryEventsNearby();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // set the adapter on the recycler view
        rvEvents.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvEvents.setLayoutManager(linearLayoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page, totalItemsCount);
            }
        };
        rvEvents.addOnScrollListener(scrollListener);
        queryEventsNearby();

    }

    private void setDefaultIfEmpty(){
        if (adapter.isEmpty()){
            rvEvents.setVisibility(View.GONE);
            tvDefaultMessage.setVisibility(View.VISIBLE);
        } else{
            rvEvents.setVisibility(View.VISIBLE);
            tvDefaultMessage.setVisibility(View.GONE);
        }
    }

    private void queryEventsNearby() {
        DatabaseClient.queryEventsNearby(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clear();
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    Event event = singleSnapshot.getValue(Event.class);
                    event.setEventId(singleSnapshot.getKey());
                    if (isValid(event)) {
                        adapter.add(event);
                    }
                }
                setDefaultIfEmpty();
                adapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void queryEvents() {
        DatabaseClient.queryEvents(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clear();
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    Event event = singleSnapshot.getValue(Event.class);
                    event.setEventId(singleSnapshot.getKey());
                    if (isValid(event)) {
                        adapter.add(event);
                    }
                }
                adapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // check whether the event should be added to the feed depending on which feed it is
    private boolean isValid(Event event) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        switch (eventType) {
            case Common.EVENT_FEED_KEY:
                if (!event.getAuthor().equals(uid) && !event.isAttending(uid)) {
                    return true;
                }
                break;
            case Common.EVENT_ATTENDING_KEY:
                if (event.isAttending(uid)) {
                    return true;
                }
                break;
            case Common.EVENT_HOSTING_KEY:
                if (event.getAuthor().equals(uid)) {
                    return true;
                }
        }
        return false;
    }

    // TODO: limit initial query and implement infinite scrolling
    private void loadNextDataFromApi(int page, int totalItemsCount) {
    }
}