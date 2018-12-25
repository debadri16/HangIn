package com.example.asus.hangin;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView mRequestsList;
    private DatabaseReference mFrndReqDatabase;
    private DatabaseReference mUsersdatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_User_id;
    private View mMainView;

    private TextView mDefTxt;
    private DatabaseReference mChckDb;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mRequestsList = (RecyclerView) mMainView.findViewById(R.id.requests_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_User_id = mAuth.getCurrentUser().getUid();

        mFrndReqDatabase = FirebaseDatabase.getInstance().getReference().child("friend_req").child(mCurrent_User_id);
        mChckDb = FirebaseDatabase.getInstance().getReference().child("friend_req");

        mDefTxt = (TextView)mMainView.findViewById(R.id.fragment_req_def);

        mFrndReqDatabase.keepSynced(true);
        mUsersdatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersdatabase.keepSynced(true);

        mRequestsList.setHasFixedSize(true);
        mRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));


        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mChckDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mCurrent_User_id)){
                    mDefTxt.setVisibility(View.VISIBLE);
                    mRequestsList.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerOptions<Requests> options =
                new FirebaseRecyclerOptions.Builder<Requests>()
                        .setQuery(mFrndReqDatabase, Requests.class)
                        .build();

        FirebaseRecyclerAdapter<Requests, RequestsViewHolder> requestsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Requests model) {

                holder.setStatus(model.getRequest_type());

                final String list_user_id = getRef(position).getKey();

                mUsersdatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        //final String userStatus = dataSnapshot.child("status").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnline);
                        }

                        holder.setName(userName);
                        holder.setUserImage(userThumb);
                        //holder.setStatus(userStatus);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent profile_intent = new Intent(getContext(), ProfileActivity.class);
                                profile_intent.putExtra("user_id", list_user_id);
                                startActivity(profile_intent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.users_single_layout, viewGroup, false);

                return new RequestsViewHolder(view);

            }
        };
        requestsRecyclerViewAdapter.startListening();
        mRequestsList.setAdapter(requestsRecyclerViewAdapter);

    }

    private static class RequestsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RequestsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setStatus(String status){
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_status);
            userNameView.setText(status);
        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_img){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_img);

            Picasso.get().load(thumb_img).placeholder(R.drawable.default_avatar).into(userImageView);

        }

        public void setUserOnline(String is_online){

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icn);

            if(is_online.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            }
            else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }

        }

    }

}
