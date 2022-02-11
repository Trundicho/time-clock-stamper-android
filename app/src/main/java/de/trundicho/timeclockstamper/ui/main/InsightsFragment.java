package de.trundicho.timeclockstamper.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
            final Intent intent1 = new Intent(Intent.ACTION_SEND);
            intent1.setType("text/plain");
            intent1.putExtra(Intent.EXTRA_TEXT, androidFilePersistence.readJson(null, null));
            try {
                startActivity(Intent.createChooser(intent1, "Select an action"));
            } catch (android.content.ActivityNotFoundException ex) {
                // (handle error)
                System.err.println("ActivityNotFoundException " + ex);
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}