package jp.co.plusize.kinoshita_keisuke.calculate_sale;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
public class SummarySales {
	private static final String List = null;

	public static void main(String[] args) {
		HashMap<String,String> branch = new HashMap<String,String>();
		HashMap<String,Long> branchSales = new HashMap<String,Long>();
		HashMap<String,String> commodity = new HashMap<String,String>();
		HashMap<String,Long> commoditySales = new HashMap<String,Long>();
		try {
			//支店定義ファイルの読み込み
			File file = new File(args[0], "branch.lst");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String s;

			while((s = br.readLine()) != null) {
				String[] items = s.split(",");

				if(items.length != 2 || !items[0].matches("[0-9]{3}") ) {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
				branch.put(items[0],items[1]); //支店コードと支店名
				branchSales.put(items[0],0L); //支店コードと金額
			}
			br.close();
		} catch(IOException e) {
			System.out.println("支店定義ファイルが存在しません");
		}
			//商品定義ファイルの読み込み
		try {
			File file = new File(args[0], "commodity.lst");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String s;

			while((s = br.readLine()) != null) {
				String[] items = s.split(",");
				if(items.length != 2 || !items[0].matches("[A-Z|a-z|0-9]{8}")) {
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
				commodity.put(items[0],items[1]); //商品コードと商品名
				commoditySales.put(items[0], 0L); //商品コードと金額
			}
			br.close();
		} catch(IOException e) {
			System.out.println("商品定義ファイルが存在しません");
		}

		//集計
		File dir =new File(args[0]);
		File[] files = dir.listFiles();
		ArrayList<File> list = new ArrayList<File>();
		try {
			for(int i = 0; i < files.length; i++) {
				File file = files[i];
				String fileName = file.getName(); //ファイルからファイル名を取得
				if(fileName.matches("[0-9]{8}.rcd$")){
					list.add(file); //リスト化
				}
			}
			//連番チェック
			for(int i =0; i + 1 < list.size() - 1; i++) {
				String str = list.get(i).getName().substring(0, 8);
				String stt = list.get(i+1).getName().substring(0, 8);
				int one = Integer.parseInt(str);
				int two = Integer.parseInt(stt);
				if(two - one != 1){
					System.out.println("売上ファイル名が連番になっていません");
					return;
				}
			}

		} catch(Exception e) {
			System.out.println("予期せぬエラーが発生しました");
		}

		try {
			for(int i = 0; i < list.size(); i++) {
				ArrayList<String> proceeds = new ArrayList<String>();
				FileReader fr = new FileReader(list.get(i));
				BufferedReader br = new BufferedReader(fr);
				String s;
				//proceedsに各値を加えてリストに入れる


				while((s = br.readLine()) != null) { //ファイルの中を参照
					String str = s;
					proceeds.add(str);
				}
				if(proceeds.size() >= 4) {
					System.out.println("<" + list.get(i).getName() + ">のフォーマットが不正です");
					return;
				}

				//マップに支店コードと売上金額を入れて保持
				String bCode = proceeds.get(0);  //支店コード
				String cCode = proceeds.get(1); //商品コード
				String name = proceeds.get(2); //売上額
				Long money = Long.parseLong(name);
				if(!branch.containsKey(bCode)) {
					System.out.println("<" + list.get(i).getName() + ">の支店コードが不正です");
					return;
				}
				if(!commodity.containsKey(cCode)) {
					System.out.println("<" + cCode + ">の商品コードが不正です");
					return;
				}
				//branchSales.put(bCode, money);
				//commoditySales.put(cCode, money);
				long sum = branchSales.get(bCode); //元の値を参照
				sum += money; //元の値にmoneyを入れる
				branchSales.put(bCode,money);
				commoditySales.put(cCode, money);
				if(sum > 9999999999L) {
					 System.out.println("合計金額が10桁を超えました");
				}

			}
		} catch(Exception e) {
			System.out.println("予期せぬエラーが発生しました");
		}
		try {
			//支店別集計ファイルを出力
			Map<String,Long> unit = new HashMap<String,Long>();
			File file = new File(args[0],"branch.out");
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			for(int i = 0; i < branch.size(); i++) {
				ArrayList<String> date = new ArrayList<String>();
				ArrayList<String> code = new ArrayList<String>();
				File da = new File(args[0], "branch.lst");
				FileReader fr = new FileReader(da);
				BufferedReader br = new BufferedReader(fr);
				String s;
				while((s = br.readLine()) != null) {
					String str =s;
					date.add(s);
					String[] items = s.split(",");
					code.add(items[0]);
				}
				unit.put(date.get(i), branchSales.get(code.get(i)));
			}
			ArrayList<Map.Entry<String,Long>> entries = new ArrayList<Map.Entry<String, Long>>(unit.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {

				public int compare(Entry<String,Long> entry1,Entry<String,Long> entry2) {
					return((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
			for(Entry<String,Long> s : entries) {
				bw.write(s.getKey() + ","+s.getValue() + "\r\n");
			}
			bw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			//商品別集計ファイルに出力
			Map<String,Long> unit = new HashMap<String,Long>();
			File file = new File(args[0],"commodity.out");
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			for(int i = 0; i < commodity.size(); i++) {
				ArrayList<String> date = new ArrayList<String>();
				ArrayList<String> code = new ArrayList<String>();
				File da = new File(args[0], "commodity.lst");
				FileReader fr = new FileReader(da);
				BufferedReader br = new BufferedReader(fr);
				String s;
				while((s = br.readLine()) != null) {
					String str =s;
					date.add(s);
					String[] items = s.split(",");
					code.add(items[0]);
				}
				unit.put(date.get(i), commoditySales.get(code.get(i)));
			}
			ArrayList<Map.Entry<String,Long>> entries = new ArrayList<Map.Entry<String, Long>>(unit.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {

				public int compare(Entry<String,Long> entry1,Entry<String,Long> entry2) {
					return((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
			for(Entry<String,Long> s : entries) {
				bw.write(s.getKey() + ","+s.getValue() + "\r\n");
			}
			bw.close();
		} catch(Exception e) {
			System.out.println("予期せぬエラーが発生しました");
		}


	}
}

