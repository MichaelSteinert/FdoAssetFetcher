package fdoassetfetcher;

import com.google.gson.JsonElement;
import com.indiscale.fdo.manager.DefaultData;
import com.indiscale.fdo.manager.DefaultMetadat;
import com.indiscale.fdo.manager.api.*;
import com.indiscale.fdo.manager.mock.MockManager;
import io.thinkit.edc.client.connector.model.Catalog;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
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
                                EdcMetadata metadata = extractMetadata(dataset);
                                addDatasetToRepository(metadata);
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

    private static EdcMetadata extractMetadata(JsonObject dataset) throws Exception {
        String id = null;
        String name = null;
        String description = null;
        String isFdo = null;
        String contenttype = null;
        for (Map.Entry<String, JsonValue> entry : dataset.entrySet()) {
            String key = entry.getKey();
            JsonValue value = entry.getValue();
            if (value.getValueType() == JsonValue.ValueType.ARRAY) {
                JsonArray valueArray = value.asJsonArray();
                if (!valueArray.isEmpty()) {
                    JsonValue element = valueArray.get(0);
                    if (element.getValueType() == JsonValue.ValueType.OBJECT) {
                        JsonObject valueObject = element.asJsonObject();
                        String extractedValue = valueObject.getString("@value", null);
                        switch (key) {
                            case "https://w3id.org/edc/v0.0.1/ns/name":
                                name = extractedValue;
                                break;
                            case "https://w3id.org/edc/v0.0.1/ns/description":
                                description = extractedValue;
                                break;
                            case "https://w3id.org/edc/v0.0.1/ns/isFdo":
                                isFdo = extractedValue;
                                break;
                            case "https://w3id.org/edc/v0.0.1/ns/id":
                                id = extractedValue;
                                break;
                            case "https://w3id.org/edc/v0.0.1/ns/contenttype":
                                contenttype = extractedValue;
                                break;
                        }
                    }
                }
            }
        }
        return new EdcMetadata(id, name, description, isFdo, contenttype);
    }

    private static void addDatasetToRepository(EdcMetadata edcMetadata) throws Exception {
        try (Manager manager = new MockManager()) {
            // Specify the FDO profile
            FdoProfile profile = manager.getProfileRegistry().getProfile("mock-profile-1");
            // Specify the target repository
            RepositoryConnection repository = manager.getRepositoryRegistry().createRepositoryConnection("mock-repo-1");
            // Specify the metadata
            InputStream mdInputStream = new ByteArrayInputStream(edcMetadata.toString().getBytes());
            // Typo in dependency DefaultMetadat? -> DefaultMetadata
            Metadata metadata = new DefaultMetadat(mdInputStream);
            // Create the FDO
            FDO fdo = manager.createFDO(profile, repository, null, metadata);
            System.out.println("Created FDO with PID: " + fdo.getPID());
        }
    }
}
