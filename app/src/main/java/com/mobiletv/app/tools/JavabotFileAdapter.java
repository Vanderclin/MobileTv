package com.mobiletv.app.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.mobiletv.app.R;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.List;

public class JavabotFileAdapter extends ArrayAdapter<JavabotFileMessage> {
    private Context context;
    private List<JavabotFileMessage> messages;

    public JavabotFileAdapter(Context context, List<JavabotFileMessage> messages) {
        super(context, 0, messages);
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_javabot, parent, false);
        }
        AppCompatTextView messageTextView = convertView.findViewById(R.id.javabot_message);
        AppCompatTextView nameTextView = convertView.findViewById(R.id.javabot_name);
        JavabotFileMessage message = messages.get(position);
        String jfa_name = message.getName().replace("\\n", "\n").replace("\\t", "\t");
        String jfa_message = message.getMessage().replace("\\n", "\n").replace("\\t", "\t");
        nameTextView.setText(jfa_name);
        messageTextView.setText(jfa_message);

        return convertView;
    }
}
