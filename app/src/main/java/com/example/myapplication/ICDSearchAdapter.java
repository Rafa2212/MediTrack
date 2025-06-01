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
public class ICDSearchAdapter extends ArrayAdapter<ICDSearchResult> implements Filterable {
    private List<ICDSearchResult> originalResults;
    private List<ICDSearchResult> filteredResults;
    private final LayoutInflater inflater;
    private Icd10SearchFilter searchFilter;

    /**
     * Constructs a new Icd10SearchAdapter with the specified context and search results.
     *
     * @param context The context used to inflate the layout
     * @param results The initial list of ICD-10 search results
     */
    public ICDSearchAdapter(@NonNull Context context, @NonNull List<ICDSearchResult> results) {
        super(context, 0, results);
        this.originalResults = new ArrayList<>(results);
        this.filteredResults = new ArrayList<>(results);
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * Creates or reuses a view for an item at the specified position and populates it with data.
     * This method inflates the icd10_search_item layout if needed and sets the ICD-10 code
     * and disease name in the appropriate TextViews.
     *
     * @param position The position of the item within the adapter's data set
     * @param convertView The old view to reuse, if possible
     * @param parent The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.icd10_search_item, parent, false);
        }

        ICDSearchResult result = getItem(position);
        if (result != null) {
            TextView codeTextView = convertView.findViewById(R.id.text_icd10_code);
            TextView nameTextView = convertView.findViewById(R.id.text_disease_name);

            codeTextView.setText(result.getCode());
            nameTextView.setText(result.getName());
        }

        return convertView;
    }

    /**
     * Returns the number of items in the filtered results list.
     *
     * @return The number of items available in the adapter
     */
    @Override
    public int getCount() {
        return filteredResults.size();
    }

    /**
     * Gets the ICD-10 search result at the specified position in the filtered results list.
     *
     * @param position The position of the item within the adapter's data set
     * @return The ICD-10 search result at the specified position, or null if position is invalid
     */
    @Nullable
    @Override
    public ICDSearchResult getItem(int position) {
        return filteredResults.get(position);
    }

    /**
     * Gets the filter used to constrain the data in this adapter.
     * Creates a new filter instance if one doesn't exist yet.
     *
     * @return The filter used for this adapter
     */
    @NonNull
    @Override
    public Filter getFilter() {
        if (searchFilter == null) {
            searchFilter = new Icd10SearchFilter();
        }
        return searchFilter;
    }

    /**
     * Updates the adapter with new search results.
     * This method replaces both the original and filtered results lists with the new data
     * and notifies the adapter that the data set has changed to refresh the UI.
     *
     * @param results The new search results to display
     */
    public void updateResults(List<ICDSearchResult> results) {
        this.originalResults = new ArrayList<>(results);
        this.filteredResults = new ArrayList<>(results);
        notifyDataSetChanged();
    }

    /**
     * Gets the original, unfiltered list of ICD-10 search results.
     *
     * @return The original list of search results
     */
    public List<ICDSearchResult> getOriginalResults() {
        return originalResults;
    }

    /**
     * Sets the original list of ICD-10 search results.
     * This method allows updating the original data set without creating a new adapter.
     *
     * @param originalResults The new list of original search results
     */
    public void setOriginalResults(List<ICDSearchResult> originalResults) {
        this.originalResults = originalResults;
    }

    /**
     * Filter for ICD-10 search results
     * This is a dummy filter as the actual filtering is done by the API
     */
    private class Icd10SearchFilter extends Filter {
        /**
         * Performs the filtering operation.
         * This is a dummy implementation as the actual filtering is done by the external API.
         * It simply returns the current filtered results without any additional filtering.
         *
         * @param constraint The filter constraint
         * @return A FilterResults object containing the filtered data
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredResults;
            filterResults.count = filteredResults.size();
            return filterResults;
        }

        /**
         * Publishes the filtering results to the UI thread.
         * Notifies the adapter that the data set has changed if there are results,
         * or that the data set is invalid if there are no results.
         *
         * @param constraint The filter constraint used to filter the data
         * @param results The results of the filtering operation
         */
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
