package fdoassetfetcher;

import io.thinkit.edc.client.connector.model.Asset;
import io.thinkit.edc.client.connector.model.Catalog;

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
            if (catalog != null) {
                System.out.println("---Fetched Dataset---");
                System.out.println(catalog.dataset().raw());
                assetStore.add(catalog.dataset().raw().toString());
                // TODO: store new assets in asset store
                System.out.println("---");
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
}
