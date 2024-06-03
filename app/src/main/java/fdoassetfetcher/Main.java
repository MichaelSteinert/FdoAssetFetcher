package fdoassetfetcher;

import com.indiscale.fdo.manager.DefaultData;
import com.indiscale.fdo.manager.DefaultMetadat;
import com.indiscale.fdo.manager.api.*;
import com.indiscale.fdo.manager.mock.MockManager;
import io.thinkit.edc.client.connector.model.Catalog;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        FdoAssetFetcher fetcher = new FdoAssetFetcher();
        Set<String> assetStore = new HashSet<>();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        System.out.println("---FDO Asset Fetcher started---");
        // Schedule fetching the catalog every 10 seconds and process any new assets
        executor.scheduleAtFixedRate(() -> {
            Catalog catalog = fetcher.fetchCatalog();
            if (catalog != null && catalog.dataset() != null && catalog.dataset().raw() != null) {
                JsonArray datasets = catalog.raw().getJsonArray("http://www.w3.org/ns/dcat#dataset");
                if (datasets != null) {
                    for (int i = 0; i < datasets.size(); i++) {
                        JsonObject dataset = datasets.getJsonObject(i);
                        String datasetId = dataset.getString("@id");
                        if (!assetStore.contains(datasetId)) {
                            assetStore.add(datasetId);
                            System.out.printf("New Dataset ID: %s%n", datasetId);
                            try {
                                addDatasetToRepository(dataset);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                } else {
                    System.out.println("Dataset is empty");
                }
            } else {
                System.out.println("Failed to fetch catalog or catalog is null, or no dataset available.");
            }
        }, 0, 10, TimeUnit.SECONDS);
        System.out.println("---FDO Asset Fetcher stopped---");
    }

    private static void addDatasetToRepository(JsonObject dataset) throws Exception {
        try (Manager manager = new MockManager()) {
            // Specify the FDO profile
            FdoProfile profile = manager.getProfileRegistry().getProfile("mock-profile-1");
            // Specify the target repository
            RepositoryConnection repository = manager.getRepositoryRegistry().createRepositoryConnection("mock-repo-1");
            // Specify the data and metadata
            InputStream dataInputStream = new ByteArrayInputStream(dataset.toString().getBytes());
            InputStream mdInputStream = new ByteArrayInputStream(dataset.toString().getBytes());
            Data data = new DefaultData(dataInputStream);
            // Typo in dependency DefaultMetadat? -> DefaultMetadata
            Metadata metadata = new DefaultMetadat(mdInputStream);
            // Create the FDO
            FDO fdo = manager.createFDO(profile, repository, data, metadata);
            System.out.println("Created FDO with PID: " + fdo.getPID());
        }
    }
}
