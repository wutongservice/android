package com.borqs.qiupu.ui.bpc;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

import com.borqs.common.adapter.LocationAdapter;
import com.borqs.common.view.PickLocationItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.LocationUtils;


public class PickLocationActivity extends BasicActivity {

//    private static final String TAG = "PickLocationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bpc_pick_location);
        setHeadTitle(R.string.history_location);

        Intent intent = getIntent();
//        ArrayList<String> locations = intent.getStringArrayListExtra(QiupuComposeActivity.HISTORY_LOCATIONS);
        ArrayList<String> locationGeos = intent.getStringArrayListExtra(LocationUtils.HISTORY_GEOS);
        final int size = null == locationGeos ? 0 : locationGeos.size();
        ArrayList<LocationUtils.Geometry> geometries = new ArrayList<LocationUtils.Geometry>(size);
        for (String geo : locationGeos) {
            geometries.add(LocationUtils.Geometry.parseGeoString(geo));
        }

        LocationAdapter locationAdapter = new LocationAdapter(this, geometries, true);
        ListView list = (ListView) findViewById(R.id.location_list);
        list.addHeaderView(initListHeadView());
        list.setAdapter(locationAdapter);
        list.setOnItemClickListener(mOnClickItemListener);
        if (geometries.isEmpty()) {
            TextView emptyText = (TextView) findViewById(R.id.empty_text);
            emptyText.setVisibility(View.VISIBLE);
        }
        showRightActionBtn(false);
    }

    private View initListHeadView() {
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.location_header_view, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 
                (int) getResources().getDimension(R.dimen.list_item_height)));
        TextView title = (TextView) view.findViewById(R.id.location_content);
        title.setText(getString(R.string.hide_location));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                LocationUtils.removeLocation();
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        return view;
    }

    private OnItemClickListener mOnClickItemListener = new OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (PickLocationItemView.class.isInstance(view)) {
                PickLocationItemView pv = (PickLocationItemView) view;
                String location = pv.getLocationURL();
                String locaitonGeo = pv.getLocationGeo();

                Intent intent = new Intent();
                if (location != null) {
                    LocationUtils.encodeCurrentExtra(intent, location, locaitonGeo);
                }
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
        
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void createHandler() {
        
    }

    @Override
    protected void loadSearch() {
        
    }

}
