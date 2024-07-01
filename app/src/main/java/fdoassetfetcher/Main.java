package fdoassetfetcher;

import io.thinkit.edc.client.connector.model.Catalog;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {
    public static void main(String[] args) {
        FdoAssetFetcher fetcher = new FdoAssetFetcher("http://localhost:19193/management");
        Set<String> assetStore = new HashSet<>();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        System.out.println("---FDO Asset Fetcher started---");
        // Schedule fetching the catalog every 10 seconds and process any new fdo assets
        executor.scheduleAtFixedRate(() -> {
			Catalog catalog = null;
			try {
				catalog = fetcher.fetchCatalog();
			}
            catch(Exception e) {
				System.out.println(e);
            }
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
                                EdcMetadata metadata = MetadataExtractor.extractMetadata(dataset);
                                // Add metadata from edc asset to fdo repository
                                MetadataRepository.addDatasetToRepository(metadata, "mock-profile-1");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
						System.out.println("Dataset is done");
                        }
                    }
                } else {
                    System.out.println("Dataset is empty");
                }
            } else {
                System.out.println("Failed to fetch catalog or catalog is null, or no dataset available.");
            }
			System.out.println("waiting...");

        }, 0, 10, TimeUnit.SECONDS);
        System.out.println("---FDO Asset Fetcher stopped---");
    }
}
