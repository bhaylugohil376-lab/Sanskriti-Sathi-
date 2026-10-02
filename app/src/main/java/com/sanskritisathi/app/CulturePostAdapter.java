package com.sanskritisathi.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CulturePostAdapter extends RecyclerView.Adapter<CulturePostAdapter.Holder> {
    private final Context context; private final List<CulturePost> items;
    public CulturePostAdapter(Context c, List<CulturePost> i){context=c;items=i;}
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p,int v){return new Holder(LayoutInflater.from(context).inflate(R.layout.item_culture_post,p,false));}
    @Override public void onBindViewHolder(@NonNull Holder h,int pos){
        CulturePost x=items.get(pos);
        h.author.setText(x.getAuthor()); h.category.setText(x.getCategory()); h.caption.setText(x.getCaption());
        h.likes.setText(String.valueOf(Math.max(0,x.getLikeCount()))); h.profile.setImageResource(x.getProfileImageResId()); h.image.setImageResource(x.getPostImageResId());
        h.like.setOnClickListener(v->{x.setLiked(!x.isLiked()); x.setLikeCount(Math.max(0,x.getLikeCount()+(x.isLiked()?1:-1))); notifyItemChanged(pos);});
        h.save.setOnClickListener(v->{x.setSaved(!x.isSaved()); h.save.setText(x.isSaved()?"🔖 Saved":"🔖 Save");});
        h.comment.setOnClickListener(v -> android.widget.Toast.makeText(context,"Comments ready",android.widget.Toast.LENGTH_SHORT).show());
        h.share.setOnClickListener(v -> { android.content.Intent s=new android.content.Intent(android.content.Intent.ACTION_SEND); s.setType("text/plain"); s.putExtra(android.content.Intent.EXTRA_TEXT,x.getCaption()); context.startActivity(android.content.Intent.createChooser(s,"Share Post"));});
    }
    @Override public int getItemCount(){return items.size();}
    static class Holder extends RecyclerView.ViewHolder{
        ImageView profile,image; TextView author,category,caption,likes,like,comment,share,save;
        Holder(View v){super(v);profile=v.findViewById(R.id.postProfileImage);image=v.findViewById(R.id.postImage);author=v.findViewById(R.id.postAuthor);category=v.findViewById(R.id.postCategory);caption=v.findViewById(R.id.postCaption);likes=v.findViewById(R.id.likeCount);like=v.findViewById(R.id.likeButton);comment=v.findViewById(R.id.commentButton);share=v.findViewById(R.id.shareButton);save=v.findViewById(R.id.saveButton);}
    }
}
