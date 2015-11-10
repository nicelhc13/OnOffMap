package mobile.snu.onoffmap;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.places.Place;

/**
 * Created by lhc on 2015-11-10.
 */
public class PlaceBean implements Parcelable{
    private String id;
    private String name;
    private String rating;
    private String address;
    private Double latitude;
    private Double longitude;

    public PlaceBean() {

    }

    public PlaceBean(Parcel in) {
        readFromParcel(in);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // What we want to parcel data.
        // We need to serialize all the member variables
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(rating);
        dest.writeString(address);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    private void readFromParcel(Parcel in) {
        id = in.readString();
        name = in.readString();
        rating = in.readString();
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Object createFromParcel(Parcel source) {
            return new PlaceBean(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new PlaceBean[0];
        }
    };
}
