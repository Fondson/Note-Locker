package com.dev.fondson.NoteLocker;

/**
 * Created by Fondson on 2016-09-11.
 */
public class CalendarItem {
    public String date;
    public String timeBegin;
    public String timeEnd;
    public String event;
    public String location;
    public CalendarItem(String date, String timeBegin, String timeEnd, String event, String location){
        this.date=date;
        this.timeBegin=timeBegin;
        this.timeEnd=timeEnd;
        this.event=event;
        this.location=location;
    }
}
