package domus.challenge.domain;

import org.springframework.stereotype.Component;

@Component
public class DirectorCounter {

    public DirectorCounterState createState() {
        return new DirectorCounterState();
    }
}
