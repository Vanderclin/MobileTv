package com.mobiletv.app.javabot;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.card.MaterialCardView;
import com.mobiletv.app.R;

import java.util.List;

public class JavabotAdapter extends RecyclerView.Adapter<JavabotAdapter.MessageViewHolder> {

    private final List<JavabotMessage> javabotMessages;
    private final Context context;

    public JavabotAdapter(List<JavabotMessage> javabotMessages, Context context) {
        this.javabotMessages = javabotMessages;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return javabotMessages.size();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_javabot, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder mHolder, int position) {
        JavabotMessage javabotMessage = javabotMessages.get(position);
        mHolder.javabotName.setText(javabotMessage.getName());
        mHolder.javabotMessage.setText(javabotMessage.getMessage());
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public MaterialCardView javabotCard;
        public AppCompatTextView javabotName;
        public AppCompatTextView javabotDate;
        public AppCompatTextView javabotMessage;

        public MessageViewHolder(View itemView) {
            super(itemView);
            javabotCard = itemView.findViewById(R.id.javabot_card);
            javabotName = itemView.findViewById(R.id.javabot_name);
            javabotDate = itemView.findViewById(R.id.javabot_date);
            javabotMessage = itemView.findViewById(R.id.javabot_message);
        }
    }
}