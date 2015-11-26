package com.github.hretaro.syoboimono.core;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.haretaro.syoboimono.domutility.IterableNodeList;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Anime{
	private String title;
	private String subTitle;
	private int count;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String chName;
	private final DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private final DateTimeFormatter hourMinute = DateTimeFormatter.ofPattern("HH:mm");
	private final DateTimeFormatter dateJp = DateTimeFormatter.ofPattern("MM月dd日");
	
	public static List<Anime> getAnimeList(LocalDate date,int days) throws IOException{
		return getAnimeList(date.toString(),days);
	}
	
	public static List<Anime> getAnimeList() throws IOException{
		return getAnimeList("",1);
	}
	
	public static List<Anime> getAnimeList(String start, int days) throws IOException{
		String startarg;
		if(start == ""){
			startarg = "";
		}else{
			startarg = "&start="+start;
		}
		URL url=null;
		try {
			url = new URL("http://cal.syoboi.jp/cal_chk.php?days="+days+startarg);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		HttpURLConnection con=null;
		try {
			con = (HttpURLConnection)url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder=null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		Document document=null;
		try {
			document = builder.parse(con.getInputStream());
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		NodeList progItems = document.getElementsByTagName("ProgItem");
		
		List<Anime> animeList = new ArrayList<Anime>();
		(new IterableNodeList(progItems)).stream()
			.forEach(a -> animeList.add(new Anime(a)));
		
		return animeList;
	}
	
	public Anime(Node progItem){
		NamedNodeMap attributes = progItem.getAttributes();
		title = attributes.getNamedItem("Title").getNodeValue();
		subTitle = attributes.getNamedItem("SubTitle").getNodeValue();
		chName = attributes.getNamedItem("ChName").getNodeValue();
		try{
			count = Integer.parseInt(attributes.getNamedItem("Count").getNodeValue());
		}catch(NumberFormatException e){
			count = 1;
		}
		
		startTime = LocalDateTime.parse(
				attributes.getNamedItem("StTime").getNodeValue()
				,parser);
		
		endTime = LocalDateTime.parse(
				attributes.getNamedItem("EdTime").getNodeValue()
				,parser);
	}
	
	public boolean isFirst(){
		return count==1;
	}
	
	public StringProperty titleProperty(){
		return new SimpleStringProperty(title);
	}
	
	public StringProperty subTitleProperty(){
		return new SimpleStringProperty(subTitle);
	}
	
	public IntegerProperty countProperty(){
		return new SimpleIntegerProperty(count);
	}
	
	public StringProperty chNameProperty(){
		return new SimpleStringProperty(chName);
	}
	
	public StringProperty timeProperty(){
		return new SimpleStringProperty(getTime());
	}
	
	@Override
	public String toString(){
		return title +"#"+count + subTitle + " " + chName + " " + startTime + "-" + endTime;
	}
	
	public String getTime(){
		return startTime.format(dateJp)
				+"("
				+startTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault())
				+") "
				+startTime.format(hourMinute)
				+"~"
				+endTime.format(hourMinute);
	}

	public String getTitle() {
		return title;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public int getCount() {
		return count;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public String getChName() {
		return chName;
	}
}
