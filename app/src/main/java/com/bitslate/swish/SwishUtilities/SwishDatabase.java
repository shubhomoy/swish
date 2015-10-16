package com.bitslate.swish.SwishUtilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bitslate.swish.SwishObjects.Bus;
import com.bitslate.swish.SwishObjects.City;
import com.bitslate.swish.SwishObjects.Flight;
import com.bitslate.swish.SwishObjects.Hotel;
import com.bitslate.swish.SwishObjects.PlanItem;

import java.util.ArrayList;

/**
 * Created by shubhomoy on 20/9/15.
 */
public class SwishDatabase {

    SQLiteDatabase db;
    Context context;
    DbHelper dbHelper;

    public SwishDatabase(Context context) {
        this.context = context;
        dbHelper = new DbHelper(context, Database_Name, null, Database_Version);
    }

    public static final int Database_Version = 1;
    public static final String Database_Name = "swish_database";
    public static final String Main_Table_Name = "itineraries";
    public static final String Main_Table_Id = "id";
    public static final String Main_Table_Itinerary_Date = "created_at";
    public static final String Main_Table_Itinerary_Name = "name";
    public static final String Main_Table_User_Id = "user_id";

    public static final String flight_table = "flights";
    public static final String flight_table_id = "id";
    public static final String flight_table_origin = "origin";
    public static final String flight_table_destination = "destination";
    public static final String flight_table_deptime = "deptime";
    public static final String flight_table_departureDate = "deptDate";
    public static final String flight_table_arrtime = "arrtime";
    public static final String flight_table_duration = "duration";
    public static final String flight_table_flightno = "flightno";
    public static final String flight_table_seatingclass = "seatingclass";
    public static final String flight_table_stops = "stops";
    public static final String flight_table_airline = "airline";
    public static final String flight_table_fare = "fare";

    public static final String bus_table = "buses";
    public static final String bus_table_id = "id";
    public static final String bus_table_origin = "origin";
    public static final String bus_table_destination = "destination";
    public static final String bus_table_duration = "duration";
    public static final String bus_table_deptime = "departure_time";
    public static final String bus_table_type = "bus_type";
    public static final String bus_table_name = "name";
    public static final String bus_table_fare = "fare";

    public static final String city_table = "citites";
    public static final String city_name = "name";
    public static final String city_id = "id";

    public static final String hotel_table = "hotels";
    public static final String hotel_table_id = "id";
    public static final String hotel_table_name = "name";
    public static final String hotel_table_rating = "rating";
    public static final String hotel_table_image = "iamges";
    public static final String hotel_table_facility = "facilities";
    public static final String hotel_table_contact = "contacts";
    public static final String hotel_table_location = "location";


    private static class DbHelper extends SQLiteOpenHelper {
        String tableCreate = "CREATE TABLE " + Main_Table_Name + "("
                + Main_Table_Id + " INTEGER PRIMARY KEY, "
                + Main_Table_Itinerary_Date + " VARCHAR(50), "
                + Main_Table_Itinerary_Name + " VARCHAR(100), "
                + Main_Table_User_Id + " integer);";

        String tableCreate2 = "CREATE TABLE " + flight_table + "("
                + flight_table_id + " INTEGER, "
                + flight_table_origin + " VARCHAR(50), "
                + flight_table_destination + " varchar(50), "
                + flight_table_deptime + " varchar(20), "
                + flight_table_departureDate + " varchar(40), "
                + flight_table_arrtime + " varchar(20), "
                + flight_table_duration + " varchar(20), "
                + flight_table_flightno + " varchar(20), "
                + flight_table_seatingclass + " varchar(10), "
                + flight_table_stops + " varchar(3), "
                + flight_table_airline + " varchar(20), "
                + flight_table_fare + " varchar(20));";

        String tableCreate3 = "CREATE TABLE " + bus_table + "("
                + bus_table_id + " INTEGER, "
                + bus_table_origin + " VARCHAR(50), "
                + bus_table_destination + " varchar(50), "
                + bus_table_deptime + " varchar(20), "
                + bus_table_duration + " varchar(20), "
                + bus_table_type + " varchar(50), "
                + bus_table_name + " varchar(50), "
                + bus_table_fare + " varchar(20));";

