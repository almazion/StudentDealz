package com.example.studentdealz;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemDealViewHolder extends RecyclerView.ViewHolder {
    public ImageView itemImage;
    public TextView discount;
    public TextView partner;
    public TextView category;

    public ItemDealViewHolder(@NonNull View itemView) {
        super(itemView);
        itemImage = itemView.findViewById(R.id.item);
        discount = itemView.findViewById(R.id.discount);
        partner = itemView.findViewById(R.id.partner);
        category = itemView.findViewById(R.id.dealCategory);
    }
}
