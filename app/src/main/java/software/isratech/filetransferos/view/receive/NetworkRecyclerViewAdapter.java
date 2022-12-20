package software.isratech.filetransferos.view.receive;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import software.isratech.filetransferos.R;

public class NetworkRecyclerViewAdapter extends RecyclerView.Adapter<NetworkRecyclerViewAdapter.ViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public NetworkRecyclerViewAdapter(
            @NonNull final Context context,
            @NonNull final List<String> data
    ) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void replaceData(final List<String> newData) {
        this.mData = newData;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearDataset() {
        this.mData.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View view = mInflater.inflate(R.layout.recycler_content, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the data to the textView in each row
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final String animal = mData.get(position);
        holder.myTextView.setText(animal);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Stores views and recycles them as they go off screen
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.recyclerServerIpAddress);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    /**
     * Get data at a given index
     */
    @NonNull
    public String getItem(int id) {
        return mData.get(id);
    }

    /**
     * Click action listener
     */
    public void setClickListener(@NonNull final ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    /**
     * parent activity will implement this method to respond to click events
     **/
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
