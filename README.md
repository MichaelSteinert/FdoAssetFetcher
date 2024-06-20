### 1. Build the connector

```bash
./gradlew connector:build
```

### 2. Run the connector

```bash
java -Dedc.keystore=connector/resources/certs/cert.pfx \
     -Dedc.keystore.password=123456 \
     -Dedc.fs.config=connector/resources/configuration/provider-configuration.properties \
     -jar connector/build/libs/connector.jar
```

### 3. Create an asset with id `myAssetId`

```bash
curl -d '{
    "@context": {
        "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
    },
    "@id": "myAssetId",
    "properties": {
        "name": "Product EDC Demo Asset",
        "contenttype": "application/json",
        "description": "Product Description about Test Asset",
        "isFdo": "true"
    },
    "dataAddress": {
        "@type": "DataAddress",
        "type": "HttpData",
        "baseUrl": "https://jsonplaceholder.typicode.com/todos",
        "proxyPath": "true",
        "proxyQueryParams": "true"
    }
}' \
  -H 'content-type: application/json' http://localhost:19193/management/v3/assets \
  -s
```

### 4. Create a unrestricted policy

```bash
curl -d '{
    "@context": {
        "odrl": "http://www.w3.org/ns/odrl/2/"
    },
    "@id": "PolicyForTestAsset",
    "@type": "PolicyDefinitionRequestDto",
    "policy": {
        "@type": "http://www.w3.org/ns/odrl/2/Set",
        "permission": [],
        "prohibition": [],
        "obligation": []
    }
}' \
  -H 'content-type: application/json' http://localhost:19193/management/v2/policydefinitions \
  -s
```

### 5. Create a contract definition for asset with id `myAssetId`

```bash
curl -d '{
  "@context": {
    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
  },
  "@id": "ContractDefinitionForTestAsset",
  "@type": "ContractDefinition",
  "accessPolicyId": "PolicyForTestAsset",
  "contractPolicyId": "PolicyForTestAsset",
      "assetsSelector": {
        "@type": "CriterionDto",
        "operandLeft": "https://w3id.org/edc/v0.0.1/ns/id",
        "operator": "=",
        "operandRight": "myAssetId"
    }
}' \
  -H 'content-type: application/json' http://localhost:19193/management/v2/contractdefinitions \
  -s
```

### 6. Fetch catalog

```bash
curl -d '{
    "@context": {
        "edc": "https://w3id.org/edc/v0.0.1/ns/"
    },
    "@type": "CatalogRequest",
    "counterPartyAddress": "http://localhost:19194/protocol",
    "protocol": "dataspace-protocol-http",
    "querySpec": {
        "offset": 0,
        "limit": 5
    }
}' \
  -H 'content-type: application/json' http://localhost:19193/management/v2/catalog/request \
  -s
```