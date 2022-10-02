import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class MovieAnalyzer {
	static String COMMA_DELIMITER = ",";
	List<List<String>> records = new ArrayList<>();
	Map<String, Integer> headers = new HashMap<String, Integer>();

	public static Integer stringToInteger(String string) {
		int value = 0;
		for(int i = 0; i < string.length(); i ++) {
			if(string.charAt(i) > '9' || string.charAt(i) < '0') {
				System.out.println(string);
				return Integer.valueOf(-1);
			}
			value = (int) (string.charAt(i) - '0') + value * 10;
		}
		return Integer.valueOf(value);
	}

	public static List<String> splitStringToList(String string) {
		List<String> result = Arrays.asList(string.split(","));
		for (int i = 0; i < result.size(); i ++)
			result.set(i, result.get(i).strip());
		return result;
	}

	public static List<String> readCSVRow(String line) {
		List<String> record = new ArrayList<String>();
		int l = 0, recording = 0, waiting = 0;
		char deLim = ',';
		for(int r = 0; r < line.length(); r ++) {
			if(recording == 0) {
				if (line.charAt(r) == '\"') {
					deLim = '\"';
				}
				else {
					if(waiting == 1) {
						if(line.charAt(r) == ',')
							waiting = 0;
					}
					else if(line.charAt(r) != ','){
						l = r;
						recording = 1;
					}
					else if(line.charAt(r) == ',') {
						record.add("");
					}
				}
			}
			else {
				if (   deLim == line.charAt(r) ||
						r == line.length() - 1
					) {
					if(line.charAt(r) != deLim) {
						record.add(line.substring(l, r + 1).strip());
					}
					else {
						record.add(line.substring(l, r).strip());
					}
					if(deLim == '\"')
						waiting = 1;
					else
						waiting = 0;
					deLim = ',';
					recording = 0;
				}
			}
		}
		return record;
	}

	public MovieAnalyzer(String csvFile) throws FileNotFoundException {
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String line;
			int header = 0;
			while ((line = br.readLine()) != null) {
				List<String> values = readCSVRow(line);
				if(header == 0) {
					for(int i = 0; i < values.size(); i ++) {
					//System.out.println(values.get(i));
						headers.put(values.get(i), Integer.valueOf(i));
					}
					header = 1;
				}
				else records.add(values);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
		This method returns a <year, count> map,
		where the key is the year while the value is the number of movies
		released in that year.
		The map should be sorted by descending order of year (i.e., from the latest to the earliest).
	*/
	public Map<Integer, Integer> getMovieCountByYear() {
		int yearId = headers.get("Released_Year");
		Comparator<Integer> c = (c1, c2) -> c1 < c2 ? 1 : 0;
		TreeMap<Integer, Integer> sortedMovieCountByYear = new TreeMap<>(Collections.reverseOrder());
		Map<Integer, Integer> movieCountByYear =
			records.stream()
			.collect(groupingBy(r -> stringToInteger(r.get(yearId)),
		    Collectors.summingInt(r -> 1)));
		sortedMovieCountByYear.putAll(movieCountByYear);
		return sortedMovieCountByYear;
	}

	/**
	* This method returns a <genre, count> map, where the key is the genre
	* while the value is the number of
	movies in that genre.
	**/

//	public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm)
//	{
//		// Create a list from elements of HashMap
//		List<Map.Entry<String, Integer> > list =
//				new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());
//
//		// Sort the list
//		Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
//			public int compare(Map.Entry<String, Integer> o1,
//			                   Map.Entry<String, Integer> o2)
//			{
//				return (o1.getValue()).compareTo(o2.getValue());
//			}
//		});
//
//		// put data from sorted list to hashmap
//		HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
//		for (Map.Entry<String, Integer> aa : list) {
//			temp.put(aa.getKey(), aa.getValue());
//		}
//		return temp;


	public Map<String, Integer> getMovieCountByGenre() {
		int genreId = headers.get("Genre");
		Comparator<Integer> c = (c1, c2) -> c1 < c2 ? 1 : 0;
		Map<String, Integer> sortedMovieCountByGenre = new HashMap<>();
		Map <String, Integer> movieCountByGenre =
			records.stream().flatMap(r -> Stream.of(r.get(genreId)))
			.flatMap(r -> Stream.of(splitStringToList(r)))
			.flatMap(List::stream)
			.collect(groupingBy(r -> r.toString(),
				Collectors.summingInt(r -> 1)));
		List<Map.Entry<String, Integer>> mapEntryList =
			new LinkedList<Map.Entry<String, Integer> >(movieCountByGenre.entrySet());
		Collections.sort(mapEntryList,
			(e1, e2) -> {
				int compareResult = e1.getValue().compareTo(e2.getValue());
				if(compareResult == 0)
					return e1.getKey().compareTo(e2.getKey());
				else return -compareResult;
			});
		for(int i = 0; i < mapEntryList.size(); i ++) {
			Map.Entry<String, Integer> entry = mapEntryList.get(i);
			sortedMovieCountByGenre.put(entry.getKey(), entry.getValue());
		}
		return sortedMovieCountByGenre;
	}

	public static void main(String[] args) {
		MovieAnalyzer movieAnalyzer;
		try {
			movieAnalyzer = new MovieAnalyzer("resources/imdb_top_500.csv");
		} catch(FileNotFoundException e) {
			System.out.println("Input CSV File is not found.");
			return;
		}
		System.out.println(movieAnalyzer.getMovieCountByGenre());
	}
}