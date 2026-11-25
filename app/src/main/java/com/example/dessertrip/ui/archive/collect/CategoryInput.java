package com.example.dessertrip.ui.archive.collect;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.example.dessertrip.ui.archive.sticker.RegisterFlowViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 카테고리 입력창(AutoCompleteTextView).
 * 입력하면 서버에서 검색한 카테고리가 자동완성 목록으로 나오고, 없는 이름이면 맨 아래에
 * "'검색어' 카테고리 추가하기" 항목이 나온다. 스티커 완성 화면과 테이스팅 노트 화면에서 함께 쓴다.
 */
public final class CategoryInput {
    private CategoryInput() { }

    public static void bind(Fragment fragment, final AutoCompleteTextView field, final RegisterFlowViewModel vm) {
        final Adapter adapter = new Adapter(field.getContext());
        field.setAdapter(adapter);
        field.setOnClickListener(v -> field.showDropDown());
        field.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int c, int d) { }
            @Override public void onTextChanged(CharSequence s, int a, int c, int d) { }
            @Override public void afterTextChanged(Editable e) { if (field.hasFocus()) vm.setCategoryQuery(e.toString()); }
        });
        field.setOnItemClickListener((parent, view, position, id) -> {
            if (adapter.addName != null && position == adapter.getCount() - 1) vm.addCategory(adapter.addName);
            else vm.pickCategory(adapter.getItem(position));
        });

        Observer<Object> refresh = o -> {
            List<String> names = vm.categoryCandidates.getValue();
            adapter.setItems(names == null ? new ArrayList<String>() : names, vm.canAddCategory() ? vm.getCategoryQuery() : null);
            if (field.hasFocus() && adapter.getCount() > 0) field.showDropDown();
        };
        vm.categoryCandidates.observe(fragment.getViewLifecycleOwner(), refresh);
        vm.categoryAdding.observe(fragment.getViewLifecycleOwner(), refresh);
        vm.refreshCategories();   // 화면을 열 때 서버의 최신 목록을 불러온다
    }

    /** 선택된 카테고리를 입력창에 표시한다. (입력 중인 글자는 지우지 않는다) */
    public static void show(AutoCompleteTextView field, String category) {
        String current = field.getText().toString();
        if (category != null ? !category.equals(current) : !field.hasFocus()) field.setText(category, false);
    }

    /** 서버가 이미 걸러 준 목록을 그대로 보여 주는 어댑터 (기본 필터는 앞글자만 비교해서 쓰지 않는다) */
    private static class Adapter extends ArrayAdapter<String> {
        String addName;          // null 이 아니면 마지막 항목이 "추가하기" 항목
        private String addLabel;

        Adapter(Context context) { super(context, android.R.layout.simple_dropdown_item_1line); }

        void setItems(List<String> names, String addName) {
            this.addName = addName;
            this.addLabel = addName == null ? null : "'" + addName + "' 카테고리 추가하기";
            setNotifyOnChange(false);
            clear();
            addAll(names);
            if (addLabel != null) add(addLabel);
            notifyDataSetChanged();
        }

        @Override public Filter getFilter() {
            return new Filter() {
                @Override protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults r = new FilterResults();
                    r.count = getCount();
                    return r;
                }
                @Override protected void publishResults(CharSequence constraint, FilterResults r) {
                    if (r.count > 0) notifyDataSetChanged(); else notifyDataSetInvalidated();
                }
                @Override public CharSequence convertResultToString(Object value) {
                    String s = String.valueOf(value);
                    return s.equals(addLabel) ? addName : s;   // 추가 항목을 고르면 입력창에는 이름만 남긴다
                }
            };
        }
    }
}
