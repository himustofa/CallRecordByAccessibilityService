package com.subrasystems.record.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chibde.visualizer.LineVisualizer;
import com.subrasystems.record.R;
import com.subrasystems.record.models.Record;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class CallRecordAdapter extends RecyclerView.Adapter<CallRecordAdapter.MyViewHolder> {

    private CallBackListener mListener;
    private Context mContext;
    private ArrayList<Record> mArrayList;
    private MediaPlayer mPlayer;

    public interface CallBackListener {
        void onDelete(int position, Record model);
    }

    public CallRecordAdapter(Context mContext, ArrayList<Record> mArrayList, CallBackListener mListener) {
        this.mContext = mContext;
        this.mArrayList = mArrayList;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final Record model = mArrayList.get(position);
        //holder.pause.setEnabled(false);
        //holder.stop.setEnabled(false);

        holder.name.setText(model.getFileName());

        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String path = Environment.getExternalStorageDirectory() + "/My Records/" + dates + "/" + number + "_" + times + ".mp4"  ;
                //File file = new File("/storage/emulated/0/Android/data/com.cobalttechnology.myfirstapplication/files/" + fileName);
                playRecord(model, holder.visualizer);
                //holder.play.setEnabled(false);
                //holder.stop.setEnabled(true);
            }
        });
        holder.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //holder.play.setEnabled(true);
                //holder.stop.setEnabled(false);
                stopRecord();
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stopRecord();
                //holder.play.setEnabled(true);
                //holder.stop.setEnabled(false);
                mListener.onDelete(position, model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private LineVisualizer visualizer;
        private TextView name;
        private ImageButton play, stop, delete;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            visualizer = (LineVisualizer) itemView.findViewById(R.id.line_visualizer);
            name = (TextView) itemView.findViewById(R.id.record_file_name);
            play = (ImageButton) itemView.findViewById(R.id.play_button);
            stop = (ImageButton) itemView.findViewById(R.id.stop_button);
            delete = (ImageButton) itemView.findViewById(R.id.delete_button);
        }
    }

    private void playRecord(Record model, LineVisualizer lineVisualizer) {
        mPlayer = new MediaPlayer();
        try {
            FileInputStream mInputStream = new FileInputStream(model.getFilePath() + "/" + model.getFileName());
            mPlayer.setDataSource(mInputStream.getFD());
            mInputStream.close();
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.start();
        /*mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
            }
        });*/

        lineVisualizer.setColor(ContextCompat.getColor(mContext, R.color.colorDeepGrey));
        lineVisualizer.setStrokeWidth(4);
        lineVisualizer.setPlayer(mPlayer.getAudioSessionId());
    }

    private void stopRecord() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.seekTo(0);
        }
    }

}
