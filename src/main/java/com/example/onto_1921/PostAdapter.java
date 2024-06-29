
package com.example.onto_1921;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.textViewTitle.setText(post.getTitle());

        holder.itemlinear.setOnClickListener(v -> {
            int clickedPosition = holder.getAdapterPosition();
            if (clickedPosition != RecyclerView.NO_POSITION) { // 위치가 유효한 경우에만 처리
                Post clickedPost = postList.get(clickedPosition);
                // 클릭한 게시글의 상세 내용을 보여주는 화면으로 이동하는 인텐트를 생성
                Intent intent = new Intent(v.getContext(), GpostDetailActivity.class);
                // 인텐트에 클릭한 게시글의 정보를 추가
                intent.putExtra("postId", clickedPost.getPostId());
                intent.putExtra("title", clickedPost.getTitle());
                intent.putExtra("content", clickedPost.getContent());
                intent.putExtra("authorId", clickedPost.getAuthorId());
                // GpostDetailActivity로 이동
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        LinearLayout itemlinear; // 리니어 레이아웃 추가

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            itemlinear = itemView.findViewById(R.id.itemlinear);
        }
    }
}