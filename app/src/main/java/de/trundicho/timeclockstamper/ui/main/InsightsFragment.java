package de.trundicho.timeclockstamper.ui.main;

import static android.app.Activity.RESULT_OK;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import de.trundicho.timeclockstamper.databinding.InsightsFragmentBinding;


public class InsightsFragment extends Fragment {
    private InsightsFragmentBinding binding;
    private AndroidFilePersistence androidFilePersistence;

    public static InsightsFragment newInstance() {
        return new InsightsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidFilePersistence = new AndroidFilePersistence("",
                "test-clockTime-list.json",
                "Europe/Berlin");
        androidFilePersistence.setActivityCallBack(new ActivityCallback(getActivity()));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.binding = InsightsFragmentBinding.inflate(inflater, container, false);
        Button exportButton = binding.exportButton;
        exportButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_TITLE, "TimeClockModel.json"); //not needed, but maybe usefull
            // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
            startActivityForResult(intent, 1);

        });
        Button importButton = binding.importButton;
        importButton.setOnClickListener(view -> {
            Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
            fileintent.setType("*/*");
            try {
                startActivityForResult(fileintent, 2);
            } catch (ActivityNotFoundException e) {
                System.err.println("ActivityNotFoundException " + e);
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                ParcelFileDescriptor fileDescriptor;
                try {
                    fileDescriptor = getActivity().getApplicationContext()
                            .getContentResolver()
                            .openFileDescriptor(uri, "w");

                    if (fileDescriptor != null) {
                        FileOutputStream outputStream = new FileOutputStream(
                                fileDescriptor.getFileDescriptor()
                        );
                        String value = androidFilePersistence.readJson(null, null);
                        outputStream.write(value.getBytes()
                        );
                        outputStream.close();
                        fileDescriptor.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == 2) {
                Uri uri = data.getData();
                try {
                    ParcelFileDescriptor fileDescriptor = getActivity().getApplicationContext()
                            .getContentResolver()
                            .openFileDescriptor(uri, "r");

                    if (fileDescriptor != null) {
                        InputStream inputStream = new FileInputStream(
                                fileDescriptor.getFileDescriptor()
                        );

                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                            String contents = reader.lines().collect(Collectors.joining("\n"));
                            androidFilePersistence.writeJson(contents);
                        }
                        fileDescriptor.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}