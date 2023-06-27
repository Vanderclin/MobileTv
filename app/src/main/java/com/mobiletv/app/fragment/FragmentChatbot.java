package com.mobiletv.app.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mobiletv.app.R;
import com.mobiletv.app.javabot.Java;
import com.mobiletv.app.javabot.JavabotAdapter;
import com.mobiletv.app.javabot.Javabot;
import com.mobiletv.app.javabot.JavabotMessage;
import com.mobiletv.app.tools.JavabotFileAdapter;
import com.mobiletv.app.tools.JavabotFileMessage;
import com.mobiletv.app.tools.JavabotFileReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@SuppressLint({"InflateParams", "NotifyDataSetChanged"})
public class FragmentChatbot extends Fragment {

    private Java mJava;
    private JavabotAdapter javabotAdapter;
    private TextInputEditText mChatMessageInput;
    private Javabot mJavabot;
    private View mChatMessageLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFloatingSend;
    private static final int REQUEST_PERMISSION_CODE = 1;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_javabot, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_javabot, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.javabot_history) {
            openDialogHistory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(getString(R.string.javabot));
        checkStoragePermissionsWrite();
        initializeFindViews();
        initializeRefresh();
        initializeBot();

        mFloatingSend.setOnClickListener(v -> {
            String msg = Objects.requireNonNull(mChatMessageInput.getText()).toString().trim();
            if (!TextUtils.isEmpty(msg)) {
                sendMessage(false, msg, "Usuário");
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    sendMessage(true, mJavabot.getQuestion(requireActivity(), msg), "Javabot");
                }, 600);
                mChatMessageInput.getText().clear();
            }
        });
    }

    private void initializeFindViews() {
        mSwipeRefreshLayout = requireActivity().findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = requireActivity().findViewById(R.id.recycler_view);
        mChatMessageInput = requireActivity().findViewById(R.id.chat_message_input);
        mFloatingSend = requireActivity().findViewById(R.id.chat_button_send);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
    }

    private void initializeRefresh() {
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!mJava.getMessages().isEmpty()) {
                openDialogDelete();
            } else {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void initializeBot() {
        mJava = new Java();
        javabotAdapter = new JavabotAdapter(mJava.getMessages(), requireActivity());
        mRecyclerView.setAdapter(javabotAdapter);
        mJavabot = new Javabot(requireActivity());
    }

    private void sendMessage(Boolean bot, String msg, String name) {
        mJava.addMessage(new JavabotMessage(bot, msg, name));
        javabotAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(mJava.getMessages().size() - 1);
        saveHistory(bot, msg, name);
    }

    private void saveHistory(boolean bot, String message, String name) {
        long timestamp = System.currentTimeMillis();
        try {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/Javabot");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, "javabot.jb");
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            // Substituir os caracteres especiais por uma representação adequada
            message = message.replace("\n", "\\n").replace("\t", "\\t");
            name = name.replace("\n", "\\n").replace("\t", "\\t");

            String output = timestamp + "=[{\"" + message + "\",\"" + name + "\"}];\n";
            bufferedWriter.write(output);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void openDialogDelete() {
        MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialDialog);
        mBuilder.setTitle(getString(R.string.delete_conversation));
        mBuilder.setMessage(getString(R.string.delete_conversation));
        mBuilder.setNegativeButton(getString(R.string.delete_not), (dialog, which) -> {
            dialog.dismiss();
            mSwipeRefreshLayout.setRefreshing(false);
        });
        mBuilder.setPositiveButton(getString(R.string.delete_yes), (dialog, which) -> {
            dialog.dismiss();
            mJava.clearMessages();
            javabotAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
        });
        mBuilder.setCancelable(false);
        mBuilder.show();
    }

    private void openDialogHistory() {
        MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialDialog);
        final View rootView = requireActivity().getLayoutInflater().inflate(R.layout.dialog_history, null);
        final AlertDialog mDialog = mBuilder.create();
        mDialog.setView(rootView);

        ListView mListView = rootView.findViewById(R.id.dialog_history_list);

        JavabotFileReader fileReader = new JavabotFileReader();
        List<JavabotFileMessage> messages = fileReader.readJavabotFile();
        JavabotFileAdapter adapter = new JavabotFileAdapter(requireActivity(), messages);
        mListView.setAdapter(adapter);

        mDialog.setCancelable(true);
        mDialog.show();

    }

    private void checkStoragePermissionsWrite() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
        } else {
            checkStoragePermissions(true);
        }
    }

    private void checkStoragePermissions(Boolean isGranted) {
        if (isGranted != null && isGranted) {
            // TODO: A permissão foi concedida, você pode prosseguir com a operação.
            Toast.makeText(requireActivity(), "A permissão foi concedida", Toast.LENGTH_SHORT).show();
        } else {
            // TODO: A permissão foi negada, você precisa lidar com isso de acordo.
            Toast.makeText(requireActivity(), "A permissão foi negada", Toast.LENGTH_SHORT).show();
        }
    }

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
        boolean allGranted = true;
        for (Boolean granted : permissions.values()) {
            if (!Boolean.TRUE.equals(granted)) {
                allGranted = false;
                break;
            }
        }
        checkStoragePermissions(allGranted);
    });

}
