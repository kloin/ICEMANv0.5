package com.teamunemployment.breadcrumbs.Album;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.Album.data.Comment;
import com.teamunemployment.breadcrumbs.Explore.Presenter;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Josiah Kendall.
 * Adapter class for the comments on the frame viewer.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private ArrayList<Comment> commentsArray;
    private Context context;
    private AlbumPresenter presenter;
    private PreferencesAPI preferencesAPI;

    public CommentAdapter(ArrayList<Comment> commentsArray, Context context, AlbumPresenter presenter) {
        this.commentsArray = commentsArray;
        this.context = context;
        this.presenter = presenter;
        // bad. Think this adapter may need a rework at some point.
        this.preferencesAPI = new PreferencesAPI(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_comment, parent, false);
        ButterKnife.bind(this, v);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }



    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Comment comment = commentsArray.get(position);
        holder.setCommentTextView(comment.getCommentText());
        holder.setProfilePic(comment.getUserId());
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.DeleteComment(comment.getId());
                commentsArray.remove(position);
                notifyItemRemoved(position);
            }
        });

        holder.messageCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if we are
                if (comment.getUserId().equals(preferencesAPI.GetUserId())) {
                   RelativeLayout commentsWrapper = holder.wrapper;
                    if (commentsWrapper.getVisibility() == View.VISIBLE) {
                        commentsWrapper.setVisibility(View.GONE);
                        return;
                    }
                    commentsWrapper.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return commentsArray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout messageCardView;

        @Bind(R.id.commenter_profile_pic) CircleImageView profilePic;
        @Bind(R.id.comment) TextView commentTextView;
        @Bind(R.id.delete_comment_button) TextView deleteButton;
        @Bind(R.id.comment_buttons_wrapper) RelativeLayout wrapper;

        public ViewHolder(RelativeLayout itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            messageCardView = itemView;
        }
        public RelativeLayout getMessageCardView() {
            return messageCardView;
        }

        public void setProfilePic(final String userId) {
            // Load user profile pic id
            new Thread(new Runnable() {
                @Override
                public void run() {
                    presenter.SynchronouslyLoadUsersProfilePicId(userId, profilePic);

                }
            }).start();

        }

        public void setCommentTextView(String text) {
            commentTextView.setText(text);
        }
    }
}
