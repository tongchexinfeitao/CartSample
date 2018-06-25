package com.ali.cartdemo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by mumu on 2018/6/25.
 */

public class MyAddSubView extends LinearLayout {
    @BindView(R.id.sub_tv)
    TextView subTv;
    @BindView(R.id.product_number_tv)
    TextView productNumberTv;
    @BindView(R.id.add_tv)
    TextView addTv;
    private int number = 1;

    public MyAddSubView(Context context) {
        this(context, null);
    }

    public MyAddSubView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = inflate(context, R.layout.add_remove_view_layout, this);
        //必须初始化
        ButterKnife.bind(view);
    }

    @OnClick({R.id.sub_tv, R.id.add_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sub_tv:
                if (number > 1) {
                    --number;
                    productNumberTv.setText(number + "");
                    if (onNumberChangeListener != null) {
                        onNumberChangeListener.onNumberChange(number);
                    }
                } else {
                    Toast.makeText(getContext(), "不能再少了", Toast.LENGTH_SHORT).show();
                }
                break;


            case R.id.add_tv:
                ++number;
                productNumberTv.setText(number + "");
                if (onNumberChangeListener != null) {
                    onNumberChangeListener.onNumberChange(number);
                }
                break;
        }
    }


    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
        productNumberTv.setText(number + "");
    }

    OnNumberChangeListener onNumberChangeListener;

    public void setOnNumberChangeListener(OnNumberChangeListener onNumberChangeListener) {
        this.onNumberChangeListener = onNumberChangeListener;
    }

    interface OnNumberChangeListener {
        void onNumberChange(int num);
    }

}
