package com.example.cryptocoin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Adapter for the recycler view in 'CryptoListActivity'
 */
public class CryptoListAdapter extends RecyclerView.Adapter<CryptoListAdapter.CryptoViewHolder> {
    private Crypto[] mCryptoList;
    private final Crypto[] clonedCryptoList;
    private final LayoutInflater mInflator;

    public CryptoListAdapter(Context context, Crypto[] cryptoList) {
        mInflator = LayoutInflater.from(context);
        this.mCryptoList = cryptoList;
        clonedCryptoList = cryptoList;
    }

    /**
     * When view holder is created, set each item of recycler view with the appropriate layout
     * @param parent view group
     * @param viewType integer
     * @return instance of the view holder
     */
    @Override
    public CryptoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mItemView = mInflator.inflate(R.layout.cryptolist_item, parent, false) ;
        return new CryptoViewHolder(mItemView, this);
    }

    /**
     * When binding data to the view holder, defining what text goes where for the specified
     * item of the list
     * @param holder view holder
     * @param position position in the list
     */
    @Override
    public void onBindViewHolder(CryptoViewHolder holder, int position) {
        Crypto mCurrent = mCryptoList[position];

        // set item text to the coin name
        holder.cryptoItemView.setText(mCurrent.coinName);
        int textColour;

        // set item information text to percentage change
        String pChange = mCurrent.dailyPercentageChange;

        if(pChange.contains("-")){
            pChange = pChange + "%";
            // make negative changes red
            textColour = Color.parseColor("#FF5656");
        }else{
            pChange = "+" + pChange + "%";
            // make positive changes green
            textColour = Color.parseColor("#4CAF50");
        }
        holder.cryptoInfoView.setText(pChange);
        holder.cryptoInfoView.setTextColor(textColour);
    }

    /**
     * Count how many items in the list of crypto coins
     * @return integer count
     */
    public int getItemCount() { return mCryptoList.length; }

    /**
     * Called when searching the recycler view
     * @return the result of the filter
     */
    public Filter getFilter(){
        return filter;
    }

    private Filter filter = new Filter() {
        /**
         * When user searches, create a list of items that match their query
         * @param charSequence user query
         * @return the new list
         */
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            Crypto[] filteredList;
            ArrayList<Crypto> filteredListWIP = new ArrayList<Crypto>();

            // if user types nothing, leave list as original
            if(charSequence == null || charSequence.length() == 0){
                filteredList = clonedCryptoList;
            } else{
                // check user query matches any results and add them to the list to display
                String query = charSequence.toString().trim().toLowerCase();
                for (Crypto crypto : clonedCryptoList) {
                    if (crypto.coinName.toLowerCase().contains(query)) {
                        filteredListWIP.add(crypto);
                    }
                }
                filteredList = filteredListWIP.toArray(new Crypto[0]);
            }

            FilterResults results =  new FilterResults();
            results.values = filteredList;

            // return list of coins that match user query
            return results;
        }

        /**
         * Set recycler view with new list of users queried coins
         * @param charSequence query
         * @param filterResults list of items
         */
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            if(filterResults.values != null){
                mCryptoList = (Crypto[]) filterResults.values;
                notifyDataSetChanged();
            }
        }
    };

    class CryptoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView cryptoItemView;
        public final TextView cryptoInfoView;
        final CryptoListAdapter mAdapter;

        public CryptoViewHolder(View itemView, CryptoListAdapter adapter) {
            super(itemView);
            cryptoItemView = (TextView) itemView.findViewById(R.id.stock);
            cryptoInfoView = (TextView) itemView.findViewById(R.id.stockinfo);
            this.mAdapter = adapter;

            // set onclick to new activity
            itemView.setOnClickListener(this);
        }

        /**
         * When a user clicks an item in the recycler view, take them to the more info page
         * @param v current view
         */
        @Override
        public void onClick(View v) {
            String name = cryptoItemView.getText().toString();
            String price = "";
            String symbol = "";
            String dailyPChange = "";
            String hourlyPChange = "";

            for (Crypto coin : mCryptoList) {
                // gather information for correct coin
                if (coin.coinName.equals(name)) {
                    price = coin.coinPrice;
                    symbol = coin.coinSymbol;
                    dailyPChange = coin.dailyPercentageChange;
                    hourlyPChange = coin.hourlyPercentageChange;
                }
            }

            // create an explicit intent
            Context context = itemView.getContext();

            Class<CryptoMoreInfo> destinationActivity = CryptoMoreInfo.class;

            Intent intent = new Intent(context, destinationActivity);

            // put all information into intent
            intent.putExtra("name", name);
            intent.putExtra("price", price);
            intent.putExtra("symbol", symbol);
            intent.putExtra("dailypchange", dailyPChange);
            intent.putExtra("hourlypchange", hourlyPChange);

            // go to destination activity
            context.startActivity(intent);
        }
    }
}
