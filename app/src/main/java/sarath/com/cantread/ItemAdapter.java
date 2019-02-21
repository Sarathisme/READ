package sarath.com.cantread;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import sarath.com.cantread.interfaces.RecyclerViewClickListener;
import sarath.com.cantread.model.User;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private final static String ERROR_TAG = "ItemAdapter ERROR";
    private final static String DEBUG_TAG = "ItemAdapter DEBUG";

    private ArrayList<User> users;
    private Context mContext;
    private RecyclerViewClickListener mListener;

    public ItemAdapter(ArrayList<User> users, Context mContext, RecyclerViewClickListener mListener) {
        this.users = users;
        this.mContext = mContext;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_item, viewGroup, false);
        return new ItemViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder itemViewHolder, int i) {
        itemViewHolder.ocrTextView.setText(processOCRText(users.get(i).getOcrText()));
        itemViewHolder.dateTextView.setText(processDate(users.get(i).getTimestamp()));

        if(users.get(i).isStarred()) {
            itemViewHolder.starImageButton.setImageDrawable(mContext.getDrawable(R.drawable.ic_star_yellow_24dp));
        } else {
            itemViewHolder.starImageButton.setImageDrawable(mContext.getDrawable(R.drawable.ic_star_border_black_24dp));
        }

        try {
            InputStream inputStream = mContext.getContentResolver().openInputStream(Uri.parse(users.get(i).getImageLocalUrl()));
            Bitmap bmp = BitmapFactory.decodeStream(inputStream);

            // TODO: Compress image and add bitmap
            itemViewHolder.pictureImageView.setImageBitmap(bmp);
            if(inputStream != null)inputStream.close();
        } catch (FileNotFoundException e) {
            Picasso.get().load(users.get(i).getImageCloudUrl()).placeholder(R.drawable.bg).into(itemViewHolder.pictureImageView);
        } catch (IOException e) {
            Log.e(ERROR_TAG, "File not found!");
        } catch (NullPointerException e) {
            Picasso.get().load(users.get(i).getImageCloudUrl()).placeholder(R.drawable.bg).into(itemViewHolder.pictureImageView);
        }
    }

    private String processOCRText(String raw) {
        String[] ocrText = raw.split("[\\r\\n]+");
        StringBuilder builder = new StringBuilder();

        int counter = 0;
        while (counter < 4 && ocrText.length > counter) {
            builder.append(ocrText[counter++]).append("\n");
        }
        return builder.toString();
    }

    private String processDate(long raw) {
        String rawDate = new Date(raw).toString();
        String[] dateArray = rawDate.split("GMT");
        return dateArray[0];
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView ocrTextView, dateTextView;
        private ImageView pictureImageView;
        private ImageButton starImageButton;

        private RecyclerViewClickListener listener;

        ItemViewHolder(@NonNull View itemView, RecyclerViewClickListener listener) {
            super(itemView);

            starImageButton = itemView.findViewById(R.id.starredImageButton);
            ocrTextView = itemView.findViewById(R.id.ocrTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            pictureImageView = itemView.findViewById(R.id.pictureImageView);

            itemView.setOnClickListener(this);
            starImageButton.setOnClickListener(this);
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.starredImageButton) {
                int position = this.getAdapterPosition();
                listener.onButtonClick(v, position);
            } else {
                int position = this.getAdapterPosition();
                listener.onItemClick(pictureImageView, position);
            }
        }
    }
}
