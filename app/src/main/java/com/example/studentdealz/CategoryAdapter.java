package com.example.studentdealz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<ItemDealViewHolder> {

 List<Item> items;

 public CategoryAdapter() {
     super();
     items = new ArrayList<>();
     items.add(new Item(R.drawable.item1, "15%", "GOLDA"));
     items.add(new Item(R.drawable.item3, "10%", "KING KONG"));
     items.add(new Item(R.drawable.item7, "23%", "Pizza Hut"));
     items.add(new Item(R.drawable.item8, "18%", "japanika"));
     items.add(new Item(R.drawable.item9, "16%", "BBB"));
 }

    @NonNull
    @Override
    public ItemDealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deal,parent,false);
        ItemDealViewHolder viewHolder = new ItemDealViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemDealViewHolder holder, int position) {
        Item item = items.get(position);
        holder.Item.setImageResource(item.getItem());
        holder.Discount.setText(item.getDiscount());
        holder.Partner.setText(item.getPartner());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
