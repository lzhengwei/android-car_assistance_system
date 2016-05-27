package csie.yuntech.edu.tw.gps;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by zhengwei on 2015/12/17.
 */
public class CustomSpinner extends ArrayAdapter<String> {
    String[] lunch = {"加油站", "停車場", "修車廠"};
    LayoutInflater inflater;
    public CustomSpinner(Context context, int textViewResourceId,
                           String[] objects) {
        super(context, textViewResourceId, objects);
       inflater=LayoutInflater.from(context);

// TODO Auto-generated constructor stub
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
// TODO Auto-generated method stub
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
// TODO Auto-generated method stub
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
// TODO Auto-generated method stub
//return super.getView(position, convertView, parent);


        View row=inflater.inflate(R.layout.row, parent, false);
        TextView label=(TextView)row.findViewById(R.id.itemtext);
        label.setText(lunch[position]);

        ImageView icon=(ImageView)row.findViewById(R.id.icon);

        Log.v("iconswitch"," "+position);

     /*  switch (position)
       {

           case 0:
               icon.setImageResource(R.drawable.gas1);
               break;
           case 1:
               icon.setImageResource(R.drawable.parking1);
               break;
           case 2:
               icon.setImageResource(R.drawable.fix1);
               break;
               default:
                   icon.setImageResource(R.drawable.car);
                   break;
       }*/
        if (lunch[position]=="加油站"){
            icon.setImageResource(R.drawable.gas1);
        }
        else if (lunch[position]=="停車場"){
            icon.setImageResource(R.drawable.parkingblack);
        }
        else
            icon.setImageResource(R.drawable.fix1);




        return row;
    }



}
