package com.example.studentdealz;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ItemDealViewHolder extends RecyclerView.ViewHolder {
  public ImageView Item;
  public TextView Discount;
  public TextView Partner;
  public TextView Food;

    public ItemDealViewHolder(@NonNull View itemView) {
        super(itemView);
        Item = itemView.findViewById(R.id.item);
        Discount = itemView.findViewById(R.id.discount);
        Partner = itemView.findViewById(R.id.partner);
        Food = itemView.findViewById(R.id.food);

    }
}
