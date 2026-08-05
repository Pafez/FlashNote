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
    private OnDeckClickListener clickListener;

    public interface OnDeckClickListener {
        void onDeckClick(Deck deck);
    }

    public DeckAdapter(List<Deck> decks, OnDeckClickListener clickListener) {
        this.decks = decks;
        this.clickListener = clickListener;
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
    }

    @Override
    public int getItemCount() {
        return decks != null ? decks.size() : 0;
    }

    static class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView deckNameText;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            deckNameText = itemView.findViewById(R.id.deckNameText);
        }
    }
}
