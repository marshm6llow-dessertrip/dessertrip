package com.example.dessertrip.ui.explore;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dessertrip.R;

// 지도 api 관련
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ExploreFragment extends Fragment {

    private static final int RED = Color.rgb(203, 75, 85);
    private static final int INK = Color.rgb(48, 43, 39);

    private Marker selectedMarker;

    public ExploreFragment() {
        super(R.layout.fragment_explore);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 예시 카페
        Cafe[] cafes = {
                new Cafe(new LatLng(37.5642, 126.9232),
                        "샘플 카페 A", "딸기 케이크 · 7,000원",
                        R.drawable.sample_yeonnam_cafe_a_dessert,
                        R.drawable.sample_yeonnam_cafe_a_dessert),
                new Cafe(new LatLng(37.5627, 126.9250),
                        "샘플 카페 B", "버터 스콘 · 4,500원", 0, 0)
        };

        // 지도 api
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.explore_map_container);

        if (mapFragment == null) return;

        mapFragment.getMapAsync(map -> {
            if (getView() != view) return;

            // 샘플 카페 마커 표시
            LatLng center = new LatLng(37.5635, 126.9239);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15f));
            map.getUiSettings().setZoomControlsEnabled(false);

            view.findViewById(R.id.map_zoom_in).setOnClickListener(v ->
                    map.animateCamera(CameraUpdateFactory.zoomIn()));

            view.findViewById(R.id.map_zoom_out).setOnClickListener(v ->
                    map.animateCamera(CameraUpdateFactory.zoomOut()));

            view.findViewById(R.id.search_this_area).setOnClickListener(v -> {
                LatLngBounds bounds = map.getProjection()
                        .getVisibleRegion().latLngBounds;
                drawMarkers(map, view, cafes, bounds);
                updateAreaName(view, map.getCameraPosition().target);
            });

            // 카페 카드
            map.setOnMarkerClickListener(marker -> {
                if (!(marker.getTag() instanceof Cafe)) return false;
                selectMarker(view, marker);
                return true;
            });

            drawMarkers(map, view, cafes, null);
        });
    }

    private int areaRequestId;

    @SuppressWarnings("deprecation")
    private void updateAreaName(View screen, LatLng center) {
        TextView areaName = screen.findViewById(R.id.explore_area_name);
        int requestId = ++areaRequestId;

        android.location.Geocoder geocoder = new android.location.Geocoder(
                requireContext().getApplicationContext(), java.util.Locale.KOREAN);

        new Thread(() -> {
            String name = null;

            try {
                java.util.List<android.location.Address> results =
                        geocoder.getFromLocation(center.latitude, center.longitude, 1);

                if (results != null && !results.isEmpty()) {
                    android.location.Address address = results.get(0);
                    String line = address.getAddressLine(0);

                    if (line != null) {
                        java.util.regex.Matcher matcher = java.util.regex.Pattern
                                .compile("(?:^|\\s)([가-힣]+[동읍면])(?=\\s|$)")
                                .matcher(line);
                        if (matcher.find()) name = matcher.group(1);
                    }

                    if (name == null) name = address.getSubLocality();
                    if (name == null) name = address.getLocality();
                }
            } catch (java.io.IOException | IllegalArgumentException ignored) {
            }

            String text = name == null ? "지역 정보 없음" : name;
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                if (getView() == screen && requestId == areaRequestId) {
                    areaName.setText(text);
                }
            });
        }).start();
    }

    private void drawMarkers(GoogleMap map, View screen, Cafe[] cafes,
                             @Nullable LatLngBounds bounds) {
        Cafe previous = selectedMarker == null
                ? null : (Cafe) selectedMarker.getTag();

        map.clear();
        selectedMarker = null;

        Marker first = null;
        Marker restored = null;

        for (Cafe cafe : cafes) {
            if (bounds != null && !bounds.contains(cafe.position)) continue;

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(cafe.position)
                    .icon(createMarkerIcon(cafe, false))
                    .anchor(0.5f, 1f));

            if (marker == null) continue;
            marker.setTag(cafe);

            if (first == null) first = marker;
            if (cafe == previous) restored = marker;
        }

        Marker next = restored != null ? restored : first;
        if (next != null) {
            selectMarker(screen, next);
        } else {
            showCafe(screen, null);
        }
    }

    private void selectMarker(View screen, Marker next) {
        if (selectedMarker == next) return;

        if (selectedMarker != null) {
            Cafe oldCafe = (Cafe) selectedMarker.getTag();
            selectedMarker.setIcon(createMarkerIcon(oldCafe, false));
            selectedMarker.setZIndex(0f);
        }

        Cafe cafe = (Cafe) next.getTag();
        next.setIcon(createMarkerIcon(cafe, true));
        next.setZIndex(1f);
        selectedMarker = next;
        showCafe(screen, cafe);
    }

    private void showCafe(View screen, @Nullable Cafe cafe) {
        TextView name = screen.findViewById(R.id.cafe_name);
        TextView menu = screen.findViewById(R.id.cafe_menu);
        ImageView photo = screen.findViewById(R.id.cafe_photo);

        name.setText(cafe == null ? "이 지역에 표시할 카페가 없어요" : cafe.name);
        menu.setText(cafe == null ? "" : cafe.menu);

        int photoRes = cafe == null ? 0 : cafe.cardPhoto;
        photo.setImageResource(photoRes);
        photo.setVisibility(photoRes == 0 ? View.GONE : View.VISIBLE);
    }

    private BitmapDescriptor createMarkerIcon(Cafe cafe, boolean selected) {
        View pin = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_explore_marker, null);

        View frame = pin.findViewById(R.id.marker_photo_frame);
        TextView label = pin.findViewById(R.id.marker_label);

        Bitmap sticker = null;
        if (cafe.pinPhoto != 0) {
            android.graphics.BitmapFactory.Options options =
                    new android.graphics.BitmapFactory.Options();
            options.inScaled = false;
            sticker = android.graphics.BitmapFactory.decodeResource(
                    getResources(), cafe.pinPhoto, options);
        }

        // 스티커가 있을 때
        frame.setVisibility(sticker != null ? View.INVISIBLE : View.GONE);

        int bubbleColor = selected ? RED : Color.WHITE;
        label.setText(cafe.name);
        label.setTextColor(selected ? Color.WHITE : INK);
        label.setBackground(rounded(bubbleColor, 8, Color.TRANSPARENT, 0));

        pin.measure(
                View.MeasureSpec.makeMeasureSpec(dp(112), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        pin.layout(0, 0, pin.getMeasuredWidth(), pin.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(
                pin.getMeasuredWidth(),
                pin.getMeasuredHeight() + dp(6),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        android.graphics.Paint paint = new android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG
                        | android.graphics.Paint.FILTER_BITMAP_FLAG);

        pin.draw(canvas);

        if (sticker != null) {
            int inset = dp(5);
            float scale = Math.min(
                    (frame.getWidth() - 2f * inset) / sticker.getWidth(),
                    (frame.getHeight() - 2f * inset) / sticker.getHeight());
            float width = sticker.getWidth() * scale;
            float height = sticker.getHeight() * scale;
            float left = frame.getLeft() + (frame.getWidth() - width) / 2f;
            float top = frame.getTop() + (frame.getHeight() - height) / 2f;
            android.graphics.RectF dest =
                    new android.graphics.RectF(left, top, left + width, top + height);

            if (selected) {
                // 선택된 스티커의 투명하지 않은 부분을 따라 빨간 윤곽선 표시
                paint.setColorFilter(new android.graphics.PorterDuffColorFilter(
                        RED, android.graphics.PorterDuff.Mode.SRC_IN));
                for (int i = 0; i < 16; i++) {
                    double angle = i * Math.PI / 8;
                    canvas.save();
                    canvas.translate(
                            (float) Math.cos(angle) * dp(2),
                            (float) Math.sin(angle) * dp(2));
                    canvas.drawBitmap(sticker, null, dest, paint);
                    canvas.restore();
                }
                paint.setColorFilter(null);
            }

            canvas.drawBitmap(sticker, null, dest, paint);
        }

        float x = bitmap.getWidth() / 2f;
        float y = label.getBottom();
        android.graphics.Path tail = new android.graphics.Path();
        tail.moveTo(x - dp(6), y - dp(2));
        tail.lineTo(x + dp(6), y - dp(2));
        tail.lineTo(x, bitmap.getHeight());
        tail.close();

        paint.setColor(bubbleColor);
        canvas.drawPath(tail, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    private GradientDrawable rounded(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));

        if (strokeWidth > 0) {
            shape.setStroke(dp(strokeWidth), strokeColor);
        }

        return shape;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        selectedMarker = null;
        super.onDestroyView();
    }

    private static class Cafe {
        final LatLng position;
        final String name;
        final String menu;
        final int pinPhoto;
        final int cardPhoto;

        Cafe(LatLng position, String name, String menu, int pinPhoto, int cardPhoto) {
            this.position = position;
            this.name = name;
            this.menu = menu;
            this.pinPhoto = pinPhoto;
            this.cardPhoto = cardPhoto;
        }
    }
}