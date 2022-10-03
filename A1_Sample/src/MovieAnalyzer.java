import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class MovieAnalyzer {
	List<List<String>> records = new ArrayList<>();
	Map<String, Integer> headers = new HashMap<>();

	public static Integer stringToInteger(String string) {
		int value = 0;
		for (int i = 0; i < string.length(); i ++) {
			if (string.charAt(i) == ',' && i != string.length() - 1) {
				continue;
			}
			if (string.charAt(i) > '9' || string.charAt(i) < '0') {
				return -1;
			}
			value = (string.charAt(i) - '0') + value * 10;
		}
		return value;
	}

	public static List<String> splitStringToList(String string) {
		List<String> result = Arrays.asList(string.split(","));
		for (int i = 0; i < result.size(); i ++)
			result.set(i, result.get(i).strip());
		return result;
	}

	public static String solveEscaped(String string) {
		return string;
	}

	public static boolean checkGenre(String genreCur, String genre) {
		List <String> genres = Arrays.asList(genreCur.split(","));
		for (int i = 0; i < genres.size(); i ++) {
			genres.set(i, genres.get(i).strip());
			if (genres.get(i).equals(genre))
				return true;
		}
		return false;
	}

	public static int getRunTime(String string) {
		int value = 0;
		for (int i = 0; i < string.length(); i ++) {
			if (string.charAt(i) > '9' || string.charAt(i) < '0') {
				break;
			}
			value = (string.charAt(i) - '0') + value * 10;
		}
		return value;
	}

	public static int getOverviewLength(String string) {
		return string.length();
	}

	public static List<List<String>> splitJoinStars(List<String> record, int id) {
		List<String> items = Arrays.asList(
				record.get(id),
				record.get(id + 1),
				record.get(id + 2),
				record.get(id + 3)
			);
		for (int i = 0; i < items.size(); i ++)
			items.set(i, items.get(i).strip());
		List<List<String>> result = new ArrayList<>();
		Collections.sort(items);
		for (int i = 0; i < items.size(); i ++)
			for (int j = i + 1; j < items.size(); j ++) {
				List<String> joinItems = Arrays.asList(items.get(i), items.get(j));
				result.add(joinItems);
			}
		return result;
	}

	public static List<String> splitStars(List<String> record, int id) {
		return Arrays.asList(
				record.get(id),
				record.get(id + 1),
				record.get(id + 2),
				record.get(id + 3)
		);
	}

	public static List<List<String>> itemJoin(List<String> items, List<String> commons) {
		List<List<String>> results = new ArrayList<>();
		for (String item : items) {
			if (item.length() == 0)
				continue;
			List<String> itemMatched = new ArrayList<>(Arrays.asList(item));
			itemMatched.addAll(commons);
			results.add(itemMatched);
		}
		return results;
	}

	public static String getAverageRatings(List<String> ratings) {
		double rating = 0;
		for (String ratingStr : ratings) {
			rating += Float.parseFloat(ratingStr);
		}
		rating /= ratings.size();
		return String.valueOf(rating);
	}

	public static String getAverageGross(List<String> grosses) {
		double gross = 0;
		for (String grossStr : grosses) {
			gross += stringToInteger(grossStr);
		}
		gross /= grosses.size();
		return String.valueOf(gross);
	}

	public static List<String> readCSVRow(String line) {
		List<String> records = new ArrayList<>();
		int l = 0, recording = 0, waiting = 0;
		char deLim = ',';

		for (int r = 0; r < line.length(); r ++) {
			if (recording == 0) {
				if (line.charAt(r) == '\"') {
					deLim = '\"';
				}
				else {
					if (waiting == 1) {
						if (line.charAt(r) == ',')
							waiting = 0;
					}
					else if (line.charAt(r) != ','){
						l = r;
						recording = 1;
					}
					else if (line.charAt(r) == ',') {
						records.add("");
					}
				}
			}
			else {
				if (   deLim == line.charAt(r) ||
						r == line.length() - 1
					) {
					if (line.charAt(r) != deLim) {
						records.add(solveEscaped(line.substring(l, r + 1).strip()));
					}
					else {
						if (r < line.length() - 1 &&
							line.charAt(r) == '\"' && line.charAt(r + 1) == '\"') {
							r += 1;
							continue;
						}
						records.add(solveEscaped(line.substring(l, r)));
					}
					if (deLim == '\"')
						waiting = 1;
					else
						waiting = 0;
					deLim = ',';
					recording = 0;
					if (r == line.length() - 1 && line.charAt(r) == ',') {
						records.add("");
					}
				}
			}
		}
		return records;
	}

	public MovieAnalyzer(String csvFile) throws FileNotFoundException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
			String line;
			int header = 0;
			while ((line = br.readLine()) != null) {
				List<String> values = readCSVRow(line);
				if (header == 0) {
					for (int i = 0; i < values.size(); i ++) {
						headers.put(values.get(i), i);
					}
					header = 1;
				}
				else {
					records.add(values);
				}
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method returns a <year, count> map,
	 * where the key is the year while the value is the number of movies
	 * released in that year.
	 * The map should be sorted by descending order of year (i.e., from the latest to the earliest).
	**/
	public Map<Integer, Integer> getMovieCountByYear() {
		int yearId = headers.get("Released_Year");
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

	public Map<String, Integer> getMovieCountByGenre() {
		int genreId = headers.get("Genre");
		Map<String, Integer> sortedMovieCountByGenre = new LinkedHashMap<>();
		Map <String, Integer> movieCountByGenre =
			records.stream().flatMap(r -> Stream.of(r.get(genreId)))
			.flatMap(r -> Stream.of(splitStringToList(r)))
			.flatMap(List::stream)
			.collect(groupingBy(r -> r,
				Collectors.summingInt(r -> 1)));
		List<Map.Entry<String, Integer>> mapEntryList =
			new LinkedList<>(movieCountByGenre.entrySet());
		Collections.sort(mapEntryList,
			(e1, e2) -> {
				int compareResult = e1.getValue().compareTo(e2.getValue());
				if (compareResult == 0)
					return e1.getKey().compareTo(e2.getKey());
				else return -compareResult;
			});
		for (Map.Entry<String, Integer> mapEntry : mapEntryList) {
			sortedMovieCountByGenre.put(mapEntry.getKey(), mapEntry.getValue());
		}
		return sortedMovieCountByGenre;
	}

	/**
	 * If two people are the stars for the same movie, then the number of movies that they co-starred increases by 1.
	 * Returns a <[star1, star2], count> map, where the key is a list of names of the stars while the
	 * value is the number of movies that they have co-starred in. Note that the length of the key is 2 and the names
	 * of the stars should be sorted by alphabetical order in the list.
	 */

	public Map<List<String>, Integer> getCoStarCount() {
		int starId = headers.get("Star1");
		Map <String, Integer> sortedMovieCountByGenre = new HashMap<>();
		return records.stream().flatMap(r -> Stream.of(splitJoinStars(r, starId)))
						.flatMap(List::stream)
						.collect(groupingBy(r -> r, Collectors.summingInt(r -> 1)));
	}

	/**
	 * Returns the top K movies (parameter top_k) by the given criterion (parameter by)
	 */
	public List<String> getTopMovies(int top_k, String by) {
		int opt = 0;
		if (by.equals("runtime")) {
			by = "Runtime";
		}
		else {
			opt = 1;
			by = "Overview";
		}
		int nameId = headers.get("Series_Title"), rankId = headers.get(by);
		int finalOpt = opt;
		return records.stream().flatMap(r -> Stream.of(Arrays.asList(r.get(nameId), r.get(rankId))))
				.sorted(
					(r1, r2) -> {
						int cmp;
						if (finalOpt == 0) {
							cmp = -(getRunTime(r1.get(1)) - getRunTime(r2.get(1)));
						}
						else cmp = -(getOverviewLength(r1.get(1)) - getOverviewLength(r2.get(1)));
						if (cmp == 0) {
							cmp = r1.get(0).compareTo(r2.get(0));
						}
						return cmp;
					}
				).flatMap(r -> Stream.of(r.get(0)))
				.limit(top_k)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the top K stars (parameter top_k) by the given criterion (parameter by). Specifically,
	 * by="rating": the results should be stars sorted by descending order of the average rating of the
	 * movies that s/he have starred in.
	 * by="gross": the results should be stars sorted by descending order of the average gross of the
	 * movies that s/he have starred in.
	 * Note that the results should be a list of star names. If two stars have the same average rating or gross, then
	 * they should be sorted by the alphabetical order of their names.
	**/
	public List<String> getTopStars(int top_k, String by) {
		int opt = 0;
		if (by.equals("rating")) {
			by = "IMDB_Rating";
		}
		else {
			opt = 1;
			by = "Gross";
		}
		int rankId = headers.get(by), starId = headers.get("Star1");
		int finalOpt = opt;
		return records.stream()
				.flatMap(r -> {
					if (r.get(rankId).length() == 0)
						return Stream.of();
					List<String> stars = splitStars(r, starId);
					return Stream.of(itemJoin(stars, Arrays.asList(r.get(rankId))));
				})
				.flatMap(List::stream)
				.collect(groupingBy(r -> r.get(0), toList()
				)).entrySet().stream()
				.flatMap(r -> {
					List <String> scores = new ArrayList<>();
					List <List<String>> namesWithScores = r.getValue();
                  for (List<String> nameWithScores : namesWithScores)
                      scores.add(nameWithScores.get(1));
					Map.Entry<String, List<String>> e =
							new HashMap.SimpleEntry<>
									(r.getKey(), scores);
					return Stream.of(e);
				})
				.flatMap(r -> Stream.of(Arrays.asList(r.getKey(), finalOpt == 0 ?
									getAverageRatings(r.getValue()) : getAverageGross(r.getValue()))))
				.sorted((r1, r2) -> {
					double doubleResult = Double.parseDouble(r2.get(1)) - Double.parseDouble(r1.get(1));
					int result = doubleResult > 0 ? 1 : -1;
					if (Math.abs(doubleResult) < 1e-7)
						result = r1.get(0).compareTo(r2.get(0));
					return result;
				}
				).limit(top_k)
				.flatMap(r -> Stream.of(r.get(0)))
				.collect(toList());
	}

	/**
	 This method searches movies based on three criterion:
	 genre: genre of the movie
	 min_rating: the rating of the movie should >= min_rating
	 max_runtime: the runtime (min) of the movie should <= max_runtime
	 Note that the results should be a list of movie titles that meet the
	 given criteria, and sorted by alphabetical
	 order of the titles.
	 */
	public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
		int genreId = headers.get("Genre"), nameId = headers.get("Series_Title");
		int ratingId = headers.get("IMDB_Rating"), runtimeId = headers.get("Runtime");
		return records.stream()
			.flatMap(r -> {
					String genreCur = r.get(genreId);
					String ratingCur = r.get(ratingId), runtimeCur = r.get(runtimeId);
					if (genreCur.length() == 0 ||
						ratingCur.length() == 0 ||
						runtimeCur.length() == 0
					) return Stream.of();
					if (!checkGenre(genreCur, genre))
						return Stream.of();
					if (getRunTime(runtimeCur) > max_runtime)
						return Stream.of();
					if (Float.parseFloat(ratingCur) < min_rating)
						return Stream.of();
					return Stream.of(r.get(nameId));
				}
			).sorted().collect(toList());
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