import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.*;

import static java.util.stream.Collectors.groupingBy;

public class MovieAnalyzer {
	static String COMMA_DELIMITER = ",";
	List<List<String>> records = new ArrayList<>();
	Map<String, Integer> headers = new HashMap<String, Integer>();

	public static Integer stringToInteger(String string) {
		int value = 0;
		for(int i = 0; i < string.length(); i ++) {
			if(string.charAt(i) > '9' || string.charAt(i) < '0')
				return Integer.valueOf(-1);
			value = (int) (string.charAt(i) - '0') + value * 10;
		}
		return Integer.valueOf(value);
	}

	public MovieAnalyzer(String csvFilePath) throws FileNotFoundException {
		try (BufferedReader br = new BufferedReader(new FileReader("book.csv"))) {
			String line;
			int header = 0;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(COMMA_DELIMITER);
				if(header == 0) {
					for(int i = 0; i < values.length; i ++) {
						headers.put(values[i], Integer.valueOf(i));
					}
					header = 1;
				}
				else records.add(Arrays.asList(values));
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
		Map<Integer, Integer> movieCountByYear =
			records.stream().collect(groupingBy(r -> MovieAnalyzer.stringToInteger(r.get(yearId)),
			Collectors.summingInt(r -> 1)));
		return movieCountByYear;
	}

//	public Map<String, Integer> getMovieCountByGenre() {
//		Map <String, Integer> movieCountByGenre;
//		return movieCountByGenre;
//	}

	public static void main(String[] args) {
		MovieAnalyzer movieAnalyzer;
		try {
			movieAnalyzer = new MovieAnalyzer("./resources/imdb_top_500.csv");
		} catch(FileNotFoundException e) {
			System.out.println("Input CSV File is not found.");
			return;
		}
		System.out.println(movieAnalyzer.getMovieCountByYear());
	}
}