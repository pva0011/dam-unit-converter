package dev.pva0011.unitconverter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private List<Conversion> conversionList;

    public MyAdapter(List<Conversion> conversionList) {
        this.conversionList = conversionList;
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.conversion, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
        Conversion conversion = conversionList.get(position);
        holder.conversionInput.setText(conversion.input());
        holder.conversionOutput.setText(conversion.output());
        holder.timestamp.setText(conversion.timestamp());
        holder.deleteButton.setOnClickListener(v -> deleteSavedConversion(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return conversionList.size();
    }

    static public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView conversionInput, conversionOutput, timestamp;
        ImageButton deleteButton;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);
            conversionInput = itemView.findViewById(R.id.conversion_input);
            conversionOutput = itemView.findViewById(R.id.conversion_output);
            timestamp = itemView.findViewById(R.id.timestamp);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    private void deleteSavedConversion(int position) {
        conversionList.remove(position);
        notifyItemRemoved(position);
    }
}
