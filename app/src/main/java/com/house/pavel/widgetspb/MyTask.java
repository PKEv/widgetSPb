package com.house.pavel.widgetspb;




import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

public class MyTask extends Thread {
    String title;//Тут храним значение заголовка сайта
    private static final String urlString = "http://gorod.gov.spb.ru/accounts/";

    String getInfoString() {
        return title;
    }

    MyTask(String userId) {
        id = userId;
    }

    private String output = "";
    private String id = "";

    private void request() {
        Document doc = null;//Здесь хранится будет разобранный html документ
        try {
            //Считываем заглавную страницу http://harrix.org
            doc = Jsoup.connect(urlString + id).get();
        } catch (IOException e) {
            //Если не получилось считать
            e.printStackTrace();
        }

        //Если всё считалось, что вытаскиваем из считанного html документа заголовок
        if (doc!=null) {
            // добавить чтение имени пользователя
            title = doc.getElementsByClass("num").get(0).text().replaceAll("\\s","");
            title += "/" + doc.getElementsByClass("num").get(1).text().replaceAll("\\s","");
            title += " ";
            //title = doc.title();
        }
        else
            title = "";


    }

    @Override
    public void run() {
            request();

    }


}
