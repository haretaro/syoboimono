package com.github.haretaro.syoboimono.example;

import java.io.IOException;
import java.util.List;

import com.github.hretaro.syoboimono.core.Anime;

public class SyoboiCalendarTest {
	public static void main(String[] args){
		List<Anime> animelist = null;
		try {
			animelist = Anime.getAnimeList();
			animelist.stream().forEach(a -> System.out.println(a));
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("no Internet connection.");
		}
	}	
}
