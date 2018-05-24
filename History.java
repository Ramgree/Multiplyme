import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class History {
    private List<String> entries;

    public History() {
        try {
            entries = Files.readAllLines(Paths.get("history.txt"));
        } catch (IOException exception) {
            System.err.println("Viga ajaloo lugemisel!");
        }
    }

    public void push(String text) {
        entries.add(text);
    }

    public List<String> get() {
        return entries;
    }

    public void save() {
        try {
            Files.write(Paths.get("history.txt"), entries);
        } catch (IOException exception) {
            System.err.println("Viga ajaloo kirjutamisel!");
        }
    }
}
