import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Paths

def readYaml(String path) {
    def file = new File(path)
    def yaml = new Yaml()
    return yaml.load(file.text)
}
