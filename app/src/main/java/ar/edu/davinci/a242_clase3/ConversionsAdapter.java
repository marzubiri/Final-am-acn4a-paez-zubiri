package ar.edu.davinci.a242_clase3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ConversionsAdapter extends RecyclerView.Adapter<ConversionsAdapter.ConversionViewHolder> {

    private ArrayList<String> conversionsList;

    public ConversionsAdapter(ArrayList<String> conversionsList) {
        this.conversionsList = conversionsList;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ConversionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.conversionTextView.setText(conversionsList.get(position));
    }

    @Override
    public int getItemCount() {
        return conversionsList.size();
    }

    public static class ConversionViewHolder extends RecyclerView.ViewHolder {
        TextView conversionTextView;

        public ConversionViewHolder(@NonNull View itemView) {
            super(itemView);
            conversionTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}
