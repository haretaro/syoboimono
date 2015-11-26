package com.github.haretaro.syoboimono.gui;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import com.github.hretaro.syoboimono.core.Anime;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Main extends Application{

	@FXML DatePicker datePicker;
	@FXML TextArea console;
	@FXML Button nowButton;
	@FXML TextField days;
	@FXML TextField titleFilterField;
	@FXML CheckBox firstCheckBox;
	@FXML TextField chNameFilterField;
	@FXML TableView<Anime> table;
	@FXML TableColumn<Anime,String> title;
	@FXML TableColumn<Anime,Integer> count;
	@FXML TableColumn<Anime,String> subTitle;
	@FXML TableColumn<Anime,String> chName;
	@FXML TableColumn<Anime,String> time;
	
	private ObservableList<Anime> animeList;
	private FilteredList<Anime> filteredAnimeList;
	
	private LocalDateTime now;
	
	public static void main(String[] args){
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException{
		stage.setTitle("しょぼい番組表のようなもの");
		AnchorPane root = FXMLLoader.load(getClass().getResource("main.fxml"));
		Scene scene = new Scene(root,800,600);
		stage.setScene(scene);
		stage.show();
	}
	
	@FXML
	public void initialize(){
		datePicker.setValue(LocalDate.now());
		datePicker.setOnAction(event -> {
			LocalDate date = datePicker.getValue();
			updateAnimeList(date);
		});

		nowButton.setOnAction(event -> {
			LocalDate target = LocalDate.now();
			//時刻が0時から5時の場合は,前日の番組表を読む
			if(now.getHour() < 5){
				target = target.minusDays(1);
			}
			// datePickerが今日を指していたら番組表を読み込む.
			// そうで無ければdatePickerを今日の日付に更新する.
			// このときdatePickerのonActionイベントが呼ばれる.
			if(datePicker.getValue().equals(target)){
				updateAnimeList(target);
			}else{
				datePicker.setValue(target);
			}
		});
		
		//daysテキストフィールドにフォーカスした時,内容を空白にする.
		days.focusedProperty().addListener(
			(arg,lostFocus,onFocus)->{
				if(onFocus){
					days.setText("");
				}
			});
		
		titleFilterField.setOnKeyReleased(event->updateFilter());
		firstCheckBox.setOnAction(event->updateFilter());
		chNameFilterField.setOnKeyReleased(event->updateFilter());
		
		//テーブルのカラムをAnimeクラスのフィールドに紐付ける
		title.setCellValueFactory(
                new PropertyValueFactory<Anime,String>("title"));
		subTitle.setCellValueFactory(
				new PropertyValueFactory<Anime,String>("subTitle"));
		count.setCellValueFactory(
				new PropertyValueFactory<Anime,Integer>("count"));
		chName.setCellValueFactory(
                new PropertyValueFactory<Anime,String>("chName"));
		time.setCellValueFactory(
                new PropertyValueFactory<Anime,String>("time"));
		
		table.getSortOrder().add(time);//テーブルはデフォルトで日付順ソート

		//テーブルの行にCSSのクラスを設定するようにする
		table.setRowFactory(new Callback<TableView<Anime>, TableRow<Anime>>() {
			@Override
			public TableRow<Anime> call(TableView<Anime> tableview) {
				final TableRow<Anime> row = new TableRow<Anime>() {
					@Override
					protected void updateItem(Anime anime, boolean empty) {
						super.updateItem(anime, empty);
						if(empty){
							getStyleClass().removeAll(Collections.singleton("endedAnime"));
							getStyleClass().removeAll(Collections.singleton("broadcastingAnime"));
						}else{
							//放送終了したアニメの行にCSSクラスを設定
							if (anime.getEndTime().compareTo(now) < 0) {
								getStyleClass().add("endedAnime");
							}else{
								getStyleClass().removeAll(Collections.singleton("endedAnime"));
							}
							//放送中のアニメの行にCSSクラスを設定
							if(anime.getStartTime().compareTo(now) < 0 && anime.getEndTime().compareTo(now) > 0){
								getStyleClass().add("broadcastingAnime");
							}else{
								getStyleClass().removeAll(Collections.singleton("broadcastingAnime"));
							}
						}
					}
				};
				return row;
			}
		});

		//アニメリストをFilteredListにラップ
		animeList = FXCollections.observableArrayList();
		filteredAnimeList = new FilteredList<>(animeList,p->true);
		
		//フィルターしたアニメリストをSortedListにラップ,コンパレータをテーブルのコンパレータにバインド
		SortedList<Anime> sortedAnimeList = new SortedList<>(filteredAnimeList);
		sortedAnimeList.comparatorProperty().bind(table.comparatorProperty());
		
		table.setItems(sortedAnimeList);
		
		now = LocalDateTime.now();
		LocalDate target = LocalDate.now();
		//時刻が0時から5時の場合は,前日の番組表を読む
		if(now.getHour() < 5){
			target = target.minusDays(1);
		}
		updateAnimeList(target);
		datePicker.setValue(target);
	}
	
	void focusOnBroadcastingAnime(){
		List<Anime> broadcastingAnimes = filteredAnimeList.stream()
				.filter(a->a.getStartTime().compareTo(now)<0 && a.getEndTime().compareTo(now)>0)
				.collect(Collectors.toList());
		//これから放送される番組までスクロール
		if (broadcastingAnimes.size() > 0) {
			int index = filteredAnimeList.indexOf(broadcastingAnimes.get(0));
			table.scrollTo(index);
		} else{
			table.scrollTo(0);
		}
	}
	
	void updateAnimeList(LocalDate date){
		now = LocalDateTime.now();
		
		//daysテキストフィールドから数字を読み込んで何日分読み込むか決める
		int loadLength;
		try{
			int n = Integer.parseInt(days.getText());
			if(0<n && n<31){
				loadLength = n;
			}else if(n>30){
				loadLength = 30;
			}else{
				loadLength = 1;
			}
		}catch(NumberFormatException e){
			loadLength = 1;
		}
		days.setText(Integer.toString(loadLength));
		
		System.out.println("start:"+date+" days:"+loadLength);
		animeList.clear();
		try {
			animeList.addAll(Anime.getAnimeList(date,loadLength));
			console.insertText(console.getLength(),"番組データ取得完了 日付:"+date+"日数:"+loadLength+"\n");
		} catch (IOException e) {
			System.out.println("internet connection error");
			console.insertText(console.getLength(),"インターネット接続に問題があります.\n");
		}
		focusOnBroadcastingAnime();
	}
	
	void updateFilter() {
		try {
			String titleFilterText = titleFilterField.getText();
			Pattern titleRegEx = Pattern.compile(titleFilterText);
			boolean isCheckboxSelected = firstCheckBox.isSelected();
			String chNameText = chNameFilterField.getText();
			Pattern chNameRegEx = Pattern.compile(chNameText);

			filteredAnimeList.setPredicate(anime -> {
				boolean titleFilter = true;
				boolean firstCheck = true;
				boolean chNameFilter = true;

				if (!titleFilterText.isEmpty())
					titleFilter = titleRegEx.matcher(anime.getTitle()).find();
				if (isCheckboxSelected) {
					firstCheck = anime.isFirst();
				}

				if (!chNameText.isEmpty()) {
					chNameFilter = chNameRegEx.matcher(anime.getChName()).find();
				}
				return titleFilter && firstCheck && chNameFilter;
			});	
			focusOnBroadcastingAnime();
		} catch (PatternSyntaxException e) {
			System.out.println("regex syntx error");
		}
	}
}
