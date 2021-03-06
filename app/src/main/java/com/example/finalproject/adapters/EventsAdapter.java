package com.example.finalproject.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalproject.Common;
import com.example.finalproject.DatabaseClient;
import com.example.finalproject.R;
import com.example.finalproject.models.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

import static com.example.finalproject.TimeAndDateFormatter.formatDateWithDayOfWeek;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    @SuppressWarnings("unused")
    private static final String TAG = "EventsAdapter";
    private Context context;
    private List<Event> events;
    private String eventType;
    private KonfettiView konfettiView;

    @SuppressWarnings("unused")
    public EventsAdapter(Context context, List<Event> events, String eventType) {
        this.context = context;
        this.events = events;
        this.eventType = eventType;
        setHasStableIds(true);
    }

    public EventsAdapter(Context context, List<Event> events, String eventType, KonfettiView konfettiView) {
        this.context = context;
        this.events = events;
        this.eventType = eventType;
        this.konfettiView = konfettiView;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void add(Event event) {
        events.add(event);
    }

    public void clear() {
        events.clear();
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public void removeAt(int position) {
        events.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, events.size());
    }

    public void launchConfetti() {
        konfettiView.build()
                .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                .setDirection(0.0, 359.0)
                .setSpeed(2f, 10f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(12, 5f))
                .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                .burst(100);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle;
        private ImageView ivEventPhoto;
        private TextView tvTime;
        private TextView tvDescription;
        private TextView tvGoing;
        private Button btnRSVP;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivEventPhoto = itemView.findViewById(R.id.ivEventPhoto);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvGoing = itemView.findViewById(R.id.tvGoing);
            btnRSVP = itemView.findViewById(R.id.btnRSVP);
            btnRSVP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position = getAdapterPosition();
                    final Event event = events.get(position);
                    DatabaseClient.rsvpUser(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(context, "Successfully Registered", Toast.LENGTH_SHORT).show();
                            removeAt(position);
                            if (konfettiView != null) {
                                //launchConfetti();
                            }
                        }
                    }, event, context);
                    Log.i("adapter", "event key: " + event.getEventId());
                }
            });
            if (eventType.equals(Common.EVENT_HOSTING_KEY) || eventType.equals(Common.EVENT_ATTENDING_KEY)) {
                btnRSVP.setVisibility(View.GONE);
            }
        }

        public void bind(Event event) {
            tvTitle.setText(event.getTitle());
            tvDescription.setText(event.getDescription());
            if (event.getNumberofAttendees() > 0) {
                if ((!event.isEventFull()) && event.spotsLeft() <= 5) {
                    tvGoing.setText(event.spotsLeft() + " spots left!");
                } else {
                    tvGoing.setText(event.getNumberofAttendees() + " going");
                }
            }
            if (event.getImageUrl() != null) {
                Glide.with(context).load(event.getImageUrl()).into(ivEventPhoto);
            }
            tvTime.setText(formatDateWithDayOfWeek(event.getDate()) + " | " + event.getTime());
            if (event.isEventFull()) {
                btnRSVP.setText(R.string.rsvp_button_event_full);
            }
        }

    }

}