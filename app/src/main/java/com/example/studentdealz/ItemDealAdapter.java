package com.example.studentdealz;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ItemDealAdapter extends RecyclerView.Adapter<ItemDealViewHolder> {
    List<Item> items;

    public  ItemDealAdapter() {
     super();
     items = new ArrayList<>();
     items.add(new Item(R.drawable.item1,"15%","GOLDA") );
     items.add(new Item(R.drawable.item4,"18%","APPLE") );
     items.add(new Item(R.drawable.item3,"10%","KING KONG") );
     items.add(new Item(R.drawable.item5,"12%","alo") );
     items.add(new Item(R.drawable.item2,"25%","YES PLANET") );
     items.add(new Item(R.drawable.item6,"8%","ChatGPT") );
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
    holder.Food.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), activity_category.class);
            view.getContext().startActivity(intent);
        }
    });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
