package com.example.onto_1921;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.util.List;

public class GeocodingTask extends AsyncTask<String, Void, LatLng> {
    private GeocodingResultListener listener;

    public GeocodingTask(GeocodingResultListener listener) {
        this.listener = listener;
    }

    @Override
    protected LatLng doInBackground(String... params) {
        String locationName = params[0];

        Geocoder geocoder = new Geocoder(listener.getContext());

        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            Log.e("GeocodingTask", "Error finding location", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(LatLng result) {
        listener.onGeocodingResult(result);
    }

    public interface GeocodingResultListener {
        void onGeocodingResult(LatLng result);
        Context getContext();
    }
}
