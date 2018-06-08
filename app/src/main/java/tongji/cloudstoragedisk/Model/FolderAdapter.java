package tongji.cloudstoragedisk.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import tongji.cloudstoragedisk.R;

public class FolderAdapter extends SimpleAdapter{

    private Context context;
    private Map<String, Object> map;
    private int resource;
    private String[] from;
    private int[] to;
    private ViewHolder viewHolder;
    private List<Directory> directories;
    private int currentItem = -1; //用于记录点击的 Item 的 position，是控制 item 展开的核心

    public interface OprationListener {
        public void download(Directory directory);
        public void upload(Directory directory);
        public void delete(Directory directory);
    }
    private OprationListener listener;

    public void setListener(OprationListener oprationListener){
        this.listener= oprationListener;
    }

    public FolderAdapter(Context context,List<Directory> directories, List<Map<String,Object>> data,int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.resource = resource;
        this.from = from;
        this.to = to;
        this.context = context;
        this.directories=directories;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //获得单个item的map
        map = (Map<String, Object>)getItem(position);
        View view = convertView;
        //获取到每个控件对应的布局中的控件
        if(view ==null ){
            view = LayoutInflater.from(context).inflate(resource,null);
            viewHolder = new ViewHolder();
            viewHolder.item_image = (ImageView)view.findViewById(R.id.item_image);
            viewHolder.item_tv_main = (TextView)view.findViewById(R.id.item_tv_main);
            viewHolder.item_tv_time = (TextView)view.findViewById(R.id.item_tv_time);
            viewHolder.item_open = (ImageView)view.findViewById(R.id.item_open);
            viewHolder.item_hide = (GridLayout)view.findViewById(R.id.item_hide);
            viewHolder.item_download=(LinearLayout) view.findViewById(R.id.item_download);
            viewHolder.item_share=(LinearLayout) view.findViewById(R.id.item_share);
            viewHolder.item_delete=(LinearLayout) view.findViewById(R.id.item_delete);
            //将隐藏的部分隐藏起来
            viewHolder.item_hide.setVisibility(View.GONE);
            view.setTag(viewHolder);
        }else
            viewHolder = (ViewHolder) view.getTag();
        //根据设置的数据设置数据
        viewHolder.item_image.setImageResource((int)map.get(from[0]));
        viewHolder.item_tv_main.setText((String)map.get(from[1]));
        viewHolder.item_tv_time.setText((String)map.get(from[2]));
        //设置点击展开菜单控件为未选择状态，选择状态图标会改变，编写背景xml文件即可实现
        viewHolder.item_open.setSelected(false);

        //根据 currentItem 记录的点击位置来设置"对应Item"的可见性（在list依次加载列表数据时，每加载一个时都看一下是不是需改变可见性的那一条）
        if (currentItem == position) {
            viewHolder.item_open.setSelected(true);
            viewHolder.item_hide.setVisibility(View.VISIBLE);
        } else {
            viewHolder.item_open.setSelected(false);
            viewHolder.item_hide.setVisibility(View.GONE);
        }
        viewHolder.item_open.setTag(position);
        viewHolder.item_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //用 currentItem 记录点击位置
                int tag = (Integer) view.getTag();
                if (tag == currentItem) { //再次点击
                    currentItem = -1; //给 currentItem 一个无效值
                } else {
                    currentItem = tag;
                }
                //通知adapter数据改变需要重新加载
                notifyDataSetChanged(); //必须有的一步
            }
        });

        viewHolder.item_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Directory directory=directories.get(position);
                listener.download(directory);
            }
        });
        viewHolder.item_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Directory directory=directories.get(position);
                listener.upload(directory);
            }
        });
        viewHolder.item_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Directory directory=directories.get(position);
                listener.delete(directory);
            }
        });
        return view;
    }

    static class ViewHolder{
        ImageView item_image;
        TextView item_tv_main;
        TextView item_tv_time;
        ImageView item_open;
        GridLayout item_hide;
        LinearLayout item_download;
        LinearLayout item_share;
        LinearLayout item_delete;
    }
}