package domus.challenge.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectorCounterState {

    private final Map<String, Integer> counts = new HashMap<>();

    public void add(Movie movie) {
        if (movie == null || movie.getDirector() == null) return;
        counts.merge(movie.getDirector(), 1, Integer::sum);
    }

    public List<String> above(int threshold) {
        return counts.entrySet().stream()
                .filter(e -> e.getValue() > threshold)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    public Map<String, Integer> snapshot() {
        return Map.copyOf(counts);
    }
}
