package application.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HexagonalPurityTest {

    @Test
    @DisplayName("Architecture rule: application.domain must have zero imports or annotations from org.springframework")
    void domainLayer_MustBePureJava_WithoutSpringFrameworkDependencies() throws IOException {
        Path domainRoot = Paths.get("src/main/java/application/domain");
        if (!Files.exists(domainRoot)) {
            // Fallback if running from root directory
            domainRoot = Paths.get("nexusmarket/src/main/java/application/domain");
        }

        assertTrue(Files.exists(domainRoot), "Domain root directory must exist: " + domainRoot);

        List<String> violations = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(domainRoot)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            List<String> lines = Files.readAllLines(path);
                            for (int i = 0; i < lines.size(); i++) {
                                String line = lines.get(i).trim();
                                if (line.startsWith("import org.springframework.")
                                        || line.startsWith("import static org.springframework.")
                                        || line.matches("^@(Service|Component|Repository|Autowired|Value|Transactional|Cacheable|CacheEvict|Caching).*")) {
                                    violations.add(String.format("%s:%d -> %s", path.getFileName(), i + 1, line));
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Error reading " + path, e);
                        }
                    });
        }

        assertTrue(violations.isEmpty(),
                "Hexagonal Architecture violation! The following domain classes contain Spring Framework dependencies:\n"
                        + String.join("\n", violations));
    }
}
