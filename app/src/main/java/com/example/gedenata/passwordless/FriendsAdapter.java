package com.example.gedenata.passwordless;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Profile;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    public static class FriendItem {
        final String id;
        final String name;
        final String image;

        public FriendItem(String id, String name, String image) {
            this.id = id;
            this.name = name;
            this.image = image;
        }
    }

    private final List<FriendItem> mValues;

    public FriendsAdapter(List<FriendItem> items) {
        mValues = items;
    }

    public FriendsAdapter() {
        mValues = new ArrayList<FriendItem>() {
            {
                add(new FriendsAdapter.FriendItem("1", "Steven Rogers", ""));
                add(new FriendsAdapter.FriendItem("1", "Anna Marie", ""));
                add(new FriendsAdapter.FriendItem("1", "Tony Stark", ""));
                add(new FriendsAdapter.FriendItem("1", "Ororo Monroe", ""));
                add(new FriendsAdapter.FriendItem("1", "Scott Summers", ""));
            }
        };
    }

    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friend, parent, false);
        return new FriendsAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final FriendsAdapter.ViewHolder holder, int position) {
        final FriendItem friendItem = mValues.get(position);
        FontHelper.setCustomTypeface(holder.mView);

        holder.mName.setText(friendItem.name);
        displayProfilePic(holder.mProfilePic, friendItem.image);

        Context c = holder.mView.getContext();
        SharedPreferences prefs = c.getSharedPreferences(Profile.getCurrentProfile().getId(), 0);
        boolean isFollowing = prefs.getBoolean(friendItem.id, false);
        updateFollowButton(holder.mFollowButton, isFollowing);

        holder.mFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context c = view.getContext();
                String userID = Profile.getCurrentProfile().getId();
                SharedPreferences prefs = c.getSharedPreferences(userID, 0);
                SharedPreferences.Editor editor = prefs.edit();

                boolean isFollowing = prefs.getBoolean(friendItem.id, false);
                editor.putBoolean(friendItem.id, !isFollowing);
                editor.commit();

                updateFollowButton((Button)view, !isFollowing);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mName;
        final ImageView mProfilePic;
        final Button mFollowButton;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mName = (TextView) view.findViewById(R.id.name);
            mProfilePic = (ImageView) view.findViewById(R.id.image);
            mFollowButton = (Button) view.findViewById(R.id.follow_button);
        }
    }

    private void displayProfilePic(ImageView imageView, String url) {
        Transformation transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(30)
                .oval(false)
                .build();
        Picasso.with(imageView.getContext())
                .load(url)
                .placeholder(R.drawable.icon_profile_empty)
                .transform(transformation)
                .into(imageView);
    }

    public void updateFollowButton(Button buttonView, boolean isFollowing) {
        Context c = buttonView.getContext();
        if (isFollowing) {
            buttonView.setText(c.getResources().getString(R.string.unfollow));
            buttonView.setBackgroundResource(R.drawable.unfollow_button_selector);
        } else {
            buttonView.setText(c.getResources().getString(R.string.follow));
            buttonView.setBackgroundResource(R.drawable.follow_button_selector);
        }
    }
}
