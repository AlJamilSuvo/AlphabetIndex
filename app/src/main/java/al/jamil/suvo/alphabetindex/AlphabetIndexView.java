package al.jamil.suvo.alphabetindex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import al.jamil.suvo.alphabetindex.databinding.AlphabetIndexListBinding;

public class AlphabetIndexView extends RelativeLayout {
    AlphabetIndexListBinding binding;
    RecyclerView target;
    HashMap<String, Integer> alphabetIndexMap;
    List<String> alphabets;
    String highLightedStr = "";
    int totalIndexSize = 0;
    float scrollIndicatorY = 0;

    public AlphabetIndexView(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.alphabet_index_list, this, true);
        TypedArray properties = context.obtainStyledAttributes(attrs, R.styleable.AlphabetIndexView);
        int bubbleColor = properties.getColor(R.styleable.AlphabetIndexView_bubble_color, Color.argb(255, 0, 0, 255));
        int bubbleTextColor = properties.getColor(R.styleable.AlphabetIndexView_bubble_text_color, Color.argb(255, 255, 255, 255));
        int scrollIndicatorColor = properties.getColor(R.styleable.AlphabetIndexView_scroll_indicator_color, Color.argb(255, 0, 0, 255));
        int listAlphabetColor = properties.getColor(R.styleable.AlphabetIndexView_list_alphabet_text_color, Color.argb(255, 0, 0, 0));
        boolean useCustomImage = properties.getBoolean(R.styleable.AlphabetIndexView_use_custom_bubble_image, false);
        if (useCustomImage) {
            binding.alphabetIndicator.setBackground(properties.getDrawable(R.styleable.AlphabetIndexView_custom_bubble_image));
        } else {
            Drawable bubbleUnWrapped = context.getResources().getDrawable(R.drawable.ic_alphabet_indicator_bg);
            Drawable bubbleWrappedDrawable = DrawableCompat.wrap(bubbleUnWrapped);
            DrawableCompat.setTint(bubbleWrappedDrawable, bubbleColor);
            binding.alphabetIndicator.setBackground(bubbleWrappedDrawable);
        }
        binding.scrollIndicator.setColorFilter(scrollIndicatorColor);
        binding.tvAlphabetIndicator.setTextColor(bubbleTextColor);


        binding.alphabetIndicator.setVisibility(GONE);
        this.alphabetIndexMap = new HashMap<>();
        this.alphabets = new ArrayList<>();

        scrollIndicatorY = binding.scrollIndicator.getY();

        properties.recycle();

    }

    public void updateIndex(HashMap<String, Integer> alphabetIndexMap, List<String> alphabets, int totalIndexSize) {
        this.alphabetIndexMap.clear();
        this.alphabets.clear();
        boolean isFirst = true;
        boolean isAbcFound = false;
        boolean isLast = true;
        this.totalIndexSize = totalIndexSize;
        for (String alphabet : alphabets) {
            char ch = alphabet.charAt(0);
//            if (ch < 'A' || ch > 'Z') {
//                if (isFirst) {
//                    this.alphabets.add(preStr);
//                    this.alphabetIndexMap.put(preStr, alphabetIndexMap.get(alphabet));
//                    isFirst = false;
//                } else if (isAbcFound && isLast) {
//                    isLast = false;
//                    this.alphabets.add(postStr);
//                    this.alphabetIndexMap.put(postStr, alphabetIndexMap.get(alphabet));
//                }
//            } else {
            isAbcFound = true;
            isFirst = false;
            this.alphabets.add(alphabet);
            this.alphabetIndexMap.put(alphabet, alphabetIndexMap.get(alphabet));
//            }
        }
        if (!isAbcFound) super.setVisibility(GONE);
        else super.setVisibility(VISIBLE);
        highLightedStr = "";
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setTarget(RecyclerView target) {
        this.target = target;

        target.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) target.getLayoutManager();
                int topVisibleMessageIndex = -1;
                int bottomVisibleMessageIndex = -1;
                if (linearLayoutManager != null) {
                    bottomVisibleMessageIndex = linearLayoutManager.findFirstVisibleItemPosition();
                }
                if (linearLayoutManager != null) {
                    topVisibleMessageIndex = linearLayoutManager.findLastVisibleItemPosition();
                }

                int index = (topVisibleMessageIndex + bottomVisibleMessageIndex) / 2;
                if (topVisibleMessageIndex <= 0) {
                    binding.scrollIndicator.setVisibility(GONE);
                } else {
                    binding.scrollIndicator.setVisibility(VISIBLE);
                    float ddy = (float) index * binding.alphabetList.getHeight() / (float) totalIndexSize;
                    binding.scrollIndicator.setTranslationY(scrollIndicatorY+ddy);

                }

            }
        });


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
                    binding.scrollIndicator.setVisibility(VISIBLE);
                }, 500);
            }


            return false;
        });
    }


}
