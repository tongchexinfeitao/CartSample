package com.ali.cartdemo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mumu on 2018/6/25.
 */

public class MyAdapter extends BaseExpandableListAdapter {


    private List<CartInfo.DataBean> sellerData;

    public MyAdapter(List<CartInfo.DataBean> sellerData) {

        this.sellerData = sellerData;
    }

    @Override
    public int getGroupCount() {
        return sellerData == null ? 0 : sellerData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return sellerData.get(groupPosition).getList() == null ? 0 : sellerData.get(groupPosition).getList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        CartInfo.DataBean dataBean = sellerData.get(groupPosition);
        ParentViewHolder parentViewHolder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_cart_parent, null);
            parentViewHolder = new ParentViewHolder(convertView);
            convertView.setTag(parentViewHolder);
        } else {
            parentViewHolder = (ParentViewHolder) convertView.getTag();
        }

        //商家名字
        parentViewHolder.sellerNameTv.setText(dataBean.getSellerName());
        //根据商品确定商家得checkbox是否被选中
        boolean currentSellerAllProductSelected = isCurrentSellerAllProductSelected(groupPosition);
        parentViewHolder.sellerCb.setChecked(currentSellerAllProductSelected);
        parentViewHolder.sellerCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击商家得checkBox
                if (onCartListChangeListener != null) {
                    onCartListChangeListener.onSellerCheckedChange(groupPosition);
                }
            }
        });

        return convertView;
    }

    //当前商家所有商品是否被选中
    public boolean isCurrentSellerAllProductSelected(int groupPosition) {
        CartInfo.DataBean dataBean = sellerData.get(groupPosition);
        List<CartInfo.DataBean.ListBean> list = dataBean.getList();
        for (CartInfo.DataBean.ListBean listBean : list) {
            //只要有一个未选中，商家就直接未选中
            if (listBean.getSelected() == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        CartInfo.DataBean dataBean = sellerData.get(groupPosition);
        List<CartInfo.DataBean.ListBean> list = dataBean.getList();
        //商品
        CartInfo.DataBean.ListBean listBean = list.get(childPosition);


        ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_cart_child, null);
            childViewHolder = new ChildViewHolder(convertView);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        //商品名字
        childViewHolder.productTitleNameTv.setText(listBean.getTitle());
        //商品单价
        childViewHolder.productPriceTv.setText(listBean.getPrice()+"");
        //商品得checkBox状态
        childViewHolder.childCb.setChecked(listBean.getSelected() == 1);
        childViewHolder.childCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击商品得checkBox
                if (onCartListChangeListener != null) {
                    onCartListChangeListener.onProductCheckedChange(groupPosition, childPosition);
                }
            }
        });
        childViewHolder.addRemoveView.setNumber(listBean.getNum());
        childViewHolder.addRemoveView.setOnNumberChangeListener(new MyAddSubView.OnNumberChangeListener() {
            @Override
            public void onNumberChange(int num) {
                //拿到商品最新得数量
                if (onCartListChangeListener != null) {
                    onCartListChangeListener.onProductNumberChange(groupPosition, childPosition, num);
                }
            }
        });


        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    //所有商品是否被选中
    public boolean isAllProductsSelected() {
        for (int i = 0; i < sellerData.size(); i++) {
            CartInfo.DataBean dataBean = sellerData.get(i);
            List<CartInfo.DataBean.ListBean> list = dataBean.getList();
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).getSelected() == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    //计算总价
    public float calculateTotalPrice() {
        float totalPrice = 0;
        for (int i = 0; i < sellerData.size(); i++) {
            CartInfo.DataBean dataBean = sellerData.get(i);
            List<CartInfo.DataBean.ListBean> list = dataBean.getList();
            for (int j = 0; j < list.size(); j++) {
                //只要是选中状态
                if (list.get(j).getSelected() == 1) {
                    float price = list.get(j).getPrice();
                    int num = list.get(j).getNum();
                    totalPrice += price * num;
                }
            }
        }
        return totalPrice;
    }

    public int calculateTotalNumber() {
        int totalNumber = 0;
        for (int i = 0; i < sellerData.size(); i++) {
            CartInfo.DataBean dataBean = sellerData.get(i);
            List<CartInfo.DataBean.ListBean> list = dataBean.getList();
            for (int j = 0; j < list.size(); j++) {
                //只要是选中状态
                if (list.get(j).getSelected() == 1) {
                    int num = list.get(j).getNum();
                    totalNumber += num;
                }
            }
        }
        return totalNumber;
    }


    //当商家得checkbox被点击得时候调用，设置当前商家得所有商品得状态
    public void changeCurrentSellerAllProductsStatus(int groupPosition, boolean isSelected) {
        CartInfo.DataBean dataBean = sellerData.get(groupPosition);
        List<CartInfo.DataBean.ListBean> listBeans = dataBean.getList();
        for (int i = 0; i < listBeans.size(); i++) {
            CartInfo.DataBean.ListBean listBean = listBeans.get(i);
            listBean.setSelected(isSelected ? 1 : 0);
        }
    }

    //当商品得checkbox被点击得时候调用，改变当前商品状态
    public void changeCurrentProductStatus(int groupPosition, int childPosition) {
        CartInfo.DataBean dataBean = sellerData.get(groupPosition);
        List<CartInfo.DataBean.ListBean> listBeans = dataBean.getList();
        CartInfo.DataBean.ListBean listBean = listBeans.get(childPosition);
        listBean.setSelected(listBean.getSelected() == 0 ? 1 : 0);
    }

    //当加减器被点击得时候调用，改变当前商品得数量
    public void changeCurrentProductNumber(int groupPosition, int childPosition, int number) {
        CartInfo.DataBean dataBean = sellerData.get(groupPosition);
        List<CartInfo.DataBean.ListBean> listBeans = dataBean.getList();
        CartInfo.DataBean.ListBean listBean = listBeans.get(childPosition);
        listBean.setNum(number);
    }

    //设置所有商品得状态
    public void changeAllProductsStatus(boolean selected) {
        for (int i = 0; i < sellerData.size(); i++) {
            CartInfo.DataBean dataBean = sellerData.get(i);
            List<CartInfo.DataBean.ListBean> list = dataBean.getList();
            for (int j = 0; j < list.size(); j++) {
                list.get(j).setSelected(selected?1:0);
            }
        }
    }


    class ParentViewHolder {
        @BindView(R.id.seller_cb)
        CheckBox sellerCb;
        @BindView(R.id.seller_name_tv)
        TextView sellerNameTv;

        ParentViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    class ChildViewHolder {
        @BindView(R.id.child_cb)
        CheckBox childCb;
        @BindView(R.id.product_icon_iv)
        ImageView productIconIv;
        @BindView(R.id.product_title_name_tv)
        TextView productTitleNameTv;
        @BindView(R.id.product_price_tv)
        TextView productPriceTv;
        @BindView(R.id.add_remove_view)
        MyAddSubView addRemoveView;

        ChildViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


    onCartListChangeListener onCartListChangeListener;

    public void setOnCartListChangeListener(MyAdapter.onCartListChangeListener onCartListChangeListener) {
        this.onCartListChangeListener = onCartListChangeListener;
    }

    public interface onCartListChangeListener {
        void onSellerCheckedChange(int groupPosition);

        void onProductCheckedChange(int groupPosition, int childPosition);

        void onProductNumberChange(int groupPosition, int childPosition, int number);
    }
}
