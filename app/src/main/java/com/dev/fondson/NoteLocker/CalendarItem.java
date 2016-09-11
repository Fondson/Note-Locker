package com.dev.fondson.NoteLocker;

/**
 * Created by Fondson on 2016-09-11.
 */
public class CalendarItem {
    public String date;
    public String time;
    public String event;
    public String location;
    public CalendarItem(String date, String time, String event, String location){
        this.date=date;
        this.time=time;
        this.event=event;
        this.location=location;
    }
}
