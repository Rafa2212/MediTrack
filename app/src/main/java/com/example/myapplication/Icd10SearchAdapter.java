package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying ICD-10 search results in an AutoCompleteTextView
 */
public class Icd10SearchAdapter extends ArrayAdapter<Icd10SearchResult> implements Filterable {
    private List<Icd10SearchResult> originalResults;
    private List<Icd10SearchResult> filteredResults;
    private LayoutInflater inflater;
    private Icd10SearchFilter searchFilter;

    public Icd10SearchAdapter(@NonNull Context context, @NonNull List<Icd10SearchResult> results) {
        super(context, 0, results);
        this.originalResults = new ArrayList<>(results);
        this.filteredResults = new ArrayList<>(results);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.icd10_search_item, parent, false);
        }

        Icd10SearchResult result = getItem(position);
        if (result != null) {
            TextView codeTextView = convertView.findViewById(R.id.text_icd10_code);
            TextView nameTextView = convertView.findViewById(R.id.text_disease_name);

            codeTextView.setText(result.getCode());
            nameTextView.setText(result.getName());
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return filteredResults.size();
    }

    @Nullable
    @Override
    public Icd10SearchResult getItem(int position) {
        return filteredResults.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if (searchFilter == null) {
            searchFilter = new Icd10SearchFilter();
        }
        return searchFilter;
    }

    /**
     * Update the adapter with new search results
     * @param results The new search results
     */
    public void updateResults(List<Icd10SearchResult> results) {
        this.originalResults = new ArrayList<>(results);
        this.filteredResults = new ArrayList<>(results);
        notifyDataSetChanged();
    }

    /**
     * Filter for ICD-10 search results
     * This is a dummy filter as the actual filtering is done by the API
     */
    private class Icd10SearchFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredResults;
            filterResults.count = filteredResults.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}