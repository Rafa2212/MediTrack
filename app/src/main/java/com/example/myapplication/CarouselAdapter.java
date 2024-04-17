package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {

    private Context context;
    private List<Disease> diseases;

    public CarouselAdapter(Context context, List<Disease> diseases) {
        this.context = context;
        this.diseases = diseases;
    }

    @Override
    public CarouselAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.disease_widget_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CarouselAdapter.ViewHolder holder, int position) {
        Disease disease = diseases.get(position);
        holder.diseaseNameText.setText(disease.getICD10());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                ScrollView scrollView = new ScrollView(v.getContext());
                TextView textView = new TextView(v.getContext());

                textView.setPadding(32, 32, 32, 32);
                textView.setText(disease.getInterpretation());  // Show disease ICD10 code in dialog

                scrollView.addView(textView);
                builder.setView(scrollView)
                        .setTitle("Disease ICD10 Code")
                        .setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return diseases.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView diseaseNameText;
        public TextView diseaseDescriptionText;

        public ViewHolder(View itemView) {
            super(itemView);
            diseaseNameText = itemView.findViewById(R.id.diseaseNameTextView);
            //diseaseDescriptionText = itemView.findViewById(R.id.diseaseDescriptionTextView);
        }
    }
}