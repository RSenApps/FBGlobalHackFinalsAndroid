package rsen.com.fbglobalhacks;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.LyricViewHolder> {
    private List<Lyric> lyrics;
    private long timestampAdjustment;
    public MyRecyclerAdapter(Context context, List<Lyric> lyrics, long timestampAdjustment) {
        this.lyrics = lyrics;
        this.timestampAdjustment = timestampAdjustment;

    }
    public void updatetimetampAdjustment(long timestampAdjustment)
    {
        this.timestampAdjustment = timestampAdjustment;
        notifyDataSetChanged();
    }
    @Override
    public LyricViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row, null);

        LyricViewHolder viewHolder = new LyricViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LyricViewHolder customViewHolder, int i) {
        boolean isCurrent = true;
        Lyric lyric = lyrics.get(i);
        try {
            Lyric nextLyric = lyrics.get(i + 1);
            long adjustedTimestamp = System.currentTimeMillis() - timestampAdjustment;
            isCurrent = lyric.timestamp >= adjustedTimestamp && nextLyric.timestamp < adjustedTimestamp;
        } catch (Exception e) {
        }
        //Setting text view title
        customViewHolder.textView.setText(lyric.line);
        if (isCurrent)
        {
            customViewHolder.textView.setTextSize(25);
            customViewHolder.textView.setTypeface(null, Typeface.BOLD);
        }
        else {
            customViewHolder.textView.setTextSize(10);
            customViewHolder.textView.setTypeface(null, Typeface.NORMAL);

        }
    }

    @Override
    public int getItemCount() {
        return lyrics.size();
    }
    public static class LyricViewHolder extends RecyclerView.ViewHolder
    {
        TextView textView;
        public LyricViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.lyric);
        }
    }
}