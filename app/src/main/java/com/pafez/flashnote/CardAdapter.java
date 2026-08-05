package com.pafez.flashnote;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter
        extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<Card> cards;
    private final OnCardLongClickListener longClickListener;

    public interface OnCardLongClickListener {
        void onCardLongClick(Card card);
    }

    public CardAdapter(
            List<Card> cards,
            OnCardLongClickListener longClickListener) {

        this.cards = cards;
        this.longClickListener = longClickListener;
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

        holder.cardFront.setText(card.front);
        holder.cardBack.setText(card.back);

        // Reset state for recycled view
        holder.layoutFront.setVisibility(View.VISIBLE);
        holder.layoutBack.setVisibility(View.GONE);
        holder.itemView.setRotationY(0);
        holder.isFlipped = false;

        // Click to flip
        holder.itemView.setOnClickListener(v -> flipCard(holder));

        // Long press
        holder.itemView.setOnLongClickListener(v -> {

            if (longClickListener != null) {

                longClickListener.onCardLongClick(card);
            }

            return true;
        });
    }

    private void flipCard(CardViewHolder holder) {
        final View view = holder.itemView;
        final View front = holder.layoutFront;
        final View back = holder.layoutBack;

        // Disable elevation (shadow) during animation to prevent "ghost" panels
        final float originalElevation = view.getElevation();
        view.setElevation(0);

        // Use hardware layer and high camera distance for smooth 3D
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        float distance = 12000 * view.getContext().getResources().getDisplayMetrics().density;
        view.setCameraDistance(distance);

        view.animate()
                .rotationY(90)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (holder.isFlipped) {
                            back.setVisibility(View.GONE);
                            front.setVisibility(View.VISIBLE);
                            holder.isFlipped = false;
                        } else {
                            front.setVisibility(View.GONE);
                            back.setVisibility(View.VISIBLE);
                            holder.isFlipped = true;
                        }
                        
                        view.setRotationY(-90);
                        view.animate()
                                .rotationY(0)
                                .setDuration(150)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        // Restore elevation and cleanup
                                        view.setElevation(originalElevation);
                                        view.setLayerType(View.LAYER_TYPE_NONE, null);
                                    }
                                })
                                .start();
                    }
                })
                .start();
    }

    @Override
    public int getItemCount() {

        return cards == null ? 0 : cards.size();
    }

    public static class CardViewHolder
            extends RecyclerView.ViewHolder {

        TextView cardFront;
        TextView cardBack;
        View layoutFront;
        View layoutBack;
        boolean isFlipped = false;

        public CardViewHolder(
                @NonNull View itemView) {

            super(itemView);

            cardFront = itemView.findViewById(R.id.cardFront);
            cardBack = itemView.findViewById(R.id.cardBack);
            layoutFront = itemView.findViewById(R.id.layoutFront);
            layoutBack = itemView.findViewById(R.id.layoutBack);
        }
    }
}