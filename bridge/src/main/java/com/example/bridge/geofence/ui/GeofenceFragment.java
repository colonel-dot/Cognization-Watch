package com.example.bridge.geofence.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapWrapper;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.example.bridge.R;
import com.example.bridge.geofence.map.bridge.MAWebViewWrapper;
import com.example.bridge.geofence.map.view.MapWebView;
import com.example.common.persistense.geofence.GeofenceRepository;
import com.example.common.persistense.geofence.GeofenceItem;

import java.util.List;
import java.util.ArrayList;

import com.example.bridge.util.ItemSpacingDecoration;
import com.example.bridge.util.StringMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GeofenceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeofenceFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GeofenceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GeofenceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GeofenceFragment newInstance(String param1, String param2) {
        GeofenceFragment fragment = new GeofenceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_geofence, container, false);
    }

    private TextView location; // inside
    private RecyclerView record;
    private GeofenceRVAdapter adapter;

    private MapWebView map;
    private AMapWrapper aMapWrapper;
    private AMap aMap;

    private Marker itemMarker;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        android.util.Log.d("GeofenceFragment", "onViewCreated");

        try {
            GeofenceRepository.initialize(requireContext());

            bindView(view);

            initMAWebViewWrapper();

            initRecyclerView();

            /* TODO:
                在本地存围栏信息，绘制在地图中
                点击 RVItem 获取条目的坐标，绘制在地图中
             */
        } catch (Exception e) {
            android.util.Log.e("GeofenceFragment", "Error in onViewCreated", e);
            e.printStackTrace();
            throw e; // rethrow
        }
    }

    private void bindView(View view) {
        location = view.findViewById(R.id.location);
        map = view.findViewById(R.id.map);
        record = view.findViewById(R.id.record);
    }

    private void initMAWebViewWrapper() {
        MAWebViewWrapper webViewWrapper = new MAWebViewWrapper(map);
        aMapWrapper = new AMapWrapper(requireContext(), webViewWrapper);
        aMapWrapper.onCreate();
        aMapWrapper.getMapAsyn(map -> {
            aMap = map;
        });
    }

    private void initRecyclerView() {
        record.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GeofenceRVAdapter(new ArrayList<>());
        record.setAdapter(adapter);

        ItemSpacingDecoration itemSpacingDecoration = new ItemSpacingDecoration(requireContext(), 6, 20, 6, false);
        record.addItemDecoration(itemSpacingDecoration);

        new Thread(() -> {
            insertSampleDataIfEmpty(); // fake data

            List<GeofenceItem> items = GeofenceRepository.INSTANCE.getAllEventsBlocking();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.list = items;

                    adapter.setOnItemClickListener(position -> {
                        GeofenceItem item = items.get(position);
                        LatLng latLng = new LatLng(item.getLat(), item.getLng());
                        if (itemMarker != null) itemMarker.remove();
                        itemMarker = aMap.addMarker(
                                new MarkerOptions()
                                        .position(latLng)
                                        .title(StringMap.mapMinuteToRelativeTime(item.getTimestamp()))
                                        .snippet("Marker 内容") // TODO
                                        .draggable(false)
                                        .visible(true));
                        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
                    });

                    adapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    private void insertSampleDataIfEmpty() {
        List<GeofenceItem> existing = GeofenceRepository.INSTANCE.getAllEventsBlocking();
        if (existing.isEmpty()) {
            int minutesSince1970 = (int)(System.currentTimeMillis() / 1000 / 60);

            List<GeofenceItem> sampleItems = new ArrayList<>();
            sampleItems.add(new GeofenceItem(0, minutesSince1970 - 60, 34.261111, 108.942222, GeofenceItem.STATUS_IN));
            sampleItems.add(new GeofenceItem(0, minutesSince1970 - 30, 34.262050, 108.943100, GeofenceItem.STATUS_OUT));
            sampleItems.add(new GeofenceItem(0, minutesSince1970 - 15, 34.261600, 108.941700, GeofenceItem.STATUS_STAYED));
            sampleItems.add(new GeofenceItem(0, minutesSince1970 - 5, 34.260800, 108.942800, GeofenceItem.STATUS_IN));

            for (GeofenceItem item : sampleItems) {
                GeofenceRepository.insertEventBlocking(item);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        aMapWrapper.onDestroy();
    }
}