package com.example.studentdealz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DealAdapter extends RecyclerView.Adapter<ItemDealViewHolder> {

    public interface OnDealClickListener {
        void onDealClick(Item item);
    }

    private final List<Item> items = new ArrayList<>();
    private final OnDealClickListener onDealClickListener;

    public DealAdapter(List<Item> initialItems) {
        this(initialItems, null);
    }

    public DealAdapter(List<Item> initialItems, OnDealClickListener onDealClickListener) {
        this.onDealClickListener = onDealClickListener;
        setItems(initialItems);
    }

    @NonNull
    @Override
    public ItemDealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deal, parent, false);
        return new ItemDealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemDealViewHolder holder, int position) {
        Item item = items.get(position);
        holder.itemImage.setImageResource(item.getImageResId());
        holder.discount.setText(item.getDiscount());
        holder.partner.setText(item.getPartner());
        holder.category.setText(item.getCategory());
        holder.itemView.setOnClickListener(view -> {
            if (onDealClickListener != null) {
                onDealClickListener.onDealClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<Item> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }
}
