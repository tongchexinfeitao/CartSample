package com.ali.cartdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.el_cart)
    ExpandableListView elCart;
    @BindView(R.id.cb_cart_all_select)
    CheckBox cbCartAllSelect;
    @BindView(R.id.tv_cart_total_price)
    TextView tvCartTotalPrice;
    @BindView(R.id.btn_cart_pay)
    Button btnCartPay;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        String url = "https://www.zhaoapi.cn/product/getCarts";
        Map<String, String> map = new HashMap<>();
        map.put("uid", "71");
        OkhtttpUtils.getInstance().doPost(url, map, new OkhtttpUtils.OkCallback() {


            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onResponse(String json) {
                CartInfo cartInfo = new Gson().fromJson(json, CartInfo.class);
                if ("0".equals(cartInfo.getCode())) {
                    List<CartInfo.DataBean> sellerData = cartInfo.getData();

                    //设置adapter
                    myAdapter = new MyAdapter(sellerData);
                    myAdapter.setOnCartListChangeListener(new MyAdapter.onCartListChangeListener() {
                        @Override
                        public void onSellerCheckedChange(int groupPosition) {
                            //商家被点击
                            boolean currentSellerAllProductSelected = myAdapter.isCurrentSellerAllProductSelected(groupPosition);
                            myAdapter.changeCurrentSellerAllProductsStatus(groupPosition, !currentSellerAllProductSelected);
                            myAdapter.notifyDataSetChanged();
                            //刷新底部数据
                            refreshSelectedAndTotalPriceAndTotalNumber();

                        }

                        @Override
                        public void onProductCheckedChange(int groupPosition, int childPosition) {
                            //点击商品得checkbox
                            myAdapter.changeCurrentProductStatus(groupPosition,childPosition);
                            myAdapter.notifyDataSetChanged();
                            //刷新底部数据
                            refreshSelectedAndTotalPriceAndTotalNumber();

                        }

                        @Override
                        public void onProductNumberChange(int groupPosition, int childPosition, int number) {
                            //当加减被点击
                            myAdapter.changeCurrentProductNumber(groupPosition,childPosition,number);
                            myAdapter.notifyDataSetChanged();
                            //刷新底部数据
                            refreshSelectedAndTotalPriceAndTotalNumber();

                            //联网更新网络上得商品数量
                        }
                    });
                    elCart.setAdapter(myAdapter);

                    //展开二级列表
                    for (int i=0;i<sellerData.size();i++){
                        elCart.expandGroup(i);
                    }
                    //刷新checkbox状态和总价和总数量
                    refreshSelectedAndTotalPriceAndTotalNumber();
                }
            }

        });
    }

    //刷新checkbox状态和总价和总数量
    private void refreshSelectedAndTotalPriceAndTotalNumber() {
        //去判断是否所有得商品都被选中
        boolean allProductsSelected = myAdapter.isAllProductsSelected();
        //设置给全选checkBox
        cbCartAllSelect.setChecked(allProductsSelected);

        //计算总价
        float totalPrice = myAdapter.calculateTotalPrice();
        tvCartTotalPrice.setText("总价 " + totalPrice);

        //计算总数量
        int totalNumber = myAdapter.calculateTotalNumber();
        btnCartPay.setText("去结算(" + totalNumber + ")");

    }

    @OnClick(R.id.cb_cart_all_select)
    public void onViewClicked() {
        //底部全选按钮
        //时候所有得商品都被选中
        boolean allProductsSelected = myAdapter.isAllProductsSelected();
        myAdapter.changeAllProductsStatus(!allProductsSelected);
        myAdapter.notifyDataSetChanged();
        //刷新底部数据
        refreshSelectedAndTotalPriceAndTotalNumber();
    }
}
