package com.example.dessertrip.ui.archive.collect;

import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.example.dessertrip.R;
import com.example.dessertrip.databinding.IncludeCategoryChipsBinding;
import com.example.dessertrip.databinding.IncludeLevelCardBinding;
import com.example.dessertrip.ui.collect.data.DessertCategories;
import com.example.dessertrip.ui.collect.data.PlateDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** 도감 화면과 플레이트 화면이 함께 쓰는 위쪽 레벨 카드와 카테고리 칩 */
public final class ArchiveHeader {
    private ArchiveHeader() { }

    /** 레벨 카드: 진행도 + 가장 많이 기록한 카테고리 3개 (부족하면 기본 카테고리로 채움) */
    public static void bindLevelCard(IncludeLevelCardBinding c, PlateDto plate) {
        int collected = plate == null ? 0 : plate.collectedCount;
        int goal = Math.max(1, plate == null ? 30 : plate.goalCount);
        c.levelTitle.setText("Lv." + (plate == null ? 1 : plate.level) + " · " + (plate == null ? "" : plate.levelTitle));
        c.levelSub.setText("다음 레벨까지 " + Math.max(0, goal - collected) + "개 디저트 기록");
        c.collectedText.setText(String.valueOf(collected));
        c.goalText.setText(" / " + goal);
        c.progress.setMax(goal);
        c.progress.setProgressCompat(Math.min(collected, goal), false);

        String[] top = topCategoryLabels(plate);
        c.countCake.setText(top[0]);
        c.countBread.setText(top[1]);
        c.countBaked.setText(top[2]);
    }

    /** 가장 많이 기록한 카테고리 3개를 "이름 개수" 문구로 만든다(많은 순서). 부족하면 기본 카테고리로 채운다. */
    public static String[] topCategoryLabels(PlateDto plate) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        if (plate != null && plate.categoryCounts != null) entries.addAll(plate.categoryCounts.entrySet());
        Collections.sort(entries, (x, y) -> Integer.compare(y.getValue(), x.getValue()));
        List<String> names = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (Map.Entry<String, Integer> e : entries) {
            if (names.size() == 3) break;
            names.add(e.getKey());
            counts.add(e.getValue());
        }
        for (String d : DessertCategories.DEFAULTS) {
            if (names.size() >= 3) break;
            if (!names.contains(d)) { names.add(d); counts.add(0); }
        }
        String[] labels = new String[3];
        for (int i = 0; i < 3; i++) labels[i] = names.get(i) + " " + counts.get(i);
        return labels;
    }

    /** 칩 개수: 기본 카테고리를 먼저(개수 0 포함), 그 뒤에 목록에서 나온 카테고리를 등장 순서대로 센다 */
    public static Map<String, Integer> countByCategory(List<String> categories) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String d : DessertCategories.DEFAULTS) counts.put(d, 0);
        for (String c : categories) {
            if (c == null || c.isEmpty()) continue;
            Integer n = counts.get(c);
            counts.put(c, n == null ? 1 : n + 1);
        }
        return counts;
    }

    /** 카테고리 필터 칩. 카테고리는 사용자가 추가할 수 있으므로 칩을 데이터에서 만든다. selected == null 이면 "전체". */
    public static void bindChips(IncludeCategoryChipsBinding c, LayoutInflater inflater, String selected,
                                 Map<String, Integer> counts, int total, final Consumer<String> onSelect) {
        final ChipGroup group = c.chipGroup;
        group.setOnCheckedStateChangeListener(null);
        for (int i = group.getChildCount() - 1; i >= 0; i--) {   // "전체" 칩만 남기고 다시 만든다
            if (group.getChildAt(i).getId() != c.chipAll.getId()) group.removeViewAt(i);
        }
        c.chipAll.setText("전체 " + total);

        int checkedId = c.chipAll.getId();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_tag_chip, group, false);
            chip.setId(View.generateViewId());
            chip.setCheckable(true);
            chip.setTag(e.getKey());
            chip.setText(e.getKey() + " " + e.getValue());
            group.addView(chip);
            if (e.getKey().equals(selected)) checkedId = chip.getId();
        }
        group.check(checkedId);
        group.setOnCheckedStateChangeListener((g, ids) -> {
            if (ids.isEmpty()) return;
            Object name = g.findViewById(ids.get(0)).getTag();
            onSelect.accept(name instanceof String ? (String) name : null);
        });
    }
}
