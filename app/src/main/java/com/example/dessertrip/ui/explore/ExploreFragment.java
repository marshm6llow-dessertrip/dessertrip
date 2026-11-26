package com.example.dessertrip.ui.explore;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.dessertrip.R;

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
    private static final int GRAY = Color.rgb(138, 125, 115);
    private static final int LINE = Color.rgb(231, 223, 213);
    private static final int PINK = Color.rgb(246, 231, 229);

    // 대한민국 범위 (제주·마라도·독도 포함)
    private static final LatLngBounds KOREA = new LatLngBounds(
            new LatLng(32.9, 124.5), new LatLng(38.7, 132.0));

    // 앱을 켤 때마다 바뀌는 값: 마커 스티커 랜덤 선택에 사용
    private static final long SESSION_SEED = new java.util.Random().nextLong();

    // {사용자가 치는 별칭, 실제로 찾을 정식 지명}
    private static final String[][] ALIASES = {
            {"홍대", "홍대입구역"},
            {"홍대입구", "홍대입구역"},
            {"성수", "성수동"},
            {"연남", "연남동"},
            {"강남", "강남역"},
            {"건대", "건대입구역"},
            {"이대", "이대역"},
            {"신촌", "신촌역"},
            {"잠실", "잠실역"},
            {"여의도", "여의도동"},
            {"해운대", "해운대구"},
            {"서면", "서면역"},
            {"경리단길", "이태원동"},
            {"익선동", "익선동"},
            {"북촌", "북촌한옥마을"},
            {"제주도", "제주특별자치도"}
    };

    // 필터 화면 항목
    private static final String[] DIST_LABELS = {"전체", "500m", "1km", "3km"};
    private static final int[] DIST_VALUES = {0, 500, 1000, 3000};
    private static final String[] DESSERTS = {
            "케이크", "소금빵", "까눌레", "쿠키", "휘낭시에", "크루아상", "기타"
    };

    private Marker selectedMarker;
    private Cafe selectedCafe;
    private String searchKeyword = "";

    // 적용된 필터 (상단 칩과 필터 화면이 같은 값을 공유)
    private final java.util.Set<String> selectedDesserts = new java.util.LinkedHashSet<>();
    private int distanceLimit = 0;      // 0 = 전체 (미터)
    private boolean openOnly = false;   // UI만, 실제 필터링은 추후

    // 필터 화면에서 고르는 중인 값 (적용 누르기 전)
    private final java.util.Set<String> draftDesserts = new java.util.LinkedHashSet<>();
    private int draftDistance = 0;
    private boolean draftOpen = false;

    private View filterView;
    private OnBackPressedCallback backCallback;

    // 상단 칩에서 지도를 다시 그릴 때 사용
    private GoogleMap currentMap;
    private Cafe[] currentCafes;

    private final java.util.List<Marker> markers = new java.util.ArrayList<>();
    private boolean suppressWatcher = false;
    private int suggestRequestId;
    private Runnable pendingSuggest;
    private final android.os.Handler uiHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());

    private interface ChipClick {
        void onClick(int index);
    }

    public ExploreFragment() {
        super(R.layout.fragment_explore);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        android.widget.EditText searchInput =
                view.findViewById(R.id.explore_search);

        // 예시 카페 (테스트용)
        // 스티커 이미지가 카페별로 생기면 STICKER 대신 각각의 R.drawable.xxx를 넣으세요
        final int STICKER = R.drawable.sample_yeonnam_cafe_a_dessert;

        Cafe[] cafes = {
                new Cafe(new LatLng(37.5642, 126.9232),
                        "샘플 카페 A", "딸기 케이크 · 7,000원", "케이크", STICKER,
                        new Sticker("케이크", STICKER), new Sticker("쿠키", STICKER)),
                new Cafe(new LatLng(37.5627, 126.9250),
                        "샘플 카페 B", "버터 스콘 · 4,500원", "기타", 0),
                new Cafe(new LatLng(37.5660, 126.9262),
                        "샘플 카페 C", "소금빵 · 3,800원", "소금빵", 0,
                        new Sticker("소금빵", STICKER), new Sticker("크루아상", STICKER)),
                new Cafe(new LatLng(37.5590, 126.9300),
                        "샘플 카페 D", "바닐라 까눌레 · 4,200원", "까눌레", 0,
                        new Sticker("까눌레", STICKER)),
                new Cafe(new LatLng(37.5655, 126.9215),
                        "샘플 카페 E", "치즈 케이크 · 6,500원", "케이크", 0,
                        new Sticker("케이크", STICKER), new Sticker("까눌레", STICKER)),
                new Cafe(new LatLng(37.5700, 126.9400),
                        "샘플 카페 F", "초코칩 쿠키 · 3,500원", "쿠키", 0,
                        new Sticker("쿠키", STICKER)),
                new Cafe(new LatLng(37.5530, 126.9100),
                        "샘플 카페 G", "휘낭시에 세트 · 9,000원", "휘낭시에", 0,
                        new Sticker("휘낭시에", STICKER)),
                new Cafe(new LatLng(37.5450, 126.9400),
                        "샘플 카페 H", "버터 크루아상 · 4,800원", "크루아상", 0,
                        new Sticker("크루아상", STICKER), new Sticker("소금빵", STICKER))
        };

        currentCafes = cafes;
        syncTopChips(view);

        final GoogleMap[] mapRef = new GoogleMap[1];

        // 필터 버튼
        view.findViewById(R.id.explore_filter_button).setOnClickListener(v ->
                openFilter(view, mapRef, cafes));

        // 엔터/검색 버튼: 카페가 안 맞으면 동네 이름으로 보고 지도 이동
        Runnable runSearch = () -> {
            searchKeyword = searchInput.getText().toString().trim();
            hideSuggestions(view);

            GoogleMap map = mapRef[0];
            if (map == null) return;

            boolean hasCafeMatch = false;
            for (Cafe c : cafes) {
                if (matches(c, searchKeyword)) {
                    hasCafeMatch = true;
                    break;
                }
            }

            if (!searchKeyword.isEmpty() && !hasCafeMatch) {
                moveToArea(map, view, cafes, searchKeyword);
                return;
            }

            drawMarkers(map, view, cafes, null);
        };

        // 입력할 때마다 검색 + 연관 검색어 갱신
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(android.text.Editable s) {
                if (suppressWatcher) return;
                searchKeyword = s.toString().trim();

                GoogleMap map = mapRef[0];
                if (map == null) return;

                drawMarkers(map, view, cafes, null);
                showSuggestions(view, searchInput, map, cafes, searchKeyword);
            }
        });

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean searchAction =
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;
            boolean enterKey = event != null
                    && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER
                    && event.getAction() == android.view.KeyEvent.ACTION_DOWN;

            if (searchAction || enterKey) {
                runSearch.run();
                hideKeyboard(searchInput);
                return true;
            }
            return false;
        });

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.explore_map_container);

        if (mapFragment == null) return;

        mapFragment.getMapAsync(map -> {
            mapRef[0] = map;
            currentMap = map;
            if (getView() != view) return;

            LatLng center = new LatLng(37.5635, 126.9239);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15f));
            map.getUiSettings().setZoomControlsEnabled(false);

            // 우리 앱 스타일 지도
            map.setMapStyle(com.google.android.gms.maps.model.MapStyleOptions
                    .loadRawResourceStyle(requireContext(), R.raw.map_style));

            // 대한민국 안에서만 이동·확대/축소
            map.setLatLngBoundsForCameraTarget(KOREA);
            map.setMinZoomPreference(6.5f);

            view.findViewById(R.id.map_zoom_in).setOnClickListener(v ->
                    map.animateCamera(CameraUpdateFactory.zoomIn()));

            view.findViewById(R.id.map_zoom_out).setOnClickListener(v ->
                    map.animateCamera(CameraUpdateFactory.zoomOut()));

            // 내 위치로 다시 이동
            view.findViewById(R.id.map_my_location).setOnClickListener(v ->
                    startAtMyLocation(map, view, cafes));

            view.findViewById(R.id.search_this_area).setOnClickListener(v -> {
                LatLngBounds bounds = map.getProjection()
                        .getVisibleRegion().latLngBounds;
                drawMarkers(map, view, cafes, bounds);
                updateAreaName(view, map.getCameraPosition().target);
            });

            map.setOnMapClickListener(latLng -> hideSuggestions(view));

            map.setOnMarkerClickListener(marker -> {
                if (!(marker.getTag() instanceof Cafe)) return false;
                hideSuggestions(view);
                selectMarker(view, marker);
                return true;
            });

            drawMarkers(map, view, cafes, null);
            startAtMyLocation(map, view, cafes);
        });
    }

    // ===== 현재 위치 =====

    private Runnable onLocationGranted;

    private final androidx.activity.result.ActivityResultLauncher<String[]> locationPermission =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts
                            .RequestMultiplePermissions(),
                    result -> {
                        boolean granted =
                                Boolean.TRUE.equals(result.get(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION))
                                        || Boolean.TRUE.equals(result.get(
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION));
                        Runnable task = onLocationGranted;
                        onLocationGranted = null;
                        if (granted && task != null && isAdded()) task.run();
                    });

    private boolean hasLocationPermission() {
        return androidx.core.content.ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED
                || androidx.core.content.ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    // 권한이 있으면 바로 내 위치로, 없으면 권한을 물어본 뒤 이동
    private void startAtMyLocation(GoogleMap map, View screen, Cafe[] cafes) {
        if (hasLocationPermission()) {
            moveToMyLocation(map, screen, cafes);
            return;
        }

        onLocationGranted = () -> {
            if (getView() == screen) moveToMyLocation(map, screen, cafes);
        };
        locationPermission.launch(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    @SuppressWarnings({"deprecation", "MissingPermission"})
    private void moveToMyLocation(GoogleMap map, View screen, Cafe[] cafes) {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);

        android.location.LocationManager lm = (android.location.LocationManager)
                requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);
        if (lm == null) return;

        android.location.Location best = null;
        for (String provider : lm.getProviders(true)) {
            android.location.Location l = lm.getLastKnownLocation(provider);
            if (l != null && (best == null || l.getTime() > best.getTime())) best = l;
        }
        if (best != null) {
            goToLocation(map, screen, cafes, best);
            return;
        }

        String provider = null;
        if (lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
            provider = android.location.LocationManager.NETWORK_PROVIDER;
        } else if (lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            provider = android.location.LocationManager.GPS_PROVIDER;
        }
        if (provider == null) return;

        lm.requestSingleUpdate(provider, new android.location.LocationListener() {
            public void onLocationChanged(@NonNull android.location.Location location) {
                goToLocation(map, screen, cafes, location);
            }

            public void onStatusChanged(String p, int status, Bundle extras) {}

            public void onProviderEnabled(@NonNull String p) {}

            public void onProviderDisabled(@NonNull String p) {}
        }, android.os.Looper.getMainLooper());
    }

    private void goToLocation(GoogleMap map, View screen, Cafe[] cafes,
                              android.location.Location loc) {
        if (getView() != screen) return;

        LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
        if (!KOREA.contains(pos)) return;

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f),
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (getView() != screen) return;
                        drawMarkers(map, screen, cafes, null);
                        updateAreaName(screen, pos);
                    }

                    @Override
                    public void onCancel() {}
                });
    }

    // ===== 상단 디저트 칩 (중복 선택, 필터와 같은 값 공유) =====

    private void syncTopChips(View screen) {
        LinearLayout box = screen.findViewById(R.id.category_box);
        if (box == null) return;

        box.removeAllViews();
        addTopChip(box, screen, "전체", selectedDesserts.isEmpty());
        for (String dessert : DESSERTS) {
            addTopChip(box, screen, dessert, selectedDesserts.contains(dessert));
        }
    }

    private void addTopChip(LinearLayout box, View screen, String label, boolean sel) {
        TextView chip = new TextView(requireContext());
        chip.setText(label);
        chip.setGravity(Gravity.CENTER);
        chip.setTextSize(14f);
        chip.setPadding(dp(14), 0, dp(14), 0);
        chip.setTextColor(sel ? Color.WHITE : INK);
        chip.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
        chip.setBackground(rounded(sel ? RED : Color.WHITE, 18,
                sel ? RED : LINE, 1));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(36));
        if (box.getChildCount() > 0) params.leftMargin = dp(8);
        box.addView(chip, params);

        chip.setOnClickListener(v -> {
            if (label.equals("전체")) {
                selectedDesserts.clear();
            } else {
                toggle(selectedDesserts, label);
            }

            syncTopChips(screen);

            if (currentMap != null && currentCafes != null) {
                drawMarkers(currentMap, screen, currentCafes, null);
            }
        });
    }

    // ===== 필터 화면 =====

    private void openFilter(View screen, GoogleMap[] mapRef, Cafe[] cafes) {
        if (filterView != null) return;

        hideKeyboard(screen);
        hideSuggestions(screen);

        ViewGroup content = requireActivity().findViewById(android.R.id.content);
        filterView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_explore_filter, content, false);
        content.addView(filterView);

        // 지금 적용된 값에서 시작
        draftDesserts.clear();
        draftDesserts.addAll(selectedDesserts);
        draftDistance = distanceLimit;
        draftOpen = openOnly;

        // 시스템 뒤로가기 = 필터 닫기
        backCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                closeFilter();
            }
        };
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), backCallback);

        TextView reset = filterView.findViewById(R.id.filter_reset);
        TextView apply = filterView.findViewById(R.id.filter_apply);
        reset.setBackground(rounded(Color.WHITE, 28, LINE, 1));
        apply.setBackground(rounded(RED, 28, Color.TRANSPARENT, 0));

        filterView.findViewById(R.id.filter_back).setOnClickListener(v -> closeFilter());

        reset.setOnClickListener(v -> {
            draftDesserts.clear();
            draftDistance = 0;
            draftOpen = false;
            renderFilter();
        });

        apply.setOnClickListener(v -> {
            selectedDesserts.clear();
            selectedDesserts.addAll(draftDesserts);
            distanceLimit = draftDistance;
            openOnly = draftOpen;

            syncTopChips(screen);
            closeFilter();

            GoogleMap map = mapRef[0];
            if (map != null) drawMarkers(map, screen, cafes, null);
        });

        renderFilter();
    }

    private void closeFilter() {
        if (backCallback != null) {
            backCallback.remove();
            backCallback = null;
        }
        if (filterView != null) {
            ViewGroup parent = (ViewGroup) filterView.getParent();
            if (parent != null) parent.removeView(filterView);
            filterView = null;
        }
    }

    // 필터 화면의 칩·토글·요약을 draft 값 기준으로 다시 그림
    private void renderFilter() {
        if (filterView == null) return;

        // 거리 (하나만 선택)
        boolean[] distOn = new boolean[DIST_LABELS.length];
        for (int i = 0; i < DIST_LABELS.length; i++) {
            distOn[i] = DIST_VALUES[i] == draftDistance;
        }
        renderChips(filterView.findViewById(R.id.filter_distance_box),
                DIST_LABELS, distOn, i -> {
                    draftDistance = DIST_VALUES[i];
                    renderFilter();
                });

        // 디저트 (여러 개 선택)
        boolean[] dessertOn = new boolean[DESSERTS.length];
        for (int i = 0; i < DESSERTS.length; i++) {
            dessertOn[i] = draftDesserts.contains(DESSERTS[i]);
        }
        renderChips(filterView.findViewById(R.id.filter_dessert_box),
                DESSERTS, dessertOn, i -> {
                    toggle(draftDesserts, DESSERTS[i]);
                    renderFilter();
                });

        // 영업 중 토글 (UI만)
        SwitchCompat sw = filterView.findViewById(R.id.filter_open_switch);
        sw.setOnCheckedChangeListener(null);
        sw.setChecked(draftOpen);
        sw.setOnCheckedChangeListener((b, checked) -> {
            draftOpen = checked;
            renderFilter();
        });

        // 선택한 조건 요약
        java.util.List<String> items = new java.util.ArrayList<>();
        if (draftDistance > 0) {
            for (int i = 0; i < DIST_VALUES.length; i++) {
                if (DIST_VALUES[i] == draftDistance) items.add(DIST_LABELS[i] + " 이내");
            }
        }
        items.addAll(draftDesserts);
        if (draftOpen) items.add("영업 중");

        filterView.findViewById(R.id.filter_summary_box)
                .setBackground(rounded(PINK, 16, Color.TRANSPARENT, 0));
        TextView title = filterView.findViewById(R.id.filter_summary_title);
        TextView text = filterView.findViewById(R.id.filter_summary_text);

        if (items.isEmpty()) {
            title.setText("선택한 조건이 없어요");
            text.setText("조건을 고르면 여기에 모아 보여드려요.");
        } else {
            title.setText("선택한 조건 " + items.size() + "개");
            text.setText(TextUtils.join(" · ", items));
        }
    }

    private void toggle(java.util.Set<String> set, String value) {
        if (!set.remove(value)) set.add(value);
    }

    // 칩을 화면 너비에 맞춰 자동으로 줄바꿈하며 그림
    private void renderChips(LinearLayout box, String[] labels, boolean[] on, ChipClick click) {
        box.removeAllViews();

        float available = getResources().getDisplayMetrics().widthPixels - dp(48);
        float used = 0;
        LinearLayout row = null;

        for (int i = 0; i < labels.length; i++) {
            boolean sel = on[i];

            TextView chip = new TextView(requireContext());
            chip.setText(labels[i]);
            chip.setGravity(Gravity.CENTER);
            chip.setTextSize(14f);
            chip.setPadding(dp(16), 0, dp(16), 0);
            chip.setTextColor(sel ? RED : GRAY);
            chip.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
            chip.setBackground(rounded(sel ? PINK : Color.WHITE, 20,
                    sel ? RED : LINE, 1));

            float width = chip.getPaint().measureText(labels[i]) + dp(32) + dp(6);

            if (row == null || used + dp(8) + width > available) {
                row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                if (box.getChildCount() > 0) rowParams.topMargin = dp(10);
                box.addView(row, rowParams);
                used = 0;
            }

            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, dp(40));
            if (used > 0) chipParams.leftMargin = dp(8);
            used += (used > 0 ? dp(8) : 0) + width;
            row.addView(chip, chipParams);

            final int index = i;
            chip.setOnClickListener(v -> click.onClick(index));
        }
    }

    // ===== 연관 검색어 =====

    private String resolveAlias(String input) {
        for (String[] alias : ALIASES) {
            if (alias[0].startsWith(input) || input.startsWith(alias[0])) {
                return alias[1];
            }
        }
        return input;
    }

    @SuppressWarnings("deprecation")
    private void showSuggestions(View screen, android.widget.EditText searchInput,
                                 GoogleMap map, Cafe[] cafes, String input) {
        LinearLayout box = screen.findViewById(R.id.explore_suggestions);
        box.removeAllViews();

        if (pendingSuggest != null) uiHandler.removeCallbacks(pendingSuggest);
        final int requestId = ++suggestRequestId;

        if (input.isEmpty()) {
            box.setVisibility(View.GONE);
            return;
        }

        // 1) 카페는 바로 표시
        int count = 0;
        for (Cafe cafe : cafes) {
            if (count >= 3) break;
            if (!matches(cafe, input)) continue;

            addSuggestionRow(box, "☕  " + cafe.name + "  ·  " + cafe.category, v ->
                    pickCafe(screen, searchInput, map, cafes, cafe));
            count++;
        }
        box.setVisibility(count == 0 ? View.GONE : View.VISIBLE);

        // 2) 지명은 입력이 0.3초 멈추면 Geocoder로 검색해서 추가
        pendingSuggest = () -> new Thread(() -> {
            java.util.List<android.location.Address> results = null;
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(
                        requireContext().getApplicationContext(), java.util.Locale.KOREAN);
                results = geocoder.getFromLocationName(
                        resolveAlias(input), 5, 32.9, 124.5, 38.7, 132.0);
            } catch (java.io.IOException | IllegalArgumentException
                     | IllegalStateException ignored) {
            }

            final java.util.List<android.location.Address> list =
                    results == null ? new java.util.ArrayList<>() : results;

            uiHandler.post(() -> {
                if (getView() != screen || requestId != suggestRequestId) return;

                java.util.Set<String> seen = new java.util.HashSet<>();
                for (android.location.Address a : list) {
                    String line = a.getAddressLine(0);
                    if (line == null) continue;

                    String addr = line.replace("대한민국 ", "").trim();

                    // 한글이 전혀 없는 결과(영문 주소)는 건너뜀
                    if (!addr.matches(".*[가-힣].*")) continue;

                    // 섞인 영문 단어 제거
                    addr = addr.replaceAll("[A-Za-z][A-Za-z\\-.]*", "")
                            .replaceAll("\\s+", " ").trim();

                    // 도로명·번지는 잘라내고 시/구/동 단위까지만
                    StringBuilder region = new StringBuilder();
                    for (String token : addr.split("\\s+")) {
                        if (token.matches(".*(로|길)\\d*(번길)?$")
                                || token.matches("\\d+(-\\d+)?")) break;
                        if (region.length() > 0) region.append(' ');
                        region.append(token);
                    }

                    String area = region.length() == 0 ? addr : region.toString();
                    final String label = area.contains(input) ? area : input + " · " + area;
                    if (label.isEmpty() || !seen.add(label)) continue;

                    final LatLng pos = new LatLng(a.getLatitude(), a.getLongitude());
                    addSuggestionRow(box, "📍  " + label, v ->
                            pickPlace(screen, searchInput, map, cafes, input, pos));
                }

                if (box.getChildCount() == 0) {
                    addSuggestionRow(box, "검색 결과가 없어요", null);
                }
                box.setVisibility(View.VISIBLE);
            });
        }).start();

        uiHandler.postDelayed(pendingSuggest, 300);
    }

    private void addSuggestionRow(LinearLayout box, String text, View.OnClickListener click) {
        TextView row = new TextView(requireContext());
        row.setText(text);
        row.setTextColor(INK);
        row.setTextSize(14f);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));
        row.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        if (click != null) {
            row.setOnClickListener(click);
        } else {
            row.setTextColor(GRAY);
        }
        box.addView(row);
    }

    private void hideSuggestions(View screen) {
        LinearLayout box = screen.findViewById(R.id.explore_suggestions);
        if (box != null) box.setVisibility(View.GONE);
    }

    private void pickCafe(View screen, android.widget.EditText searchInput,
                          GoogleMap map, Cafe[] cafes, Cafe cafe) {
        setSearchText(searchInput, cafe.name);
        searchKeyword = cafe.name;
        hideSuggestions(screen);
        hideKeyboard(searchInput);

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(cafe.position, 16f));
        drawMarkers(map, screen, cafes, null);
        updateAreaName(screen, cafe.position);

        for (Marker m : markers) {
            if (m.getTag() == cafe) {
                selectMarker(screen, m);
                break;
            }
        }
    }

    private void pickPlace(View screen, android.widget.EditText searchInput,
                           GoogleMap map, Cafe[] cafes, String typed, LatLng pos) {
        setSearchText(searchInput, typed);
        searchKeyword = "";
        hideSuggestions(screen);
        hideKeyboard(searchInput);

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f),
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (getView() != screen) return;
                        drawMarkers(map, screen, cafes, null);
                        updateAreaName(screen, pos);
                    }

                    @Override
                    public void onCancel() {}
                });
    }

    private void setSearchText(android.widget.EditText searchInput, String text) {
        suppressWatcher = true;
        searchInput.setText(text);
        searchInput.setSelection(text.length());
        suppressWatcher = false;
    }

    private void hideKeyboard(View anyView) {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager)
                        requireContext().getSystemService(
                                android.content.Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(anyView.getWindowToken(), 0);
    }

    private boolean matches(Cafe cafe, String keyword) {
        if (keyword == null || keyword.isEmpty()) return true;
        String k = keyword.toLowerCase(java.util.Locale.KOREAN);
        return cafe.name.toLowerCase(java.util.Locale.KOREAN).contains(k)
                || cafe.menu.toLowerCase(java.util.Locale.KOREAN).contains(k)
                || cafe.category.toLowerCase(java.util.Locale.KOREAN).contains(k);
    }

    // 선택한 디저트가 이 카페의 대표 디저트이거나 저장된 스티커에 있으면 표시
    private boolean matchesDessert(Cafe cafe) {
        if (selectedDesserts.isEmpty()) return true;
        if (selectedDesserts.contains(cafe.category)) return true;
        for (Sticker s : cafe.stickers) {
            if (selectedDesserts.contains(s.category)) return true;
        }
        return false;
    }

    // 이 카페에 저장된 스티커 중 하나를 랜덤으로 고름
    // - 앱을 켤 때마다 다른 스티커 (SESSION_SEED)
    // - 같은 실행 중에는 같은 필터 상태에서 항상 같은 스티커 (깜빡임 방지)
    // - 디저트를 선택했으면 그 디저트 스티커만 후보
    private int pickStickerRes(Cafe cafe) {
        java.util.List<Sticker> pool = new java.util.ArrayList<>();
        for (Sticker s : cafe.stickers) {
            if (selectedDesserts.isEmpty() || selectedDesserts.contains(s.category)) {
                pool.add(s);
            }
        }
        if (pool.isEmpty()) return 0;

        long seed = SESSION_SEED
                ^ (cafe.name.hashCode() * 31L)
                ^ selectedDesserts.hashCode();
        return pool.get(new java.util.Random(seed).nextInt(pool.size())).res;
    }

    // ===== 동네 이동 =====

    @SuppressWarnings("deprecation")
    private void moveToArea(GoogleMap map, View screen, Cafe[] cafes, String query) {
        android.location.Geocoder geocoder = new android.location.Geocoder(
                requireContext().getApplicationContext(), java.util.Locale.KOREAN);

        new Thread(() -> {
            LatLng target = null;
            try {
                java.util.List<android.location.Address> results =
                        geocoder.getFromLocationName(
                                resolveAlias(query), 1, 32.9, 124.5, 38.7, 132.0);
                if (results != null && !results.isEmpty()) {
                    target = new LatLng(results.get(0).getLatitude(),
                            results.get(0).getLongitude());
                }
            } catch (java.io.IOException | IllegalArgumentException ignored) {}

            final LatLng finalTarget = target;
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                if (getView() != screen || finalTarget == null) return;

                searchKeyword = "";

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(finalTarget, 15f),
                        new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                if (getView() != screen) return;
                                drawMarkers(map, screen, cafes, null);
                                updateAreaName(screen, finalTarget);
                            }

                            @Override
                            public void onCancel() {}
                        });
            });
        }).start();
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
                    if (name == null) name = address.getAdminArea();
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

    // ===== 마커 =====

    private void drawMarkers(GoogleMap map, View screen, Cafe[] cafes,
                             @Nullable LatLngBounds bounds) {

        map.clear();
        markers.clear();
        selectedMarker = null;
        selectedCafe = null;

        LatLng mapCenter = map.getCameraPosition().target;
        float[] distance = new float[1];
        int resultCount = 0;

        for (Cafe cafe : cafes) {
            if (bounds != null && !bounds.contains(cafe.position)) continue;

            // 디저트 종류 (비어 있으면 전체)
            if (!matchesDessert(cafe)) continue;

            // 거리: 지도 중심 기준
            if (distanceLimit > 0) {
                Location.distanceBetween(
                        mapCenter.latitude, mapCenter.longitude,
                        cafe.position.latitude, cafe.position.longitude, distance);
                if (distance[0] > distanceLimit) continue;
            }

            if (!matches(cafe, searchKeyword)) continue;

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(cafe.position)
                    .icon(createMarkerIcon(cafe, false))
                    .anchor(0.5f, 1f));

            if (marker == null) continue;

            marker.setTag(cafe);
            markers.add(marker);
            resultCount++;
        }

        TextView resultText = screen.findViewById(R.id.explore_result_count);
        resultText.setText("검색 결과 " + resultCount + "개");

        showCafe(screen, null);
    }

    private void selectMarker(View screen, Marker next) {
        Cafe cafe = (Cafe) next.getTag();

        if (selectedCafe == cafe) {
            next.setIcon(createMarkerIcon(cafe, false));
            next.setZIndex(0f);

            selectedMarker = null;
            selectedCafe = null;

            showCafe(screen, null);
            return;
        }

        if (selectedMarker != null) {
            Cafe oldCafe = (Cafe) selectedMarker.getTag();
            selectedMarker.setIcon(createMarkerIcon(oldCafe, false));
            selectedMarker.setZIndex(0f);
        }

        next.setIcon(createMarkerIcon(cafe, true));
        next.setZIndex(1f);

        selectedMarker = next;
        selectedCafe = cafe;

        showCafe(screen, cafe);
    }

    private void showCafe(View screen, @Nullable Cafe cafe) {
        View card = screen.findViewById(R.id.explore_cafe_card);

        if (cafe == null) {
            card.setVisibility(View.GONE);
            return;
        }

        card.setVisibility(View.VISIBLE);
        TextView name = screen.findViewById(R.id.cafe_name);
        TextView menu = screen.findViewById(R.id.cafe_menu);
        ImageView photo = screen.findViewById(R.id.cafe_photo);

        name.setText(cafe.name);
        menu.setText(cafe.menu);

        photo.setImageResource(cafe.cardPhoto);
        photo.setVisibility(cafe.cardPhoto == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    private BitmapDescriptor createMarkerIcon(Cafe cafe, boolean selected) {
        View pin = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_explore_marker, null);

        View frame = pin.findViewById(R.id.marker_photo_frame);
        TextView label = pin.findViewById(R.id.marker_label);

        // 저장된 스티커 중 랜덤으로 고른 하나
        int stickerRes = pickStickerRes(cafe);
        Bitmap sticker = null;
        if (stickerRes != 0) {
            android.graphics.BitmapFactory.Options options =
                    new android.graphics.BitmapFactory.Options();
            options.inScaled = false;
            sticker = android.graphics.BitmapFactory.decodeResource(
                    getResources(), stickerRes, options);
        }

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
        closeFilter();
        currentMap = null;
        currentCafes = null;
        selectedMarker = null;
        selectedCafe = null;
        markers.clear();
        super.onDestroyView();
    }

    // 사용자가 테이스팅 노트로 저장한 스티커 (디저트 종류 + 이미지)
    private static class Sticker {
        final String category;
        final int res;

        Sticker(String category, int res) {
            this.category = category;
            this.res = res;
        }
    }

    private static class Cafe {
        final LatLng position;
        final String name;
        final String menu;
        final String category;
        final int cardPhoto;
        final java.util.List<Sticker> stickers;

        Cafe(LatLng position, String name, String menu,
             String category, int cardPhoto, Sticker... stickers) {
            this.position = position;
            this.name = name;
            this.menu = menu;
            this.category = category;
            this.cardPhoto = cardPhoto;
            this.stickers = java.util.Arrays.asList(stickers);
        }
    }
}