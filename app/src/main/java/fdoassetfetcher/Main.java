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
        FdoAssetFetcher fetcher = new FdoAssetFetcher();
        Set<String> assetStore = new HashSet<>();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        // Schedule fetching the catalog every 10 seconds and process any new assets
        executor.scheduleAtFixedRate(() -> {
            Catalog catalog = fetcher.fetchCatalog();
            if (catalog != null && catalog.dataset() != null && catalog.dataset().raw() != null) {
                System.out.println("---Fetched Asset(s)---");
                JsonArray datasets = catalog.raw().getJsonArray("http://www.w3.org/ns/dcat#dataset");
                if (datasets != null) {
                    for (int i = 0; i < datasets.size(); i++) {
                        JsonObject dataset = datasets.getJsonObject(i);
                        String datasetId = dataset.getString("@id");
                        System.out.printf("Dataset ID: %s%n", datasetId);
                    }
                } else {
                    System.out.println("Dataset is empty");
                }
            } else {
                System.out.println("Failed to fetch catalog or catalog is null, or no dataset available.");
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
}