        String tableCreate4 = "CREATE TABLE " + city_table + "("
                + city_name + " varchar(50), "
                + city_id + " VARCHAR(50));";

        public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(tableCreate);
            db.execSQL(tableCreate2);
            db.execSQL(tableCreate3);
            db.execSQL(tableCreate4);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public void close() {
        db.close();
    }

    public void open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = dbHelper.getReadableDatabase();
        }
    }

    public int insertNewItinerary(PlanItem item) {
        ContentValues cv = new ContentValues();
        cv.put(Main_Table_Itinerary_Date, item.created_at);
        cv.put(Main_Table_User_Id, item.user_id);
        cv.put(Main_Table_Itinerary_Name, item.name);
        cv.put(Main_Table_Id, item.id);
        db.insert(Main_Table_Name, null, cv);
        return item.id;
    }

    public void addNewFlight(Flight flight, int id) {
        ContentValues cv = new ContentValues();
        cv.put(flight_table_id, id);
        cv.put(flight_table_departureDate, flight.timestamp);
        cv.put(flight_table_origin, flight.origin);
        cv.put(flight_table_destination, flight.destination);
        cv.put(flight_table_deptime, flight.deptime);
        cv.put(flight_table_arrtime, flight.arrtime);
        cv.put(flight_table_duration, flight.duration);
        cv.put(flight_table_flightno, flight.flightno);
        cv.put(flight_table_seatingclass, flight.seatingclass);
        cv.put(flight_table_stops, flight.stops);
        cv.put(flight_table_airline, flight.airline);
        try{
            cv.put(flight_table_fare, flight.fare.totalfare);
        }catch (Exception e) {
            cv.put(flight_table_fare, flight.totalfare_db);
        }
        db.insert(flight_table, null, cv);
    }

    public void addNewBus(Bus bus, int id) {
        ContentValues cv = new ContentValues();
        cv.put(bus_table_id, id);
        cv.put(bus_table_origin, bus.origin);
        cv.put(bus_table_destination, bus.destination);
        cv.put(bus_table_duration, bus.duration);
        cv.put(bus_table_deptime, bus.DepartureTime);
        cv.put(bus_table_type, bus.BusType);
        cv.put(bus_table_name, bus.TravelsName);
        try{
            cv.put(bus_table_fare, bus.fare.totalfare);
        }catch (Exception e) {
            cv.put(bus_table_fare, bus.totalfare_db);
        }
        db.insert(bus_table, null, cv);
    }

    public ArrayList<PlanItem> getAllPlans(ArrayList<PlanItem> list) {
        Cursor c = db.rawQuery("select * from " + Main_Table_Name, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            PlanItem item = new PlanItem();
            item.name = c.getString(c.getColumnIndex(Main_Table_Itinerary_Name));
            item.id = c.getInt(c.getColumnIndex(Main_Table_Id));
            //item.created_at = c.getString(c.getColumnIndex(Main_Table_Itinerary_Date));
            list.add(item);
            while (c.moveToNext()) {
                PlanItem item2 = new PlanItem();
                item2.name = c.getString(c.getColumnIndex(Main_Table_Itinerary_Name));
                item2.id = c.getInt(c.getColumnIndex(Main_Table_Id));
                //item2.created_at = c.getString(c.getColumnIndex(Main_Table_Itinerary_Date));
                list.add(item2);
            }
        }
        return list;
    }

    public PlanItem findItinery(int id) {
        Cursor c = db.rawQuery("select * from " + Main_Table_Name + " where " + Main_Table_Id + "= '" + id + "'", null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            PlanItem planItem = new PlanItem();
            planItem.id = c.getInt(c.getColumnIndex(Main_Table_Id));
            planItem.name = c.getString(c.getColumnIndex(Main_Table_Itinerary_Name));
            planItem.created_at = c.getString(c.getColumnIndex(Main_Table_Itinerary_Date));
            planItem.user_id = c.getInt(c.getColumnIndex(Main_Table_User_Id));
            return planItem;
        } else {
            return null;
        }
    }

    public ArrayList<Flight> findFlights(int id, ArrayList<Flight> list) {
        Cursor c = db.rawQuery("select * from " + flight_table + " where " + flight_table_id + "= '" + id + "'", null);
        if (c.moveToFirst()) {
            do {
                Flight flight = new Flight();
                flight.timestamp = c.getString(c.getColumnIndex(flight_table_departureDate));
                flight.origin = c.getString(c.getColumnIndex(flight_table_origin));
                flight.destination = c.getString(c.getColumnIndex(flight_table_destination));
                flight.deptime = c.getString(c.getColumnIndex(flight_table_deptime));
                flight.arrtime = c.getString(c.getColumnIndex(flight_table_arrtime));
                flight.duration = c.getString(c.getColumnIndex(flight_table_duration));
                flight.flightno = c.getString(c.getColumnIndex(flight_table_flightno));
                flight.seatingclass = c.getString(c.getColumnIndex(flight_table_seatingclass));
                flight.stops = c.getString(c.getColumnIndex(flight_table_stops));
                flight.airline = c.getString(c.getColumnIndex(flight_table_airline));
                flight.fare = flight.new FlightFare();
                flight.fare.totalfare = c.getString(c.getColumnIndex(flight_table_fare));
                list.add(flight);
            } while (c.moveToNext());
        }
        return list;
    }

    public ArrayList<Bus> findBuses(int id, ArrayList<Bus> list) {
        Cursor c = db.rawQuery("select * from " + bus_table + " where " + bus_table_id + "= '" + id + "'", null);
        if (c.moveToFirst()) {
            do {
                Bus bus = new Bus();
                bus.origin = c.getString(c.getColumnIndex(bus_table_origin));
                bus.destination = c.getString(c.getColumnIndex(bus_table_destination));
                bus.DepartureTime = c.getString(c.getColumnIndex(bus_table_deptime));
                bus.duration = c.getString(c.getColumnIndex(bus_table_duration));
                bus.TravelsName = c.getString(c.getColumnIndex(bus_table_name));
                bus.BusType = c.getString(c.getColumnIndex(bus_table_type));
                bus.fare = bus.new BusFare();
                bus.fare.totalfare = c.getString(c.getColumnIndex(bus_table_fare));
                list.add(bus);
            } while (c.moveToNext());
        }
        return list;
    }

    public void removeFlight(Flight flight, int id) {
        db.delete(flight_table, flight_table_id + "= ? and "
                + flight_table_airline + "= ? and " + flight_table_flightno + "= ? and "
                + flight_table_origin + "= ?", new String[]{String.valueOf(id), flight.airline, flight.flightno, flight.origin});
    }

    public void removeAllFlightsOfTrip(int trip_id) {
        db.delete(flight_table, flight_table_id+"="+trip_id, null);
    }

    public void removeBus(Bus bus, int id) {
        db.delete(bus_table, bus_table_id + "= ? and "
                + bus_table_deptime + "= ? and " + bus_table_type + "= ? and "
                + bus_table_origin + "= ? and "
                + bus_table_name + "= ?", new String[]{String.valueOf(id), bus.DepartureTime, bus.BusType, bus.origin, bus.TravelsName});
    }

    public void removeAllBusesOfTrip(int trip_id) {
        db.delete(bus_table, bus_table_id+"="+trip_id, null);
    }

    public void insertCity(String name, String id) {
        ContentValues cv = new ContentValues();
        cv.put(city_name, name);
        cv.put(city_id, id);
        db.insert(city_table, null, cv);
    }

    public ArrayList<City> findCity(String name, ArrayList<City> list) {
        list.removeAll(list);
        list.clear();
        Cursor c = db.rawQuery("select * from " + city_table + " where " + city_name + " like '" + name + "%'", null);
        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    City city = new City();
                    city.name = c.getString(c.getColumnIndex(city_name));
                    city.id = c.getString(c.getColumnIndex(city_id));
                    list.add(city);
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public void removePlan(int id) {
        db.delete(Main_Table_Name, Main_Table_Id + "=" + id, null);
        db.delete(flight_table, flight_table_id+"="+id, null);
        db.delete(bus_table, bus_table_id+"="+id, null);
    }

    public void removeAllPlans() {
        db.delete(Main_Table_Name, null, null);
    }
}
