package al.jamil.suvo.alphabetindex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import al.jamil.suvo.alphabetindex.databinding.AlphabetIndexListBinding;
import al.jamil.suvo.alphabetindex.databinding.AlphabetIndexSingleItemBinding;

public class AlphabetIndexView extends RelativeLayout {
    AlphabetIndexListBinding binding;
    RecyclerView target;
    HashMap<String, Integer> alphabetIndexMap;
    AlphabetAdapter adapter;
    List<String> alphabets;
    String highLightedStr = "";

    public AlphabetIndexView(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.alphabet_index_list, this, true);
        TypedArray properties = context.obtainStyledAttributes(attrs, R.styleable.AlphabetIndexView);
        int textColor = properties.getColor(R.styleable.AlphabetIndexView_box_text_color, Color.argb(255, 0, 0, 0));
        binding.tvAlphabetIndicator.setTextColor(textColor);
        boolean useCustomImage = properties.getBoolean(R.styleable.AlphabetIndexView_use_custom_image, false);
        if (useCustomImage) {
            binding.alphabetIndicator.setBackground(properties.getDrawable(R.styleable.AlphabetIndexView_use_custom_image));
        }


        adapter = new AlphabetAdapter();
        binding.alphabetList.setAdapter(adapter);
        binding.alphabetIndicator.setVisibility(GONE);
        this.alphabetIndexMap = new HashMap<>();
        this.alphabets = new ArrayList<>();
        properties.recycle();

    }

    public void updateIndex(HashMap<String, Integer> alphabetIndexMap, List<String> alphabets) {
        String preStr = "…";
        String postStr = "#";
        this.alphabetIndexMap.clear();
        this.alphabets.clear();
        boolean isFirst = true;
        boolean isAbcFound = false;
        boolean isLast = true;
        for (String alphabet : alphabets) {
            char ch = alphabet.charAt(0);
            if (ch < 'A' || ch > 'Z') {
                if (isFirst) {
                    this.alphabets.add(preStr);
                    this.alphabetIndexMap.put(preStr, alphabetIndexMap.get(alphabet));
                    isFirst = false;
                } else if (isAbcFound && isLast) {
                    isLast = false;
                    this.alphabets.add(postStr);
                    this.alphabetIndexMap.put(postStr, alphabetIndexMap.get(alphabet));
                }
            } else {
                isAbcFound = true;
                isFirst = false;
                this.alphabets.add(alphabet);
                this.alphabetIndexMap.put(alphabet, alphabetIndexMap.get(alphabet));
            }
        }
        if (!isAbcFound) super.setVisibility(GONE);
        else super.setVisibility(VISIBLE);
        highLightedStr = "";
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setTarget(RecyclerView target) {
        this.target = target;
        binding.alphabetList.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                float cy = event.getY();
                float ty = binding.alphabetList.getHeight();
                int index = (int) (alphabets.size() * cy / ty);
                if (index >= 0 && index < alphabets.size()) {
                    binding.scrollIndicator.setTranslationY(event.getY() - 15);
                    binding.alphabetIndicator.setTranslationY(event.getY() - 15);
                    String str = alphabets.get(index);
                    target.scrollToPosition(alphabetIndexMap.get(str));
                    binding.alphabetIndicator.setVisibility(VISIBLE);
                    binding.tvAlphabetIndicator.setText(str);
                    binding.scrollIndicator.setVisibility(VISIBLE);
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                getHandler().postDelayed(() -> {
                    binding.alphabetIndicator.setVisibility(GONE);
                    binding.scrollIndicator.setVisibility(GONE);
                }, 500);
            }


            return false;
        });
    }

    class AlphabetViewHolder extends RecyclerView.ViewHolder {
        AlphabetIndexSingleItemBinding binding;

        AlphabetViewHolder(AlphabetIndexSingleItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    class AlphabetAdapter extends RecyclerView.Adapter<AlphabetViewHolder> {

        @NonNull
        @Override
        public AlphabetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            AlphabetIndexSingleItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                    R.layout.alphabet_index_single_item, parent, false);
            return new AlphabetViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull AlphabetViewHolder holder, int position) {
            String alphabet = alphabets.get(position);
            if (alphabet == null) {
                holder.binding.invalidateAll();
            } else {
                holder.binding.alphabetNormal.setText(alphabet);
                holder.binding.alphabetHighlighted.setText(alphabet);
                if (alphabet.equals(highLightedStr)) {
                    holder.binding.alphabetHighlighted.setVisibility(VISIBLE);
                    holder.binding.alphabetNormal.setVisibility(GONE);
                } else {
                    holder.binding.alphabetHighlighted.setVisibility(GONE);
                    holder.binding.alphabetNormal.setVisibility(VISIBLE);
                }
            }


        }

        @Override
        public int getItemCount() {
            return alphabets.size();
        }
    }

}
