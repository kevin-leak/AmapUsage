package com.example.amapusage.model

import android.os.Parcel
import android.os.Parcelable
import android.text.SpannableString
import androidx.lifecycle.LiveData
import kotlin.properties.Delegates

open class LocationModel() : LiveData<LocationModel>(), Parcelable{

    lateinit var placeTitle: CharSequence
    lateinit var placeDesc: String
    var latitude by Delegates.notNull<Double>()
    var longitude by Delegates.notNull<Double>()

    constructor(parcel: Parcel) : this() {
        placeTitle = parcel.readString()!!
        placeDesc = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(placeTitle.toString())
        parcel.writeString(placeDesc)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationModel> {
        override fun createFromParcel(parcel: Parcel): LocationModel {
            return LocationModel(parcel)
        }

        override fun newArray(size: Int): Array<LocationModel?> {
            return arrayOfNulls(size)
        }
    }

}