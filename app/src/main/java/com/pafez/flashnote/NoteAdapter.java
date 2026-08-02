package com.pafez.flashnote;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter
        extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes;

    private OnNoteLongClickListener longClickListener;

    public interface OnNoteLongClickListener {
        void onNoteLongClick(Note note);
    }

    public NoteAdapter(
            List<Note> notes,
            OnNoteLongClickListener longClickListener) {

        this.notes = notes;
        this.longClickListener = longClickListener;
    }

    public void updateNotes(List<Note> newNotes) {

        this.notes = newNotes;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_note,
                                parent,
                                false
                        );

        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull NoteViewHolder holder,
            int position) {

        Note note = notes.get(position);

        // Get first line as title
        String title =
                note.text.trim();

        if (title.contains("\n")) {

            title =
                    title.substring(
                            0,
                            title.indexOf("\n")
                    );
        }

        if (title.isEmpty()) {

            title =
                    "Untitled Note";
        }

        holder.noteTitle.setText(title);

        // Format creation date
        String date =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                ).format(
                        new Date(note.createdAt)
                );

        holder.noteDate.setText(date);

        // Long press
        holder.itemView.setOnLongClickListener(v -> {

            if (longClickListener != null) {

                longClickListener.onNoteLongClick(note);
            }

            return true;
        });
    }

    @Override
    public int getItemCount() {

        return notes.size();
    }

    static class NoteViewHolder
            extends RecyclerView.ViewHolder {

        TextView noteTitle;

        TextView noteDate;

        public NoteViewHolder(
                @NonNull View itemView) {

            super(itemView);

            noteTitle =
                    itemView.findViewById(
                            R.id.noteTitle
                    );

            noteDate =
                    itemView.findViewById(
                            R.id.noteDate
                    );
        }
    }
}