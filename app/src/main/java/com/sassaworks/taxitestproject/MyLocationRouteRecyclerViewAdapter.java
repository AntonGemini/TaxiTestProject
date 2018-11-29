package com.sassaworks.taxitestproject;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sassaworks.taxitestproject.LocationRouteFragment.OnListFragmentInteractionListener;
import com.sassaworks.taxitestproject.database.LocationRoute;
import com.sassaworks.taxitestproject.database.LocationRouteDao;

import java.util.List;

public class MyLocationRouteRecyclerViewAdapter extends RecyclerView.Adapter<MyLocationRouteRecyclerViewAdapter.ViewHolder> {

    private final List<LocationRouteDao.TempLocal> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyLocationRouteRecyclerViewAdapter(List<LocationRouteDao.TempLocal> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_locationroute, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getName());
        holder.mContentView.setText(String.valueOf(mValues.get(position).getCreated_at()));
        holder.mNumberView.setText(String.valueOf(mValues.get(position).getCnt()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final TextView mNumberView;

        public LocationRouteDao.TempLocal mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
            mNumberView = view.findViewById(R.id.route_number);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
