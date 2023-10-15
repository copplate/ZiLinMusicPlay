package com.example.zilinmusicplay.adapetrrlv;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zilinmusicplay.R;
import com.example.zilinmusicplay.bean.Song;

import java.util.List;

public class RecySongListApapter extends RecyclerView.Adapter<RecySongListApapter.MySongItemViewHolder> {
    //Adapter是为我们的数据和列表之间做桥梁的，所以它一定要有一个数据源
    private List<Song> songLists;
    private Context context;
    private OnSongItemClickListener onSongItemClickListener;

    public RecySongListApapter(List<Song> songLists, Context context) {
        this.songLists = songLists;
        this.context = context;
    }

    @NonNull
    @Override
    public MySongItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //把布局转换成真正的View
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_name_list, parent, false);
        MySongItemViewHolder holder = new MySongItemViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MySongItemViewHolder holder, int position) {
        holder.bind(songLists.get(position));
    }

    @Override
    public int getItemCount() {
        return songLists == null ? 0 : songLists.size();
    }

    class MySongItemViewHolder extends RecyclerView.ViewHolder{
        private TextView tvSongName;
        private LinearLayout llSongItem;
        public MySongItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSongName = itemView.findViewById(R.id.tv_song_name);
            llSongItem = itemView.findViewById(R.id.ll_song_item);
            llSongItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onSongItemClickListener != null) {
                        onSongItemClickListener.click(getAdapterPosition());
                    }
                }
            });
        }

        public void bind(Song song) {
            tvSongName.setText(song.getSongName());
        }
    }

    public void setOnSongItemClickListener(OnSongItemClickListener onSongItemClickListener) {
        this.onSongItemClickListener = onSongItemClickListener;
    }

    public interface OnSongItemClickListener {
        void click(int position);
    }

}
