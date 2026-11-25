package com.example.dessertrip.ui.collect.common;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.dessertrip.ui.collect.AppContainer;
import com.example.dessertrip.ui.collect.DessertApplication;
import com.example.dessertrip.ui.archive.collect.ArchiveViewModel;
import com.example.dessertrip.ui.archive.plate.PlateViewModel;
import com.example.dessertrip.R;
import com.example.dessertrip.ui.collect.core.util.Event;
import com.example.dessertrip.ui.archive.sticker.RegisterFlowViewModel;

public final class ViewModels {
    private ViewModels() { }

    private static ViewModelProvider.Factory factory(Fragment f) {
        final AppContainer c = DessertApplication.container(f.requireContext());
        return new ViewModelProvider.Factory() {
            @SuppressWarnings("unchecked")
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> cls) {
                if (cls == ArchiveViewModel.class) return (T) new ArchiveViewModel(c.repository);
                if (cls == PlateViewModel.class) return (T) new PlateViewModel(c.repository);
                if (cls == RegisterFlowViewModel.class) return (T) new RegisterFlowViewModel(c.repository, c.backgroundRemover);
                throw new IllegalArgumentException("Unknown ViewModel: " + cls.getName());
            }
        };
    }

    /** Fragment 범위 ViewModel */
    public static <T extends ViewModel> T of(Fragment f, Class<T> cls) {
        return new ViewModelProvider(f, factory(f)).get(cls);
    }

    /** 기록하기 플로우(register_graph) 전체가 공유하는 ViewModel */
    public static RegisterFlowViewModel register(Fragment f) {
        NavController nc = NavHostFragment.findNavController(f);
        return new ViewModelProvider(nc.getViewModelStoreOwner(R.id.register_graph), factory(f))
                .get(RegisterFlowViewModel.class);
    }

    /** 일회성 메시지를 토스트로 표시 */
    public static void showMessages(final Fragment f, LiveData<Event<String>> messages) {
        messages.observe(f.getViewLifecycleOwner(), event -> {
            String m = event == null ? null : event.getContentIfNotHandled();
            if (m != null && f.getContext() != null) Toast.makeText(f.requireContext(), m, Toast.LENGTH_SHORT).show();
        });
    }
}
