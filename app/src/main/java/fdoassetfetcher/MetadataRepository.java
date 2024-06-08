package fdoassetfetcher;

import com.indiscale.fdo.manager.DefaultData;
import com.indiscale.fdo.manager.DefaultFdo;
import com.indiscale.fdo.manager.DefaultMetadata;
import com.indiscale.fdo.manager.api.*;
import com.indiscale.fdo.manager.doip.DoipRepository;
import com.indiscale.fdo.manager.util.Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MetadataRepository {
    public static void addDatasetToRepository(EdcMetadata edcMetadata, String profileName) throws Exception {

        String linkaheadJson = System.getProperty("test.linkahead.json", ".test.linkahead.json");

        RepositoryConfig config = null;
        try {
          config = Util.jsonToRepositoryConfig(new File(linkaheadJson));
        } catch (FileNotFoundException e) {
          System.err.println(
              "WARNING: Cannot setup connection to LinkAhead since the config file `"
                  + linkaheadJson
                  + "` does not exist.");
        }
        try (DoipRepository repository = new DoipRepository(config)) {
			FdoProfile profile = FdoProfile.GENERIC_FDO;
            InputStream mdInputStream = new ByteArrayInputStream(edcMetadata.toString().getBytes());
            Metadata metadata = new DefaultMetadata(mdInputStream);
            Data data = new DefaultData(new ByteArrayInputStream("https://jsonplaceholder.typicode.com/todos".getBytes())); //TODO replace by data URL
			FDO fdo = repository.createFDO(new DefaultFdo(null, profile, data, metadata));
            System.out.println("Created FDO with PID: " + fdo.getPID());
        }
    }
}
