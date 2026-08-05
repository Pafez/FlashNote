package com.pafez.flashnote;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {

    private List<Deck> decks;
    private final OnDeckClickListener clickListener;
    private final OnDeckLongClickListener longClickListener;

    public interface OnDeckClickListener {
        void onDeckClick(Deck deck);
    }

    public interface OnDeckLongClickListener {
        void onDeckLongClick(Deck deck);
    }

    public DeckAdapter(List<Deck> decks, OnDeckClickListener clickListener, OnDeckLongClickListener longClickListener) {
        this.decks = decks;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void updateDecks(List<Deck> newDecks) {
        this.decks = newDecks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deck, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        Deck deck = decks.get(position);
        holder.deckNameText.setText(deck.name);
        
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onDeckClick(deck);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onDeckLongClick(deck);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return decks != null ? decks.size() : 0;
    }

    public static class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView deckNameText;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            deckNameText = itemView.findViewById(R.id.deckNameText);
        }
    }
}
