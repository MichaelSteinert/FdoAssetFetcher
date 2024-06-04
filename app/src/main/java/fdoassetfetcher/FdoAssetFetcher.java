package fdoassetfetcher;

import io.thinkit.edc.client.connector.EdcConnectorClient;
import io.thinkit.edc.client.connector.model.*;
import io.thinkit.edc.client.connector.services.Assets;
import io.thinkit.edc.client.connector.services.Catalogs;
import jakarta.json.Json;

import java.util.List;

public class FdoAssetFetcher {
    private final EdcConnectorClient edcClient = EdcConnectorClient.newBuilder()
            .managementUrl("http://localhost:19193/management")
            .build();

    public Catalog fetchCatalog() {
        Catalogs catalogService = edcClient.catalogs();
        // Criteria for fetching only FDO asset
        List<Criterion> criteria = List.of(
                Criterion.Builder.newInstance().raw(Json.createObjectBuilder()
                        .add("operandLeft", "https://w3id.org/edc/v0.0.1/ns/isFdo")
                        .add("operator", "=")
                        .add("operandRight", "true")
                        .build()).build()
        );
        // Query for fetching specific asset from catalog
        QuerySpec query = QuerySpec.Builder.newInstance()
                .offset(0)
                .limit(10)
                .sortOrder("ASC")
                //.sortField("field1")
                .filterExpression(criteria)
                .build();
        // Request to catalog
        CatalogRequest request = CatalogRequest.Builder.newInstance()
                .protocol("dataspace-protocol-http")
                .counterPartyAddress("http://localhost:19194/protocol")
                .querySpec(query)
                .build();
        Result<Catalog> result = catalogService.request(request);
        if (result.isSucceeded() && result.getContent() != null) {
            return result.getContent();
        } else {
            result.getErrors().forEach(e -> System.out.println(e.message()));
            return null;
        }
    }

    public Asset fetchAsset(String assetId) {
        Assets assetsService = edcClient.assets();
        Result<Asset> result = assetsService.get(assetId);
        if (result.isSucceeded() && result.getContent() != null) {
            return result.getContent();
        } else {
            result.getErrors().forEach(e -> System.out.println(e.message()));
            return null;
        }
    }
}
