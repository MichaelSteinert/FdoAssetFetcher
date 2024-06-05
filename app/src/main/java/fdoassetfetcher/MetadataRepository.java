package fdoassetfetcher;

import com.indiscale.fdo.manager.DefaultMetadat;
import com.indiscale.fdo.manager.api.*;
import com.indiscale.fdo.manager.mock.MockManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MetadataRepository {
    public static void addDatasetToRepository(EdcMetadata edcMetadata, String profileName) throws Exception {
        try (Manager manager = new MockManager()) {
            FdoProfile profile = manager.getProfileRegistry().getProfile(profileName);
            RepositoryConnection repository = manager.getRepositoryRegistry().createRepositoryConnection("mock-repo-1");
            InputStream mdInputStream = new ByteArrayInputStream(edcMetadata.toString().getBytes());
            Metadata metadata = new DefaultMetadat(mdInputStream);
            FDO fdo = manager.createFDO(profile, repository, null, metadata);
            System.out.println("Created FDO with PID: " + fdo.getPID());
        }
    }
}
