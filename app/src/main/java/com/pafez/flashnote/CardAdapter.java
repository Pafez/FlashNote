package com.pafez.flashnote;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter
        extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<Card> cards;

    private OnCardLongClickListener longClickListener;
    private OnCardClickListener clickListener;

    public interface OnCardLongClickListener {
        void onCardLongClick(Card card);
    }

    public interface OnCardClickListener {
        void onCardClick(Card card);
    }

    public CardAdapter(
            List<Card> cards,
            OnCardLongClickListener longClickListener,
            OnCardClickListener clickListener) {

        this.cards = cards;
        this.longClickListener = longClickListener;
        this.clickListener = clickListener;
    }

    public void updateCards(List<Card> newCards) {

        this.cards = newCards;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_card,
                                parent,
                                false
                        );

        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CardViewHolder holder,
            int position) {

        Card card = cards.get(position);

        holder.cardFront.setText("Front: " + card.front);
        holder.cardBack.setText("Back: " + card.back);

        // Click to view
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCardClick(card);
            }
        });

        // Long press
        holder.itemView.setOnLongClickListener(v -> {

            if (longClickListener != null) {

                longClickListener.onCardLongClick(card);
            }

            return true;
        });
    }

    @Override
    public int getItemCount() {

        return cards == null ? 0 : cards.size();
    }

    static class CardViewHolder
            extends RecyclerView.ViewHolder {

        TextView cardFront;
        TextView cardBack;

        public CardViewHolder(
                @NonNull View itemView) {

            super(itemView);

            cardFront =
                    itemView.findViewById(
                            R.id.cardFront
                    );

            cardBack =
                    itemView.findViewById(
                            R.id.cardBack
                    );
        }
    }
}