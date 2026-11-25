package com.example.dessertrip.ui.archive.sticker;

import android.content.res.ColorStateList;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import com.example.dessertrip.R;
import com.example.dessertrip.databinding.FragmentGalleryBinding;
import com.example.dessertrip.databinding.ItemAlbumBinding;
import com.example.dessertrip.databinding.ItemGalleryPhotoBinding;
import com.example.dessertrip.ui.collect.common.ViewModels;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 앨범에서 사진 한 장 선택 (MediaStore). 사진 탭 / 앨범 탭 */
public class GalleryFragment extends Fragment {
    private static class DevicePhoto {
        final Uri uri;
        final String bucket;
        DevicePhoto(Uri uri, String bucket) { this.uri = uri; this.bucket = bucket; }
    }

    private FragmentGalleryBinding b;
    private final List<DevicePhoto> photos = new ArrayList<>();
    private String bucket;       // null = 전체
    private Uri selected;
    private PhotoAdapter photoAdapter;
    private AlbumAdapter albumAdapter;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) loadPhotos();
                else if (b != null) b.permissionText.setVisibility(View.VISIBLE);
            });

    private String permission() {
        return Build.VERSION.SDK_INT >= 33 ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentGalleryBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        final NavController nav = NavHostFragment.findNavController(this);
        b.topBar.titleText.setText("사진 선택");
        b.topBar.backButton.setVisibility(View.INVISIBLE);
        b.topBar.closeButton.setOnClickListener(v -> nav.popBackStack());

        photoAdapter = new PhotoAdapter();
        b.photoGrid.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        b.photoGrid.setAdapter(photoAdapter);
        albumAdapter = new AlbumAdapter();
        b.albumList.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.albumList.setAdapter(albumAdapter);

        b.tabs.addTab(b.tabs.newTab().setText("사진"));
        b.tabs.addTab(b.tabs.newTab().setText("앨범"));
        b.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { showTab(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        b.permissionText.setOnClickListener(v -> permissionLauncher.launch(permission()));
        b.addButton.setOnClickListener(v -> {
            if (selected == null) return;
            ViewModels.register(this).setPicked(selected);
            nav.navigate(R.id.stickerFragment);
        });
        renderSelection();

        if (ContextCompat.checkSelfPermission(requireContext(), permission()) == PackageManager.PERMISSION_GRANTED) {
            loadPhotos();
        } else {
            permissionLauncher.launch(permission());
        }
    }

    private void showTab(int pos) {
        boolean albums = pos == 1;
        b.photoGrid.setVisibility(albums ? View.GONE : View.VISIBLE);
        b.albumList.setVisibility(albums ? View.VISIBLE : View.GONE);
        b.sectionLabel.setText(albums ? "앨범" : (bucket == null ? "최근 항목" : bucket));
    }

    private void loadPhotos() {
        final ContentResolver resolver = requireContext().getContentResolver();
        b.permissionText.setVisibility(View.GONE);
        io.execute(() -> {
            final List<DevicePhoto> list = query(resolver);
            main.post(() -> {
                if (b == null) return;
                photos.clear();
                photos.addAll(list);
                photoAdapter.notifyDataSetChanged();
                albumAdapter.rebuild();
            });
        });
    }

    private static List<DevicePhoto> query(ContentResolver resolver) {
        List<DevicePhoto> result = new ArrayList<>();
        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        try (Cursor c = resolver.query(collection, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")) {
            if (c == null) return result;
            int idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int bucketCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            while (c.moveToNext() && result.size() < 500) {
                String name = c.getString(bucketCol);
                result.add(new DevicePhoto(ContentUris.withAppendedId(collection, c.getLong(idCol)), name == null ? "기타" : name));
            }
        }
        return result;
    }

    private List<DevicePhoto> visible() {
        if (bucket == null) return photos;
        List<DevicePhoto> out = new ArrayList<>();
        for (DevicePhoto p : photos) if (p.bucket.equals(bucket)) out.add(p);
        return out;
    }

    private void renderSelection() {
        b.addButton.setEnabled(selected != null);
        b.selectedTitle.setText(selected != null ? "사진 선택됨" : "사진을 선택해 주세요");
        if (selected != null) Glide.with(this).load(selected).into(b.selectedThumb);
        else b.selectedThumb.setImageDrawable(null);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }

    @Override public void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.VH> {
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemGalleryPhotoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            final DevicePhoto p = visible().get(position);
            boolean isSel = p.uri.equals(selected);
            Glide.with(h.b.image).load(p.uri).centerCrop().into(h.b.image);
            h.b.border.setVisibility(isSel ? View.VISIBLE : View.GONE);
            // 선택됨: 장미색 원(공통 모양 + 색), 선택 안 됨: 테두리 있는 원. 재활용될 때 색이 남지 않게 tint 를 지운다.
            h.b.checkCircle.setBackgroundResource(isSel ? R.drawable.bg_oval : R.drawable.bg_circle_select);
            h.b.checkCircle.setBackgroundTintList(isSel
                    ? ColorStateList.valueOf(ContextCompat.getColor(h.b.getRoot().getContext(), R.color.rose)) : null);
            h.b.checkCircle.setImageResource(isSel ? R.drawable.ic_check : 0);
            h.b.getRoot().setOnClickListener(v -> {
                selected = isSel ? null : p.uri;
                notifyDataSetChanged();
                renderSelection();
            });
        }

        @Override public int getItemCount() { return visible().size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemGalleryPhotoBinding b;
            VH(ItemGalleryPhotoBinding b) { super(b.getRoot()); this.b = b; }
        }
    }

    private class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.VH> {
        private final List<String> names = new ArrayList<>();
        private final Map<String, List<DevicePhoto>> groups = new LinkedHashMap<>();

        void rebuild() {
            groups.clear();
            for (DevicePhoto p : photos) {
                List<DevicePhoto> g = groups.get(p.bucket);
                if (g == null) { g = new ArrayList<>(); groups.put(p.bucket, g); }
                g.add(p);
            }
            names.clear();
            names.addAll(groups.keySet());
            notifyDataSetChanged();
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemAlbumBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            final String name = names.get(position);
            List<DevicePhoto> g = groups.get(name);
            h.b.name.setText(name);
            h.b.count.setText(g.size() + "장");
            Glide.with(h.b.thumb).load(g.get(0).uri).centerCrop().into(h.b.thumb);
            h.b.getRoot().setOnClickListener(v -> {
                bucket = name;
                photoAdapter.notifyDataSetChanged();
                TabLayout.Tab t = b.tabs.getTabAt(0);
                if (t != null) t.select();
                showTab(0);
            });
        }

        @Override public int getItemCount() { return names.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAlbumBinding b;
            VH(ItemAlbumBinding b) { super(b.getRoot()); this.b = b; }
        }
    }
}
